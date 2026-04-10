package com.example.emotiontherapy.data.local

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
        RewardTokenEntity::class,
        TokenBoardCellEntity::class,
        TokenBoardSettingsEntity::class
    ],
    version = 6,
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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `token_board_cells` (
                        `childId` INTEGER NOT NULL,
                        `cellIndex` INTEGER NOT NULL,
                        `tokenId` INTEGER,
                        `isReached` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`childId`, `cellIndex`),
                        FOREIGN KEY(`childId`) REFERENCES `children`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`tokenId`) REFERENCES `reward_tokens`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_token_board_cells_childId` ON `token_board_cells` (`childId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_token_board_cells_tokenId` ON `token_board_cells` (`tokenId`)"
                )
                db.execSQL(
                    """
                    INSERT INTO `token_board_cells` (`childId`, `cellIndex`, `tokenId`, `isReached`)
                    SELECT `children`.`id`, `indices`.`cellIndex`, NULL, 0
                    FROM `children`
                    CROSS JOIN (
                        SELECT 0 AS `cellIndex`
                        UNION ALL SELECT 1
                        UNION ALL SELECT 2
                        UNION ALL SELECT 3
                        UNION ALL SELECT 4
                        UNION ALL SELECT 5
                        UNION ALL SELECT 6
                        UNION ALL SELECT 7
                        UNION ALL SELECT 8
                        UNION ALL SELECT 9
                        UNION ALL SELECT 10
                        UNION ALL SELECT 11
                    ) AS `indices`
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `token_board_settings` (
                        `childId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        PRIMARY KEY(`childId`),
                        FOREIGN KEY(`childId`) REFERENCES `children`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_token_board_settings_childId` ON `token_board_settings` (`childId`)"
                )
                db.execSQL(
                    """
                    INSERT INTO `token_board_settings` (`childId`, `title`)
                    SELECT `id`, 'Token System'
                    FROM `children`
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE `completed_exercise_sessions`
                    ADD COLUMN `includeInEvolution` INTEGER NOT NULL DEFAULT 1
                    """.trimIndent()
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
                ).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
