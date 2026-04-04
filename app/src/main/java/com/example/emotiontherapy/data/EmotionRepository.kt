package com.example.emotiontherapy.data

import com.example.emotiontherapy.Child
import com.example.emotiontherapy.EmotionImageItem
import com.example.emotiontherapy.EmotionItem
import com.example.emotiontherapy.ExerciseHistoryItem
import com.example.emotiontherapy.ExerciseHistoryRoundItem
import com.example.emotiontherapy.ExerciseLayout
import com.example.emotiontherapy.ExerciseRoundResult
import com.example.emotiontherapy.RewardTokenItem
import com.example.emotiontherapy.TokenBoardCellItem
import com.example.emotiontherapy.TokenBoardSettingsItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EmotionRepository(
    private val dao: EmotionTherapyDao
) {
    fun observeChildren(): Flow<List<Child>> {
        return dao.observeChildren().map { children ->
            children.map { entity -> Child(id = entity.id, name = entity.name) }
        }
    }

    fun observeChild(childId: Int): Flow<Child?> {
        return dao.observeChildById(childId).map { entity ->
            entity?.let { Child(id = it.id, name = it.name) }
        }
    }

    fun observeEmotionsForChild(childId: Int): Flow<List<EmotionItem>> {
        return dao.observeEmotionsForChild(childId).map { emotions ->
            emotions.map { item ->
                EmotionItem(
                    id = item.emotion.id,
                    name = item.emotion.name,
                    images = item.images.map { image ->
                        EmotionImageItem(
                            id = image.id,
                            uri = image.uri
                        )
                    }
                )
            }
        }
    }

    fun observeExerciseHistoryForChild(childId: Int): Flow<List<ExerciseHistoryItem>> {
        return dao.observeCompletedExerciseSessionsForChild(childId).map { sessions ->
            sessions.map { item ->
                ExerciseHistoryItem(
                    id = item.session.id,
                    layout = ExerciseLayout.valueOf(item.session.layout),
                    layoutLabel = item.session.layout.replace("_", " "),
                    endedAtMillis = item.session.endedAtMillis,
                    totalRounds = item.session.totalRounds,
                    correctRounds = item.session.correctRounds,
                    accuracyPercent = item.session.accuracyPercent,
                    includeInEvolution = item.session.includeInEvolution,
                    rounds = item.results.sortedBy { it.roundNumber }.map { round ->
                        ExerciseHistoryRoundItem(
                            roundNumber = round.roundNumber,
                            promptEmotionName = round.promptEmotionName,
                            isCorrect = round.isCorrect
                        )
                    }
                )
            }
        }
    }

    fun observeRewardTokensForChild(childId: Int): Flow<List<RewardTokenItem>> {
        return dao.observeRewardTokensForChild(childId).map { tokens ->
            tokens.map { token ->
                RewardTokenItem(
                    id = token.id,
                    uri = token.uri
                )
            }
        }
    }

    fun observeTokenBoardForChild(childId: Int): Flow<List<TokenBoardCellItem>> {
        return dao.observeTokenBoardForChild(childId).map { cells ->
            cells.map { cell ->
                TokenBoardCellItem(
                    cellIndex = cell.cellIndex,
                    tokenId = cell.tokenId,
                    tokenUri = cell.tokenUri,
                    isReached = cell.isReached
                )
            }
        }
    }

    fun observeTokenBoardSettingsForChild(childId: Int): Flow<TokenBoardSettingsItem> {
        return dao.observeTokenBoardSettingsForChild(childId).map { settings ->
            TokenBoardSettingsItem(
                title = settings?.title ?: "Token System"
            )
        }
    }

    suspend fun addChild(name: String) {
        val childId = dao.insertChild(ChildEntity(name = name)).toInt()
        initializeTokenBoard(childId)
    }

    suspend fun renameChild(childId: Int, newName: String) {
        dao.updateChildName(childId, newName)
    }

    suspend fun deleteChild(childId: Int) {
        dao.deleteChildById(childId)
    }

    suspend fun addEmotion(childId: Int, emotionName: String, imageUris: List<String>) {
        val emotionId = dao.insertEmotion(
            EmotionEntity(
                childId = childId,
                name = emotionName
            )
        ).toInt()

        dao.insertEmotionImages(
            imageUris.map { uri ->
                EmotionImageEntity(
                    emotionId = emotionId,
                    uri = uri
                )
            }
        )
    }

    suspend fun addImagesToEmotion(emotionId: Int, imageUris: List<String>) {
        dao.insertEmotionImages(
            imageUris.map { uri ->
                EmotionImageEntity(
                    emotionId = emotionId,
                    uri = uri
                )
            }
        )
    }

    suspend fun renameEmotion(emotionId: Int, newName: String) {
        dao.updateEmotionName(emotionId, newName)
    }

    suspend fun deleteEmotion(emotionId: Int) {
        dao.deleteEmotionById(emotionId)
    }

    suspend fun deleteEmotionImages(imageIds: List<Int>) {
        dao.deleteEmotionImagesByIds(imageIds)
    }

    suspend fun saveCompletedExerciseSession(
        childId: Int,
        layout: ExerciseLayout,
        startedAtMillis: Long,
        endedAtMillis: Long,
        roundResults: List<ExerciseRoundResult>
    ) {
        val correctRounds = roundResults.count { it.isCorrect }
        val accuracyPercent = if (roundResults.isEmpty()) 0 else (correctRounds * 100) / roundResults.size

        val sessionId = dao.insertCompletedExerciseSession(
            CompletedExerciseSessionEntity(
                childId = childId,
                layout = layout.name,
                startedAtMillis = startedAtMillis,
                endedAtMillis = endedAtMillis,
                totalRounds = roundResults.size,
                correctRounds = correctRounds,
                accuracyPercent = accuracyPercent,
                includeInEvolution = true
            )
        ).toInt()

        if (roundResults.isNotEmpty()) {
            dao.insertExerciseRoundResults(
                roundResults.map { round ->
                    ExerciseRoundResultEntity(
                        sessionId = sessionId,
                        roundNumber = round.roundNumber,
                        promptEmotionName = round.promptEmotionName,
                        selectedEmotionId = round.selectedEmotionId,
                        correctEmotionId = round.correctEmotionId,
                        isCorrect = round.isCorrect
                    )
                }
            )
        }
    }

    suspend fun addRewardTokens(childId: Int, imageUris: List<String>) {
        dao.insertRewardTokens(
            imageUris.map { uri ->
                RewardTokenEntity(
                    childId = childId,
                    uri = uri
                )
            }
        )
    }

    suspend fun deleteRewardTokens(tokenIds: List<Int>) {
        dao.deleteRewardTokensByIds(tokenIds)
    }

    suspend fun updateTokenBoardCellToken(childId: Int, cellIndex: Int, tokenId: Int?) {
        ensureTokenBoard(childId)
        dao.updateTokenBoardCellToken(childId, cellIndex, tokenId)
    }

    suspend fun updateTokenBoardCellReached(childId: Int, cellIndex: Int, isReached: Boolean) {
        ensureTokenBoard(childId)
        dao.updateTokenBoardCellReached(childId, cellIndex, isReached)
    }

    suspend fun resetTokenBoard(childId: Int) {
        ensureTokenBoard(childId)
        dao.resetTokenBoard(childId)
    }

    suspend fun updateTokenBoardTitle(childId: Int, title: String) {
        ensureTokenBoard(childId)
        ensureTokenBoardSettings(childId)
        dao.updateTokenBoardTitle(childId, title)
    }

    suspend fun ensureTokenBoard(childId: Int) {
        if (dao.getTokenBoardCellCount(childId) == 0) {
            initializeTokenBoard(childId)
        }
        ensureTokenBoardSettings(childId)
    }

    private suspend fun initializeTokenBoard(childId: Int) {
        dao.insertTokenBoardCells(
            List(12) { index ->
                TokenBoardCellEntity(
                    childId = childId,
                    cellIndex = index
                )
            }
        )
    }

    private suspend fun ensureTokenBoardSettings(childId: Int) {
        if (dao.getTokenBoardSettingsCount(childId) == 0) {
            dao.insertTokenBoardSettings(
                TokenBoardSettingsEntity(
                    childId = childId,
                    title = "Token System"
                )
            )
        }
    }

    suspend fun setSessionIncludeInEvolution(sessionId: Int, includeInEvolution: Boolean) {
        dao.updateSessionIncludeInEvolution(sessionId, includeInEvolution)
    }
}
