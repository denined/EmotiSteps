package com.example.emotiontherapy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emotiontherapy.navigation.AppNavGraph
import com.example.emotiontherapy.presentation.ChildrenViewModel
import com.example.emotiontherapy.presentation.EmotionsViewModel
import com.example.emotiontherapy.presentation.EmotionTherapyViewModel
import com.example.emotiontherapy.presentation.EvolutionViewModel
import com.example.emotiontherapy.presentation.ExercisesViewModel
import com.example.emotiontherapy.presentation.TokensViewModel
import com.example.emotiontherapy.ui.theme.EmotionTherapyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EmotionTherapyAppTheme {
                val appStateViewModel: EmotionTherapyViewModel = viewModel()
                val childrenViewModel: ChildrenViewModel = viewModel()
                val emotionsViewModel: EmotionsViewModel = viewModel()
                val exercisesViewModel: ExercisesViewModel = viewModel()
                val evolutionViewModel: EvolutionViewModel = viewModel()
                val tokensViewModel: TokensViewModel = viewModel()
                val uiState = appStateViewModel.uiState.collectAsStateWithLifecycle()

                AppNavGraph(
                    uiState = uiState.value,
                    childrenViewModel = childrenViewModel,
                    emotionsViewModel = emotionsViewModel,
                    exercisesViewModel = exercisesViewModel,
                    evolutionViewModel = evolutionViewModel,
                    tokensViewModel = tokensViewModel
                )
            }
        }
    }
}
