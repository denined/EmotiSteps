package com.example.emotiontherapy.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow

class EmotionTherapyViewModel(application: Application) : AndroidViewModel(application) {
    private val store = EmotionTherapyStoreProvider.get(application)
    val uiState: StateFlow<EmotionTherapyUiState> = store.uiState
}
