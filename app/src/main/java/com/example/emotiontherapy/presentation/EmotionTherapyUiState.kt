package com.example.emotiontherapy.presentation

import com.example.emotiontherapy.model.Child
import com.example.emotiontherapy.model.EmotionItem
import com.example.emotiontherapy.model.ExerciseHistoryItem
import com.example.emotiontherapy.model.ExerciseSession
import com.example.emotiontherapy.model.RewardTokenItem
import com.example.emotiontherapy.model.TokenBoardCellItem
import com.example.emotiontherapy.model.TokenBoardSettingsItem

data class EmotionTherapyUiState(
    val children: List<Child> = emptyList(),
    val selectedChild: Child? = null,
    val emotions: List<EmotionItem> = emptyList(),
    val exerciseSession: ExerciseSession? = null,
    val exerciseHistory: List<ExerciseHistoryItem> = emptyList(),
    val rewardTokens: List<RewardTokenItem> = emptyList(),
    val tokenBoardCells: List<TokenBoardCellItem> = emptyList(),
    val tokenBoardSettings: TokenBoardSettingsItem = TokenBoardSettingsItem()
)
