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
        val inputBuffer = ByteBuffer.allocateDirect(4 * 48 * 48).order(ByteOrder.nativeOrder())
        for (y in 0 until 48) {
            for (x in 0 until 48) {
                val pixel = scaledBitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF
                val grayscale = (red + green + blue) / 3f / 255f
                inputBuffer.putFloat(grayscale)
            }
        }
        inputBuffer.rewind()

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(inputBuffer, output)

        val probabilities = output[0]
        val sortedIndices = probabilities.indices.sortedByDescending { probabilities[it] }
        val maxIndex = sortedIndices.firstOrNull() ?: 0
        val secondaryIndex = sortedIndices.getOrNull(1)
        return EmotionPrediction(
            label = labels.getOrElse(maxIndex) { "Unknown" },
            confidence = probabilities[maxIndex],
            isPrototype = false,
            secondaryLabel = secondaryIndex?.let { labels.getOrElse(it) { "Unknown" } },
            secondaryConfidence = secondaryIndex?.let { probabilities[it] }
        )
    }

    companion object {
        private const val MODEL_FILE = "emotion_model.tflite"
        private const val LABELS_FILE = "emotion_labels.txt"
        private const val UNCERTAIN_LABEL = "Uncertain"
        private const val MODEL_CONFIDENCE_THRESHOLD = 0.60f
        private const val PROTOTYPE_CONFIDENCE_THRESHOLD = 0.60f

        fun create(context: Context): EmotionRecognitionEngine {
            return EmotionRecognitionEngine(
                context = context.applicationContext,
                prototypeClassifier = PrototypeEmotionClassifier()
            )
        }

        fun cropFace(bitmap: Bitmap, boundingBox: Rect): Bitmap? {
            val left = boundingBox.left.coerceIn(0, bitmap.width - 1)
            val top = boundingBox.top.coerceIn(0, bitmap.height - 1)
            val right = boundingBox.right.coerceIn(left + 1, bitmap.width)
            val bottom = boundingBox.bottom.coerceIn(top + 1, bitmap.height)
            val width = right - left
            val height = bottom - top
            if (width <= 0 || height <= 0) return null
            return Bitmap.createBitmap(bitmap, left, top, width, height)
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
