package com.example.lichvannien.utils;

/**
 * Lớp cung cấp các phương thức tính toán lịch âm chuẩn xác
 * Thuật toán dựa trên Hồ Ngọc Đức's Lunar Calendar chuyển đổi từ Javascript
 */
public class LunarCalendarUtil {

    private static final String[] CAN = {"Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"};
    private static final String[] CHI = {"Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"};
    private static final String[] CHI_HOUR = {"Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"};

    private static final double PI = Math.PI;
    private static final int[] SUNLONG_MAJOR = new int[]{0, 3, 6, 9};

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
    
    /**
     * Chuyển đổi ngày dương lịch sang ngày âm lịch
     * @param solarYear năm dương lịch
     * @param solarMonth tháng dương lịch (1-12)
     * @param solarDay ngày dương lịch
     * @return đối tượng LunarDate chứa thông tin âm lịch
     */
    public static LunarDate getLunarDate(int solarYear, int solarMonth, int solarDay) {
        int jd = jdFromDate(solarDay, solarMonth, solarYear);
        return convertSolar2Lunar(solarDay, solarMonth, solarYear, 7.0);
    }

    /**
     * Chuyển đổi ngày dương lịch sang âm lịch
     */
    public static LunarDate convertSolar2Lunar(int dd, int mm, int yy, double timeZone) {
        int juliusDay = jdFromDate(dd, mm, yy);
        return convertJulius2Lunar(juliusDay, timeZone, yy);
    }

    /**
     * Chuyển đổi ngày Julius sang âm lịch
     */
    public static LunarDate convertJulius2Lunar(int juliusDay, double timeZone, int yy) {
        int k, nm, sunLong, a11, b11, lunarDay, lunarMonth, lunarYear, diff, leapMonthDiff;
        boolean isLeapMonth = false;

        k = (int)((juliusDay - 2415021.076998695) / 29.530588853);
        nm = getNewMoonDay(k + 1, timeZone);
        
        if (nm > juliusDay) {
            nm = getNewMoonDay(k, timeZone);
        }
        
        a11 = getLunarMonth11(yy, timeZone);
        b11 = a11;
        
        if (a11 >= nm) {
            lunarYear = yy;
            a11 = getLunarMonth11(yy - 1, timeZone);
        } else {
            lunarYear = yy + 1;
            b11 = getLunarMonth11(yy + 1, timeZone);
        }
        
        lunarDay = juliusDay - nm + 1;
        
        diff = (int)((nm - a11) / 29);
        lunarMonth = diff + 11;
        
        if (b11 - a11 > 365) {
            leapMonthDiff = getLeapMonthOffset(a11, timeZone);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff == leapMonthDiff) {
                    isLeapMonth = true;
                }
            }
        }
        
        if (lunarMonth > 12) {
            lunarMonth = lunarMonth - 12;
        }
        
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1;
        }
        
        return new LunarDate(lunarDay, lunarMonth, lunarYear, isLeapMonth, juliusDay);
    }

    /**
     * Chuyển đổi ngày, tháng, năm thành ngày Julius
     */
    public static int jdFromDate(int dd, int mm, int yy) {
        int a = (14 - mm) / 12;
        int y = yy + 4800 - a;
        int m = mm + 12 * a - 3;
        int jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
        
        if (jd < 2299161) {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083;
        }
        
        return jd;
    }

    /**
     * Tính toán ngày bắt đầu trăng mới từ k
     */
    public static int getNewMoonDay(int k, double timeZone) {
        double T = k / 1236.85;
        double T2 = T * T;
        double T3 = T2 * T;
        double dr = PI / 180;
        double Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3;
        Jd1 = Jd1 + 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr);
        double M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3;
        double Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3;
        double F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3;
        double C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr) + 0.0021 * Math.sin(2 * dr * M);
        C1 = C1 - 0.4068 * Math.sin(Mpr * dr) + 0.0161 * Math.sin(dr * 2 * Mpr);
        C1 = C1 - 0.0004 * Math.sin(dr * 3 * Mpr);
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
        double JdNew = Jd1 + C1 - deltat;
        return (int)(JdNew + 0.5 + timeZone / 24);
    }

    /**
     * Tính toán tháng 11 âm lịch cho năm yy
     */
    public static int getLunarMonth11(int yy, double timeZone) {
        double off = jdFromDate(31, 12, yy) - 2415021;
        int k = (int)(off / 29.530588853);
        int nm = getNewMoonDay(k, timeZone);
        int sunLong = getSunLongitude(nm, timeZone);
        
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1, timeZone);
        }
        
        return nm;
    }

    /**
     * Tính toán offset tháng nhuận
     */
    public static int getLeapMonthOffset(int a11, double timeZone) {
        int k = (int)((a11 - 2415021.076998695) / 29.530588853 + 0.5);
        int last = 0;
        int i = 1;
        int arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone);
        
        do {
            last = arc;
            i++;
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone);
        } while (arc != last && i < 14);
        
        return i - 1;
    }

    /**
     * Tính toán kinh độ mặt trời
     */
    public static int getSunLongitude(int jdn, double timeZone) {
        double T = (jdn - 2451545.5 - timeZone / 24) / 36525;
        double T2 = T * T;
        double dr = PI / 180;
        double M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
        double L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
        double DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M);
        DL = DL + (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M) + 0.000290 * Math.sin(dr * 3 * M);
        double L = L0 + DL;
        L = L * dr;
        L = L - PI * 2 * (Math.floor(L / (PI * 2)));
        return (int)(Math.floor(L / PI * 6));
    }

    /**
     * Tính toán can chi cho năm
     */
    public static String getCanChi(int year) {
        int can = (year - 4) % 10;
        if (can < 0) can += 10;
        int chi = (year - 4) % 12;
        if (chi < 0) chi += 12;
        return CAN[can] + " " + CHI[chi];
    }

    /**
     * Tính toán can chi cho ngày
     */
    public static String getCanChiDay(int jd) {
        int can = (jd + 9) % 10;
        int chi = (jd + 1) % 12;
        return CAN[can] + " " + CHI[chi];
    }

    /**
     * Tính toán can chi cho giờ
     */
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

    // Hàm test nhanh chuyển đổi ngày dương sang âm, in ra logcat
    public static void testLunarConversion() {
        int solarDay = 22;
        int solarMonth = 5;
        int solarYear = 2025;
        LunarDate lunar = getLunarDate(solarYear, solarMonth, solarDay);
        android.util.Log.d("LunarTest", "Dương lịch: " + solarDay + "/" + solarMonth + "/" + solarYear +
                " => Âm lịch: " + lunar.day + "/" + lunar.month + (lunar.isLeapMonth ? " (nhuận)" : "") + "/" + lunar.year);
    }
}