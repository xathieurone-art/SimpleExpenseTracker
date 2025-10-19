package com.example.simpleexpensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.simpleexpensetracker.data.AppDatabase
import com.example.simpleexpensetracker.data.Expense
import com.example.simpleexpensetracker.data.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: ExpenseRepository
    val allExpenses: LiveData<List<Expense>>

    init {
        val dao = AppDatabase.getDatabase(application).expenseDao()
        repo = ExpenseRepository(dao)
        allExpenses = repo.allExpenses
    }

    fun addExpense(expense: Expense) = viewModelScope.launch {
        repo.insert(expense)
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repo.delete(expense)
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repo.update(expense)
    }
}
