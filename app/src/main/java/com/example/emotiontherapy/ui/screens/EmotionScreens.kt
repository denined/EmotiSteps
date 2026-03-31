package com.example.emotiontherapy.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emotiontherapy.Child
import com.example.emotiontherapy.EmotionItem
import com.example.emotiontherapy.ui.components.ComingSoonScreen
import com.example.emotiontherapy.ui.components.EmotionThumbnail

@Composable
fun AddEmotionsScreen(
    child: Child?,
    emotions: List<EmotionItem>,
    onSaveEmotion: (String, List<String>) -> Unit,
    onEmotionClick: (EmotionItem) -> Unit,
    onDeleteEmotion: (Int) -> Unit,
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

    val context = LocalContext.current
    var emotionName by rememberSaveable { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf(emptyList<String>()) }
    var formError by rememberSaveable { mutableStateOf("") }
    var emotionPendingDelete by remember { mutableStateOf<EmotionItem?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val persistedUris = uris.map { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
            uri.toString()
        }.distinct()

        if (persistedUris.isNotEmpty()) {
            selectedImageUris = (selectedImageUris + persistedUris).distinct()
            formError = ""
        }
    }

    val pendingDeleteEmotion = emotionPendingDelete
    if (pendingDeleteEmotion != null) {
        AlertDialog(
            onDismissRequest = { emotionPendingDelete = null },
            title = { Text("Delete Emotion") },
            text = {
                Text("Delete ${pendingDeleteEmotion.name} and all of its pictures?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEmotion(pendingDeleteEmotion.id)
                        emotionPendingDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { emotionPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add Emotions - ${child.name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = emotionName,
            onValueChange = { emotionName = it },
            label = { Text("Emotion Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { imagePicker.launch(arrayOf("image/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Pictures From Device")
        }

        Text(
            text = "Selected pictures for new emotion: ${selectedImageUris.size}",
            style = MaterialTheme.typography.bodyMedium
        )

        if (selectedImageUris.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(selectedImageUris, key = { index, uriString -> "$index-$uriString" }) { _, uriString ->
                    EmotionThumbnail(uriString = uriString, modifier = Modifier.size(88.dp))
                }
            }
        }

        if (formError.isNotBlank()) {
            Text(text = formError, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                val trimmedName = emotionName.trim()
                if (trimmedName.isBlank()) {
                    formError = "Please enter an emotion name."
                    return@Button
                }
                if (selectedImageUris.isEmpty()) {
                    formError = "Please select at least one picture."
                    return@Button
                }

                onSaveEmotion(trimmedName, selectedImageUris)
                emotionName = ""
                selectedImageUris = emptyList()
                formError = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Emotion")
        }

        if (emotions.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(emotions, key = { it.id }) { emotion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEmotionClick(emotion) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = emotion.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { emotionPendingDelete = emotion }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete emotion"
                                    )
                                }
                            }
                            Text(
                                text = "Pictures: ${emotion.images.size}",
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(
                                    emotion.images,
                                    key = { index, image -> "${emotion.id}-$index-${image.id}" }
                                ) { _, image ->
                                    EmotionThumbnail(uriString = image.uri, modifier = Modifier.size(88.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmotionDetailScreen(
    emotion: EmotionItem?,
    onAddImages: (Int, List<String>) -> Unit,
    onRenameEmotion: (Int, String) -> Unit,
    onDeleteImages: (List<Int>) -> Unit,
    onBack: () -> Unit
) {
    if (emotion == null) {
        ComingSoonScreen(
            title = "Emotion Not Found",
            message = "Go back and select an emotion again.",
            onBack = onBack
        )
        return
    }

    val context = LocalContext.current
    var selectedImageIds by remember(emotion.id) { mutableStateOf(setOf<Int>()) }
    var showDeleteDialog by remember(emotion.id) { mutableStateOf(false) }
    var showRenameDialog by remember(emotion.id) { mutableStateOf(false) }
    var renameValue by remember(emotion.id) { mutableStateOf(emotion.name) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val persistedUris = uris.map { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
            uri.toString()
        }.distinct()

        if (persistedUris.isNotEmpty()) {
            onAddImages(emotion.id, persistedUris)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Pictures") },
            text = {
                Text("Delete ${selectedImageIds.size} selected picture(s) from ${emotion.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteImages(selectedImageIds.toList())
                        selectedImageIds = emptySet()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Emotion") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text("Emotion Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameEmotion(emotion.id, renameValue)
                        showRenameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = emotion.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                if (selectedImageIds.isNotEmpty()) {
                    Text(
                        text = "${selectedImageIds.size} picture(s) selected",
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Row {
                IconButton(onClick = {
                    renameValue = emotion.name
                    showRenameDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Rename emotion"
                    )
                }
                IconButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add pictures"
                    )
                }
                if (selectedImageIds.isNotEmpty()) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete selected pictures"
                        )
                    }
                }
            }
        }

        if (emotion.images.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No pictures added yet. Use the plus button to add some.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    emotion.images,
                    key = { image -> "${emotion.id}-${image.id}" }
                ) { image ->
                    Card(
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                if (selectedImageIds.isNotEmpty()) {
                                    selectedImageIds = if (selectedImageIds.contains(image.id)) {
                                        selectedImageIds - image.id
                                    } else {
                                        selectedImageIds + image.id
                                    }
                                }
                            },
                            onLongClick = {
                                selectedImageIds = if (selectedImageIds.contains(image.id)) {
                                    selectedImageIds - image.id
                                } else {
                                    selectedImageIds + image.id
                                }
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            EmotionThumbnail(
                                uriString = image.uri,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                            Text(
                                text = if (selectedImageIds.contains(image.id)) {
                                    "Selected"
                                } else {
                                    "Long press to select"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
