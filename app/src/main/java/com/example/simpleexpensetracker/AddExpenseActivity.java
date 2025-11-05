package com.example.simpleexpensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

// ✅ FIXED: Import the generated View Binding class
import com.example.simpleexpensetracker.databinding.ActivityAddExpenseBinding;

// ✅ SOLUTION: Import the Expense data class
import com.example.simpleexpensetracker.data.Expense;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private ActivityAddExpenseBinding binding;
    private Calendar calendar;
    private ExpenseViewModel expenseViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        calendar = Calendar.getInstance();

        binding.etDate.setOnClickListener(v -> showDatePicker());

        binding.btnSave.setOnClickListener(v -> {
            String amountStr = binding.etAmount.getText().toString();
            String category = binding.etCategory.getText().toString();
            String note = binding.etNote.getText().toString();
            String date = binding.etDate.getText().toString();

            if (amountStr.isEmpty() || category.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please enter amount, category, and date", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                Expense newExpense = new Expense();
                newExpense.setAmount(amount);
                newExpense.setCategory(category);
                newExpense.setNote(note);
                newExpense.setDate(date);

                expenseViewModel.addExpense(newExpense);

                Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    binding.etDate.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}
