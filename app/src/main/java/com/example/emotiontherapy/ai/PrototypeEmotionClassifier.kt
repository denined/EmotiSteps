package com.example.emotiontherapy.ai

import android.graphics.PointF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import kotlin.math.abs

class PrototypeEmotionClassifier {
    fun classify(face: Face): EmotionPrediction {
        val smile = face.smilingProbability ?: 0f
        val leftEyeOpen = face.leftEyeOpenProbability ?: 0.5f
        val rightEyeOpen = face.rightEyeOpenProbability ?: 0.5f
        val averageEyeOpen = (leftEyeOpen + rightEyeOpen) / 2f
        val mouthOpen = mouthOpenRatio(face)
        val eyebrowTension = eyebrowTension(face)
        val headTurn = abs(face.headEulerAngleY)

        if (smile >= 0.68f) {
            return EmotionPrediction(label = "Happy", confidence = smile, isPrototype = true)
        }

        if (mouthOpen >= 0.12f && averageEyeOpen >= 0.60f) {
            val confidence = ((mouthOpen * 4f) + averageEyeOpen) / 2f
            return EmotionPrediction(
                label = "Surprised",
                confidence = confidence.coerceIn(0f, 1f),
                isPrototype = true
            )
        }

        if (smile <= 0.18f && eyebrowTension <= 0.075f && averageEyeOpen <= 0.70f) {
            val confidence = (0.55f + (0.12f - mouthOpen).coerceAtLeast(0f) + (0.08f - eyebrowTension).coerceAtLeast(0f))
            return EmotionPrediction(
                label = "Angry",
                confidence = confidence.coerceIn(0f, 1f),
                isPrototype = true
            )
        }

        if (smile <= 0.22f && mouthOpen <= 0.05f && averageEyeOpen <= 0.58f) {
            val confidence = (0.5f + (0.60f - averageEyeOpen).coerceAtLeast(0f))
            return EmotionPrediction(
                label = "Sad",
                confidence = confidence.coerceIn(0f, 1f),
                isPrototype = true
            )
        }

        val neutralConfidence = 0.45f + (1f - headTurn / 45f).coerceIn(0f, 0.25f)
        return EmotionPrediction(
            label = "Neutral",
            confidence = neutralConfidence.coerceIn(0f, 1f),
            isPrototype = true
        )
    }

    private fun mouthOpenRatio(face: Face): Float {
        val upperLip = face.getContour(FaceContour.UPPER_LIP_TOP)?.points.orEmpty()
        val lowerLip = face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points.orEmpty()
        if (upperLip.isEmpty() || lowerLip.isEmpty()) return 0f

        val upperCenterY = upperLip.map { it.y.toDouble() }.average().toFloat()
        val lowerCenterY = lowerLip.map { it.y.toDouble() }.average().toFloat()
        return ((lowerCenterY - upperCenterY) / face.boundingBox.height().coerceAtLeast(1)).coerceAtLeast(0f)
    }

    private fun eyebrowTension(face: Face): Float {
        val leftEyebrow = face.getContour(FaceContour.LEFT_EYEBROW_TOP)?.points.orEmpty()
        val rightEyebrow = face.getContour(FaceContour.RIGHT_EYEBROW_TOP)?.points.orEmpty()
        val leftEye = face.getContour(FaceContour.LEFT_EYE)?.points.orEmpty()
        val rightEye = face.getContour(FaceContour.RIGHT_EYE)?.points.orEmpty()

        val leftDistance = averageVerticalDistance(leftEyebrow, leftEye)
        val rightDistance = averageVerticalDistance(rightEyebrow, rightEye)
        val averageDistance = listOf(leftDistance, rightDistance).filter { it > 0f }.average().toFloat()
        return (averageDistance / face.boundingBox.height().coerceAtLeast(1)).coerceAtLeast(0f)
    }

    private fun averageVerticalDistance(
        contourA: List<PointF>,
        contourB: List<PointF>
    ): Float {
        if (contourA.isEmpty() || contourB.isEmpty()) return 0f
        val averageAY = contourA.map { it.y.toDouble() }.average().toFloat()
        val averageBY = contourB.map { it.y.toDouble() }.average().toFloat()
        return abs(averageAY - averageBY)
    }
}
