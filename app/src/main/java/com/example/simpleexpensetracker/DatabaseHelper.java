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

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseTracker.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_BUDGET = "budget";

    public static final String COL_EXPENSE_ID = "id";
    public static final String COL_EXPENSE_CATEGORY = "category";
    public static final String COL_EXPENSE_AMOUNT = "amount";
    public static final String COL_EXPENSE_NOTE = "note";
    public static final String COL_EXPENSE_DATE = "date";

    private static final String KEY_BUDGET_ID = "id";
    private static final String KEY_BUDGET_MONTHLY = "monthly_limit";
    private static final String KEY_BUDGET_WEEKLY = "weekly_limit";
    private static final String KEY_BUDGET_DAILY = "daily_limit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

        db.execSQL(CREATE_EXPENSES_TABLE);
        db.execSQL(CREATE_BUDGET_TABLE);

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
                Log.e("DatabaseHelper", "Error upgrading. Recreating DB.", e);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
                onCreate(db);
            }
        }
    }

    public boolean addExpense(String category, double amount, String note, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EXPENSE_CATEGORY, category);
        values.put(COL_EXPENSE_AMOUNT, amount);
        values.put(COL_EXPENSE_NOTE, note);
        values.put(COL_EXPENSE_DATE, date);
        long result = db.insert(TABLE_EXPENSES, null, values);
        return result != -1;
    }

    public Cursor getAllExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " ORDER BY " + COL_EXPENSE_DATE + " DESC", null);
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
        }
    }

    public void setBudget(double monthlyLimit, double weeklyLimit, double dailyLimit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BUDGET_MONTHLY, monthlyLimit);
        values.put(KEY_BUDGET_WEEKLY, weeklyLimit);
        values.put(KEY_BUDGET_DAILY, dailyLimit);
        db.update(TABLE_BUDGET, values, KEY_BUDGET_ID + " = ?", new String[]{"1"});
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
}
