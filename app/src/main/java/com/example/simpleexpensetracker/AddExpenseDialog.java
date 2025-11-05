package com.example.simpleexpensetracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.example.simpleexpensetracker.databinding.DialogAddExpenseBinding;
import com.example.simpleexpensetracker.data.Expense;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseDialog {

    public interface OnSaveListener {
        void onSave(Expense expense);
    }

    public static void show(Context context, OnSaveListener listener) {
        DialogAddExpenseBinding binding = DialogAddExpenseBinding.inflate(LayoutInflater.from(context));

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        binding.editDate.setText(dateFormat.format(calendar.getTime()));

        binding.editDate.setOnClickListener(v -> {
            new DatePickerDialog(context,
                    (view1, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        binding.editDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        new AlertDialog.Builder(context)
                .setTitle("Add Expense")
                .setView(binding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    String cat = binding.editCategory.getText().toString().trim();
                    String amtStr = binding.editAmount.getText().toString().trim();
                    String nt = binding.editNote.getText().toString().trim();
                    String dateStr = binding.editDate.getText().toString();

                    if (cat.isEmpty() || amtStr.isEmpty()) {
                        Toast.makeText(context, "Category and Amount are required.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double value = Double.parseDouble(amtStr);

                        Expense expense = new Expense();
                        expense.setCategory(cat);
                        expense.setAmount(value);
                        expense.setNote(nt);
                        expense.setDate(dateStr);

                        listener.onSave(expense);

                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid amount.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
