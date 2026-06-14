package com.example.emotiontherapy.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class EvolutionViewModel(application: Application) : AndroidViewModel(application) {
    private val store = EmotionTherapyStoreProvider.get(application)

    fun setSessionIncludeInEvolution(sessionId: Int, includeInEvolution: Boolean) =
        store.setSessionIncludeInEvolution(sessionId, includeInEvolution)
}
