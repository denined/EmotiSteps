package com.example.emotiontherapy.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_round_results",
    foreignKeys = [
        ForeignKey(
            entity = CompletedExerciseSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ExerciseRoundResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val roundNumber: Int,
    val promptEmotionName: String,
    val selectedEmotionId: Int,
    val correctEmotionId: Int,
    val isCorrect: Boolean
)
