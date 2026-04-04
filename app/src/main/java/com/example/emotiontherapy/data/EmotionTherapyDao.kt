package com.example.emotiontherapy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface EmotionTherapyDao {
    @Query("SELECT * FROM children ORDER BY name ASC")
    fun observeChildren(): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :childId")
    fun observeChildById(childId: Int): Flow<ChildEntity?>

    @Transaction
    @Query("SELECT * FROM emotions WHERE childId = :childId ORDER BY name ASC")
    fun observeEmotionsForChild(childId: Int): Flow<List<EmotionWithImages>>

    @Transaction
    @Query("SELECT * FROM completed_exercise_sessions WHERE childId = :childId ORDER BY endedAtMillis DESC")
    fun observeCompletedExerciseSessionsForChild(childId: Int): Flow<List<CompletedExerciseSessionWithResults>>

    @Query("SELECT * FROM reward_tokens WHERE childId = :childId ORDER BY id DESC")
    fun observeRewardTokensForChild(childId: Int): Flow<List<RewardTokenEntity>>

    @Query("SELECT * FROM token_board_settings WHERE childId = :childId LIMIT 1")
    fun observeTokenBoardSettingsForChild(childId: Int): Flow<TokenBoardSettingsEntity?>

    @Query(
        """
        SELECT 
            token_board_cells.cellIndex AS cellIndex,
            token_board_cells.tokenId AS tokenId,
            reward_tokens.uri AS tokenUri,
            token_board_cells.isReached AS isReached
        FROM token_board_cells
        LEFT JOIN reward_tokens ON token_board_cells.tokenId = reward_tokens.id
        WHERE token_board_cells.childId = :childId
        ORDER BY token_board_cells.cellIndex ASC
        """
    )
    fun observeTokenBoardForChild(childId: Int): Flow<List<TokenBoardCellWithToken>>

    @Insert
    suspend fun insertChild(child: ChildEntity): Long

    @Insert
    suspend fun insertTokenBoardCells(cells: List<TokenBoardCellEntity>)

    @Insert
    suspend fun insertTokenBoardSettings(settings: TokenBoardSettingsEntity)

    @Query("UPDATE children SET name = :newName WHERE id = :childId")
    suspend fun updateChildName(childId: Int, newName: String)

    @Query("DELETE FROM children WHERE id = :childId")
    suspend fun deleteChildById(childId: Int)

    @Insert
    suspend fun insertEmotion(emotion: EmotionEntity): Long

    @Query("UPDATE emotions SET name = :newName WHERE id = :emotionId")
    suspend fun updateEmotionName(emotionId: Int, newName: String)

    @Query("DELETE FROM emotions WHERE id = :emotionId")
    suspend fun deleteEmotionById(emotionId: Int)

    @Insert
    suspend fun insertEmotionImages(images: List<EmotionImageEntity>)

    @Query("DELETE FROM emotion_images WHERE id IN (:imageIds)")
    suspend fun deleteEmotionImagesByIds(imageIds: List<Int>)

    @Insert
    suspend fun insertCompletedExerciseSession(session: CompletedExerciseSessionEntity): Long

    @Query(
        """
        UPDATE completed_exercise_sessions
        SET includeInEvolution = :includeInEvolution
        WHERE id = :sessionId
        """
    )
    suspend fun updateSessionIncludeInEvolution(sessionId: Int, includeInEvolution: Boolean)

    @Insert
    suspend fun insertExerciseRoundResults(results: List<ExerciseRoundResultEntity>)

    @Insert
    suspend fun insertRewardTokens(tokens: List<RewardTokenEntity>)

    @Query("DELETE FROM reward_tokens WHERE id IN (:tokenIds)")
    suspend fun deleteRewardTokensByIds(tokenIds: List<Int>)

    @Query("SELECT COUNT(*) FROM token_board_cells WHERE childId = :childId")
    suspend fun getTokenBoardCellCount(childId: Int): Int

    @Query(
        """
        UPDATE token_board_cells
        SET tokenId = :tokenId, isReached = CASE WHEN :tokenId IS NULL THEN 0 ELSE isReached END
        WHERE childId = :childId AND cellIndex = :cellIndex
        """
    )
    suspend fun updateTokenBoardCellToken(childId: Int, cellIndex: Int, tokenId: Int?)

    @Query(
        """
        UPDATE token_board_cells
        SET isReached = :isReached
        WHERE childId = :childId AND cellIndex = :cellIndex
        """
    )
    suspend fun updateTokenBoardCellReached(childId: Int, cellIndex: Int, isReached: Boolean)

    @Query(
        """
        UPDATE token_board_cells
        SET tokenId = NULL, isReached = 0
        WHERE childId = :childId
        """
    )
    suspend fun resetTokenBoard(childId: Int)

    @Query(
        """
        UPDATE token_board_settings
        SET title = :title
        WHERE childId = :childId
        """
    )
    suspend fun updateTokenBoardTitle(childId: Int, title: String)

    @Query("SELECT COUNT(*) FROM token_board_settings WHERE childId = :childId")
    suspend fun getTokenBoardSettingsCount(childId: Int): Int
}
