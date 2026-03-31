package com.example.emotiontherapy.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emotiontherapy.Child
import com.example.emotiontherapy.RewardTokenItem
import com.example.emotiontherapy.ui.components.ComingSoonScreen
import com.example.emotiontherapy.ui.components.EmotionThumbnail

@Composable
fun RewardTokensScreen(
    child: Child?,
    tokens: List<RewardTokenItem>,
    onAddTokens: (List<String>) -> Unit,
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

    val context = LocalContext.current
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "${child.name} Tokens", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        Button(
            onClick = { tokenPicker.launch(arrayOf("image/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload Reward Tokens")
        }

        if (tokens.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No reward tokens uploaded yet.",
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
                items(tokens, key = { token -> token.id }) { token ->
                    Card(modifier = Modifier.fillMaxWidth()) {
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
    }
}
