package com.example.emotiontherapy.model

data class EmotionImageItem(
    val id: Int,
    val uri: String
)

data class EmotionItem(
    val id: Int,
    val name: String,
    val images: List<EmotionImageItem>
) {
    val imageUris: List<String>
        get() = images.map { it.uri }
}
