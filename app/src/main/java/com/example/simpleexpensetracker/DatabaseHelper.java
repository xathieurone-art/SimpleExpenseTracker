package com.example.simpleexpensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseTracker.db";
    private static final int DATABASE_VERSION = 5;

    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_BUDGET = "budget";
    public static final String TABLE_NOTIFICATIONS = "notifications";

    public static final String COL_EXPENSE_ID = "id";
    public static final String COL_EXPENSE_CATEGORY = "category";
    public static final String COL_EXPENSE_AMOUNT = "amount";
    public static final String COL_EXPENSE_NOTE = "note";
    public static final String COL_EXPENSE_DATE = "date";

    private static final String KEY_BUDGET_ID = "id";
    private static final String KEY_BUDGET_MONTHLY = "monthly_limit";
    private static final String KEY_BUDGET_WEEKLY = "weekly_limit";
    private static final String KEY_BUDGET_DAILY = "daily_limit";

    public static final String COL_NOTIF_ID = "id";
    public static final String COL_NOTIF_MESSAGE = "message";
    public static final String COL_NOTIF_TIMESTAMP = "timestamp";
    public static final String COL_NOTIF_IS_READ = "is_read";
    public static final String COL_NOTIF_TYPE = "notification_type";

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    private SimpleDateFormat getUtcDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + "("
                + COL_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_EXPENSE_CATEGORY + " TEXT,"
                + COL_EXPENSE_AMOUNT + " REAL,"
                + COL_EXPENSE_NOTE + " TEXT,"
                + COL_EXPENSE_DATE + " TEXT" + ")";

        String CREATE_BUDGET_TABLE = "CREATE TABLE " + TABLE_BUDGET + "("
                + KEY_BUDGET_ID + " INTEGER PRIMARY KEY,"
                + KEY_BUDGET_MONTHLY + " REAL,"
                + KEY_BUDGET_WEEKLY + " REAL,"
                + KEY_BUDGET_DAILY + " REAL" + ")";

        String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + COL_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_NOTIF_MESSAGE + " TEXT,"
                + COL_NOTIF_TIMESTAMP + " TEXT,"
                + COL_NOTIF_IS_READ + " INTEGER DEFAULT 0,"
                + COL_NOTIF_TYPE + " TEXT" + ")";

        db.execSQL(CREATE_EXPENSES_TABLE);
        db.execSQL(CREATE_BUDGET_TABLE);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);

        ContentValues initialBudget = new ContentValues();
        initialBudget.put(KEY_BUDGET_ID, 1);
        initialBudget.put(KEY_BUDGET_MONTHLY, 0.0);
        initialBudget.put(KEY_BUDGET_WEEKLY, 0.0);
        initialBudget.put(KEY_BUDGET_DAILY, 0.0);
        db.insert(TABLE_BUDGET, null, initialBudget);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_BUDGET + " RENAME COLUMN budget_amount TO " + KEY_BUDGET_MONTHLY);
                db.execSQL("ALTER TABLE " + TABLE_BUDGET + " ADD COLUMN " + KEY_BUDGET_WEEKLY + " REAL DEFAULT 0.0");
                db.execSQL("ALTER TABLE " + TABLE_BUDGET + " ADD COLUMN " + KEY_BUDGET_DAILY + " REAL DEFAULT 0.0");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error upgrading from v1 to v2. Recreating DB.", e);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
                onCreate(db);
                return;
            }
        }
        if (oldVersion < 3) {
            String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                    + COL_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_NOTIF_MESSAGE + " TEXT,"
                    + COL_NOTIF_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + COL_NOTIF_IS_READ + " INTEGER DEFAULT 0" + ")";
            db.execSQL(CREATE_TABLE_NOTIFICATIONS);
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_NOTIFICATIONS + " ADD COLUMN " + COL_NOTIF_TYPE + " TEXT");
        }
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
            String CREATE_TABLE_NOTIFICATIONS_V5 = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                    + COL_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_NOTIF_MESSAGE + " TEXT,"
                    + COL_NOTIF_TIMESTAMP + " TEXT,"
                    + COL_NOTIF_IS_READ + " INTEGER DEFAULT 0,"
                    + COL_NOTIF_TYPE + " TEXT" + ")";
            db.execSQL(CREATE_TABLE_NOTIFICATIONS_V5);
        }
    }

    public boolean addExpense(String category, double amount, String note, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;
        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(COL_EXPENSE_CATEGORY, category);
            values.put(COL_EXPENSE_AMOUNT, amount);
            values.put(COL_EXPENSE_NOTE, note);
            values.put(COL_EXPENSE_DATE, date);
            result = db.insert(TABLE_EXPENSES, null, values);

            if (result != -1) {
                checkBudgetLimitsAndNotify(db);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding expense or checking limits", e);
        } finally {
            db.endTransaction();
            db.close();
        }
        return result != -1;
    }

    private void checkBudgetLimitsAndNotify(SQLiteDatabase db) {
        double monthlyLimit = getMonthlyBudget();
        double totalMonth = getTotalExpensesForCurrentMonth();
        if (monthlyLimit > 0 && totalMonth > monthlyLimit) {
            String period = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
            String type = "monthly_" + period;
            if (!hasNotificationForType(db, type)) {
                String message = String.format(Locale.getDefault(), "You have exceeded your monthly budget of ₱%,.2f!", monthlyLimit);
                addNotification(db, message, type);
                return;
            }
        }

        double weeklyLimit = getWeeklyLimit();
        double totalWeek = getTotalExpensesForCurrentWeek();
        if (weeklyLimit > 0 && totalWeek > weeklyLimit) {
            Calendar cal = Calendar.getInstance();
            String period = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.WEEK_OF_YEAR);
            String type = "weekly_" + period;
            if (!hasNotificationForType(db, type)) {
                String message = String.format(Locale.getDefault(), "You have exceeded your weekly budget of ₱%,.2f!", weeklyLimit);
                addNotification(db, message, type);
                return;
            }
        }

        double dailyLimit = getDailyLimit();
        double totalDay = getTotalExpensesForToday();
        if (dailyLimit > 0 && totalDay > dailyLimit) {
            String period = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String type = "daily_" + period;
            if (!hasNotificationForType(db, type)) {
                String message = String.format(Locale.getDefault(), "You have exceeded your daily budget of ₱%,.2f!", dailyLimit);
                addNotification(db, message, type);
            }
        }
    }

    private boolean hasNotificationForType(SQLiteDatabase db, String type) {
        try (Cursor cursor = db.query(TABLE_NOTIFICATIONS, new String[]{COL_NOTIF_ID}, COL_NOTIF_TYPE + " = ?", new String[]{type}, null, null, null, "1")) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    public void addNotification(String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            addNotification(db, message, type);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    private void addNotification(SQLiteDatabase db, String message, String type) {
        ContentValues values = new ContentValues();
        values.put(COL_NOTIF_MESSAGE, message);
        values.put(COL_NOTIF_TYPE, type);
        values.put(COL_NOTIF_TIMESTAMP, getUtcDateFormat().format(new Date()));
        db.insert(TABLE_NOTIFICATIONS, null, values);

        NotificationUtils.showBudgetWarningNotification(context, message);
    }

    public Cursor getAllExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " ORDER BY " + COL_EXPENSE_DATE + " DESC", null);
    }

    public Cursor getRecentExpenses(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EXPENSES, null, null, null, null, null, COL_EXPENSE_ID + " DESC", String.valueOf(limit));
    }

    public Cursor getAllExpensesForExport() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " ORDER BY " + COL_EXPENSE_DATE + " DESC", null);
    }

    public void resetAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.delete(TABLE_EXPENSES, null, null);
            db.delete(TABLE_NOTIFICATIONS, null, null);

            ContentValues values = new ContentValues();
            values.put(KEY_BUDGET_MONTHLY, 0.0);
            values.put(KEY_BUDGET_WEEKLY, 0.0);
            values.put(KEY_BUDGET_DAILY, 0.0);
            db.update(TABLE_BUDGET, values, KEY_BUDGET_ID + " = ?", new String[]{"1"});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error resetting all data", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void setBudget(double monthlyLimit, double weeklyLimit, double dailyLimit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BUDGET_MONTHLY, monthlyLimit);
        values.put(KEY_BUDGET_WEEKLY, weeklyLimit);
        values.put(KEY_BUDGET_DAILY, dailyLimit);
        db.update(TABLE_BUDGET, values, KEY_BUDGET_ID + " = ?", new String[]{"1"});
        db.close();
    }

    public double getMonthlyBudget() {
        return getBudgetColumnValue(KEY_BUDGET_MONTHLY);
    }

    public double getWeeklyLimit() {
        return getBudgetColumnValue(KEY_BUDGET_WEEKLY);
    }

    public double getDailyLimit() {
        return getBudgetColumnValue(KEY_BUDGET_DAILY);
    }

    private double getBudgetColumnValue(String columnName) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_BUDGET, new String[]{columnName}, KEY_BUDGET_ID + " = ?", new String[]{"1"}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
        }
        return 0.0;
    }

    public double getTotalExpensesForCurrentMonth() {
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String currentMonthString = dateFormat.format(new Date());
        String query = "SELECT SUM(" + COL_EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE strftime('%Y-%m', " + COL_EXPENSE_DATE + ") = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{currentMonthString})) {
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getDouble(0);
            }
        }
        return 0.0;
    }

    public double getTotalExpensesForCurrentWeek() {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        String weekStart = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        String weekEnd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        String query = "SELECT SUM(" + COL_EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + COL_EXPENSE_DATE + " BETWEEN ? AND ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{weekStart, weekEnd})) {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
        }
        return 0.0;
    }

    public double getTotalExpensesForToday() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String query = "SELECT SUM(" + COL_EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + COL_EXPENSE_DATE + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{today})) {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
        }
        return 0.0;
    }

    public Cursor getAllNotifications() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATIONS + " ORDER BY " + COL_NOTIF_TIMESTAMP + " DESC", null);
    }

    public int getUnreadNotificationCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS + " WHERE " + COL_NOTIF_IS_READ + " = 0", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        return count;
    }

    public void markAllNotificationsAsRead() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOTIF_IS_READ, 1);
        db.update(TABLE_NOTIFICATIONS, values, COL_NOTIF_IS_READ + " = ?", new String[]{"0"});
        db.close();
    }

    public void deleteAllNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTIFICATIONS, null, null);
        db.close();
    }
}
