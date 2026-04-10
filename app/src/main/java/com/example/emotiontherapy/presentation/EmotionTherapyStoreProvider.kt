package com.example.emotiontherapy.presentation

import android.app.Application

object EmotionTherapyStoreProvider {
    @Volatile
    private var instance: EmotionTherapyStore? = null

    fun get(application: Application): EmotionTherapyStore {
        return instance ?: synchronized(this) {
            instance ?: EmotionTherapyStore(application).also { instance = it }
        }
    }
}
