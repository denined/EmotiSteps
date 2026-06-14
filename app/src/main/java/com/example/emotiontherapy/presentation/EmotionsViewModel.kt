package com.example.emotiontherapy.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class EmotionsViewModel(application: Application) : AndroidViewModel(application) {
    private val store = EmotionTherapyStoreProvider.get(application)

    fun addEmotion(emotionName: String, imageUris: List<String>) =
        store.addEmotion(emotionName, imageUris)

    fun addImagesToEmotion(emotionId: Int, imageUris: List<String>) =
        store.addImagesToEmotion(emotionId, imageUris)

    fun renameEmotion(emotionId: Int, newName: String) =
        store.renameEmotion(emotionId, newName)

    fun deleteEmotion(emotionId: Int) = store.deleteEmotion(emotionId)

    fun deleteEmotionImages(imageIds: List<Int>) = store.deleteEmotionImages(imageIds)
}
