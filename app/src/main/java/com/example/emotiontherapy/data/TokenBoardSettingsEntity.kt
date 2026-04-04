package com.example.emotiontherapy.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "token_board_settings",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("childId")]
)
data class TokenBoardSettingsEntity(
    @PrimaryKey val childId: Int,
    val title: String = "Token System"
)
