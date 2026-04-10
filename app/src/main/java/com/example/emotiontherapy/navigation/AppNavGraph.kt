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
import com.example.emotiontherapy.model.Child
import com.example.emotiontherapy.model.ExerciseType
import com.example.emotiontherapy.presentation.ChildrenViewModel
import com.example.emotiontherapy.presentation.EmotionsViewModel
import com.example.emotiontherapy.presentation.EmotionTherapyUiState
import com.example.emotiontherapy.presentation.EvolutionViewModel
import com.example.emotiontherapy.presentation.ExercisesViewModel
import com.example.emotiontherapy.presentation.TokensViewModel
import com.example.emotiontherapy.ui.components.BottomNavigationBar
import com.example.emotiontherapy.ui.screens.ExerciseCompleteScreen
import com.example.emotiontherapy.ui.screens.ExerciseSessionScreen
import com.example.emotiontherapy.ui.screens.ExerciseStatsScreen
import com.example.emotiontherapy.ui.screens.AddChildScreen
import com.example.emotiontherapy.ui.screens.AddEmotionFormScreen
import com.example.emotiontherapy.ui.screens.AddEmotionsScreen
import com.example.emotiontherapy.ui.screens.AiRecognitionScreen
import com.example.emotiontherapy.ui.screens.EmotionDetailScreen
import com.example.emotiontherapy.ui.screens.EvolutionScreen
import com.example.emotiontherapy.ui.screens.ExercisesMenuScreen
import com.example.emotiontherapy.ui.screens.FindEmotionConfigScreen
import com.example.emotiontherapy.ui.screens.MainScreen
import com.example.emotiontherapy.ui.screens.MoreScreen
import com.example.emotiontherapy.ui.screens.RewardTokensScreen
import com.example.emotiontherapy.ui.screens.ShowEmotionConfigScreen

@Composable
fun AppNavGraph(
    uiState: EmotionTherapyUiState,
    childrenViewModel: ChildrenViewModel,
    emotionsViewModel: EmotionsViewModel,
    exercisesViewModel: ExercisesViewModel,
    evolutionViewModel: EvolutionViewModel,
    tokensViewModel: TokensViewModel
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
                        if (uiState.selectedChild?.id == child.id) {
                            childrenViewModel.clearSelectedChild()
                        } else {
                            childrenViewModel.selectChild(child.id)
                        }
                    },
                    onRenameChild = { childId, newName ->
                        childrenViewModel.renameChild(childId, newName)
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
                        childrenViewModel.addChild(childName)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(MORE_ROUTE) {
                MoreScreen(
                    child = uiState.selectedChild,
                    onSeeEvolutionClick = { navController.navigate(EVOLUTION_ROUTE) },
                    onTokensClick = { navController.navigate(TOKENS_ROUTE) },
                    onAiRecognitionClick = { navController.navigate(AI_ROUTE) },
                    onSelectChildClick = {
                        navController.navigate(MAIN_ROUTE) {
                            popUpTo(MAIN_ROUTE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(EVOLUTION_ROUTE) {
                EvolutionScreen(
                    child = uiState.selectedChild,
                    history = uiState.exerciseHistory,
                    onSetSessionIncludeInEvolution = { sessionId, includeInEvolution ->
                        evolutionViewModel.setSessionIncludeInEvolution(sessionId, includeInEvolution)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(TOKENS_ROUTE) {
                RewardTokensScreen(
                    child = uiState.selectedChild,
                    tokens = uiState.rewardTokens,
                    tokenBoardCells = uiState.tokenBoardCells,
                    tokenBoardSettings = uiState.tokenBoardSettings,
                    onAddTokens = { imageUris ->
                        tokensViewModel.addRewardTokens(imageUris)
                    },
                    onDeleteTokens = { tokenIds ->
                        tokensViewModel.deleteRewardTokens(tokenIds)
                    },
                    onAssignTokenToCell = { cellIndex, tokenId ->
                        tokensViewModel.assignTokenToBoardCell(cellIndex, tokenId)
                    },
                    onSetCellReached = { cellIndex, isReached ->
                        tokensViewModel.setTokenBoardCellReached(cellIndex, isReached)
                    },
                    onResetTokenBoard = {
                        tokensViewModel.resetTokenBoard()
                    },
                    onUpdateTokenBoardTitle = { title ->
                        tokensViewModel.updateTokenBoardTitle(title)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ADD_EMOTIONS_ROUTE) {
                AddEmotionsScreen(
                    child = uiState.selectedChild,
                    emotions = uiState.emotions,
                    onAddEmotionClick = { navController.navigate(ADD_EMOTION_FORM_ROUTE) },
                    onEmotionClick = { emotion ->
                        selectedEmotionId = emotion.id
                        navController.navigate(EMOTION_DETAIL_ROUTE)
                    },
                    onDeleteEmotion = { emotionId ->
                        emotionsViewModel.deleteEmotion(emotionId)
                        if (selectedEmotionId == emotionId) {
                            selectedEmotionId = null
                        }
                    },
                    onBack = {
                        navController.navigate(MAIN_ROUTE) {
                            popUpTo(MAIN_ROUTE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(ADD_EMOTION_FORM_ROUTE) {
                AddEmotionFormScreen(
                    child = uiState.selectedChild,
                    onSaveEmotion = { emotionName, imageUris ->
                        emotionsViewModel.addEmotion(emotionName, imageUris)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(EMOTION_DETAIL_ROUTE) {
                EmotionDetailScreen(
                    emotion = uiState.emotions.find { it.id == selectedEmotionId },
                    onAddImages = { emotionId, imageUris ->
                        emotionsViewModel.addImagesToEmotion(emotionId, imageUris)
                    },
                    onRenameEmotion = { emotionId, newName ->
                        emotionsViewModel.renameEmotion(emotionId, newName)
                    },
                    onDeleteImages = { imageIds ->
                        emotionsViewModel.deleteEmotionImages(imageIds)
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
                    onSelectChildClick = {
                        navController.navigate(MAIN_ROUTE) {
                            popUpTo(MAIN_ROUTE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onShowEmotionClick = { navController.navigate(SHOW_EMOTION_ROUTE) },
                    onFindEmotionClick = { navController.navigate(FIND_EMOTION_ROUTE) }
                )
            }

            composable(SHOW_EMOTION_ROUTE) {
                ShowEmotionConfigScreen(
                    child = uiState.selectedChild,
                    emotions = uiState.emotions.filter { it.imageUris.isNotEmpty() },
                    onStartSingleEmotion = { focusEmotionId, distractorEmotionIds ->
                        exercisesViewModel.startSingleEmotionExercise(focusEmotionId, distractorEmotionIds)
                        navController.navigate(EXERCISE_SESSION_ROUTE)
                    },
                    onStartMixedEmotions = { emotionIds ->
                        exercisesViewModel.startMixedEmotionExercise(emotionIds)
                        navController.navigate(EXERCISE_SESSION_ROUTE)
                    },
                    onSelectChildClick = {
                        navController.navigate(MAIN_ROUTE) {
                            popUpTo(MAIN_ROUTE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onBack = {
                        exercisesViewModel.clearExerciseSession()
                        navController.popBackStack()
                    }
                )
            }

            composable(FIND_EMOTION_ROUTE) {
                FindEmotionConfigScreen(
                    child = uiState.selectedChild,
                    emotions = uiState.emotions.filter { it.imageUris.isNotEmpty() },
                    onStartFindEmotion = { emotionIds, difficulty ->
                        exercisesViewModel.startFindEmotionExercise(emotionIds, difficulty)
                        navController.navigate(EXERCISE_SESSION_ROUTE)
                    },
                    onSelectChildClick = {
                        navController.navigate(MAIN_ROUTE) {
                            popUpTo(MAIN_ROUTE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(EXERCISE_SESSION_ROUTE) {
                ExerciseSessionScreen(
                    session = uiState.exerciseSession,
                    emotions = uiState.emotions,
                    onAnswerSelected = { emotionId ->
                        exercisesViewModel.submitExerciseAnswer(emotionId)
                    },
                    onNextRound = { exercisesViewModel.nextExerciseRound() },
                    onEndSession = {
                        if (uiState.exerciseSession?.exerciseType == ExerciseType.SHOW_THE_EMOTION) {
                            exercisesViewModel.endExerciseSession()
                            navController.navigate(EXERCISE_COMPLETE_ROUTE)
                        } else {
                            exercisesViewModel.clearExerciseSession()
                            navController.popBackStack(EXERCISES_ROUTE, inclusive = false)
                        }
                    },
                    onBack = {
                        exercisesViewModel.clearExerciseSession()
                        navController.popBackStack()
                    }
                )
            }

            composable(EXERCISE_COMPLETE_ROUTE) {
                ExerciseCompleteScreen(
                    session = uiState.exerciseSession,
                    onShowStatistics = { navController.navigate(EXERCISE_STATS_ROUTE) },
                    onClose = {
                        exercisesViewModel.clearExerciseSession()
                        navController.popBackStack(EXERCISES_ROUTE, inclusive = false)
                    }
                )
            }

            composable(EXERCISE_STATS_ROUTE) {
                ExerciseStatsScreen(
                    session = uiState.exerciseSession,
                    onBack = { navController.popBackStack() },
                    onClose = {
                        exercisesViewModel.clearExerciseSession()
                        navController.popBackStack(EXERCISES_ROUTE, inclusive = false)
                    }
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
                        childrenViewModel.deleteChild(pendingChild.id)
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
