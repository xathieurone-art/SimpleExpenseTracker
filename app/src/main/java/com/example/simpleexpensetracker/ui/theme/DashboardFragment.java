package com.example.simpleexpensetracker.ui.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.simpleexpensetracker.DatabaseHelper;
import com.example.simpleexpensetracker.R;
import com.example.simpleexpensetracker.data.Expense;
import com.example.simpleexpensetracker.ui.theme.adapter.ExpenseAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView totalExpenseText, remainingBudgetText, dailySpentText, dailyRemainingText, weeklySpentText, weeklyRemainingText;
    private TextView monthlyBudgetWarning, weeklyBudgetWarning, dailyBudgetWarning;
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private ArrayList<Expense> expenseList;
    private DatabaseHelper db;
    private View notificationLayout;
    private ImageView notificationDot;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db = new DatabaseHelper(getContext());
        totalExpenseText = view.findViewById(R.id.totalExpenseText);
        remainingBudgetText = view.findViewById(R.id.remainingBudgetText);
        dailySpentText = view.findViewById(R.id.dailySpentText);
        dailyRemainingText = view.findViewById(R.id.dailyRemainingText);
        weeklySpentText = view.findViewById(R.id.weeklySpentText);
        weeklyRemainingText = view.findViewById(R.id.weeklyRemainingText);
        recyclerView = view.findViewById(R.id.recentExpensesRecycler);
        notificationLayout = view.findViewById(R.id.notificationLayout);
        notificationDot = view.findViewById(R.id.notification_dot);
        monthlyBudgetWarning = view.findViewById(R.id.monthlyBudgetWarning);
        weeklyBudgetWarning = view.findViewById(R.id.weeklyBudgetWarning);
        dailyBudgetWarning = view.findViewById(R.id.dailyBudgetWarning);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseList = new ArrayList<>();
        adapter = new ExpenseAdapter(expenseList);
        recyclerView.setAdapter(adapter);

        notificationLayout.setOnClickListener(v -> {
            Fragment notificationFragment = new NotificationFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, notificationFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (getContext() == null || db == null) {
            return;
        }

        double spentToday = db.getTotalExpensesForToday();
        double spentWeek = db.getTotalExpensesForCurrentWeek();
        double spentMonth = db.getTotalExpensesForCurrentMonth();

        double dailyLimit = db.getDailyLimit();
        double weeklyLimit = db.getWeeklyLimit();
        double monthlyLimit = db.getMonthlyBudget();

        double remainingToday = Math.max(0, dailyLimit - spentToday);
        double remainingWeek = Math.max(0, weeklyLimit - spentWeek);
        double remainingMonth = Math.max(0, monthlyLimit - spentMonth);

        totalExpenseText.setText(String.format(Locale.getDefault(), "₱%,.2f", spentMonth));
        remainingBudgetText.setText(String.format(Locale.getDefault(), "Remaining: ₱%,.2f", remainingMonth));
        dailySpentText.setText(String.format(Locale.getDefault(), "₱%,.2f", spentToday));
        dailyRemainingText.setText(String.format(Locale.getDefault(), "Rem: ₱%,.2f", remainingToday));
        weeklySpentText.setText(String.format(Locale.getDefault(), "₱%,.2f", spentWeek));
        weeklyRemainingText.setText(String.format(Locale.getDefault(), "Rem: ₱%,.2f", remainingWeek));

        final int dangerColor = ContextCompat.getColor(requireContext(), R.color.danger_red);
        final int defaultColor = ContextCompat.getColor(requireContext(), R.color.white);
        final int secondaryColor = ContextCompat.getColor(requireContext(), R.color.secondary_text_color);

        if (monthlyLimit > 0 && spentMonth > monthlyLimit) {
            totalExpenseText.setTextColor(dangerColor);
            remainingBudgetText.setTextColor(dangerColor);
            monthlyBudgetWarning.setVisibility(View.VISIBLE);
        } else {
            totalExpenseText.setTextColor(defaultColor);
            remainingBudgetText.setTextColor(secondaryColor);
            monthlyBudgetWarning.setVisibility(View.GONE);
        }

        if (weeklyLimit > 0 && spentWeek > weeklyLimit) {
            weeklySpentText.setTextColor(dangerColor);
            weeklyRemainingText.setTextColor(dangerColor);
            weeklyBudgetWarning.setVisibility(View.VISIBLE);
        } else {
            weeklySpentText.setTextColor(defaultColor);
            weeklyRemainingText.setTextColor(secondaryColor);
            weeklyBudgetWarning.setVisibility(View.GONE);
        }

        if (dailyLimit > 0 && spentToday > dailyLimit) {
            dailySpentText.setTextColor(dangerColor);
            dailyRemainingText.setTextColor(dangerColor);
            dailyBudgetWarning.setVisibility(View.VISIBLE);
        } else {
            dailySpentText.setTextColor(defaultColor);
            dailyRemainingText.setTextColor(secondaryColor);
            dailyBudgetWarning.setVisibility(View.GONE);
        }

        expenseList.clear();
        try (Cursor cursor = db.getRecentExpenses(5)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_ID);
                int categoryIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_CATEGORY);
                int amountIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_AMOUNT);
                int noteIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_NOTE);
                int dateIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_DATE);

                do {
                    expenseList.add(new Expense(
                            cursor.getInt(idIndex),
                            cursor.getString(categoryIndex),
                            cursor.getDouble(amountIndex),
                            cursor.getString(noteIndex),
                            cursor.getString(dateIndex)
                    ));
                } while (cursor.moveToNext());
            }
        }
        adapter.notifyDataSetChanged();

        checkForNotifications();
    }

    public void checkForNotifications() {
        if (db == null) {
            return;
        }

        if (db.getUnreadNotificationCount() > 0) {
            notificationDot.setVisibility(View.VISIBLE);
        } else {
            notificationDot.setVisibility(View.GONE);
        }
    }
}
