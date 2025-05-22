package com.example.lichvannien.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarMonth {
    public int year;
    public int month;
    public List<CalendarDay> days;

    public CalendarMonth(int year, int month, List<CalendarDay> days) {
        this.year = year;
        this.month = month;
        this.days = days;
    }

    public static CalendarMonth create(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday

        List<CalendarDay> days = new ArrayList<>();

        // Add days from previous month
        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear = month == 1 ? year - 1 : year;
        calendar.set(prevYear, prevMonth - 1, 1);
        int daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = firstDayOfWeek - 1; i >= 0; i--) {
            int day = daysInPrevMonth - i;
            CalendarDay calDay = CalendarDay.fromDate(prevYear, prevMonth, day);
            days.add(calDay.copyWithCurrentMonth(false));
        }

        // Add days of current month
        for (int day = 1; day <= daysInMonth; day++) {
            days.add(CalendarDay.fromDate(year, month, day));
        }

        // Add days from next month to complete the grid
        int nextMonth = month == 12 ? 1 : month + 1;
        int nextYear = month == 12 ? year + 1 : year;
        int remainingDays = 42 - days.size(); // 6 weeks * 7 days

        for (int day = 1; day <= remainingDays; day++) {
            CalendarDay calDay = CalendarDay.fromDate(nextYear, nextMonth, day);
            days.add(calDay.copyWithCurrentMonth(false));
        }

        return new CalendarMonth(year, month, days);
    }
}