package com.example.emotiontherapy.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.emotiontherapy.model.FindEmotionDifficulty

class ExercisesViewModel(application: Application) : AndroidViewModel(application) {
    private val store = EmotionTherapyStoreProvider.get(application)

    fun clearExerciseSession() = store.clearExerciseSession()
    fun startSingleEmotionExercise(focusEmotionId: Int, distractorEmotionIds: List<Int>) =
        store.startSingleEmotionExercise(focusEmotionId, distractorEmotionIds)

    fun startMixedEmotionExercise(emotionIds: List<Int>) =
        store.startMixedEmotionExercise(emotionIds)

    fun startFindEmotionExercise(emotionIds: List<Int>, difficulty: FindEmotionDifficulty) =
        store.startFindEmotionExercise(emotionIds, difficulty)

    fun submitExerciseAnswer(answerEmotionId: Int) = store.submitExerciseAnswer(answerEmotionId)
    fun nextExerciseRound() = store.nextExerciseRound()
    fun endExerciseSession() = store.endExerciseSession()
}
