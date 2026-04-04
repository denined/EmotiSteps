package com.example.emotiontherapy.data

data class TokenBoardCellWithToken(
    val cellIndex: Int,
    val tokenId: Int?,
    val tokenUri: String?,
    val isReached: Boolean
)
