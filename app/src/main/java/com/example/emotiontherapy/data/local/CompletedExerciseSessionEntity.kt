package com.example.emotiontherapy.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completed_exercise_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("childId")]
)
data class CompletedExerciseSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val childId: Int,
    val layout: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val totalRounds: Int,
    val correctRounds: Int,
    val accuracyPercent: Int,
    val includeInEvolution: Boolean = true
)
