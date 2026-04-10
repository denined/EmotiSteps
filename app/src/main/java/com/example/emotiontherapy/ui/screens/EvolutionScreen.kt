package com.example.emotiontherapy.ui.screens

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emotiontherapy.model.Child
import com.example.emotiontherapy.model.ExerciseHistoryItem
import com.example.emotiontherapy.model.ExerciseLayout
import com.example.emotiontherapy.ui.components.ComingSoonScreen
import com.example.emotiontherapy.ui.components.FirstTimeTipDialog
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EvolutionScreen(
    child: Child?,
    history: List<ExerciseHistoryItem>,
    onSetSessionIncludeInEvolution: (Int, Boolean) -> Unit,
    onBack: () -> Unit
) {
    if (child == null) {
        ComingSoonScreen(
            title = "Child Not Selected",
            message = "Return and select a child profile first.",
            onBack = onBack
        )
        return
    }

    FirstTimeTipDialog(
        key = "tip_evolution_screen",
        title = "Evolution",
        message = "This screen shows saved sessions over time. If a session should not affect progress charts, you can exclude it from evolution without removing it from history."
    )

    var selectedLayout by rememberSaveable { mutableStateOf(ExerciseLayout.SINGLE_EMOTION) }
    val filteredHistory = history.filter { it.layout == selectedLayout }
    val includedHistory = filteredHistory.filter { it.includeInEvolution }
    val context = LocalContext.current
    var exportStatus by rememberSaveable { mutableStateOf<String?>(null) }
    val averageAccuracy = if (includedHistory.isEmpty()) 0 else includedHistory.sumOf { it.accuracyPercent } / includedHistory.size
    val bestAccuracy = includedHistory.maxOfOrNull { it.accuracyPercent } ?: 0
    var summaryExpanded by rememberSaveable { mutableStateOf(false) }
    val groupedHistory = filteredHistory
        .groupBy { formatMonthYear(it.endedAtMillis) }
        .toList()
        .sortedByDescending { (_, sessions) -> sessions.maxOfOrNull { it.endedAtMillis } ?: 0L }
    val expandedMonths = remember(filteredHistory) {
        mutableStateListOf<String>().apply {
            groupedHistory.firstOrNull()?.first?.let(::add)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Evolution - ${child.name}", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { selectedLayout = ExerciseLayout.SINGLE_EMOTION },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedLayout == ExerciseLayout.SINGLE_EMOTION) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (selectedLayout == ExerciseLayout.SINGLE_EMOTION) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                border = if (selectedLayout == ExerciseLayout.SINGLE_EMOTION) {
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                }
            ) {
                Text("Single Emotion")
            }
            OutlinedButton(
                onClick = { selectedLayout = ExerciseLayout.MIXED_EMOTIONS },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedLayout == ExerciseLayout.MIXED_EMOTIONS) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (selectedLayout == ExerciseLayout.MIXED_EMOTIONS) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                border = if (selectedLayout == ExerciseLayout.MIXED_EMOTIONS) {
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                }
            ) {
                Text("Mixed Emotions")
            }
        }

        if (exportStatus != null) {
            Text(text = exportStatus!!)
        }

        if (filteredHistory.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No saved sessions for this layout yet.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EvolutionSummaryCard(
                    expanded = summaryExpanded,
                    sessionCount = includedHistory.size,
                    averageAccuracy = averageAccuracy,
                    bestAccuracy = bestAccuracy,
                    onToggle = { summaryExpanded = !summaryExpanded }
                )

                EvolutionChart(
                    sessions = includedHistory,
                    onDownloadChart = {
                        exportStatus = exportEvolutionChart(
                            context = context,
                            childName = child.name,
                            layout = selectedLayout,
                            sessions = includedHistory
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    groupedHistory.forEach { (month, sessions) ->
                        item(key = "header-$month") {
                            val isExpanded = expandedMonths.contains(month)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isExpanded) {
                                            expandedMonths.remove(month)
                                        } else {
                                            expandedMonths.add(month)
                                        }
                                    },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = month,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${sessions.size} session(s), ${sessions.count { it.includeInEvolution }} included",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) {
                                            Icons.Filled.ExpandLess
                                        } else {
                                            Icons.Filled.ExpandMore
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (expandedMonths.contains(month)) {
                            items(sessions, key = { it.id }) { session ->
                                val isIncluded = session.includeInEvolution
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = if (isIncluded) {
                                            MaterialTheme.colorScheme.surface
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                                        }
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isIncluded) {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Session ${formatTimestamp(session.endedAtMillis)}",
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isIncluded) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Text(
                                            text = "Layout: ${session.layoutLabel}",
                                            color = if (isIncluded) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Text(
                                            text = "Accuracy: ${session.accuracyPercent}%",
                                            color = if (isIncluded) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Text(
                                            text = "Correct: ${session.correctRounds}/${session.totalRounds}",
                                            color = if (isIncluded) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isIncluded) {
                                                    "Included in evolution"
                                                } else {
                                                    "Excluded from evolution"
                                                },
                                                color = if (isIncluded) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                            OutlinedButton(
                                                onClick = {
                                                    onSetSessionIncludeInEvolution(
                                                        session.id,
                                                        !session.includeInEvolution
                                                    )
                                                },
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isIncluded) {
                                                        MaterialTheme.colorScheme.outline
                                                    } else {
                                                        MaterialTheme.colorScheme.primary
                                                    }
                                                )
                                            ) {
                                                Text(
                                                    if (isIncluded) {
                                                        "Exclude"
                                                    } else {
                                                        "Include"
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvolutionSummaryCard(
    expanded: Boolean,
    sessionCount: Int,
    averageAccuracy: Int,
    bestAccuracy: Int,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Summary",
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Text(text = "Sessions in evolution: $sessionCount")
                Text(text = "Average accuracy: $averageAccuracy%")
                Text(text = "Best accuracy: $bestAccuracy%")
            }
        }
    }
}

@Composable
fun EvolutionChart(
    sessions: List<ExerciseHistoryItem>,
    onDownloadChart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Accuracy Evolution",
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = onDownloadChart,
                    enabled = sessions.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = "Download chart"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartWidth = size.width
                val chartHeight = size.height
                val leftPadding = 24f
                val bottomPadding = 24f
                val topPadding = 12f
                val usableWidth = chartWidth - leftPadding
                val usableHeight = chartHeight - bottomPadding - topPadding

                drawLine(
                    color = outlineColor,
                    start = Offset(leftPadding, topPadding + usableHeight),
                    end = Offset(chartWidth, topPadding + usableHeight),
                    strokeWidth = 3f
                )
                drawLine(
                    color = outlineColor,
                    start = Offset(leftPadding, topPadding),
                    end = Offset(leftPadding, topPadding + usableHeight),
                    strokeWidth = 3f
                )

                if (sessions.isEmpty()) return@Canvas

                val points = sessions
                    .sortedBy { it.endedAtMillis }
                    .mapIndexed { index, session ->
                        val x = if (sessions.size == 1) {
                            leftPadding + (usableWidth / 2f)
                        } else {
                            leftPadding + (usableWidth * index / (sessions.size - 1).toFloat())
                        }
                        val y = topPadding + usableHeight - (usableHeight * (session.accuracyPercent / 100f))
                        Offset(x, y)
                    }

                for (index in 0 until points.lastIndex) {
                    drawLine(
                        color = primaryColor,
                        start = points[index],
                        end = points[index + 1],
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }

                points.forEach { point ->
                    drawCircle(
                        color = primaryColor,
                        radius = 8f,
                        center = point
                    )
                }

                drawRect(
                    color = primaryColor.copy(alpha = 0.08f),
                    topLeft = Offset(leftPadding, topPadding),
                    size = androidx.compose.ui.geometry.Size(usableWidth, usableHeight),
                    style = Stroke(width = 1f)
                )
            }
        }
    }
}

private fun formatTimestamp(timeMillis: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(timeMillis))
}

private fun formatMonthYear(timeMillis: Long): String {
    return SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date(timeMillis))
}

private fun exportEvolutionChart(
    context: android.content.Context,
    childName: String,
    layout: ExerciseLayout,
    sessions: List<ExerciseHistoryItem>
): String {
    if (sessions.isEmpty()) {
        return "No chart data available to export."
    }

    val width = 1600
    val height = 900
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val backgroundPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    val axisPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#7A7A7A")
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    val linePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1B5E20")
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }
    val pointPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1B5E20")
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val titlePaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 54f
        isFakeBoldText = true
        isAntiAlias = true
    }
    val labelPaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 32f
        isAntiAlias = true
    }

    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    canvas.drawText(
        "$childName - ${layout.name.replace("_", " ")} Evolution",
        70f,
        90f,
        titlePaint
    )

    val left = 110f
    val top = 150f
    val right = width - 70f
    val bottom = height - 120f
    val usableWidth = right - left
    val usableHeight = bottom - top

    canvas.drawLine(left, top, left, bottom, axisPaint)
    canvas.drawLine(left, bottom, right, bottom, axisPaint)

    val sortedSessions = sessions.sortedBy { it.endedAtMillis }
    val points = sortedSessions.mapIndexed { index, session ->
        val x = if (sortedSessions.size == 1) {
            left + (usableWidth / 2f)
        } else {
            left + usableWidth * index / (sortedSessions.size - 1).toFloat()
        }
        val y = bottom - usableHeight * (session.accuracyPercent / 100f)
        android.graphics.PointF(x, y)
    }

    for (index in 0 until points.lastIndex) {
        canvas.drawLine(
            points[index].x,
            points[index].y,
            points[index + 1].x,
            points[index + 1].y,
            linePaint
        )
    }

    points.forEachIndexed { index, point ->
        canvas.drawCircle(point.x, point.y, 12f, pointPaint)
        canvas.drawText(
            "${sortedSessions[index].accuracyPercent}%",
            point.x - 24f,
            point.y - 20f,
            labelPaint
        )
    }

    canvas.drawText("0%", 35f, bottom + 10f, labelPaint)
    canvas.drawText("100%", 20f, top + 10f, labelPaint)
    canvas.drawText("Sessions", right - 120f, height - 40f, labelPaint)
    canvas.drawText("Accuracy", 30f, top - 20f, labelPaint)

    val fileName = buildString {
        append(childName.replace(" ", "_"))
        append("_")
        append(layout.name.lowercase())
        append("_evolution_")
        append(System.currentTimeMillis())
        append(".png")
    }

    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            Environment.DIRECTORY_DOWNLOADS + "/EmotionTherapyApp"
        )
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }

    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        ?: return "Failed to create chart file."

    return try {
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        } ?: return "Failed to open chart file for writing."

        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
        "Chart downloaded to Downloads/EmotionTherapyApp."
    } catch (_: Exception) {
        resolver.delete(uri, null, null)
        "Failed to download chart."
    }
}
