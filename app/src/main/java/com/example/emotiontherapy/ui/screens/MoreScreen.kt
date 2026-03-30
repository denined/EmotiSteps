package com.example.emotiontherapy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emotiontherapy.Child
import com.example.emotiontherapy.ui.components.BigMenuButton

@Composable
fun MoreScreen(
    child: Child?,
    onSeeEvolutionClick: () -> Unit,
    onChooseTokensClick: () -> Unit,
    onAiRecognitionClick: () -> Unit
) {
    if (child == null) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Select a child first to access more options.",
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "${child.name} More",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        BigMenuButton(title = "See Evolution", onClick = onSeeEvolutionClick)
        BigMenuButton(title = "Choose Tokens", onClick = onChooseTokensClick)
        BigMenuButton(title = "AI Recognition", onClick = onAiRecognitionClick)
    }
}
