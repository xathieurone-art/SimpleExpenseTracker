package com.example.simpleexpensetracker.ui.theme;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.simpleexpensetracker.DatabaseHelper;
import com.example.simpleexpensetracker.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Locale;

public class BudgetFragment extends Fragment {

    private TextView tvMonthlyBudget, tvMonthlySpent, tvMonthlyRemaining;
    private TextView tvWeeklyDetails, tvDailyDetails;
    private TextView tvProgressPercentage;
    private CircularProgressIndicator progressBar;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        db = new DatabaseHelper(getContext());

        tvMonthlyBudget = view.findViewById(R.id.tvCurrentBudget);
        tvMonthlySpent = view.findViewById(R.id.tvSpent);
        tvMonthlyRemaining = view.findViewById(R.id.tvRemaining);
        tvWeeklyDetails = view.findViewById(R.id.tvWeeklyDetails);
        tvDailyDetails = view.findViewById(R.id.tvDailyDetails);
        progressBar = view.findViewById(R.id.progressBar);
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage);

        view.findViewById(R.id.btnSetBudget).setOnClickListener(v -> showSetBudgetDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgetData();
    }

    private void showSetBudgetDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_budget, null);

        final EditText etMonthly = dialogView.findViewById(R.id.etDialogMonthly);
        final EditText etWeekly = dialogView.findViewById(R.id.etDialogWeekly);
        final EditText etDaily = dialogView.findViewById(R.id.etDialogDaily);

        etMonthly.setText(String.format(Locale.US, "%.2f", db.getMonthlyBudget()));
        etWeekly.setText(String.format(Locale.US, "%.2f", db.getWeeklyLimit()));
        etDaily.setText(String.format(Locale.US, "%.2f", db.getDailyLimit()));

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setTitle("Set Budgets")
                .setPositiveButton("Save", (dialog, which) -> {
                    String monthlyStr = etMonthly.getText().toString();
                    String weeklyStr = etWeekly.getText().toString();
                    String dailyStr = etDaily.getText().toString();

                    double monthly = monthlyStr.isEmpty() ? 0.0 : Double.parseDouble(monthlyStr);
                    double weekly = weeklyStr.isEmpty() ? 0.0 : Double.parseDouble(weeklyStr);
                    double daily = dailyStr.isEmpty() ? 0.0 : Double.parseDouble(dailyStr);

                    db.setBudget(monthly, weekly, daily);
                    Toast.makeText(getContext(), "Budgets saved!", Toast.LENGTH_SHORT).show();
                    loadBudgetData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadBudgetData() {
        double monthlyLimit = db.getMonthlyBudget();
        double weeklyLimit = db.getWeeklyLimit();
        double dailyLimit = db.getDailyLimit();

        double spentThisMonth = db.getTotalExpensesForCurrentMonth();
        double spentThisWeek = db.getTotalExpensesForCurrentWeek();
        double spentToday = db.getTotalExpensesForToday();

        double remainingMonth = monthlyLimit - spentThisMonth;
        tvMonthlyBudget.setText(String.format(Locale.getDefault(), "Budget: ₱%.2f", monthlyLimit));
        tvMonthlySpent.setText(String.format(Locale.getDefault(), "Spent: ₱%.2f", spentThisMonth));
        tvMonthlyRemaining.setText(String.format(Locale.getDefault(), "Remaining: ₱%.2f", remainingMonth));

        if (monthlyLimit > 0) {
            int progress = (int) ((spentThisMonth / monthlyLimit) * 100);
            progressBar.setProgress(Math.min(progress, 100));
            tvProgressPercentage.setText(String.format(Locale.getDefault(), "%d%%", progress));
        } else {
            progressBar.setProgress(0);
            tvProgressPercentage.setText("0%");
        }

        tvWeeklyDetails.setText(String.format(Locale.getDefault(),
                "This Week: ₱%.2f / ₱%.2f", spentThisWeek, weeklyLimit));
        tvDailyDetails.setText(String.format(Locale.getDefault(),
                "Today: ₱%.2f / ₱%.2f", spentToday, dailyLimit));
    }
}
