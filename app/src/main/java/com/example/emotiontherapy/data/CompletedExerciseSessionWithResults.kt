package com.example.emotiontherapy.data

import androidx.room.Embedded
import androidx.room.Relation

data class CompletedExerciseSessionWithResults(
    @Embedded val session: CompletedExerciseSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val results: List<ExerciseRoundResultEntity>
)
