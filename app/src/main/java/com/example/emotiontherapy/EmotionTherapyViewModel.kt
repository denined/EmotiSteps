package com.example.emotiontherapy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emotiontherapy.data.AppDatabase
import com.example.emotiontherapy.data.EmotionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class EmotionTherapyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EmotionRepository(
        AppDatabase.getDatabase(application).emotionTherapyDao()
    )

    private val selectedChildId = MutableStateFlow<Int?>(null)
    private val exerciseSession = MutableStateFlow<ExerciseSession?>(null)

    private val children = repository.observeChildren()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val selectedChild = selectedChildId
        .flatMapLatest { childId ->
            if (childId == null) flowOf(null) else repository.observeChild(childId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val emotions = selectedChildId
        .flatMapLatest { childId ->
            if (childId == null) flowOf(emptyList()) else repository.observeEmotionsForChild(childId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val exerciseHistory = selectedChildId
        .flatMapLatest { childId ->
            if (childId == null) flowOf(emptyList()) else repository.observeExerciseHistoryForChild(childId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val rewardTokens = selectedChildId
        .flatMapLatest { childId ->
            if (childId == null) flowOf(emptyList()) else repository.observeRewardTokensForChild(childId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<EmotionTherapyUiState> = combine(
        children,
        selectedChild,
        emotions,
        exerciseSession
    ) { children, selectedChild, emotions, exerciseSession ->
        EmotionTherapyUiState(
            children = children,
            selectedChild = selectedChild,
            emotions = emotions,
            exerciseSession = exerciseSession
        )
    }.combine(exerciseHistory) { state, exerciseHistory ->
        state.copy(exerciseHistory = exerciseHistory)
    }.combine(rewardTokens) { state, rewardTokens ->
        state.copy(rewardTokens = rewardTokens)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        EmotionTherapyUiState()
    )

    fun selectChild(childId: Int) {
        selectedChildId.value = childId
        exerciseSession.value = null
    }

    fun clearSelectedChild() {
        selectedChildId.value = null
        exerciseSession.value = null
    }

    fun clearExerciseSession() {
        exerciseSession.value = null
    }

    fun addChild(name: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return

        viewModelScope.launch {
            repository.addChild(cleanName)
        }
    }

    fun deleteChild(childId: Int) {
        viewModelScope.launch {
            repository.deleteChild(childId)
            if (selectedChildId.value == childId) {
                selectedChildId.value = null
                exerciseSession.value = null
            }
        }
    }

    fun addEmotion(emotionName: String, imageUris: List<String>) {
        val childId = selectedChildId.value ?: return
        val cleanName = emotionName.trim()
        val sanitizedUris = imageUris.distinct()
        if (cleanName.isBlank() || sanitizedUris.isEmpty()) return

        viewModelScope.launch {
            repository.addEmotion(childId, cleanName, sanitizedUris)
        }
    }

    fun addImagesToEmotion(emotionId: Int, imageUris: List<String>) {
        val sanitizedUris = imageUris.distinct()
        if (sanitizedUris.isEmpty()) return

        viewModelScope.launch {
            repository.addImagesToEmotion(emotionId, sanitizedUris)
        }
    }

    fun addRewardTokens(imageUris: List<String>) {
        val childId = selectedChildId.value ?: return
        val sanitizedUris = imageUris.distinct()
        if (sanitizedUris.isEmpty()) return

        viewModelScope.launch {
            repository.addRewardTokens(childId, sanitizedUris)
        }
    }

    fun deleteEmotionImages(imageIds: List<Int>) {
        if (imageIds.isEmpty()) return

        viewModelScope.launch {
            repository.deleteEmotionImages(imageIds)
        }
    }

    fun startSingleEmotionExercise(focusEmotionId: Int, distractorEmotionIds: List<Int>) {
        val emotionMap = uiState.value.emotions.associateBy { it.id }
        val focusEmotion = emotionMap[focusEmotionId] ?: return
        val sanitizedDistractorIds = distractorEmotionIds.distinct().take(3)
        val distractors = sanitizedDistractorIds.mapNotNull { emotionMap[it] }
            .filter { it.imageUris.isNotEmpty() }

        if (focusEmotion.imageUris.isEmpty() || distractors.isEmpty()) return

        exerciseSession.value = ExerciseSession(
            exerciseType = ExerciseType.SHOW_THE_EMOTION,
            layout = ExerciseLayout.SINGLE_EMOTION,
            selectedEmotionIds = listOf(focusEmotionId) + sanitizedDistractorIds,
            focusEmotionId = focusEmotionId,
            distractorEmotionIds = sanitizedDistractorIds,
            currentRound = buildSingleEmotionRound(focusEmotion, distractors)
        )
    }

    fun startMixedEmotionExercise(emotionIds: List<Int>) {
        val selectedEmotions = uiState.value.emotions
            .filter { emotionIds.contains(it.id) }
            .filter { it.imageUris.isNotEmpty() }

        if (selectedEmotions.size < 2) return

        exerciseSession.value = ExerciseSession(
            exerciseType = ExerciseType.SHOW_THE_EMOTION,
            layout = ExerciseLayout.MIXED_EMOTIONS,
            selectedEmotionIds = selectedEmotions.map { it.id },
            currentRound = buildMixedEmotionRound(selectedEmotions)
        )
    }

    fun startFindEmotionExercise(
        emotionIds: List<Int>,
        difficulty: FindEmotionDifficulty
    ) {
        val selectedEmotions = uiState.value.emotions
            .filter { emotionIds.contains(it.id) }
            .filter { it.imageUris.isNotEmpty() }

        if (selectedEmotions.size < 2) return

        val focusEmotion = selectedEmotions.random(Random)
        exerciseSession.value = ExerciseSession(
            exerciseType = ExerciseType.FIND_THE_EMOTION,
            selectedEmotionIds = selectedEmotions.map { it.id },
            findEmotionDifficulty = difficulty,
            currentRound = ExerciseRound(
                promptEmotionName = focusEmotion.name,
                cards = emptyList(),
                correctEmotionId = focusEmotion.id
            )
        )
    }

    fun submitExerciseAnswer(answerEmotionId: Int) {
        val session = exerciseSession.value ?: return
        if (session.exerciseType == ExerciseType.SHOW_THE_EMOTION && session.selectedAnswerEmotionId != null) {
            return
        }

        if (session.exerciseType == ExerciseType.FIND_THE_EMOTION) {
            handleFindEmotionAnswer(session, answerEmotionId)
            return
        }

        val isCorrect = answerEmotionId == session.currentRound.correctEmotionId
        val roundResult = ExerciseRoundResult(
            roundNumber = session.roundNumber,
            promptEmotionName = session.currentRound.promptEmotionName,
            selectedEmotionId = answerEmotionId,
            correctEmotionId = session.currentRound.correctEmotionId
        )
        val feedback = if (isCorrect) {
            "Correct. Great work."
        } else {
            "Not this one. The correct answer was ${session.currentRound.promptEmotionName}."
        }

        exerciseSession.value = session.copy(
            answeredRounds = session.answeredRounds + roundResult,
            selectedAnswerEmotionId = answerEmotionId,
            feedbackMessage = feedback
        )
    }

    fun endExerciseSession() {
        val childId = selectedChildId.value ?: return
        val session = exerciseSession.value ?: return
        val layout = session.layout ?: return

        viewModelScope.launch {
            repository.saveCompletedExerciseSession(
                childId = childId,
                layout = layout,
                startedAtMillis = session.startedAtMillis,
                endedAtMillis = System.currentTimeMillis(),
                roundResults = session.answeredRounds
            )
        }
    }

    fun nextExerciseRound() {
        val session = exerciseSession.value ?: return
        val emotionMap = uiState.value.emotions.associateBy { it.id }

        val nextRound = when (session.layout) {
            ExerciseLayout.SINGLE_EMOTION -> {
                val focusEmotion = emotionMap[session.focusEmotionId] ?: return
                val distractors = session.distractorEmotionIds
                    .mapNotNull { emotionMap[it] }
                    .filter { it.imageUris.isNotEmpty() }

                if (focusEmotion.imageUris.isEmpty() || distractors.isEmpty()) return
                buildSingleEmotionRound(focusEmotion, distractors)
            }

            ExerciseLayout.MIXED_EMOTIONS -> {
                val selectedEmotions = session.selectedEmotionIds
                    .mapNotNull { emotionMap[it] }
                    .filter { it.imageUris.isNotEmpty() }

                if (selectedEmotions.size < 2) return
                buildMixedEmotionRound(selectedEmotions)
            }

            null -> return
        }

        exerciseSession.value = session.copy(
            roundNumber = session.roundNumber + 1,
            currentRound = nextRound,
            selectedAnswerEmotionId = null,
            feedbackMessage = "",
            currentRoundCorrectHits = 0
        )
    }

    private fun handleFindEmotionAnswer(
        session: ExerciseSession,
        answerEmotionId: Int
    ) {
        if (answerEmotionId != session.currentRound.correctEmotionId) {
            val decreasedHits = (session.currentRoundCorrectHits - 1).coerceAtLeast(0)
            exerciseSession.value = session.copy(
                currentRoundCorrectHits = decreasedHits,
                feedbackMessage = "Not this one. Keep looking for ${session.currentRound.promptEmotionName}.",
                selectedAnswerEmotionId = answerEmotionId
            )
            return
        }

        val newHitCount = session.currentRoundCorrectHits + 1
        val selectedEmotions = uiState.value.emotions
            .filter { session.selectedEmotionIds.contains(it.id) }
            .filter { it.imageUris.isNotEmpty() }

        if (selectedEmotions.size < 2) return

        if (newHitCount >= 8) {
            val nextFocusEmotion = selectedEmotions
                .filter { it.id != session.currentRound.correctEmotionId }
                .ifEmpty { selectedEmotions }
                .random(Random)
            exerciseSession.value = session.copy(
                roundNumber = session.roundNumber + 1,
                currentRound = ExerciseRound(
                    promptEmotionName = nextFocusEmotion.name,
                    cards = emptyList(),
                    correctEmotionId = nextFocusEmotion.id
                ),
                currentRoundCorrectHits = 0,
                feedbackMessage = "Great. Now find ${nextFocusEmotion.name}.",
                selectedAnswerEmotionId = answerEmotionId
            )
            return
        }

        exerciseSession.value = session.copy(
            currentRoundCorrectHits = newHitCount,
            feedbackMessage = "Correct taps: $newHitCount/8",
            selectedAnswerEmotionId = answerEmotionId
        )
    }

    private fun buildSingleEmotionRound(
        focusEmotion: EmotionItem,
        distractors: List<EmotionItem>
    ): ExerciseRound {
        val cards = listOf(
            ExerciseCard(
                emotionId = focusEmotion.id,
                imageUri = focusEmotion.imageUris.random(Random)
            )
        ) + distractors.map { distractorEmotion ->
            ExerciseCard(
                emotionId = distractorEmotion.id,
                imageUri = distractorEmotion.imageUris.random(Random)
            )
        }

        return ExerciseRound(
            promptEmotionName = focusEmotion.name,
            cards = cards.shuffled(Random),
            correctEmotionId = focusEmotion.id
        )
    }

    private fun buildMixedEmotionRound(selectedEmotions: List<EmotionItem>): ExerciseRound {
        val correctEmotion = selectedEmotions.random(Random)
        val cards = selectedEmotions.map { emotion ->
            ExerciseCard(
                emotionId = emotion.id,
                imageUri = emotion.imageUris.random(Random)
            )
        }.shuffled(Random)

        return ExerciseRound(
            promptEmotionName = correctEmotion.name,
            cards = cards,
            correctEmotionId = correctEmotion.id
        )
    }
}
