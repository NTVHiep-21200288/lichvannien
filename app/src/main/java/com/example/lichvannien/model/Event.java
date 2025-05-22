package com.example.lichvannien.model;

import java.util.Calendar;

public class Event {
    private long id;
    private String title;
    private String eventType;
    private int year;
    private int month;
    private int day;
    private String startTime;
    private String endTime;
    private boolean isAllDay;
    private String reminder;
    private String note;

    public Event() {
    }

    public Event(long id, String title, String eventType, int year, int month, int day, 
                 String startTime, String endTime, boolean isAllDay, String reminder, String note) {
        this.id = id;
        this.title = title;
        this.eventType = eventType;
        this.year = year;
        this.month = month;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAllDay = isAllDay;
        this.reminder = reminder;
        this.note = note;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // Trả về Calendar từ ngày tháng trong sự kiện
    public Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // Calendar tháng bắt đầu từ 0
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar;
    }

    /**
     * Factory method to create a simple event with minimal details
     */
    public static Event createSimpleEvent(String title, int year, int month, int day, String eventType) {
        return new Event(
                0, // id will be assigned by database
                title,
                eventType,
                year,
                month,
                day,
                "", // no start time
                "", // no end time
                true, // all day event
                "", // no reminder
                "" // no note
        );
    }

    public String getDate() {
        // Returns date in yyyy-MM-dd format
        return String.format("%04d-%02d-%02d", year, month, day);
    }
}
