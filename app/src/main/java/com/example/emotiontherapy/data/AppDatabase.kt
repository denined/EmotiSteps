package com.example.emotiontherapy.data

import android.content.Context
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ChildEntity::class,
        EmotionEntity::class,
        EmotionImageEntity::class,
        CompletedExerciseSessionEntity::class,
        ExerciseRoundResultEntity::class,
        RewardTokenEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emotionTherapyDao(): EmotionTherapyDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `completed_exercise_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `childId` INTEGER NOT NULL,
                        `layout` TEXT NOT NULL,
                        `startedAtMillis` INTEGER NOT NULL,
                        `endedAtMillis` INTEGER NOT NULL,
                        `totalRounds` INTEGER NOT NULL,
                        `correctRounds` INTEGER NOT NULL,
                        `accuracyPercent` INTEGER NOT NULL,
                        FOREIGN KEY(`childId`) REFERENCES `children`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_completed_exercise_sessions_childId` ON `completed_exercise_sessions` (`childId`)"
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `exercise_round_results` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `roundNumber` INTEGER NOT NULL,
                        `promptEmotionName` TEXT NOT NULL,
                        `selectedEmotionId` INTEGER NOT NULL,
                        `correctEmotionId` INTEGER NOT NULL,
                        `isCorrect` INTEGER NOT NULL,
                        FOREIGN KEY(`sessionId`) REFERENCES `completed_exercise_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_exercise_round_results_sessionId` ON `exercise_round_results` (`sessionId`)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reward_tokens` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `childId` INTEGER NOT NULL,
                        `uri` TEXT NOT NULL,
                        FOREIGN KEY(`childId`) REFERENCES `children`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_reward_tokens_childId` ON `reward_tokens` (`childId`)"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emotion_therapy.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
