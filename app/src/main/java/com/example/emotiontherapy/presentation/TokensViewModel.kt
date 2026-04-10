package com.example.emotiontherapy.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class TokensViewModel(application: Application) : AndroidViewModel(application) {
    private val store = EmotionTherapyStoreProvider.get(application)

    fun addRewardTokens(imageUris: List<String>) = store.addRewardTokens(imageUris)
    fun deleteRewardTokens(tokenIds: List<Int>) = store.deleteRewardTokens(tokenIds)
    fun assignTokenToBoardCell(cellIndex: Int, tokenId: Int?) =
        store.assignTokenToBoardCell(cellIndex, tokenId)

    fun setTokenBoardCellReached(cellIndex: Int, isReached: Boolean) =
        store.setTokenBoardCellReached(cellIndex, isReached)

    fun resetTokenBoard() = store.resetTokenBoard()
    fun updateTokenBoardTitle(title: String) = store.updateTokenBoardTitle(title)
}
