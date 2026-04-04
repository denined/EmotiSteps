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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.emotiontherapy.requiredCorrectHits
import com.example.emotiontherapy.ui.components.BigMenuButton
import com.example.emotiontherapy.ui.components.ComingSoonScreen
import com.example.emotiontherapy.ui.components.EmotionThumbnail
import com.example.emotiontherapy.ui.components.FirstTimeTipDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val FindEmotionBubbleSize = 96.dp

@Composable
fun ExercisesMenuScreen(
    child: Child?,
    onSelectChildClick: () -> Unit,
    onShowEmotionClick: () -> Unit,
    onFindEmotionClick: () -> Unit
) {
    if (child == null) {
        ComingSoonScreen(
            title = "No Child Selected",
            message = "Return and select a child profile first.",
            onBack = onSelectChildClick,
            actionLabel = "Select Child"
        )
        return
    }

    FirstTimeTipDialog(
        key = "tip_exercises_screen",
        title = "Exercises",
        message = "Choose an activity for ${child.name}. \"Show the emotion\" tracks scores and progress, while \"Find the emotion\" is a calmer attention game without scoring."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Exercises - ${child.name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.weight(0.6f))

        ExerciseMenuButton(
            title = "Show the emotion",
            subtitle = "Recognition practice with saved emotion pictures",
            icon = Icons.Filled.Visibility,
            onClick = onShowEmotionClick
        )
        ExerciseMenuButton(
            title = "Find the emotion",
            subtitle = "Attention game with moving emotion pictures",
            icon = Icons.Filled.CenterFocusStrong,
            onClick = onFindEmotionClick
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ExerciseMenuButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                )
            }
        }
    }
}

@Composable
fun ShowEmotionConfigScreen(
    child: Child?,
    emotions: List<EmotionItem>,
    onStartSingleEmotion: (Int, List<Int>) -> Unit,
    onStartMixedEmotions: (List<Int>) -> Unit,
    onBack: () -> Unit,
    onSelectChildClick: () -> Unit
) {
    if (child == null) {
        ComingSoonScreen(
            title = "No Child Selected",
            message = "Return and select a child profile first.",
            onBack = onSelectChildClick,
            actionLabel = "Select Child"
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Show the Emotion - ${child.name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (emotions.size < 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = "Add at least two emotions with pictures before starting this exercise.",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Choose layout",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Start Exercise")
        }
    }
}

@Composable
fun FindEmotionConfigScreen(
    child: Child?,
    emotions: List<EmotionItem>,
    onStartFindEmotion: (List<Int>, FindEmotionDifficulty) -> Unit,
    onBack: () -> Unit,
    onSelectChildClick: () -> Unit
) {
    if (child == null) {
        ComingSoonScreen(
            title = "No Child Selected",
            message = "Return and select a child profile first.",
            onBack = onSelectChildClick,
            actionLabel = "Select Child"
        )
        return
    }

    var selectedEmotionIds by remember { mutableStateOf(setOf<Int>()) }
    var difficulty by rememberSaveable { mutableStateOf(FindEmotionDifficulty.MEDIUM) }
    var configError by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Find the Emotion - ${child.name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (emotions.size < 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = "Add at least two emotions with pictures before starting this exercise.",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Choose difficulty",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Start Exercise")
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

    FirstTimeTipDialog(
        key = "tip_exercise_stop_button",
        title = "Exercise Controls",
        message = "You can stop the current exercise at any time by pressing the stop button at the top right."
    )

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
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = "Round ${session.roundNumber}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (countdownValue >= 0) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
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
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
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
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = session.currentRound.promptEmotionName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
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
            return@Column
        }

        val isCorrectSelection = session.roundResolved &&
            session.selectedAnswerEmotionId == session.currentRound.correctEmotionId

        LaunchedEffect(session.roundNumber, session.selectedAnswerEmotionId) {
            if (isCorrectSelection) {
                delay(650)
                onNextRound()
            }
        }

        Text(
            text = "Show me ${session.currentRound.promptEmotionName}",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (session.layout == ExerciseLayout.SINGLE_EMOTION) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                session.currentRound.cards.forEach { card ->
                    ShowEmotionAnswerCard(
                        imageUri = card.imageUri,
                        isEnabled = !session.roundResolved,
                        isCorrectSelection = session.selectedAnswerEmotionId == card.emotionId &&
                            card.emotionId == session.currentRound.correctEmotionId,
                        onClick = { onAnswerSelected(card.emotionId) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
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
                    ShowEmotionAnswerCard(
                        imageUri = card.imageUri,
                        isEnabled = !session.roundResolved,
                        isCorrectSelection = session.selectedAnswerEmotionId == card.emotionId && isCorrectSelection,
                        onClick = { onAnswerSelected(card.emotionId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowEmotionAnswerCard(
    imageUri: String,
    isEnabled: Boolean,
    isCorrectSelection: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isCorrectSelection) {
            Card(
                modifier = Modifier
                    .matchParentSize()
                    .scale(1.04f),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xCC00E676)
                )
            ) {}
        }

        Button(
            onClick = onClick,
            enabled = isEnabled,
            modifier = Modifier.matchParentSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            EmotionThumbnail(
                uriString = imageUri,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
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
                    session.findEmotionDifficulty.initialSpawnDelayRangeMs.random(random)
                } else {
                    session.findEmotionDifficulty.spawnDelayRangeMs.random(random)
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
    var showIncorrectFeedback by remember(bubble.id) { mutableStateOf(false) }
    val alpha = remember(bubble.id) { Animatable(0f) }
    val scale = remember(bubble.id) { Animatable(0.92f) }
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
            .size(FindEmotionBubbleSize)
            .offset(
                x = areaWidth * bubble.xFraction,
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
                        showIncorrectFeedback = true
                        delay(180)
                        showIncorrectFeedback = false
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

        if (showIncorrectFeedback) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.04f),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xCCCFD8DC)
                )
            ) {}
        }

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(24.dp)
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
        visibleDurationMillis = session.findEmotionDifficulty.visibleDurationChoicesMs.random(random)
    )
}

private val FindEmotionDifficulty.correctAppearanceProbability: Float
    get() = when (this) {
        FindEmotionDifficulty.EASY -> 0.4f
        FindEmotionDifficulty.MEDIUM -> 0.3f
        FindEmotionDifficulty.HARD -> 0.2f
    }

private val FindEmotionDifficulty.visibleDurationChoicesMs: List<Int>
    get() = when (this) {
        FindEmotionDifficulty.EASY -> listOf(6_000, 7_000, 8_000)
        FindEmotionDifficulty.MEDIUM -> listOf(5_000, 6_000, 7_000)
        FindEmotionDifficulty.HARD -> listOf(4_000, 5_000, 6_000)
    }

private val FindEmotionDifficulty.initialSpawnDelayRangeMs: LongRange
    get() = when (this) {
        FindEmotionDifficulty.EASY -> 900L..1_400L
        FindEmotionDifficulty.MEDIUM -> 600L..1_000L
        FindEmotionDifficulty.HARD -> 450L..800L
    }

private val FindEmotionDifficulty.spawnDelayRangeMs: LongRange
    get() = when (this) {
        FindEmotionDifficulty.EASY -> 2_800L..4_200L
        FindEmotionDifficulty.MEDIUM -> 2_200L..3_500L
        FindEmotionDifficulty.HARD -> 1_500L..2_600L
    }

private fun findAvailableBubblePosition(
    activeBubbles: List<FindEmotionBubble>,
    random: Random
): Pair<Float, Float> {
    val candidatePositions = listOf(
        0.05f to 0.05f,
        0.37f to 0.05f,
        0.69f to 0.05f,
        0.05f to 0.39f,
        0.37f to 0.39f,
        0.69f to 0.39f,
        0.05f to 0.73f,
        0.37f to 0.73f,
        0.69f to 0.73f
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
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(text = "Rounds completed: ${session.answeredRounds.size}")

        Button(
            onClick = onShowStatistics,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Show Statistics")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ExerciseStatsScreen(
    session: ExerciseSession?,
    onBack: () -> Unit,
    onClose: () -> Unit
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Session Statistics",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close statistics"
                )
            }
        }
        Text(text = "Correct guesses: ${session.correctRounds}/${session.answeredRounds.size}")
        Text(text = "Accuracy: ${session.accuracyPercent}%")

        if (session.answeredRounds.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = "No answered rounds were recorded in this session.",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(session.answeredRounds, key = { it.roundNumber }) { round ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Round ${round.roundNumber}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Prompt: ${round.promptEmotionName}",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
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
