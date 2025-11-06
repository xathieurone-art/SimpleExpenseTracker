package com.example.simpleexpensetracker.ui.theme;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.example.simpleexpensetracker.DatabaseHelper;
import com.example.simpleexpensetracker.R;
import com.example.simpleexpensetracker.ui.theme.adapter.CategoryListAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseFragment extends Fragment {

    private TextInputLayout inputAmountLayout;
    private EditText etCategory, etAmount, etNote, etDate;
    private Button btnSave;
    private DatabaseHelper db;
    private Calendar calendar;

    private TextInputLayout otherCategoryLayout;
    private EditText otherCategoryEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        inputAmountLayout = view.findViewById(R.id.inputAmount);
        etCategory = view.findViewById(R.id.etCategory);
        etAmount = view.findViewById(R.id.etAmount);
        etNote = view.findViewById(R.id.etNote);
        etDate = view.findViewById(R.id.etDate);
        btnSave = view.findViewById(R.id.btnSave);

        otherCategoryLayout = view.findViewById(R.id.other_category_layout);
        otherCategoryEditText = view.findViewById(R.id.other_category_edit_text);

        db = new DatabaseHelper(getContext());
        calendar = Calendar.getInstance();

        etDate.setOnClickListener(v -> showDatePicker());
        etCategory.setOnClickListener(v -> showCategoryDialog());
        btnSave.setOnClickListener(v -> saveExpense());

        inputAmountLayout.setEndIconOnClickListener(v -> {
            CalculatorDialogFragment calculatorDialog = new CalculatorDialogFragment();
            calculatorDialog.setCalculatorListener(result -> etAmount.setText(String.format(Locale.getDefault(), "%.2f", result)));
            calculatorDialog.show(getParentFragmentManager(), "CalculatorDialog");
        });

        return view;
    }

    private void showDatePicker() {
        new DatePickerDialog(
                getContext(),
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    etDate.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Category");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_category_list, null);
        builder.setView(dialogView);

        ListView listView = dialogView.findViewById(R.id.categoryListView);

        String[] categories = { "Food", "Transport", "Shopping", "Rent", "Bills", "Entertainment", "Health", "Others" };
        int[] icons = { R.drawable.ic_food, R.drawable.ic_transport, R.drawable.ic_shopping, R.drawable.ic_home, R.drawable.ic_bill, R.drawable.ic_entertainment, R.drawable.ic_health, R.drawable.ic_more };

        CategoryListAdapter adapter = new CategoryListAdapter(getContext(), categories, icons);
        listView.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = categories[position];
            etCategory.setText(selectedCategory);

            if (selectedCategory.equals("Others")) {
                otherCategoryLayout.setVisibility(View.VISIBLE);
            } else {
                otherCategoryLayout.setVisibility(View.GONE);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveExpense() {
        String categorySelection = etCategory.getText().toString().trim();
        String finalCategory;

        if (categorySelection.equals("Others")) {
            finalCategory = otherCategoryEditText.getText().toString().trim();
        } else {
            finalCategory = categorySelection;
        }

        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (TextUtils.isEmpty(finalCategory) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(date)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
            return;
        }

        double monthlyLimit = db.getMonthlyBudget();
        double weeklyLimit = db.getWeeklyLimit();
        double dailyLimit = db.getDailyLimit();

        double spentMonth = db.getTotalExpensesForCurrentMonth();
        double spentWeek = db.getTotalExpensesForCurrentWeek();
        double spentToday = db.getTotalExpensesForToday();

        String warningMessage = null;

        if (monthlyLimit > 0 && (spentMonth + amount) > monthlyLimit) {
            warningMessage = String.format(Locale.getDefault(),
                    "Adding this expense will exceed your monthly budget of ₱%,.2f. Do you want to continue?", monthlyLimit);
        } else if (weeklyLimit > 0 && (spentWeek + amount) > weeklyLimit) {
            warningMessage = String.format(Locale.getDefault(),
                    "Adding this expense will exceed your weekly budget of ₱%,.2f. Do you want to continue?", weeklyLimit);
        } else if (dailyLimit > 0 && (spentToday + amount) > dailyLimit) {
            warningMessage = String.format(Locale.getDefault(),
                    "Adding this expense will exceed your daily budget of ₱%,.2f. Do you want to continue?", dailyLimit);
        }

        if (warningMessage != null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Budget Warning")
                    .setMessage(warningMessage)
                    .setPositiveButton("Continue", (dialog, which) -> performSave(finalCategory, amount, note, date))
                    .setNegativeButton("Cancel", null)
                    .setIcon(R.drawable.ic_warning_red)
                    .show();
        } else {
            performSave(finalCategory, amount, note, date);
        }
    }

    private void performSave(String category, double amount, String note, String date) {
        boolean isInserted = db.addExpense(category, amount, note, date);
        if (isInserted) {
            Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        } else {
            Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
        }
    }
}
