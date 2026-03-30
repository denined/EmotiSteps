package com.example.emotiontherapy.ai

class PredictionSmoother(
    private val windowSize: Int = 5
) {
    private val recentPredictions = ArrayDeque<EmotionPrediction>()

    fun push(prediction: EmotionPrediction): EmotionPrediction {
        if (recentPredictions.size == windowSize) {
            recentPredictions.removeFirst()
        }
        recentPredictions.addLast(prediction)

        val grouped = recentPredictions.groupBy { it.label }
        val bestGroup = grouped.entries.maxWithOrNull(
            compareBy<Map.Entry<String, List<EmotionPrediction>>>(
                { it.value.size },
                { it.value.sumOf { predictionItem -> predictionItem.confidence.toDouble() } }
            )
        ) ?: return prediction

        val averageConfidence = bestGroup.value
            .map { it.confidence.toDouble() }
            .average()
            .toFloat()

        return EmotionPrediction(
            label = bestGroup.key,
            confidence = averageConfidence,
            isPrototype = bestGroup.value.any { it.isPrototype },
            isUncertain = bestGroup.value.all { it.isUncertain }
        )
    }

    fun clear() {
        recentPredictions.clear()
    }
}
