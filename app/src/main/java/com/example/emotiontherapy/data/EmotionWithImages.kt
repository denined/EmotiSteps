package com.example.emotiontherapy.data

import androidx.room.Embedded
import androidx.room.Relation

data class EmotionWithImages(
    @Embedded val emotion: EmotionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "emotionId"
    )
    val images: List<EmotionImageEntity>
)
