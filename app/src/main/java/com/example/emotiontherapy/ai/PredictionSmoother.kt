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
        val rankedGroups = grouped.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, List<EmotionPrediction>>> { it.value.size }
                    .thenByDescending { entry ->
                        entry.value.sumOf { predictionItem -> predictionItem.confidence.toDouble() }
                    }
            )
        val bestGroup = rankedGroups.firstOrNull() ?: return prediction
        val secondGroup = rankedGroups.getOrNull(1)

        val averageConfidence = bestGroup.value
            .map { it.confidence.toDouble() }
            .average()
            .toFloat()
        val secondaryConfidence = secondGroup?.value
            ?.map { it.confidence.toDouble() }
            ?.average()
            ?.toFloat()

        return EmotionPrediction(
            label = bestGroup.key,
            confidence = averageConfidence,
            isPrototype = bestGroup.value.any { it.isPrototype },
            isUncertain = bestGroup.value.all { it.isUncertain },
            secondaryLabel = secondGroup?.key,
            secondaryConfidence = secondaryConfidence
        )
    }

    fun clear() {
        recentPredictions.clear()
    }
}
