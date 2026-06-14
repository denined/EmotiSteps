package com.example.emotiontherapy.data.local

data class TokenBoardCellWithToken(
    val cellIndex: Int,
    val tokenId: Int?,
    val tokenUri: String?,
    val isReached: Boolean
)
