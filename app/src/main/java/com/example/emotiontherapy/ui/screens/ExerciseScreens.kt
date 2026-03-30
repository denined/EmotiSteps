package com.example.emotiontherapy.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emotiontherapy.Child
import com.example.emotiontherapy.EmotionItem
import com.example.emotiontherapy.ExerciseLayout
import com.example.emotiontherapy.ExerciseSession
import com.example.emotiontherapy.ExerciseType
import com.example.emotiontherapy.FindEmotionDifficulty
import com.example.emotiontherapy.ui.components.BigMenuButton
import com.example.emotiontherapy.ui.components.ComingSoonScreen
import com.example.emotiontherapy.ui.components.EmotionThumbnail
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ExercisesMenuScreen(
    child: Child?,
    onShowEmotionClick: () -> Unit,
    onFindEmotionClick: () -> Unit
) {
    if (child == null) {
        ComingSoonScreen(
            title = "No Child Selected",
            message = "Return and select a child profile first.",
            onBack = {}
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Exercises - ${child.name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        BigMenuButton(
            title = "Show the emotion",
            onClick = onShowEmotionClick
        )
        BigMenuButton(
            title = "Find the emotion",
            onClick = onFindEmotionClick
        )
    }
}

@Composable
fun ShowEmotionConfigScreen(
    child: Child?,
    emotions: List<EmotionItem>,
    onStartSingleEmotion: (Int, List<Int>) -> Unit,
    onStartMixedEmotions: (List<Int>) -> Unit,
    onBack: () -> Unit
) {
    if (child == null) {
        ComingSoonScreen(
            title = "No Child Selected",
            message = "Return and select a child profile first.",
            onBack = onBack
        )
        return
    }

    var selectedLayout by rememberSaveable { mutableStateOf(ExerciseLayout.SINGLE_EMOTION) }
    var focusEmotionId by remember { mutableStateOf<Int?>(null) }
    var distractorIds by remember { mutableStateOf(setOf<Int>()) }
    var mixedEmotionIds by remember { mutableStateOf(setOf<Int>()) }
    var configError by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Show the Emotion - ${child.name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        if (emotions.size < 2) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add at least two emotions with pictures before starting this exercise.",
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
            return
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Choose layout", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedLayout == ExerciseLayout.SINGLE_EMOTION,
                        onClick = { selectedLayout = ExerciseLayout.SINGLE_EMOTION }
                    )
                    Text("One emotion in focus")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedLayout == ExerciseLayout.MIXED_EMOTIONS,
                        onClick = { selectedLayout = ExerciseLayout.MIXED_EMOTIONS }
                    )
                    Text("Mixed emotions")
                }
            }
        }

        if (selectedLayout == ExerciseLayout.SINGLE_EMOTION) {
            Text(text = "Choose the emotion to exercise", fontWeight = FontWeight.SemiBold)
            emotions.forEach { emotion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = focusEmotionId == emotion.id,
                        onClick = {
                            focusEmotionId = emotion.id
                            distractorIds = distractorIds - emotion.id
                            configError = ""
                        }
                    )
                    Text(emotion.name)
                }
            }

            Text(text = "Choose distractor emotions", fontWeight = FontWeight.SemiBold)
            emotions.filter { it.id != focusEmotionId }.forEach { emotion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = distractorIds.contains(emotion.id),
                        onCheckedChange = { checked ->
                            distractorIds = if (checked) {
                                if (distractorIds.size >= 3) {
                                    configError = "You can choose at most three distractor emotions."
                                    distractorIds
                                } else {
                                    configError = ""
                                    distractorIds + emotion.id
                                }
                            } else {
                                configError = ""
                                distractorIds - emotion.id
                            }
                        }
                    )
                    Text(emotion.name)
                }
            }
        } else {
            Text(text = "Choose the emotions for this mixed session", fontWeight = FontWeight.SemiBold)
            emotions.forEach { emotion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = mixedEmotionIds.contains(emotion.id),
                        onCheckedChange = { checked ->
                            mixedEmotionIds = if (checked) {
                                mixedEmotionIds + emotion.id
                            } else {
                                mixedEmotionIds - emotion.id
                            }
                            configError = ""
                        }
                    )
                    Text(emotion.name)
                }
            }
        }

        if (configError.isNotBlank()) {
            Text(text = configError, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                if (selectedLayout == ExerciseLayout.SINGLE_EMOTION) {
                    val focusId = focusEmotionId
                    if (focusId == null) {
                        configError = "Choose the emotion that should be practiced."
                        return@Button
                    }
                    if (distractorIds.isEmpty()) {
                        configError = "Choose at least one distractor emotion."
                        return@Button
                    }
                    onStartSingleEmotion(focusId, distractorIds.toList())
                } else {
                    if (mixedEmotionIds.size < 2) {
                        configError = "Choose at least two emotions for the mixed session."
                        return@Button
                    }
                    onStartMixedEmotions(mixedEmotionIds.toList())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Exercise")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun FindEmotionConfigScreen(
    child: Child?,
    emotions: List<EmotionItem>,
    onStartFindEmotion: (List<Int>, FindEmotionDifficulty) -> Unit,
    onBack: () -> Unit
) {
    if (child == null) {
        ComingSoonScreen(
            title = "No Child Selected",
            message = "Return and select a child profile first.",
            onBack = onBack
        )
        return
    }

    var selectedEmotionIds by remember { mutableStateOf(setOf<Int>()) }
    var difficulty by rememberSaveable { mutableStateOf(FindEmotionDifficulty.MEDIUM) }
    var configError by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Find the Emotion - ${child.name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        if (emotions.size < 2) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add at least two emotions with pictures before starting this exercise.",
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
            return
        }

        Text(
            text = "Choose the emotions that will appear in the game",
            fontWeight = FontWeight.SemiBold
        )

        emotions.forEach { emotion ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = selectedEmotionIds.contains(emotion.id),
                    onCheckedChange = { checked ->
                        selectedEmotionIds = if (checked) {
                            selectedEmotionIds + emotion.id
                        } else {
                            selectedEmotionIds - emotion.id
                        }
                        configError = ""
                    }
                )
                Text(emotion.name)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Choose difficulty", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = difficulty == FindEmotionDifficulty.EASY,
                        onClick = { difficulty = FindEmotionDifficulty.EASY }
                    )
                    Text("Easy")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = difficulty == FindEmotionDifficulty.MEDIUM,
                        onClick = { difficulty = FindEmotionDifficulty.MEDIUM }
                    )
                    Text("Medium")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = difficulty == FindEmotionDifficulty.HARD,
                        onClick = { difficulty = FindEmotionDifficulty.HARD }
                    )
                    Text("Hard")
                }
            }
        }

        if (configError.isNotBlank()) {
            Text(text = configError, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                if (selectedEmotionIds.size < 2) {
                    configError = "Choose at least two emotions for this game."
                    return@Button
                }
                onStartFindEmotion(selectedEmotionIds.toList(), difficulty)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Exercise")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun ExerciseSessionScreen(
    session: ExerciseSession?,
    emotions: List<EmotionItem>,
    onAnswerSelected: (Int) -> Unit,
    onNextRound: () -> Unit,
    onEndSession: () -> Unit,
    onBack: () -> Unit
) {
    if (session == null) {
        ComingSoonScreen(
            title = "Exercise Not Ready",
            message = "Go back and start an exercise session first.",
            onBack = onBack
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Round ${session.roundNumber}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            IconButton(onClick = onEndSession) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "End session"
                )
            }
        }

        if (session.exerciseType == ExerciseType.FIND_THE_EMOTION) {
            var countdownValue by remember(session.roundNumber) {
                mutableStateOf(if (session.roundNumber > 1) 5 else -1)
            }
            val encouragementMessage = remember(session.roundNumber) {
                listOf("Great!", "Good work!", "Amazing!", "Excellent!", "Well done!").random()
            }
            val countdownPulse = rememberInfiniteTransition(label = "countdownPulse")
            val countdownScale by countdownPulse.animateFloat(
                initialValue = 0.92f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "countdownScale"
            )

            LaunchedEffect(session.roundNumber) {
                if (session.roundNumber > 1) {
                    for (value in 5 downTo 0) {
                        countdownValue = value
                        delay(1_000)
                    }
                    countdownValue = -1
                }
            }

            Text(
                text = "Find ${session.currentRound.promptEmotionName}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Correct taps this round: ${session.currentRoundCorrectHits}/8",
                style = MaterialTheme.typography.bodyLarge
            )

            if (countdownValue >= 0) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = encouragementMessage,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = countdownValue.toString(),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.scale(countdownScale)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Next emotion:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = session.currentRound.promptEmotionName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                FindEmotionPlayArea(
                    session = session,
                    emotions = emotions,
                    onEmotionTap = onAnswerSelected,
                    modifier = Modifier.weight(1f)
                )
            }

            if (session.feedbackMessage.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = session.feedbackMessage,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            return@Column
        }

        Text(
            text = "Show me ${session.currentRound.promptEmotionName}",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        if (session.layout == ExerciseLayout.SINGLE_EMOTION) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                session.currentRound.cards.forEach { card ->
                    Button(
                        onClick = { onAnswerSelected(card.emotionId) },
                        enabled = session.selectedAnswerEmotionId == null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        EmotionThumbnail(
                            uriString = card.imageUri,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    session.currentRound.cards,
                    key = { card -> "${card.emotionId}-${card.imageUri}" }
                ) { card ->
                    Button(
                        onClick = { onAnswerSelected(card.emotionId) },
                        enabled = session.selectedAnswerEmotionId == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        EmotionThumbnail(
                            uriString = card.imageUri,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        if (session.feedbackMessage.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = session.feedbackMessage,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Button(onClick = onNextRound, modifier = Modifier.fillMaxWidth()) {
                Text("Next Round")
            }
        }
    }
}

@Composable
private fun FindEmotionPlayArea(
    session: ExerciseSession,
    emotions: List<EmotionItem>,
    onEmotionTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBubbles = remember(session.roundNumber, session.currentRound.correctEmotionId) {
        mutableStateListOf<FindEmotionBubble>()
    }
    val random = remember(session.roundNumber, session.currentRound.correctEmotionId) {
        Random(System.currentTimeMillis())
    }

    LaunchedEffect(session.roundNumber, session.currentRound.correctEmotionId, emotions) {
        activeBubbles.clear()
        var nextBubbleId = 0
        var lastCorrectSpawnAt = 0L
        while (true) {
            val now = System.currentTimeMillis()
            val minimumVisibleBubbles = if (activeBubbles.isEmpty()) 2 else 1
            while (activeBubbles.size < minimumVisibleBubbles) {
                val bubble = buildFindEmotionBubble(
                    bubbleId = nextBubbleId++,
                    session = session,
                    emotions = emotions,
                    activeBubbles = activeBubbles.toList(),
                    random = random,
                    forceCorrect = activeBubbles.none { it.emotionId == session.currentRound.correctEmotionId } &&
                        (now - lastCorrectSpawnAt >= 6_500L),
                    correctAppearanceProbability = session.findEmotionDifficulty.correctAppearanceProbability
                )
                if (bubble.emotionId == session.currentRound.correctEmotionId) {
                    lastCorrectSpawnAt = System.currentTimeMillis()
                }
                activeBubbles += bubble
            }

            while (activeBubbles.size >= 4) {
                delay(250)
            }

            val bubble = buildFindEmotionBubble(
                bubbleId = nextBubbleId++,
                session = session,
                emotions = emotions,
                activeBubbles = activeBubbles.toList(),
                random = random,
                forceCorrect = activeBubbles.none { it.emotionId == session.currentRound.correctEmotionId } &&
                    (System.currentTimeMillis() - lastCorrectSpawnAt >= 6_500L),
                correctAppearanceProbability = session.findEmotionDifficulty.correctAppearanceProbability
            )
            if (bubble.emotionId == session.currentRound.correctEmotionId) {
                lastCorrectSpawnAt = System.currentTimeMillis()
            }
            activeBubbles += bubble
            delay(
                if (activeBubbles.size <= 1) {
                    random.nextLong(500L, 900L)
                } else {
                    random.nextLong(2_200L, 3_500L)
                }
            )
        }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            activeBubbles.forEach { bubble ->
                key(bubble.id) {
                    FindEmotionBubbleItem(
                        bubble = bubble,
                        areaWidth = maxWidth,
                        areaHeight = maxHeight,
                        isCorrect = bubble.emotionId == session.currentRound.correctEmotionId,
                        onCorrectTap = {
                            activeBubbles.removeAll { it.id == bubble.id }
                            onEmotionTap(bubble.emotionId)
                        },
                        onIncorrectTap = {
                            onEmotionTap(bubble.emotionId)
                        },
                        onExpired = {
                            activeBubbles.removeAll { it.id == bubble.id }
                        }
                    )
                }
            }
        }
    }
}

private data class FindEmotionBubble(
    val id: Int,
    val emotionId: Int,
    val imageUri: String,
    val xFraction: Float,
    val yFraction: Float,
    val visibleDurationMillis: Int
)

@Composable
private fun FindEmotionBubbleItem(
    bubble: FindEmotionBubble,
    areaWidth: Dp,
    areaHeight: Dp,
    isCorrect: Boolean,
    onCorrectTap: () -> Unit,
    onIncorrectTap: () -> Unit,
    onExpired: () -> Unit
) {
    var removed by remember(bubble.id) { mutableStateOf(false) }
    var showCorrectFeedback by remember(bubble.id) { mutableStateOf(false) }
    val alpha = remember(bubble.id) { Animatable(0f) }
    val scale = remember(bubble.id) { Animatable(0.92f) }
    val shakeOffset = remember(bubble.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(bubble.id) {
        alpha.animateTo(1f, animationSpec = tween(450, easing = LinearEasing))
        scale.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        delay(bubble.visibleDurationMillis.toLong())
        if (!removed) {
            alpha.animateTo(0f, animationSpec = tween(500, easing = LinearEasing))
            removed = true
            onExpired()
        }
    }

    Box(
        modifier = Modifier
            .size(112.dp)
            .offset(
                x = areaWidth * bubble.xFraction + shakeOffset.value.dp,
                y = areaHeight * bubble.yFraction
            )
            .scale(scale.value)
            .alpha(alpha.value)
            .clickable {
                if (removed) return@clickable
                if (isCorrect) {
                    removed = true
                    scope.launch {
                        showCorrectFeedback = true
                        scale.animateTo(1.12f, animationSpec = tween(180, easing = FastOutSlowInEasing))
                        alpha.animateTo(0f, animationSpec = tween(220, easing = LinearEasing))
                        onCorrectTap()
                    }
                } else {
                    scope.launch {
                        shakeOffset.snapTo(-10f)
                        repeat(3) {
                            shakeOffset.animateTo(10f, animationSpec = tween(45, easing = LinearEasing))
                            shakeOffset.animateTo(-10f, animationSpec = tween(45, easing = LinearEasing))
                        }
                        shakeOffset.animateTo(0f, animationSpec = tween(45, easing = LinearEasing))
                    }
                    onIncorrectTap()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (showCorrectFeedback) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.22f),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xCC00E676)
                )
            ) {}
        }

        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            EmotionThumbnail(
                uriString = bubble.imageUri,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

private fun buildFindEmotionBubble(
    bubbleId: Int,
    session: ExerciseSession,
    emotions: List<EmotionItem>,
    activeBubbles: List<FindEmotionBubble>,
    random: Random,
    forceCorrect: Boolean,
    correctAppearanceProbability: Float
): FindEmotionBubble {
    val selectedEmotions = emotions
        .filter { session.selectedEmotionIds.contains(it.id) }
        .filter { it.imageUris.isNotEmpty() }
    val correctEmotion = selectedEmotions.firstOrNull { it.id == session.currentRound.correctEmotionId }
    val distractors = selectedEmotions.filter { it.id != session.currentRound.correctEmotionId }
    val chosenEmotion = when {
        forceCorrect || distractors.isEmpty() -> correctEmotion
        random.nextFloat() < correctAppearanceProbability -> correctEmotion
        else -> distractors.random(random)
    } ?: selectedEmotions.first()

    val (xFraction, yFraction) = findAvailableBubblePosition(activeBubbles, random)

    return FindEmotionBubble(
        id = bubbleId,
        emotionId = chosenEmotion.id,
        imageUri = chosenEmotion.imageUris.random(random),
        xFraction = xFraction,
        yFraction = yFraction,
        visibleDurationMillis = listOf(5_000, 6_000, 7_000).random(random)
    )
}

private val FindEmotionDifficulty.correctAppearanceProbability: Float
    get() = when (this) {
        FindEmotionDifficulty.EASY -> 0.4f
        FindEmotionDifficulty.MEDIUM -> 0.3f
        FindEmotionDifficulty.HARD -> 0.2f
    }

private fun findAvailableBubblePosition(
    activeBubbles: List<FindEmotionBubble>,
    random: Random
): Pair<Float, Float> {
    val candidatePositions = listOf(
        0.02f to 0.02f,
        0.36f to 0.02f,
        0.70f to 0.02f,
        0.02f to 0.38f,
        0.36f to 0.38f,
        0.70f to 0.38f,
        0.02f to 0.72f,
        0.36f to 0.72f,
        0.70f to 0.72f
    ).shuffled(random)

    val occupiedPositions = activeBubbles.map { it.xFraction to it.yFraction }.toSet()
    return candidatePositions.firstOrNull { it !in occupiedPositions } ?: candidatePositions.first()
}

@Composable
fun ExerciseCompleteScreen(
    session: ExerciseSession?,
    onShowStatistics: () -> Unit,
    onClose: () -> Unit
) {
    if (session == null) {
        ComingSoonScreen(
            title = "Session Not Found",
            message = "Go back and start an exercise session first.",
            onBack = onClose
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Congratulations",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Rounds completed: ${session.answeredRounds.size}")

        Button(onClick = onShowStatistics, modifier = Modifier.fillMaxWidth()) {
            Text("Show Statistics")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ExerciseStatsScreen(
    session: ExerciseSession?,
    onBack: () -> Unit
) {
    if (session == null) {
        ComingSoonScreen(
            title = "Statistics Not Found",
            message = "Go back and start an exercise session first.",
            onBack = onBack
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }

        Text(text = "Session Statistics", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(text = "Correct guesses: ${session.correctRounds}/${session.answeredRounds.size}")
        Text(text = "Accuracy: ${session.accuracyPercent}%")

        if (session.answeredRounds.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No answered rounds were recorded in this session.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(session.answeredRounds, key = { it.roundNumber }) { round ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Round ${round.roundNumber}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "Prompt: ${round.promptEmotionName}")
                            Text(
                                text = if (round.isCorrect) "Status: Correct" else "Status: Incorrect",
                                color = if (round.isCorrect) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
