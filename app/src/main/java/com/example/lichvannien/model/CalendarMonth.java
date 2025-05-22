package com.example.lichvannien.model;

import android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarMonth {
    private static final String TAG = "CalendarMonth";

    public int year;
    public int month;
    public List<CalendarDay> days;

    public CalendarMonth(int year, int month, List<CalendarDay> days) {
        this.year = year;
        this.month = month;
        this.days = days != null ? days : new ArrayList<>();
    }

    public static CalendarMonth create(int year, int month) {
        Log.d(TAG, "Creating calendar for " + month + "/" + year);

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday

            List<CalendarDay> days = new ArrayList<>();

            // Thêm các ngày từ tháng trước
            if (firstDayOfWeek > 0) {
                int prevMonth = month == 1 ? 12 : month - 1;
                int prevYear = month == 1 ? year - 1 : year;

                Calendar prevCalendar = Calendar.getInstance();
                prevCalendar.set(prevYear, prevMonth - 1, 1);
                int daysInPrevMonth = prevCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                for (int i = firstDayOfWeek - 1; i >= 0; i--) {
                    int day = daysInPrevMonth - i;
                    try {
                        CalendarDay calDay = CalendarDay.fromDate(prevYear, prevMonth, day);
                        days.add(calDay.copyWithCurrentMonth(false));
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating prev month day: " + e.getMessage());
                    }
                }
            }

            // Thêm các ngày của tháng hiện tại
            for (int day = 1; day <= daysInMonth; day++) {
                try {
                    CalendarDay calDay = CalendarDay.fromDate(year, month, day);
                    days.add(calDay);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating current month day " + day + ": " + e.getMessage());
                }
            }

            // Thêm các ngày từ tháng sau để hoàn thành lưới 6x7 = 42 ô
            int nextMonth = month == 12 ? 1 : month + 1;
            int nextYear = month == 12 ? year + 1 : year;
            int remainingDays = 42 - days.size();

            for (int day = 1; day <= remainingDays && day <= 31; day++) {
                try {
                    CalendarDay calDay = CalendarDay.fromDate(nextYear, nextMonth, day);
                    days.add(calDay.copyWithCurrentMonth(false));
                } catch (Exception e) {
                    Log.e(TAG, "Error creating next month day: " + e.getMessage());
                }
            }

            Log.d(TAG, "Created " + days.size() + " days for calendar");
            return new CalendarMonth(year, month, days);

        } catch (Exception e) {
            Log.e(TAG, "Error creating calendar month: " + e.getMessage());
            return new CalendarMonth(year, month, new ArrayList<>());
        }
    }
}