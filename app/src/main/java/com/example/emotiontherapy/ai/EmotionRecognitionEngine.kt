package com.example.emotiontherapy.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.google.android.gms.tflite.java.TfLite
import com.google.android.gms.tasks.Task
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.InterpreterApi.Options.TfLiteRuntime
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min

class EmotionRecognitionEngine private constructor(
    private val context: Context,
    private val prototypeClassifier: PrototypeEmotionClassifier
) {
    private val interpreterRef = AtomicReference<InterpreterApi?>(null)
    private var labels: List<String> = emptyList()
    @Volatile
    private var usingModel = false

    fun initialize(): Task<Void> {
        return TfLite.initialize(context).addOnSuccessListener {
            try {
                val interpreterOptions = InterpreterApi.Options()
                    .setRuntime(TfLiteRuntime.FROM_SYSTEM_ONLY)
                val modelBuffer = loadModelFile(context, MODEL_FILE)
                labels = context.assets.open(LABELS_FILE)
                    .bufferedReader()
                    .useLines { lines -> lines.map { it.trim() }.filter { it.isNotEmpty() }.toList() }
                if (labels.isNotEmpty()) {
                    interpreterRef.set(InterpreterApi.create(modelBuffer, interpreterOptions))
                    usingModel = true
                }
            } catch (_: Exception) {
                usingModel = false
            }
        }
    }

    fun classify(faceBitmap: Bitmap?, fallbackFace: com.google.mlkit.vision.face.Face): EmotionPrediction {
        val interpreter = interpreterRef.get()
        if (interpreter != null && faceBitmap != null && labels.isNotEmpty()) {
            try {
                return classifyWithThreshold(classifyWithModel(interpreter, faceBitmap))
            } catch (_: Exception) {
            }
        }
        return classifyWithThreshold(prototypeClassifier.classify(fallbackFace))
    }

    fun modelStatusMessage(): String {
        return if (usingModel) {
            "TensorFlow Lite model active"
        } else {
            "Prototype classifier active while the TensorFlow Lite model is unavailable."
        }
    }

    fun close() {
        interpreterRef.getAndSet(null)?.close()
    }

    private fun classifyWithThreshold(prediction: EmotionPrediction): EmotionPrediction {
        val threshold = if (prediction.isPrototype) {
            PROTOTYPE_CONFIDENCE_THRESHOLD
        } else {
            MODEL_CONFIDENCE_THRESHOLD
        }
        return if (prediction.confidence < threshold) {
            prediction.copy(label = UNCERTAIN_LABEL, isUncertain = true)
        } else {
            prediction.copy(isUncertain = false)
        }
    }

    private fun classifyWithModel(
        interpreter: InterpreterApi,
        faceBitmap: Bitmap
    ): EmotionPrediction {
        val scaledBitmap = Bitmap.createScaledBitmap(faceBitmap, 48, 48, true)
        val grayscaleValues = FloatArray(48 * 48)
        var minLuminance = 1f
        var maxLuminance = 0f

        for (y in 0 until 48) {
            for (x in 0 until 48) {
                val pixel = scaledBitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF
                val grayscale = (0.299f * red + 0.587f * green + 0.114f * blue) / 255f
                val index = (y * 48) + x
                grayscaleValues[index] = grayscale
                if (grayscale < minLuminance) minLuminance = grayscale
                if (grayscale > maxLuminance) maxLuminance = grayscale
            }
        }

        val inputBuffer = ByteBuffer.allocateDirect(4 * 48 * 48).order(ByteOrder.nativeOrder())
        val luminanceRange = (maxLuminance - minLuminance).coerceAtLeast(0.15f)
        for (value in grayscaleValues) {
            val normalized = ((value - minLuminance) / luminanceRange).coerceIn(0f, 1f)
            inputBuffer.putFloat(normalized)
        }
        inputBuffer.rewind()

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(inputBuffer, output)

        val probabilities = output[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        return EmotionPrediction(
            label = labels.getOrElse(maxIndex) { "Unknown" },
            confidence = probabilities[maxIndex],
            isPrototype = false
        )
    }

    companion object {
        private const val MODEL_FILE = "emotion_model.tflite"
        private const val LABELS_FILE = "emotion_labels.txt"
        private const val UNCERTAIN_LABEL = "Uncertain"
        private const val MODEL_CONFIDENCE_THRESHOLD = 0.55f
        private const val PROTOTYPE_CONFIDENCE_THRESHOLD = 0.60f
        private const val FACE_CROP_SCALE = 1.35f
        private const val MIN_FACE_CROP_SIZE = 48

        fun create(context: Context): EmotionRecognitionEngine {
            return EmotionRecognitionEngine(
                context = context.applicationContext,
                prototypeClassifier = PrototypeEmotionClassifier()
            )
        }

        fun cropFace(bitmap: Bitmap, boundingBox: Rect): Bitmap? {
            if (bitmap.width <= 1 || bitmap.height <= 1) return null

            val faceWidth = boundingBox.width().coerceAtLeast(1)
            val faceHeight = boundingBox.height().coerceAtLeast(1)
            val faceCenterX = boundingBox.exactCenterX()
            val faceCenterY = boundingBox.exactCenterY()
            val targetSide = (max(faceWidth, faceHeight) * FACE_CROP_SCALE).toInt()
                .coerceAtLeast(MIN_FACE_CROP_SIZE)
            val halfSide = targetSide / 2f

            var left = (faceCenterX - halfSide).toInt()
            var top = (faceCenterY - halfSide).toInt()
            var right = left + targetSide
            var bottom = top + targetSide

            if (left < 0) {
                right = min(bitmap.width, right - left)
                left = 0
            }
            if (top < 0) {
                bottom = min(bitmap.height, bottom - top)
                top = 0
            }
            if (right > bitmap.width) {
                val shift = right - bitmap.width
                left = (left - shift).coerceAtLeast(0)
                right = bitmap.width
            }
            if (bottom > bitmap.height) {
                val shift = bottom - bitmap.height
                top = (top - shift).coerceAtLeast(0)
                bottom = bitmap.height
            }

            val side = min(right - left, bottom - top)
            if (side <= 0) return null

            val squareLeft = left.coerceIn(0, bitmap.width - side)
            val squareTop = top.coerceIn(0, bitmap.height - side)
            return Bitmap.createBitmap(bitmap, squareLeft, squareTop, side, side)
        }

        private fun loadModelFile(context: Context, fileName: String): MappedByteBuffer {
            val assetFileDescriptor = context.assets.openFd(fileName)
            FileInputStream(assetFileDescriptor.fileDescriptor).channel.use { fileChannel ->
                return fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.declaredLength
                )
            }
        }
    }
}
