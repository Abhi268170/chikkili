package com.example.helloandroid

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

/**
 * @Entity tells Room: "Create a database table from this class"
 *
 * Each field becomes a column:
 *   id (TEXT, primary key) | title (TEXT) | amount (REAL) | type (TEXT) | date (TEXT)
 *
 * The @TypeConverters annotation tells Room how to convert LocalDate
 * and TransactionType, since Room only understands basic types like
 * String, Int, Double, etc.
 */
@Entity(tableName = "transactions")
@TypeConverters(Converters::class)
data class Transaction(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String = "",
    val amount: Double,
    val type: TransactionType,
    val date: LocalDate = LocalDate.now(),
    val categoryId: String? = null // Nullable for migration compatibility
)

/**
 * Two possible types of transactions
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * TypeConverters teach Room how to store types it doesn't understand.
 *
 * Room knows: String, Int, Long, Double, Float, ByteArray
 * Room does NOT know: LocalDate, Enum
 *
 * So we convert:
 *   LocalDate  ←→  String   (e.g., "2026-02-10")
 *   TransactionType  ←→  String   (e.g., "INCOME")
 */
class Converters {

    // LocalDate → String (for saving to database)
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.toString()  // "2026-02-10"
    }

    // String → LocalDate (for reading from database)
    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    // TransactionType → String (for saving)
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name  // "INCOME" or "EXPENSE"
    }

    // String → TransactionType (for reading)
    @TypeConverter
    fun toTransactionType(typeString: String): TransactionType {
        return TransactionType.valueOf(typeString)
    }
}
