package com.example.simpleexpensetracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.simpleexpensetracker.data.Expense;
import com.example.simpleexpensetracker.data.ExpenseRepository;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final LiveData<List<Expense>> allExpenses;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        allExpenses = repository.getAllExpenses();
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return allExpenses;
    }

    public void addExpense(Expense expense) {
        repository.insert(expense);
    }

    public void updateExpense(Expense expense) {
        repository.update(expense);
    }

    public void deleteExpense(Expense expense) {
        repository.delete(expense);
    }
}
