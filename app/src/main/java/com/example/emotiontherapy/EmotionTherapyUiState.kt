package com.example.emotiontherapy

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
