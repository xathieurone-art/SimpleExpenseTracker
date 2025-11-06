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

    private BottomNavigationView bottomNavigationView;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String FIRST_RUN_KEY = "isFirstRun";
    private static final String PERIODIC_WORK_TAG = "PeriodicReminderWork";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
                    NotificationUtils.showWelcomeNotification(this);
                    schedulePeriodicNotificationWorker();
                } else {
                    Toast.makeText(this, "Notifications will not be shown.", Toast.LENGTH_LONG).show();
                    SettingsManager.setNotificationsEnabled(this, false);
                }
                editor.putBoolean(FIRST_RUN_KEY, false).apply();
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

        initializeAppLogic();
    }

    private void initializeAppLogic() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(FIRST_RUN_KEY, true);

        if (isFirstRun) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                NotificationUtils.showWelcomeNotification(this);
                schedulePeriodicNotificationWorker();
                prefs.edit().putBoolean(FIRST_RUN_KEY, false).apply();
            }
        }
    }

    public void schedulePeriodicNotificationWorker() {
        PeriodicWorkRequest periodicRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 6, TimeUnit.HOURS)
                        .setInitialDelay(6, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                PERIODIC_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicRequest
        );
    }

    public void cancelPeriodicNotificationWorker() {
        WorkManager.getInstance(this).cancelUniqueWork(PERIODIC_WORK_TAG);
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

    public void updateNotificationBadge() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof DashboardFragment) {
            ((DashboardFragment) currentFragment).checkForNotifications();
        }
    }
}
