package com.example.simpleexpensetracker.ui.theme;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.simpleexpensetracker.DatabaseHelper;
import com.example.simpleexpensetracker.MainActivity;
import com.example.simpleexpensetracker.R;
import com.example.simpleexpensetracker.SettingsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private DatabaseHelper db;
    private MaterialSwitch notificationSwitch;
    private static final String TAG = "SettingsFragment";

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    handleNotificationSwitch(true);
                } else {
                    notificationSwitch.setChecked(false);
                    Snackbar.make(requireView(), "Notification permission is required.", Snackbar.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<String> requestWriteStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    exportDataToCsv();
                } else {
                    Snackbar.make(requireView(), "Storage permission is required to export data.", Snackbar.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> importCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        showImportConfirmationDialog(uri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showSettingsBadge(false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(getContext());

        View themeSetting = view.findViewById(R.id.setting_theme);
        View importDataSetting = view.findViewById(R.id.setting_import_data);
        View exportDataSetting = view.findViewById(R.id.setting_export_data);
        View aboutSetting = view.findViewById(R.id.setting_about);
        View resetDataSetting = view.findViewById(R.id.setting_reset_data);
        View calculatorSetting = view.findViewById(R.id.setting_calculator);
        notificationSwitch = view.findViewById(R.id.switch_notifications);

        themeSetting.setEnabled(false);
        themeSetting.setAlpha(0.5f);

        exportDataSetting.setOnClickListener(v -> checkStoragePermissionAndExport());
        importDataSetting.setOnClickListener(v -> openFilePicker());

        notificationSwitch.setChecked(SettingsManager.areNotificationsEnabled(requireContext()));
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkNotificationPermission();
            } else {
                handleNotificationSwitch(false);
            }
        });

        aboutSetting.setOnClickListener(v -> showAboutDialog());
        resetDataSetting.setOnClickListener(v -> showResetConfirmationDialog());
        calculatorSetting.setOnClickListener(v -> {
            CalculatorDialogFragment calculatorDialog = new CalculatorDialogFragment();
            calculatorDialog.show(getParentFragmentManager(), "CalculatorDialog");
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = { "text/csv", "text/comma-separated-values", "application/csv", "text/plain" };
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        importCsvLauncher.launch(intent);
    }

    private void showImportConfirmationDialog(Uri uri) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Import Data")
                .setMessage("This will delete all current data and replace it with data from the selected file. This action cannot be undone. Do you want to continue?")
                .setPositiveButton("Import", (dialog, which) -> importDataFromCsv(uri))
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.ic_import)
                .show();
    }

    private void importDataFromCsv(Uri uri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            db.resetAllData();
            String line;
            int expenseCount = 0;
            boolean budgetSet = false;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length == 0) continue;

                if ("BUDGET".equalsIgnoreCase(tokens[0]) && tokens.length >= 4) {
                    try {
                        double monthly = Double.parseDouble(tokens[1]);
                        double weekly = Double.parseDouble(tokens[2]);
                        double daily = Double.parseDouble(tokens[3]);
                        db.setBudget(monthly, weekly, daily);
                        budgetSet = true;
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Could not parse budget line: " + line, e);
                    }
                    continue;
                }

                if ("ID".equalsIgnoreCase(tokens[0])) {
                    continue;
                }

                if (tokens.length >= 4) {
                    try {
                        double amount = Double.parseDouble(tokens[1]);
                        String category = tokens[2].replace("\"", "");
                        String date = tokens[3];
                        String note = (tokens.length > 4) ? tokens[4].replace("\"", "") : "";
                        db.addExpense(category, amount, note, date);
                        expenseCount++;
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Could not parse expense amount in line: " + line, e);
                    }
                }
            }

            String budgetMessage = budgetSet ? "Budget and " + expenseCount : String.valueOf(expenseCount);
            Snackbar.make(requireView(), budgetMessage + " expense records imported.", Snackbar.LENGTH_LONG).show();

            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_dashboard);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error importing CSV", e);
            Snackbar.make(requireView(), "Failed to import data. Check file format.", Snackbar.LENGTH_LONG).show();
        }
    }

    private void checkStoragePermissionAndExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportDataToCsv();
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                exportDataToCsv();
            } else {
                requestWriteStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void exportDataToCsv() {
        Cursor cursor = db.getAllExpensesForExport();
        if (cursor == null) {
            Toast.makeText(getContext(), "No data to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "ExpenseData_" + timeStamp + ".csv";

        StringBuilder csvContent = new StringBuilder();

        double monthly = db.getMonthlyBudget();
        double weekly = db.getWeeklyLimit();
        double daily = db.getDailyLimit();
        csvContent.append("BUDGET,").append(monthly).append(",").append(weekly).append(",").append(daily).append("\n");

        csvContent.append("ID,Amount,Category,Date,Note\n");

        if (cursor.getCount() > 0) {
            int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_ID);
            int amountIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_AMOUNT);
            int categoryIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_CATEGORY);
            int dateIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_DATE);
            int noteIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_NOTE);

            while (cursor.moveToNext()) {
                csvContent.append(cursor.getString(idIndex)).append(",");
                csvContent.append(cursor.getDouble(amountIndex)).append(",");
                csvContent.append("\"").append(cursor.getString(categoryIndex)).append("\",");
                csvContent.append(cursor.getString(dateIndex)).append(",");
                String note = cursor.getString(noteIndex).replace("\n", " ").replace("\"", "\"\"");
                csvContent.append("\"").append(note).append("\"\n");
            }
        }
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = requireContext().getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);

        if (uri != null) {
            try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(csvContent.toString().getBytes());
                    Snackbar.make(requireView(), "Data exported to Downloads folder.", Snackbar.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Snackbar.make(requireView(), "Failed to export data.", Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(requireView(), "Failed to create file for export.", Snackbar.LENGTH_LONG).show();
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                handleNotificationSwitch(true);
            }
        } else {
            handleNotificationSwitch(true);
        }
    }

    private void handleNotificationSwitch(boolean isEnabled) {
        SettingsManager.setNotificationsEnabled(requireContext(), isEnabled);
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;

        if (isEnabled) {
            mainActivity.schedulePeriodicNotificationWorker();
            Snackbar.make(requireView(), "Periodic reminders enabled.", Snackbar.LENGTH_SHORT).show();
        } else {
            mainActivity.cancelPeriodicNotificationWorker();
            Snackbar.make(requireView(), "Periodic reminders disabled.", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("About Expense Tracker")
                .setMessage("Version 1.0\n\nDeveloped by [Archons]")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset All Data")
                .setMessage("Are you sure you want to delete all expenses and budget data? This action cannot be undone.")
                .setPositiveButton("Reset", (dialog, which) -> {
                    db.resetAllData();
                    Toast.makeText(getContext(), "All data has been reset.", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                        if (bottomNav != null) {
                            bottomNav.setSelectedItemId(R.id.nav_dashboard);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }
}
