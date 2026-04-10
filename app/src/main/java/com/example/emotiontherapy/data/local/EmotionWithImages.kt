package com.example.emotiontherapy.data.local

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
