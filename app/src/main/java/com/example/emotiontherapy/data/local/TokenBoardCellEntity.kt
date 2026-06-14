package com.example.emotiontherapy.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "token_board_cells",
    primaryKeys = ["childId", "cellIndex"],
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RewardTokenEntity::class,
            parentColumns = ["id"],
            childColumns = ["tokenId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("childId"), Index("tokenId")]
)
data class TokenBoardCellEntity(
    val childId: Int,
    val cellIndex: Int,
    val tokenId: Int? = null,
    val isReached: Boolean = false
)
