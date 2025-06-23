package com.example.petsit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "booking.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CAR_CATEGORY = "car_category";
    public static final String COLUMN_PAYMENT_MODE = "payment_mode";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TOTAL_AMOUNT = "total_amount";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BOOKINGS_TABLE = "CREATE TABLE " + TABLE_BOOKINGS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CAR_CATEGORY + " TEXT,"
                + COLUMN_PAYMENT_MODE + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_TOTAL_AMOUNT + " REAL"
                + ")";
        db.execSQL(CREATE_BOOKINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        // Create tables again
        onCreate(db);
    }

    // Insert a new booking
    public void insertBooking(String carCategory, String paymentMode, String date, float totalAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CAR_CATEGORY, carCategory);
        values.put(COLUMN_PAYMENT_MODE, paymentMode);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TOTAL_AMOUNT, totalAmount);

        db.insert(TABLE_BOOKINGS, null, values);
        db.close();
    }

    // Get booking count grouped by car category
    public Cursor getBookingCountByCarCategory() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COLUMN_CAR_CATEGORY + ", COUNT(*) as count " +
                        "FROM " + TABLE_BOOKINGS + " GROUP BY " + COLUMN_CAR_CATEGORY, null);
    }

    // Get booking count grouped by payment mode
    public Cursor getBookingCountByPaymentMode() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COLUMN_PAYMENT_MODE + ", COUNT(*) as count " +
                        "FROM " + TABLE_BOOKINGS + " GROUP BY " + COLUMN_PAYMENT_MODE, null);
    }

    // Get booking count filtered by both car category and payment mode
    public Cursor getFilteredBookingCount(String selectedCategory, String selectedPayment) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_CAR_CATEGORY + "=? AND " + COLUMN_PAYMENT_MODE + "=?";
        String[] selectionArgs = { selectedCategory, selectedPayment };
        return db.rawQuery(
                "SELECT " + COLUMN_CAR_CATEGORY + ", COUNT(*) as count " +
                        "FROM " + TABLE_BOOKINGS +
                        " WHERE " + selection + " GROUP BY " + COLUMN_CAR_CATEGORY, selectionArgs);
    }
}
