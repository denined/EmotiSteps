package com.example.emotiontherapy.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.emotiontherapy.Child
import com.example.emotiontherapy.EmotionTherapyUiState
import com.example.emotiontherapy.EmotionTherapyViewModel
import com.example.emotiontherapy.ui.components.BottomNavigationBar
import com.example.emotiontherapy.ui.components.ComingSoonScreen
import com.example.emotiontherapy.ui.screens.AddChildScreen
import com.example.emotiontherapy.ui.screens.AddEmotionsScreen
import com.example.emotiontherapy.ui.screens.AiRecognitionScreen
import com.example.emotiontherapy.ui.screens.EmotionDetailScreen
import com.example.emotiontherapy.ui.screens.EvolutionScreen
import com.example.emotiontherapy.ExerciseType
import com.example.emotiontherapy.ui.screens.ExerciseCompleteScreen
import com.example.emotiontherapy.ui.screens.ExerciseSessionScreen
import com.example.emotiontherapy.ui.screens.ExerciseStatsScreen
import com.example.emotiontherapy.ui.screens.ExercisesMenuScreen
import com.example.emotiontherapy.ui.screens.FindEmotionConfigScreen
import com.example.emotiontherapy.ui.screens.MainScreen
import com.example.emotiontherapy.ui.screens.MoreScreen
import com.example.emotiontherapy.ui.screens.RewardTokensScreen
import com.example.emotiontherapy.ui.screens.ShowEmotionConfigScreen

@Composable
fun AppNavGraph(
    uiState: EmotionTherapyUiState,
    viewModel: EmotionTherapyViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var childPendingDelete by remember { mutableStateOf<Child?>(null) }
    var selectedEmotionId by remember { mutableStateOf<Int?>(null) }
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomNavRoutes = remember {
        setOf(MAIN_ROUTE, ADD_EMOTIONS_ROUTE, EXERCISES_ROUTE, MORE_ROUTE)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                BottomNavigationBar(
                    currentRoute = currentRoute ?: MAIN_ROUTE,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(MAIN_ROUTE) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MAIN_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MAIN_ROUTE) {
                MainScreen(
                    children = uiState.children,
                    selectedChild = uiState.selectedChild,
                    onChildClick = { child ->
                        viewModel.selectChild(child.id)
                    },
                    onDeleteChildClick = { child ->
                        childPendingDelete = child
                    },
                    onAddChildClick = { navController.navigate(ADD_CHILD_ROUTE) }
                )
            }

            composable(ADD_CHILD_ROUTE) {
                AddChildScreen(
                    onSave = { childName ->
                        viewModel.addChild(childName)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(MORE_ROUTE) {
                MoreScreen(
                    child = uiState.selectedChild,
                    onSeeEvolutionClick = { navController.navigate(EVOLUTION_ROUTE) },
                    onChooseTokensClick = { navController.navigate(TOKENS_ROUTE) },
                    onAiRecognitionClick = { navController.navigate(AI_ROUTE) }
                )
            }

            composable(EVOLUTION_ROUTE) {
                EvolutionScreen(
                    child = uiState.selectedChild,
                    history = uiState.exerciseHistory,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(TOKENS_ROUTE) {
                RewardTokensScreen(
                    child = uiState.selectedChild,
                    tokens = uiState.rewardTokens,
                    onAddTokens = { imageUris ->
                        viewModel.addRewardTokens(imageUris)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ADD_EMOTIONS_ROUTE) {
                AddEmotionsScreen(
                    child = uiState.selectedChild,
                    emotions = uiState.emotions,
                    onSaveEmotion = { emotionName, imageUris ->
                        viewModel.addEmotion(emotionName, imageUris)
                    },
                    onEmotionClick = { emotion ->
                        selectedEmotionId = emotion.id
                        navController.navigate(EMOTION_DETAIL_ROUTE)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(EMOTION_DETAIL_ROUTE) {
                EmotionDetailScreen(
                    emotion = uiState.emotions.find { it.id == selectedEmotionId },
                    onAddImages = { emotionId, imageUris ->
                        viewModel.addImagesToEmotion(emotionId, imageUris)
                    },
                    onDeleteImages = { imageIds ->
                        viewModel.deleteEmotionImages(imageIds)
                    },
                    onBack = {
                        selectedEmotionId = null
                        navController.popBackStack()
                    }
                )
            }

            composable(EXERCISES_ROUTE) {
                ExercisesMenuScreen(
                    child = uiState.selectedChild,
                    onShowEmotionClick = { navController.navigate(SHOW_EMOTION_ROUTE) },
                    onFindEmotionClick = { navController.navigate(FIND_EMOTION_ROUTE) }
                )
            }

            composable(SHOW_EMOTION_ROUTE) {
                ShowEmotionConfigScreen(
                    child = uiState.selectedChild,
                    emotions = uiState.emotions.filter { it.imageUris.isNotEmpty() },
                    onStartSingleEmotion = { focusEmotionId, distractorEmotionIds ->
                        viewModel.startSingleEmotionExercise(focusEmotionId, distractorEmotionIds)
                        navController.navigate(EXERCISE_SESSION_ROUTE)
                    },
                    onStartMixedEmotions = { emotionIds ->
                        viewModel.startMixedEmotionExercise(emotionIds)
                        navController.navigate(EXERCISE_SESSION_ROUTE)
                    },
                    onBack = {
                        viewModel.clearExerciseSession()
                        navController.popBackStack()
                    }
                )
            }

            composable(FIND_EMOTION_ROUTE) {
                FindEmotionConfigScreen(
                    child = uiState.selectedChild,
                    emotions = uiState.emotions.filter { it.imageUris.isNotEmpty() },
                    onStartFindEmotion = { emotionIds, difficulty ->
                        viewModel.startFindEmotionExercise(emotionIds, difficulty)
                        navController.navigate(EXERCISE_SESSION_ROUTE)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(EXERCISE_SESSION_ROUTE) {
                ExerciseSessionScreen(
                    session = uiState.exerciseSession,
                    emotions = uiState.emotions,
                    onAnswerSelected = { emotionId ->
                        viewModel.submitExerciseAnswer(emotionId)
                    },
                    onNextRound = { viewModel.nextExerciseRound() },
                    onEndSession = {
                        if (uiState.exerciseSession?.exerciseType == ExerciseType.SHOW_THE_EMOTION) {
                            viewModel.endExerciseSession()
                            navController.navigate(EXERCISE_COMPLETE_ROUTE)
                        } else {
                            viewModel.clearExerciseSession()
                            navController.popBackStack(EXERCISES_ROUTE, inclusive = false)
                        }
                    },
                    onBack = {
                        viewModel.clearExerciseSession()
                        navController.popBackStack()
                    }
                )
            }

            composable(EXERCISE_COMPLETE_ROUTE) {
                ExerciseCompleteScreen(
                    session = uiState.exerciseSession,
                    onShowStatistics = { navController.navigate(EXERCISE_STATS_ROUTE) },
                    onClose = {
                        viewModel.clearExerciseSession()
                        navController.popBackStack(EXERCISES_ROUTE, inclusive = false)
                    }
                )
            }

            composable(EXERCISE_STATS_ROUTE) {
                ExerciseStatsScreen(
                    session = uiState.exerciseSession,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AI_ROUTE) {
                AiRecognitionScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    val pendingChild = childPendingDelete
    if (pendingChild != null) {
        AlertDialog(
            onDismissRequest = { childPendingDelete = null },
            title = { Text("Delete Child") },
            text = {
                Text(
                    "Delete ${pendingChild.name} and all associated emotions and images?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChild(pendingChild.id)
                        childPendingDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { childPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
