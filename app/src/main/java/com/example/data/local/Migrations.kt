package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room Database Migrations for Income Control.
 *
 * Example of how a future migration should be written (e.g. from version 4 to 5):
 *
 * val MIGRATION_4_5 = object : Migration(4, 5) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         // Example: db.execSQL("ALTER TABLE expenses ADD COLUMN receiptUri TEXT")
 *     }
 * }
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN coversMonths INTEGER NOT NULL DEFAULT 1")
    }
}

val ALL_MIGRATIONS: Array<Migration> = arrayOf(
    MIGRATION_4_5
)
