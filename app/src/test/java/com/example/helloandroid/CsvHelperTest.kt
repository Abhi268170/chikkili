package com.example.helloandroid

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CsvHelperTest {

    @Test
    fun testTransactionsToCsv() {
        val transactions = listOf(
            Transaction("1", "Milk", "Grocery shop", 5.5, TransactionType.EXPENSE, LocalDate.of(2026, 2, 10)),
            Transaction("2", "Salary", "Monthly pay", 5000.0, TransactionType.INCOME, LocalDate.of(2026, 2, 11))
        )

        val csv = CsvHelper.transactionsToCsv(transactions)
        val lines = csv.lineSequence().filter { it.isNotBlank() }.toList()

        assertEquals(3, lines.size)
        assertEquals("id,title,description,amount,type,date,categoryId", lines[0])
        assertEquals("1,Milk,Grocery shop,5.5,EXPENSE,2026-02-10,null", lines[1])
        assertEquals("2,Salary,Monthly pay,5000.0,INCOME,2026-02-11,null", lines[2])
    }

    @Test
    fun testCsvToTransactions() {
        val csv = """
            id,title,description,amount,type,date,categoryId
            1,Milk,"Shop, corner",5.5,EXPENSE,2026-02-10,cat1
            2,Salary,"Pay ""check""${'"'},5000.0,INCOME,2026-02-11,
            3,Bonus,,1000.0,INCOME,2026-02-12,null
        """.trimIndent()

        val transactions = CsvHelper.csvToTransactions(csv)

        assertEquals(3, transactions.size)
        
        assertEquals("1", transactions[0].id)
        assertEquals("Milk", transactions[0].title)
        assertEquals("Shop, corner", transactions[0].description)
        assertEquals(5.5, transactions[0].amount, 0.0)
        assertEquals(TransactionType.EXPENSE, transactions[0].type)
        assertEquals(LocalDate.of(2026, 2, 10), transactions[0].date)
        assertEquals("cat1", transactions[0].categoryId)

        assertEquals("2", transactions[1].id)
        assertEquals("Pay \"check\"", transactions[1].description)
        assertEquals(null, transactions[1].categoryId) // Test empty categoryId

        assertEquals("3", transactions[2].id)
        assertEquals("Bonus", transactions[2].title)
        assertEquals(1000.0, transactions[2].amount, 0.0)
        assertEquals(TransactionType.INCOME, transactions[2].type)
        assertEquals(LocalDate.of(2026, 2, 12), transactions[2].date)
        assertEquals(null, transactions[2].categoryId) // Test "null" string categoryId
    }

    @Test
    fun testEscapeCsv() {
        val transactions = listOf(
            Transaction("1", "Comma, Here", "Quote \" Here", 10.0, TransactionType.EXPENSE, LocalDate.of(2026, 2, 10))
        )
        val csv = CsvHelper.transactionsToCsv(transactions)
        val lines = csv.lineSequence().filter { it.isNotBlank() }.toList()
        
        // id,title,description,amount,type,date,categoryId
        // 1,"Comma, Here","Quote "" Here",10.0,EXPENSE,2026-02-10,null
        assertEquals("1,\"Comma, Here\",\"Quote \"\" Here\",10.0,EXPENSE,2026-02-10,null", lines[1])

        val parsed = CsvHelper.csvToTransactions(csv)
        assertEquals("Comma, Here", parsed[0].title)
        assertEquals("Quote \" Here", parsed[0].description)
    }
}
