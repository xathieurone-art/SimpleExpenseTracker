package com.example.simpleexpensetracker.data;

import android.app.Application;
import androidx.lifecycle.LiveData;

// ✅ 2. SOLUTION: Added all the necessary import statements.
import com.example.simpleexpensetracker.ExpenseDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {
    private final ExpenseDao expenseDao;
    private final LiveData<List<Expense>> allExpenses;
    private final ExecutorService executorService;

    public ExpenseRepository(Application application) {

        ExpenseDatabase db = ExpenseDatabase.getDatabase(application);
        expenseDao = db.expenseDao();
        allExpenses = expenseDao.getAllExpenses();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return allExpenses;
    }

    public void insert(Expense expense) {
        executorService.execute(() -> expenseDao.insert(expense));
    }


    public void update(Expense expense) {
        executorService.execute(() -> expenseDao.update(expense));
    }


    public void delete(Expense expense) {
        executorService.execute(() -> expenseDao.delete(expense));
    }
}
