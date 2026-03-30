package com.example.emotiontherapy.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.Rect as AndroidRect
import android.graphics.YuvImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.emotiontherapy.ai.EmotionPrediction
import com.example.emotiontherapy.ai.EmotionRecognitionEngine
import com.example.emotiontherapy.ai.PredictionSmoother
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun AiRecognitionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(context.hasCameraPermission())
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text(
            text = "AI Recognition",
            fontSize = 28.sp
        )

        Text(
            text = "Step 4: live face detection with the local TensorFlow Lite emotion model. The prototype classifier stays as a fallback if the model is unavailable."
        )

        if (!hasCameraPermission) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Camera permission is required to use AI recognition.")
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Allow Camera")
                    }
                }
            }
        } else {
            CameraPreviewCard()
        }
    }
}

@Composable
private fun CameraPreviewCard() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val detector = remember { createFaceDetector() }
    val recognitionEngine = remember { EmotionRecognitionEngine.create(context) }
    val predictionSmoother = remember { PredictionSmoother() }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    var faceBounds by remember { mutableStateOf<NormalizedFaceBounds?>(null) }
    var emotionPrediction by remember { mutableStateOf<EmotionPrediction?>(null) }
    val latestFaceBounds by rememberUpdatedState(faceBounds)

    DisposableEffect(lifecycleOwner, previewView, detector, analysisExecutor, recognitionEngine, predictionSmoother) {
        recognitionEngine.initialize()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                surfaceProvider = previewView.surfaceProvider
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(
                        analysisExecutor,
                        FaceDetectionAnalyzer(detector, recognitionEngine) { result ->
                            faceBounds = result.bounds
                            emotionPrediction = result.prediction?.let { predictionSmoother.push(it) }
                            if (result.prediction == null) {
                                predictionSmoother.clear()
                            }
                        }
                    )
                }
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        }

        cameraProviderFuture.addListener(listener, mainExecutor)

        onDispose {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
            detector.close()
            recognitionEngine.close()
            analysisExecutor.shutdown()
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val bounds = latestFaceBounds
                    if (bounds != null) {
                        val left = size.width * bounds.left
                        val top = size.height * bounds.top
                        val width = size.width * bounds.width
                        val height = size.height * bounds.height

                        drawRect(
                            color = Color(0xFF00E676),
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            style = Stroke(width = 6f)
                        )
                        drawLine(
                            color = Color(0xFF00E676),
                            start = Offset(left, top),
                            end = Offset(left + 40f, top),
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFF00E676),
                            start = Offset(left, top),
                            end = Offset(left, top + 40f),
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                emotionPrediction?.let { prediction ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.68f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (prediction.isUncertain) {
                                    "Detected emotion: Uncertain"
                                } else {
                                    "Detected emotion: ${prediction.label}"
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (prediction.isUncertain) {
                                    "Confidence is too low for a reliable classification."
                                } else {
                                    "Confidence: ${(prediction.confidence * 100).toInt()}%"
                                },
                                color = Color.White.copy(alpha = 0.92f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Text(
                text = if (faceBounds != null) "Face detected" else "Searching for a face...",
                color = if (faceBounds != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = faceBounds.toGuidanceMessage(),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = recognitionEngine.modelStatusMessage(),
                style = MaterialTheme.typography.bodySmall
            )

        }
    }
}

private fun createFaceDetector(): FaceDetector {
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()
    return FaceDetection.getClient(options)
}

private class FaceDetectionAnalyzer(
    private val detector: FaceDetector,
    private val recognitionEngine: EmotionRecognitionEngine,
    private val onFaceDetected: (FaceDetectionResult) -> Unit
) : ImageAnalysis.Analyzer {
    private val isProcessing = AtomicBoolean(false)

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val uprightBitmap = imageProxy.toBitmap(rotation)
        val image = InputImage.fromMediaImage(mediaImage, rotation)
        detector.process(image)
            .addOnSuccessListener { faces ->
                val firstFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                val prediction = if (firstFace != null && uprightBitmap != null) {
                    val faceBitmap = EmotionRecognitionEngine.cropFace(uprightBitmap, firstFace.boundingBox)
                    recognitionEngine.classify(faceBitmap, firstFace)
                } else {
                    null
                }
                onFaceDetected(
                    FaceDetectionResult(
                        bounds = firstFace?.boundingBox?.toNormalizedBounds(
                            imageWidth = imageProxy.width,
                            imageHeight = imageProxy.height,
                            rotationDegrees = rotation,
                            isFrontCamera = true
                        ),
                        prediction = prediction
                    )
                )
            }
            .addOnFailureListener {
                onFaceDetected(FaceDetectionResult())
            }
            .addOnCompleteListener {
                isProcessing.set(false)
                imageProxy.close()
            }
    }
}

private data class FaceDetectionResult(
    val bounds: NormalizedFaceBounds? = null,
    val prediction: EmotionPrediction? = null
)

private data class NormalizedFaceBounds(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

private fun Rect.toNormalizedBounds(
    imageWidth: Int,
    imageHeight: Int,
    rotationDegrees: Int,
    isFrontCamera: Boolean
): NormalizedFaceBounds {
    val rotatedWidth = if (rotationDegrees == 90 || rotationDegrees == 270) imageHeight else imageWidth
    val rotatedHeight = if (rotationDegrees == 90 || rotationDegrees == 270) imageWidth else imageHeight

    val normalizedLeft = left.toFloat() / rotatedWidth
    val normalizedTop = top.toFloat() / rotatedHeight
    val normalizedRight = right.toFloat() / rotatedWidth
    val normalizedBottom = bottom.toFloat() / rotatedHeight

    val mirroredLeft = if (isFrontCamera) 1f - normalizedRight else normalizedLeft

    return NormalizedFaceBounds(
        left = mirroredLeft.coerceIn(0f, 1f),
        top = normalizedTop.coerceIn(0f, 1f),
        width = (normalizedRight - normalizedLeft).coerceIn(0f, 1f),
        height = (normalizedBottom - normalizedTop).coerceIn(0f, 1f)
    )
}

private fun ImageProxy.toBitmap(rotationDegrees: Int): Bitmap? {
    val nv21Buffer = yuv420888ToNv21()
    val yuvImage = YuvImage(nv21Buffer, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    if (!yuvImage.compressToJpeg(AndroidRect(0, 0, width, height), 100, out)) {
        return null
    }
    val jpegBytes = out.toByteArray()
    val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size) ?: return null
    if (rotationDegrees == 0) return bitmap

    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun ImageProxy.yuv420888ToNv21(): ByteArray {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)

    val chromaRowStride = planes[1].rowStride
    val chromaPixelStride = planes[1].pixelStride
    var offset = ySize

    if (chromaPixelStride == 2 && chromaRowStride == width) {
        val vBytes = ByteArray(vSize)
        val uBytes = ByteArray(uSize)
        vBuffer.get(vBytes)
        uBuffer.get(uBytes)
        for (i in 0 until vSize step 2) {
            nv21[offset++] = vBytes[i]
            nv21[offset++] = uBytes[i]
        }
        return nv21
    }

    val uBytes = ByteArray(uSize)
    val vBytes = ByteArray(vSize)
    uBuffer.get(uBytes)
    vBuffer.get(vBytes)

    for (row in 0 until height / 2) {
        for (col in 0 until width / 2) {
            val vuPos = row * chromaRowStride + col * chromaPixelStride
            nv21[offset++] = vBytes[vuPos]
            nv21[offset++] = uBytes[vuPos]
        }
    }

    return nv21
}

private fun NormalizedFaceBounds?.toGuidanceMessage(): String {
    if (this == null) {
        return "Position the face clearly in front of the camera."
    }

    val centerX = left + (width / 2f)
    val centerY = top + (height / 2f)
    val centeredHorizontally = centerX in 0.35f..0.65f
    val centeredVertically = centerY in 0.28f..0.72f
    val faceLargeEnough = width >= 0.20f && height >= 0.20f

    return when {
        !faceLargeEnough -> "Move a little closer to the camera."
        !centeredHorizontally && centerX < 0.35f -> "Move slightly to the right."
        !centeredHorizontally && centerX > 0.65f -> "Move slightly to the left."
        !centeredVertically && centerY < 0.28f -> "Lower the camera a little."
        !centeredVertically && centerY > 0.72f -> "Raise the camera a little."
        else -> "Hold still. Face position looks good."
    }
}

private fun Context.hasCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
}
