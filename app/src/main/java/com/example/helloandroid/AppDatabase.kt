package com.example.helloandroid

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The actual database.
 *
 * Think of this as the "file" that holds all your data on the phone.
 * It's stored at: /data/data/com.example.helloandroid/databases/finance_tracker_db
 *
 * @Database tells Room:
 *   - Which tables to create (entities = [Transaction::class])
 *   - What version the schema is (version = 1)
 *
 * "Singleton" pattern: We only ever create ONE instance of this database.
 * If we created multiple, they could conflict with each other.
 */
@Database(
    entities = [Transaction::class, Category::class, Budget::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Room auto-generates this method to give us our DAO
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        // @Volatile means: all threads see the latest value immediately
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance as AppDatabase
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add categoryId column to transactions table
                db.execSQL("ALTER TABLE transactions ADD COLUMN categoryId TEXT DEFAULT NULL")

                // Create categories table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)

                // Create budgets table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `budgets` (
                        `yearMonth` TEXT NOT NULL,
                        `categoryId` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        PRIMARY KEY(`yearMonth`, `categoryId`),
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)

                // Create index on categoryId in budgets table
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_categoryId` ON `budgets` (`categoryId`)")
            }
        }
    }
}
