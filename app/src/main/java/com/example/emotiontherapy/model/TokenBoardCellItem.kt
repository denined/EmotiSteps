package com.example.emotiontherapy.model

data class TokenBoardCellItem(
    val cellIndex: Int,
    val tokenId: Int? = null,
    val tokenUri: String? = null,
    val isReached: Boolean = false
)
