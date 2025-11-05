package com.example.simpleexpensetracker.ui.theme;

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
import java.util.ArrayList;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView totalExpenseText, remainingBudgetText, weeklyExpenseText, dailyExpenseText;
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

        totalExpenseText = view.findViewById(R.id.totalExpenseText);
        remainingBudgetText = view.findViewById(R.id.remainingBudgetText);
        weeklyExpenseText = view.findViewById(R.id.weeklyExpenseText);
        dailyExpenseText = view.findViewById(R.id.dailyExpenseText);

        recyclerView = view.findViewById(R.id.recentExpensesRecycler);
        notificationLayout = view.findViewById(R.id.notificationLayout);
        notificationDot = view.findViewById(R.id.notification_dot);

        db = new DatabaseHelper(getContext());

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

            if (notificationDot != null) {
                notificationDot.setVisibility(View.GONE);
            }
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

        double monthlyBudget = db.getMonthlyBudget();
        double totalExpensesForMonth = db.getTotalExpensesForCurrentMonth();
        double totalExpensesForWeek = db.getTotalExpensesForCurrentWeek();
        double totalExpensesForToday = db.getTotalExpensesForToday();
        double remainingBudget = Math.max(0, monthlyBudget - totalExpensesForMonth);

        totalExpenseText.setText(String.format(Locale.getDefault(), "Spent This Month: ₱%.2f", totalExpensesForMonth));
        remainingBudgetText.setText(String.format(Locale.getDefault(), "Remaining: ₱%.2f", remainingBudget));
        weeklyExpenseText.setText(String.format(Locale.getDefault(), "This Week: ₱%.2f", totalExpensesForWeek));
        dailyExpenseText.setText(String.format(Locale.getDefault(), "Today: ₱%.2f", totalExpensesForToday));

        if (monthlyBudget > 0 && totalExpensesForMonth > monthlyBudget) {
            totalExpenseText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        } else {
            totalExpenseText.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        }

        expenseList.clear();
        Cursor cursor = db.getAllExpenses();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                expenseList.add(new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("note")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date"))
                ));
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();

        checkForNotifications();
    }

    private void checkForNotifications() {
        if (getContext() == null || db == null) {
            return;
        }

        double monthlyBudget = db.getMonthlyBudget();
        double totalExpensesForMonth = db.getTotalExpensesForCurrentMonth();

        if (monthlyBudget > 0 && totalExpensesForMonth > monthlyBudget) {
            notificationDot.setVisibility(View.VISIBLE);
        } else {
            notificationDot.setVisibility(View.GONE);
        }
    }
}
