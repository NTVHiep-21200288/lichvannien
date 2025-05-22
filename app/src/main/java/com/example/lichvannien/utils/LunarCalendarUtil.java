package com.example.lichvannien.utils;

public class LunarCalendarUtil {

    private static final String[] CAN = {"Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"};
    private static final String[] CHI = {"Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"};
    private static final String[] CHI_HOUR = {"Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"};

    public static class LunarDate {
        public int day;
        public int month;
        public int year;
        public boolean isLeapMonth;
        public int jd;

        public LunarDate(int day, int month, int year, boolean isLeapMonth, int jd) {
            this.day = day;
            this.month = month;
            this.year = year;
            this.isLeapMonth = isLeapMonth;
            this.jd = jd;
        }
    }

    public static LunarDate getLunarDate(int year, int month, int day) {
        double jd = getJulia(day, month, year);
        int k = (int) Math.floor((jd - 2415021.076998695) / 29.530588853);
        double monthStart = getNewMoonDay(k + 1);

        if (monthStart > jd) {
            monthStart = getNewMoonDay(k);
        }

        double a11 = getLunarMonth11(year);
        double b11 = a11;
        int lunarYear = year;

        if (a11 >= monthStart) {
            lunarYear = year - 1;
            a11 = getLunarMonth11(lunarYear);
        }

        int diff = (int) Math.floor((monthStart - a11) / 29);
        int lunarMonth = diff + 11;

        if (b11 - a11 > 365) {
            int leapMonthDiff = getLeapMonthOffset(a11);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff == leapMonthDiff) {
                    lunarMonth = -lunarMonth;
                }
            }
        }

        if (lunarMonth > 12) {
            lunarMonth -= 12;
        }
        if (lunarMonth <= 0) {
            lunarMonth += 12;
        }

        int lunarDay = (int) (jd - monthStart + 1);

        return new LunarDate(
                lunarDay,
                Math.abs(lunarMonth),
                lunarYear,
                lunarMonth < 0,
                (int) jd
        );
    }

    private static double getJulia(int dd, int mm, int yy) {
        int a = (14 - mm) / 12;
        int y = yy - a;
        int m = mm + 12 * a - 3;
        int jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
        if (jd < 2299161) {
            return dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083.0;
        } else {
            return (double) jd;
        }
    }

    private static double getNewMoonDay(int k) {
        double T = k / 1236.85;
        double T2 = T * T;
        double T3 = T2 * T;
        double dr = Math.PI / 180;
        double Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3;
        Jd1 += 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr);
        double M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3;
        double Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3;
        double F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3;
        double C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr) + 0.0021 * Math.sin(2 * dr * M);
        C1 = C1 - 0.4068 * Math.sin(Mpr * dr) + 0.0161 * Math.sin(dr * 2 * Mpr);
        C1 -= 0.0004 * Math.sin(dr * 3 * Mpr);
        C1 = C1 + 0.0104 * Math.sin(dr * 2 * F) - 0.0051 * Math.sin(dr * (M + Mpr));
        C1 = C1 - 0.0074 * Math.sin(dr * (M - Mpr)) + 0.0004 * Math.sin(dr * (2 * F + M));
        C1 = C1 - 0.0004 * Math.sin(dr * (2 * F - M)) - 0.0006 * Math.sin(dr * (2 * F + Mpr));
        C1 = C1 + 0.0010 * Math.sin(dr * (2 * F - Mpr)) + 0.0005 * Math.sin(dr * (2 * Mpr + M));
        double deltat;
        if (T < -11) {
            deltat = 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3;
        } else {
            deltat = -0.000278 + 0.000265 * T + 0.000262 * T2;
        }
        return Jd1 + C1 - deltat;
    }

    private static double getLunarMonth11(int yy) {
        double off = getJulia(31, 12, yy) - 2415021.076998695;
        int k = (int) Math.floor(off / 29.530588853);
        double nm = getNewMoonDay(k);
        int sunLong = getSunLongitude(nm);
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1);
        }
        return nm;
    }

    private static int getLeapMonthOffset(double a11) {
        int k = (int) Math.floor(0.5 + (a11 - 2415021.076998695) / 29.530588853);
        int last = 0;
        int i = 1;
        int arc = getSunLongitude(getNewMoonDay(k + i));
        do {
            last = arc;
            i++;
            arc = getSunLongitude(getNewMoonDay(k + i));
        } while (arc != last && i < 14);
        return i - 1;
    }

    private static int getSunLongitude(double jdn) {
        double T = (jdn - 2451545.0) / 36525;
        double T2 = T * T;
        double dr = Math.PI / 180;
        double M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
        double L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
        double DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M);
        DL += (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M) + 0.000290 * Math.sin(dr * 3 * M);
        double L = L0 + DL;
        L *= dr;
        L -= Math.PI * 2 * Math.floor(L / (Math.PI * 2));
        return (int) Math.floor(L / Math.PI * 6);
    }

    public static String getCanChi(int year) {
        int can = (year + 6) % 10;
        int chi = (year + 8) % 12;
        return CAN[can] + " " + CHI[chi];
    }

    public static String getCanChiDay(int jd) {
        int can = (jd + 9) % 10;
        int chi = (jd + 1) % 12;
        return CAN[can] + " " + CHI[chi];
    }

    public static String getHourCanChi(int hour, String dayCanChi) {
        String[] parts = dayCanChi.split(" ");
        int dayCanIndex = 0;
        for (int i = 0; i < CAN.length; i++) {
            if (CAN[i].equals(parts[0])) {
                dayCanIndex = i;
                break;
            }
        }

        int hourChiIndex;
        if (hour >= 23 || hour <= 0) hourChiIndex = 0;  // Tý
        else if (hour <= 2) hourChiIndex = 1;   // Sửu
        else if (hour <= 4) hourChiIndex = 2;   // Dần
        else if (hour <= 6) hourChiIndex = 3;   // Mão
        else if (hour <= 8) hourChiIndex = 4;   // Thìn
        else if (hour <= 10) hourChiIndex = 5;  // Tỵ
        else if (hour <= 12) hourChiIndex = 6;  // Ngọ
        else if (hour <= 14) hourChiIndex = 7;  // Mùi
        else if (hour <= 16) hourChiIndex = 8;  // Thân
        else if (hour <= 18) hourChiIndex = 9;  // Dậu
        else if (hour <= 20) hourChiIndex = 10; // Tuất
        else hourChiIndex = 11; // Hợi

        int hourCanIndex = (dayCanIndex * 2 + hourChiIndex) % 10;
        return CAN[hourCanIndex] + " " + CHI_HOUR[hourChiIndex];
    }
}