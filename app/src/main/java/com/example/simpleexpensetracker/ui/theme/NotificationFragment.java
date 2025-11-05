package com.example.simpleexpensetracker.ui.theme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.simpleexpensetracker.DatabaseHelper;
import com.example.simpleexpensetracker.R;

import java.util.Locale;

public class NotificationFragment extends Fragment {

    private TextView notificationMessageText;
    private DatabaseHelper db;

    public NotificationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationMessageText = view.findViewById(R.id.notification_message);
        db = new DatabaseHelper(getContext());

        displayNotificationMessage();

        return view;
    }

    private void displayNotificationMessage() {
        double monthlyBudget = db.getMonthlyBudget();
        double totalExpensesForMonth = db.getTotalExpensesForCurrentMonth();

        if (monthlyBudget > 0 && totalExpensesForMonth > monthlyBudget) {
            double overspendAmount = totalExpensesForMonth - monthlyBudget;
            String message = String.format(Locale.getDefault(),
                    "You have exceeded your monthly budget of ₱%.2f by ₱%.2f. Consider reviewing your spending.",
                    monthlyBudget, overspendAmount);
            notificationMessageText.setText(message);
        } else {
            notificationMessageText.setText("You have no new notifications. Great job staying on budget!");
        }
    }
}
