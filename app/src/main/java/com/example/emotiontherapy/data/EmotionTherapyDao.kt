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

    @Insert
    suspend fun insertChild(child: ChildEntity): Long

    @Query("DELETE FROM children WHERE id = :childId")
    suspend fun deleteChildById(childId: Int)

    @Insert
    suspend fun insertEmotion(emotion: EmotionEntity): Long

    @Insert
    suspend fun insertEmotionImages(images: List<EmotionImageEntity>)

    @Query("DELETE FROM emotion_images WHERE id IN (:imageIds)")
    suspend fun deleteEmotionImagesByIds(imageIds: List<Int>)

    @Insert
    suspend fun insertCompletedExerciseSession(session: CompletedExerciseSessionEntity): Long

    @Insert
    suspend fun insertExerciseRoundResults(results: List<ExerciseRoundResultEntity>)

    @Insert
    suspend fun insertRewardTokens(tokens: List<RewardTokenEntity>)
}
