package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration strategy for AppDatabase.
 *
 * Current canonical version: 6.
 *
 * Historical schema records prior to version 6 were not exported. To prevent crashes
 * and data loss for pre-release dev builds (v1 through v5) while strictly guarding production
 * user data, explicit no-op/alignment migrations are registered for v1->v6, and
 * fallbackToDestructiveMigrationOnDowngrade() is used instead of blanket fallbackToDestructiveMigration().
 */
object AppDatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Dev migration v1 -> v2 schema alignment
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Dev migration v2 -> v3 schema alignment
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Dev migration v3 -> v4 schema alignment
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Dev migration v4 -> v5 schema alignment
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Dev migration v5 -> v6 schema alignment
        }
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6
    )
}
