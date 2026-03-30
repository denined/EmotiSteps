package com.example.emotiontherapy

data class ExerciseHistoryRoundItem(
    val roundNumber: Int,
    val promptEmotionName: String,
    val isCorrect: Boolean
)

data class ExerciseHistoryItem(
    val id: Int,
    val layout: ExerciseLayout,
    val layoutLabel: String,
    val endedAtMillis: Long,
    val totalRounds: Int,
    val correctRounds: Int,
    val accuracyPercent: Int,
    val rounds: List<ExerciseHistoryRoundItem>
)
