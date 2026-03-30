package com.example.emotiontherapy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emotiontherapy.navigation.AppNavGraph
import com.example.emotiontherapy.ui.theme.EmotionTherapyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EmotionTherapyAppTheme {
                val viewModel: EmotionTherapyViewModel = viewModel()
                val uiState = viewModel.uiState.collectAsStateWithLifecycle()

                AppNavGraph(
                    uiState = uiState.value,
                    viewModel = viewModel
                )
            }
        }
    }
}
