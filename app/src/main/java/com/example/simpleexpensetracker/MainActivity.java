package com.example.simpleexpensetracker;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.simpleexpensetracker.ui.theme.AddExpenseFragment;
import com.example.simpleexpensetracker.ui.theme.BudgetFragment;
import com.example.simpleexpensetracker.ui.theme.DashboardFragment;
import com.example.simpleexpensetracker.ui.theme.ExpenseListFragment;
import com.example.simpleexpensetracker.ui.theme.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String FIRST_RUN_KEY = "isFirstRun";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
                    // The user just granted permission on the first run.
                    // This is the ONLY place we should show the welcome notification in this path.
                    NotificationUtils.showWelcomeNotification(this);
                    // Mark first run as complete.
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                            .putBoolean(FIRST_RUN_KEY, false).apply();

                    schedulePeriodicNotificationWorker();
                } else {
                    SettingsManager.setNotificationsEnabled(this, false);
                    Toast.makeText(this, "Notifications will not be shown as permission was denied.", Toast.LENGTH_LONG).show();
                    // Mark first run as complete even if they deny, so we don't ask again.
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                            .putBoolean(FIRST_RUN_KEY, false).apply();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                fragment = new DashboardFragment();
            } else if (id == R.id.nav_expenses) {
                fragment = new ExpenseListFragment();
            } else if (id == R.id.nav_add) {
                fragment = new AddExpenseFragment();
            } else if (id == R.id.nav_budget) {
                fragment = new BudgetFragment();
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });

        handleFirstRunAndPermissions();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void showSettingsBadge(boolean show) {
        if (show) {
            bottomNavigationView.getOrCreateBadge(R.id.nav_settings).setVisible(true);
        } else {
            bottomNavigationView.removeBadge(R.id.nav_settings);
        }
    }

    private void handleFirstRunAndPermissions() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(FIRST_RUN_KEY, true);


        if (isFirstRun) {
            SettingsManager.setNotificationsEnabled(this, true);
            askForNotificationPermission();
        } else {
            if (SettingsManager.areNotificationsEnabled(this)) {
                askForNotificationPermission();
            }
        }
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {

                handleGrantedPermission();
            } else {

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {

            handleGrantedPermission();
        }
    }

    private void handleGrantedPermission() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(FIRST_RUN_KEY, true);

        if (isFirstRun) {
            NotificationUtils.showWelcomeNotification(this);
            prefs.edit().putBoolean(FIRST_RUN_KEY, false).apply();
        }

        schedulePeriodicNotificationWorker();
    }

    public void schedulePeriodicNotificationWorker() {
        PeriodicWorkRequest periodicNotificationRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 6, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "PeriodicReminderWork",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicNotificationRequest
        );
    }

    public void cancelPeriodicNotificationWorker() {
        WorkManager.getInstance(this).cancelUniqueWork("PeriodicReminderWork");
    }

    public void updateNotificationBadge() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof DashboardFragment) {
            ((DashboardFragment) currentFragment).checkForNotifications();
        }
    }
}
