package com.example.emotiontherapy.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(MAIN_ROUTE, "Children", Icons.Filled.People),
    BottomNavItem(ADD_EMOTIONS_ROUTE, "Emotions", Icons.Filled.Mood),
    BottomNavItem(EXERCISES_ROUTE, "Exercises", Icons.Filled.SportsEsports),
    BottomNavItem(MORE_ROUTE, "More", Icons.Filled.MoreHoriz)
)
