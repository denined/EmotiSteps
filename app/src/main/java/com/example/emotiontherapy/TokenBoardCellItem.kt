package com.example.emotiontherapy

data class TokenBoardCellItem(
    val cellIndex: Int,
    val tokenId: Int? = null,
    val tokenUri: String? = null,
    val isReached: Boolean = false
)
