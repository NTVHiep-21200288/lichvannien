package com.example.lichvannien.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.lichvannien.model.CalendarDay;
import com.example.lichvannien.model.CalendarMonth;

public class CalendarViewModel extends ViewModel {

    private MutableLiveData<CalendarMonth> currentMonth = new MutableLiveData<>();
    private MutableLiveData<CalendarDay> selectedDay = new MutableLiveData<>();

    private int currentYear = 0;
    private int currentMonthNumber = 0;

    public LiveData<CalendarMonth> getCurrentMonth() {
        return currentMonth;
    }

    public LiveData<CalendarDay> getSelectedDay() {
        return selectedDay;
    }

    public void loadMonth(int year, int month) {
        currentYear = year;
        currentMonthNumber = month;

        CalendarMonth calendarMonth = CalendarMonth.create(year, month);
        currentMonth.setValue(calendarMonth);
    }

    public void navigateToPreviousMonth() {
        if (currentMonthNumber == 1) {
            loadMonth(currentYear - 1, 12);
        } else {
            loadMonth(currentYear, currentMonthNumber - 1);
        }
    }

    public void navigateToNextMonth() {
        if (currentMonthNumber == 12) {
            loadMonth(currentYear + 1, 1);
        } else {
            loadMonth(currentYear, currentMonthNumber + 1);
        }
    }

    public void selectDay(CalendarDay day) {
        selectedDay.setValue(day);
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public int getCurrentMonthNumber() {
        return currentMonthNumber;
    }
}