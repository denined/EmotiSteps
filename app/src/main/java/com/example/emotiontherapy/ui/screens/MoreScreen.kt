package com.example.emotiontherapy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emotiontherapy.Child
import com.example.emotiontherapy.ui.components.ComingSoonScreen

@Composable
fun MoreScreen(
    child: Child?,
    onSeeEvolutionClick: () -> Unit,
    onTokensClick: () -> Unit,
    onAiRecognitionClick: () -> Unit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "More - ${child.name}",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(0.6f))

        MoreMenuButton(
            title = "See Evolution",
            subtitle = "Review progress and statistics over time",
            icon = Icons.Filled.AutoGraph,
            onClick = onSeeEvolutionClick
        )
        MoreMenuButton(
            title = "Tokens",
            subtitle = "Manage rewards and the token board",
            icon = Icons.Filled.Redeem,
            onClick = onTokensClick
        )
        MoreMenuButton(
            title = "AI Recognition",
            subtitle = "Use the camera or gallery for emotion detection",
            icon = Icons.Filled.FaceRetouchingNatural,
            onClick = onAiRecognitionClick
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MoreMenuButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
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
