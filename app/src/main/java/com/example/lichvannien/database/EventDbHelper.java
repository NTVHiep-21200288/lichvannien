package com.example.lichvannien.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.lichvannien.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "events.db";
    private static final int DATABASE_VERSION = 1;

    // Tên bảng và các cột
    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_EVENT_TYPE = "event_type";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_ALL_DAY = "all_day";
    public static final String COLUMN_REMINDER = "reminder";
    public static final String COLUMN_NOTE = "note";

    // Câu lệnh tạo bảng
    private static final String SQL_CREATE_EVENTS_TABLE =
            "CREATE TABLE " + TABLE_EVENTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_EVENT_TYPE + " TEXT, " +
                    COLUMN_YEAR + " INTEGER, " +
                    COLUMN_MONTH + " INTEGER, " +
                    COLUMN_DAY + " INTEGER, " +
                    COLUMN_START_TIME + " TEXT, " +
                    COLUMN_END_TIME + " TEXT, " +
                    COLUMN_ALL_DAY + " INTEGER DEFAULT 0, " +
                    COLUMN_REMINDER + " TEXT, " +
                    COLUMN_NOTE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_EVENTS;

    public EventDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    // Thêm sự kiện mới
    public long addEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, event.getTitle());
        values.put(COLUMN_EVENT_TYPE, event.getEventType());
        values.put(COLUMN_YEAR, event.getYear());
        values.put(COLUMN_MONTH, event.getMonth());
        values.put(COLUMN_DAY, event.getDay());
        values.put(COLUMN_START_TIME, event.getStartTime());
        values.put(COLUMN_END_TIME, event.getEndTime());
        values.put(COLUMN_ALL_DAY, event.isAllDay() ? 1 : 0);
        values.put(COLUMN_REMINDER, event.getReminder());
        values.put(COLUMN_NOTE, event.getNote());

        long id = db.insert(TABLE_EVENTS, null, values);
        db.close();
        return id;
    }

    // Lấy tất cả sự kiện
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                event.setEventType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TYPE)));
                event.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)));
                event.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MONTH)));
                event.setDay(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAY)));
                event.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
                event.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
                event.setAllDay(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALL_DAY)) == 1);
                event.setReminder(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER)));
                event.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE)));

                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return events;
    }

    // Lấy sự kiện theo ngày
    public List<Event> getEventsByDate(int year, int month, int day) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_YEAR + " = ? AND " + COLUMN_MONTH + " = ? AND " + COLUMN_DAY + " = ?";
        String[] selectionArgs = {String.valueOf(year), String.valueOf(month), String.valueOf(day)};

        Cursor cursor = db.query(
                TABLE_EVENTS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                event.setEventType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TYPE)));
                event.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)));
                event.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MONTH)));
                event.setDay(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAY)));
                event.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
                event.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
                event.setAllDay(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALL_DAY)) == 1);
                event.setReminder(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER)));
                event.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE)));

                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return events;
    }

    // Lấy sự kiện theo tháng
    public List<Event> getEventsByMonth(int year, int month) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_YEAR + " = ? AND " + COLUMN_MONTH + " = ?";
        String[] selectionArgs = {String.valueOf(year), String.valueOf(month)};

        Cursor cursor = db.query(
                TABLE_EVENTS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                event.setEventType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TYPE)));
                event.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)));
                event.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MONTH)));
                event.setDay(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAY)));
                event.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
                event.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
                event.setAllDay(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALL_DAY)) == 1);
                event.setReminder(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER)));
                event.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE)));

                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return events;
    }

    // Cập nhật sự kiện
    public int updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, event.getTitle());
        values.put(COLUMN_EVENT_TYPE, event.getEventType());
        values.put(COLUMN_YEAR, event.getYear());
        values.put(COLUMN_MONTH, event.getMonth());
        values.put(COLUMN_DAY, event.getDay());
        values.put(COLUMN_START_TIME, event.getStartTime());
        values.put(COLUMN_END_TIME, event.getEndTime());
        values.put(COLUMN_ALL_DAY, event.isAllDay() ? 1 : 0);
        values.put(COLUMN_REMINDER, event.getReminder());
        values.put(COLUMN_NOTE, event.getNote());

        int result = db.update(TABLE_EVENTS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(event.getId())});
        db.close();
        return result;
    }

    // Xóa sự kiện
    public void deleteEvent(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
