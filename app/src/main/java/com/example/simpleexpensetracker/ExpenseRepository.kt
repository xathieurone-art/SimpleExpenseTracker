package com.example.simpleexpensetracker.data

class ExpenseRepository(private val dao: ExpenseDao) {
    val allExpenses = dao.getAllExpenses()

    suspend fun insert(expense: Expense) = dao.insert(expense)
    suspend fun update(expense: Expense) = dao.update(expense)
    suspend fun delete(expense: Expense) = dao.delete(expense)
}
