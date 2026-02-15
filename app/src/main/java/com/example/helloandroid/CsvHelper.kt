package com.example.helloandroid

import java.time.LocalDate

/**
 * Helper class to convert between Transactions and CSV format.
 */
object CsvHelper {

    private const val HEADER = "id,title,description,amount,type,date,categoryId"

    /**
     * Converts a list of transactions to a CSV string.
     */
    fun transactionsToCsv(transactions: List<Transaction>): String {
        val sb = StringBuilder()
        sb.append(HEADER).append("\n")

        for (t in transactions) {
            sb.append(escapeCsv(t.id)).append(",")
            sb.append(escapeCsv(t.title)).append(",")
            sb.append(escapeCsv(t.description)).append(",")
            sb.append(t.amount).append(",")
            sb.append(t.type.name).append(",")
            sb.append(t.date.toString()).append(",")
            sb.append(escapeCsv(t.categoryId ?: "null"))
            sb.append("\r\n")
        }
        return sb.toString()
    }

    /**
     * Parses a CSV string into a list of transactions.
     */
    fun csvToTransactions(csv: String): List<Transaction> {
        val lines = csv.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return emptyList()

        val transactions = mutableListOf<Transaction>()
        
        // Skip header if it exists
        val startLine = if (lines[0].startsWith("id,title")) 1 else 0

        for (i in startLine until lines.size) {
            try {
                val parts = parseCsvLine(lines[i])
                if (parts.size >= 6) {
                    val id = parts[0]
                    val title = parts[1]
                    val description = parts[2]
                    val amount = parts[3].toDoubleOrNull() ?: 0.0
                    val type = TransactionType.valueOf(parts[4])
                    val date = LocalDate.parse(parts[5])
                    val categoryIdRaw = if (parts.size > 6) parts[6] else null
                    val categoryId = if (categoryIdRaw == "null" || categoryIdRaw.isNullOrBlank()) null else categoryIdRaw

                    transactions.add(
                        Transaction(
                            id = id,
                            title = title,
                            description = description,
                            amount = amount,
                            type = type,
                            date = date,
                            categoryId = categoryId
                        )
                    )
                }
            } catch (e: Exception) {
                // Skip malformed lines
                e.printStackTrace()
            }
        }
        return transactions
    }

    /**
     * Basic CSV line parser that handles quoted values containing commas.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    // Double quote inside quotes = escaped quote
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString())
                current = StringBuilder()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    /**
     * Escapes values for CSV if they contain commas or quotes.
     */
    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }
}
