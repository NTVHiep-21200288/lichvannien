package com.example.lichvannien.model;

import com.example.lichvannien.utils.LunarCalendarUtil;

import java.util.Calendar;

public class CalendarDay {
    public int solarDay;
    public int solarMonth;
    public int solarYear;
    public int lunarDay;
    public int lunarMonth;
    public int lunarYear;
    public boolean isLeapMonth;
    public String canChi;
    public String hourCanChi;
    public String tietKhi;
    public boolean isToday;
    public boolean isCurrentMonth;
    public boolean isHoliday;
    public String holidayName;

    public CalendarDay(int solarDay, int solarMonth, int solarYear,
                       int lunarDay, int lunarMonth, int lunarYear,
                       boolean isLeapMonth, String canChi, String hourCanChi,
                       String tietKhi, boolean isToday, boolean isCurrentMonth,
                       boolean isHoliday, String holidayName) {
        this.solarDay = solarDay;
        this.solarMonth = solarMonth;
        this.solarYear = solarYear;
        this.lunarDay = lunarDay;
        this.lunarMonth = lunarMonth;
        this.lunarYear = lunarYear;
        this.isLeapMonth = isLeapMonth;
        this.canChi = canChi;
        this.hourCanChi = hourCanChi != null ? hourCanChi : "";
        this.tietKhi = tietKhi;
        this.isToday = isToday;
        this.isCurrentMonth = isCurrentMonth;
        this.isHoliday = isHoliday;
        this.holidayName = holidayName;
    }

    public static CalendarDay fromDate(int year, int month, int day) {
        LunarCalendarUtil.LunarDate lunarDate = LunarCalendarUtil.getLunarDate(year, month, day);
        String canChi = LunarCalendarUtil.getCanChiDay(lunarDate.jd);
        String tietKhi = LunarCalendarUtil.getTietKhi(month, day);

        Calendar today = Calendar.getInstance();
        boolean isToday = year == today.get(Calendar.YEAR) &&
                month == today.get(Calendar.MONTH) + 1 &&
                day == today.get(Calendar.DAY_OF_MONTH);

        String holiday = getHoliday(day, month, lunarDate.day, lunarDate.month);

        return new CalendarDay(
                day, month, year,
                lunarDate.day, lunarDate.month, lunarDate.year,
                lunarDate.isLeapMonth, canChi, "",
                tietKhi, isToday, true,
                holiday != null, holiday
        );
    }

    public CalendarDay copyWithCurrentMonth(boolean isCurrentMonth) {
        return new CalendarDay(
                this.solarDay, this.solarMonth, this.solarYear,
                this.lunarDay, this.lunarMonth, this.lunarYear,
                this.isLeapMonth, this.canChi, this.hourCanChi,
                this.tietKhi, this.isToday, isCurrentMonth,
                this.isHoliday, this.holidayName
        );
    }

    private static String getHoliday(int solarDay, int solarMonth, int lunarDay, int lunarMonth) {
        // Lễ dương lịch
        switch (solarMonth) {
            case 1:
                if (solarDay == 1) return "Tết Dương lịch";
                break;
            case 2:
                if (solarDay == 14) return "Lễ tình nhân";
                break;
            case 3:
                if (solarDay == 8) return "Quốc tế Phụ nữ";
                break;
            case 4:
                if (solarDay == 30) return "Giải phóng miền Nam";
                break;
            case 5:
                if (solarDay == 1) return "Quốc tế Lao động";
                break;
            case 6:
                if (solarDay == 1) return "Quốc tế Thiếu nhi";
                break;
            case 9:
                if (solarDay == 2) return "Quốc khánh";
                break;
            case 10:
                if (solarDay == 20) return "Ngày Phụ nữ Việt Nam";
                break;
            case 11:
                if (solarDay == 20) return "Ngày Nhà giáo Việt Nam";
                break;
            case 12:
                if (solarDay == 25) return "Lễ Giáng sinh";
                break;
        }

        // Lễ âm lịch
        switch (lunarMonth) {
            case 1:
                if (lunarDay == 1) return "Tết Nguyên đán";
                else if (lunarDay == 2 || lunarDay == 3) return "Tết Nguyên đán";
                else if (lunarDay == 15) return "Rằm tháng Giêng";
                break;
            case 3:
                if (lunarDay == 3) return "Tết Hàn thực";
                break;
            case 4:
                if (lunarDay == 15) return "Phật đản";
                break;
            case 5:
                if (lunarDay == 5) return "Tết Đoan ngọ";
                break;
            case 7:
                if (lunarDay == 15) return "Lễ Vu lan";
                break;
            case 8:
                if (lunarDay == 15) return "Tết Trung thu";
                break;
            case 10:
                if (lunarDay == 10) return "Tết Thất tịch";
                break;
            case 12:
                if (lunarDay == 23) return "Ông Táo chầu trời";
                break;
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CalendarDay that = (CalendarDay) obj;
        return solarDay == that.solarDay &&
                solarMonth == that.solarMonth &&
                solarYear == that.solarYear;
    }
}
