package com.example.emotiontherapy

enum class ExerciseType {
    SHOW_THE_EMOTION,
    FIND_THE_EMOTION
}

enum class FindEmotionDifficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class ExerciseLayout {
    SINGLE_EMOTION,
    MIXED_EMOTIONS
}

data class ExerciseCard(
    val emotionId: Int,
    val imageUri: String
)

data class ExerciseRound(
    val promptEmotionName: String,
    val cards: List<ExerciseCard>,
    val correctEmotionId: Int
)

data class ExerciseRoundResult(
    val roundNumber: Int,
    val promptEmotionName: String,
    val selectedEmotionId: Int,
    val correctEmotionId: Int
) {
    val isCorrect: Boolean
        get() = selectedEmotionId == correctEmotionId
}

data class ExerciseSession(
    val exerciseType: ExerciseType,
    val layout: ExerciseLayout? = null,
    val selectedEmotionIds: List<Int>,
    val focusEmotionId: Int? = null,
    val distractorEmotionIds: List<Int> = emptyList(),
    val findEmotionDifficulty: FindEmotionDifficulty = FindEmotionDifficulty.MEDIUM,
    val startedAtMillis: Long = System.currentTimeMillis(),
    val roundNumber: Int = 1,
    val currentRound: ExerciseRound,
    val answeredRounds: List<ExerciseRoundResult> = emptyList(),
    val selectedAnswerEmotionId: Int? = null,
    val feedbackMessage: String = "",
    val currentRoundCorrectHits: Int = 0
) {
    val correctRounds: Int
        get() = answeredRounds.count { it.isCorrect }

    val accuracyPercent: Int
        get() = if (answeredRounds.isEmpty()) 0 else (correctRounds * 100) / answeredRounds.size
}
