package com.example.simpleexpensetracker.ui.theme;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simpleexpensetracker.DatabaseHelper;
import com.example.simpleexpensetracker.R;
import com.example.simpleexpensetracker.data.Expense;
import com.example.simpleexpensetracker.ui.theme.adapter.ExpenseAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpenseListFragment extends Fragment {

    private TextInputEditText etSearch;
    private ChipGroup chipGroupCategory;
    private TextView tvNoResults;
    private RecyclerView recyclerView;
    private DatabaseHelper db;
    private ExpenseAdapter adapter;
    private ArrayList<Expense> allExpenses;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_expense_list, container, false);

        etSearch = view.findViewById(R.id.etSearch);
        chipGroupCategory = view.findViewById(R.id.chip_group_category);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvNoResults = view.findViewById(R.id.tvNoResults);

        db = new DatabaseHelper(getContext());
        allExpenses = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpenseAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSearchListener();
        setupChipGroupListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllExpensesFromDatabase();
        filterExpenses();
    }

    private void loadAllExpensesFromDatabase() {
        allExpenses.clear();
        try (Cursor cursor = db.getAllExpenses()) {
            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow("id");
                int categoryCol = cursor.getColumnIndexOrThrow("category");
                int amountCol = cursor.getColumnIndexOrThrow("amount");
                int noteCol = cursor.getColumnIndexOrThrow("note");
                int dateCol = cursor.getColumnIndexOrThrow("date");

                while (cursor.moveToNext()) {
                    allExpenses.add(new Expense(
                            cursor.getInt(idCol),
                            cursor.getString(categoryCol),
                            cursor.getDouble(amountCol),
                            cursor.getString(noteCol),
                            cursor.getString(dateCol)
                    ));
                }
            }
        }
    }

    private void filterExpenses() {
        String searchQuery = etSearch.getText().toString().toLowerCase().trim();
        String selectedCategory = "All";

        int checkedChipId = chipGroupCategory.getCheckedChipId();
        if (checkedChipId != View.NO_ID && checkedChipId != R.id.chip_all) {
            Chip selectedChip = chipGroupCategory.findViewById(checkedChipId);
            if (selectedChip != null) {
                selectedCategory = selectedChip.getText().toString();
            }
        }

        ArrayList<Expense> filteredList = new ArrayList<>();

        final List<String> standardCategories = Arrays.asList(
                "Food", "Transport", "Shopping", "Rent", "Bills", "Entertainment", "Health"
        );

        for (Expense expense : allExpenses) {
            boolean categoryMatches;
            String currentCategory = expense.getCategory();

            if (selectedCategory.equals("Other")) {
                categoryMatches = !standardCategories.contains(currentCategory);
            } else {
                categoryMatches = selectedCategory.equals("All") || currentCategory.equalsIgnoreCase(selectedCategory);
            }

            boolean searchMatches = searchQuery.isEmpty() ||
                    currentCategory.toLowerCase().contains(searchQuery) ||
                    (expense.getNote() != null && expense.getNote().toLowerCase().contains(searchQuery));

            if (categoryMatches && searchMatches) {
                filteredList.add(expense);
            }
        }

        adapter.updateData(filteredList);
        updateEmptyViewVisibility(filteredList.isEmpty());
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExpenses();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupChipGroupListener() {
        chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
            filterExpenses();
        });
    }

    private void updateEmptyViewVisibility(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);
        }
    }
}
