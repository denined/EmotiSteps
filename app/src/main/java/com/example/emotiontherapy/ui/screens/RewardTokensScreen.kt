@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.emotiontherapy.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.emotiontherapy.model.Child
import com.example.emotiontherapy.model.RewardTokenItem
import com.example.emotiontherapy.model.TokenBoardCellItem
import com.example.emotiontherapy.model.TokenBoardSettingsItem
import com.example.emotiontherapy.ui.components.ComingSoonScreen
import com.example.emotiontherapy.ui.components.EmotionThumbnail
import com.example.emotiontherapy.ui.components.FirstTimeTipDialog

private const val TOKEN_LIBRARY_SECTION = "library"
private const val TOKEN_BOARD_SECTION = "board"

@Composable
fun RewardTokensScreen(
    child: Child?,
    tokens: List<RewardTokenItem>,
    tokenBoardCells: List<TokenBoardCellItem>,
    tokenBoardSettings: TokenBoardSettingsItem,
    onAddTokens: (List<String>) -> Unit,
    onDeleteTokens: (List<Int>) -> Unit,
    onAssignTokenToCell: (Int, Int?) -> Unit,
    onSetCellReached: (Int, Boolean) -> Unit,
    onResetTokenBoard: () -> Unit,
    onUpdateTokenBoardTitle: (String) -> Unit,
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
        key = "tip_tokens_screen",
        title = "Tokens",
        message = "First add token images, then open Token System to stamp them onto the board. Long press a square to mark it as reached."
    )

    val context = LocalContext.current
    var selectedSection by rememberSaveable { mutableStateOf(TOKEN_LIBRARY_SECTION) }
    var selectedTokenIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var cellPickerIndex by remember { mutableStateOf<Int?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showTitleDialog by remember { mutableStateOf(false) }

    val tokenPicker = rememberLauncherForActivityResult(
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
            onAddTokens(persistedUris)
        }
    }

    val displayedBoardCells = remember(tokenBoardCells) {
        if (tokenBoardCells.isEmpty()) {
            List(12) { index -> TokenBoardCellItem(cellIndex = index) }
        } else {
            tokenBoardCells.sortedBy { it.cellIndex }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tokens - ${child.name}",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    selectedSection = TOKEN_LIBRARY_SECTION
                    selectedTokenIds = emptySet()
                },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedSection == TOKEN_LIBRARY_SECTION) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (selectedSection == TOKEN_LIBRARY_SECTION) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                border = if (selectedSection == TOKEN_LIBRARY_SECTION) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Add Tokens")
            }
            OutlinedButton(
                onClick = {
                    selectedSection = TOKEN_BOARD_SECTION
                    selectedTokenIds = emptySet()
                },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedSection == TOKEN_BOARD_SECTION) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (selectedSection == TOKEN_BOARD_SECTION) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                shape = RoundedCornerShape(20.dp),
                border = if (selectedSection == TOKEN_BOARD_SECTION) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                }
            ) {
                Text("Token System")
            }
        }

        if (selectedSection == TOKEN_LIBRARY_SECTION) {
            TokenLibrarySection(
                tokens = tokens,
                selectedTokenIds = selectedTokenIds,
                onAddClick = { tokenPicker.launch(arrayOf("image/*")) },
                onTokenLongPress = { tokenId ->
                    selectedTokenIds = selectedTokenIds + tokenId
                },
                onTokenClick = { tokenId ->
                    if (selectedTokenIds.isNotEmpty()) {
                        selectedTokenIds = if (selectedTokenIds.contains(tokenId)) {
                            selectedTokenIds - tokenId
                        } else {
                            selectedTokenIds + tokenId
                        }
                    }
                },
                onDeleteSelected = { showDeleteDialog = true }
            )
        } else {
            TokenBoardSection(
                tokens = tokens,
                cells = displayedBoardCells,
                title = tokenBoardSettings.title,
                onCellClick = { cellIndex ->
                    cellPickerIndex = cellIndex
                },
                onCellLongPress = { cell ->
                    onSetCellReached(cell.cellIndex, !cell.isReached)
                },
                onEditTitle = { showTitleDialog = true },
                onResetBoard = { showResetDialog = true }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Tokens") },
            text = { Text("Delete the selected token images? Any board squares using them will be cleared.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTokens(selectedTokenIds.toList())
                        selectedTokenIds = emptySet()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Refresh Token System") },
            text = { Text("Clear all 12 squares and remove their reached state?") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetTokenBoard()
                        showResetDialog = false
                    }
                ) {
                    Text("Refresh")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTitleDialog) {
        var titleInput by remember(tokenBoardSettings.title) {
            mutableStateOf(tokenBoardSettings.title)
        }
        AlertDialog(
            onDismissRequest = { showTitleDialog = false },
            title = { Text("Edit Token System Title") },
            text = {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Board title") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateTokenBoardTitle(titleInput)
                        showTitleDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTitleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val pickedCellIndex = cellPickerIndex
    if (pickedCellIndex != null) {
        AlertDialog(
            onDismissRequest = { cellPickerIndex = null },
            title = { Text("Choose Token") },
            text = {
                if (tokens.isEmpty()) {
                    Text("Add token images first.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tokens.size, key = { index -> tokens[index].id }) { index ->
                            val token = tokens[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            onAssignTokenToCell(pickedCellIndex, token.id)
                                            cellPickerIndex = null
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    EmotionThumbnail(
                                        uriString = token.uri,
                                        modifier = Modifier.height(64.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text("Use this token")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        onAssignTokenToCell(pickedCellIndex, null)
                        cellPickerIndex = null
                    }
                ) {
                    Text("Clear Square")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { cellPickerIndex = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TokenLibrarySection(
    tokens: List<RewardTokenItem>,
    selectedTokenIds: Set<Int>,
    onAddClick: () -> Unit,
    onTokenLongPress: (Int) -> Unit,
    onTokenClick: (Int) -> Unit,
    onDeleteSelected: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Token Images",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (selectedTokenIds.isNotEmpty()) {
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete selected tokens"
                    )
                }
            }
            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add tokens"
                )
            }
        }
    }

    if (tokens.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Text(
                text = "No token images uploaded yet.",
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tokens, key = { token -> token.id }) { token ->
            val isSelected = selectedTokenIds.contains(token.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onTokenClick(token.id) },
                        onLongClick = { onTokenLongPress(token.id) }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = if (isSelected) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
                } else {
                    null
                }
            ) {
                EmotionThumbnail(
                    uriString = token.uri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun TokenBoardSection(
    tokens: List<RewardTokenItem>,
    cells: List<TokenBoardCellItem>,
    title: String,
    onCellClick: (Int) -> Unit,
    onCellLongPress: (TokenBoardCellItem) -> Unit,
    onEditTitle: () -> Unit,
    onResetBoard: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onEditTitle) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit board title"
                )
            }
            IconButton(onClick = onResetBoard) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh token system"
                )
            }
        }
    }

    if (tokens.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Text(
                text = "Add token images first, then tap each square to stamp a token into it.",
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(cells, key = { cell -> cell.cellIndex }) { cell ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .combinedClickable(
                        onClick = { onCellClick(cell.cellIndex) },
                        onLongClick = { onCellLongPress(cell) }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (cell.isReached) {
                        Color(0xFFDFF3DF)
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    if (cell.isReached) Color(0xFF2E7D32) else MaterialTheme.colorScheme.tertiary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (cell.tokenUri != null) {
                        EmotionThumbnail(
                            uriString = cell.tokenUri,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = "Tap to\nchoose",
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}
