package com.example.simpleexpensetracker.ui.theme.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.simpleexpensetracker.R;
import com.example.simpleexpensetracker.data.Expense;

import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenseList;

    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Expense expense = expenseList.get(position);


        String note = expense.getNote();


        if (note != null && !note.trim().isEmpty()) {

            holder.txtCategory.setText(String.format("%s (%s)", expense.getCategory(), note));
        } else {

            holder.txtCategory.setText(expense.getCategory());
        }


        holder.txtDate.setText(expense.getDate());
        holder.txtAmount.setText(String.format(Locale.getDefault(), "₱%.2f", expense.getAmount()));
    }

    @Override
    public int getItemCount() {
        return expenseList != null ? expenseList.size() : 0;
    }

    public void updateData(List<Expense> newExpenseList) {
        this.expenseList = newExpenseList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtCategory, txtAmount, txtDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtCategory = itemView.findViewById(R.id.expenseCategoryText);
            txtAmount = itemView.findViewById(R.id.expenseAmountText);
            txtDate = itemView.findViewById(R.id.expenseDateText);
        }
    }
}
