package com.example.emotiontherapy.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emotion_images",
    foreignKeys = [
        ForeignKey(
            entity = EmotionEntity::class,
            parentColumns = ["id"],
            childColumns = ["emotionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("emotionId")]
)
data class EmotionImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emotionId: Int,
    val uri: String
)
