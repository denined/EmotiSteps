package com.example.emotiontherapy.ai

data class EmotionPrediction(
    val label: String,
    val confidence: Float,
    val isPrototype: Boolean = true,
    val isUncertain: Boolean = false
)
