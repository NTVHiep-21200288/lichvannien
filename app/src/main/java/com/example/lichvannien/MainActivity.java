package com.example.lichvannien;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lichvannien.adapter.CalendarAdapter;
import com.example.lichvannien.model.CalendarDay;
import com.example.lichvannien.model.CalendarMonth;
import com.example.lichvannien.utils.LunarCalendarUtil;
import com.example.lichvannien.viewmodel.CalendarViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.OnDayClickListener {

    private CalendarViewModel viewModel;
    private CalendarAdapter calendarAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeUpdateRunnable;

    // Views
    private TextView tvCurrentDate;
    private TextView tvLunarDate;
    private TextView tvCanChi;
    private TextView tvTietKhi;
    private TextView tvMonthYear;
    private TextView tvSelectedDate;
    private TextView tvSelectedLunar;
    private TextView tvSelectedCanChi;
    private TextView tvSelectedHoliday;
    private RecyclerView rvCalendar;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private ImageButton btnToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupViewModel();
        setupCalendar();
        setupClickListeners();
        startRealtimeUpdate();

        // Load current month
        Calendar today = Calendar.getInstance();
        viewModel.loadMonth(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1);
    }

    private void initViews() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvLunarDate = findViewById(R.id.tvLunarDate);
        tvCanChi = findViewById(R.id.tvCanChi);
        tvTietKhi = findViewById(R.id.tvTietKhi);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedLunar = findViewById(R.id.tvSelectedLunar);
        tvSelectedCanChi = findViewById(R.id.tvSelectedCanChi);
        tvSelectedHoliday = findViewById(R.id.tvSelectedHoliday);
        rvCalendar = findViewById(R.id.rvCalendar);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnToday = findViewById(R.id.btnToday);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        viewModel.getCurrentMonth().observe(this, this::onMonthChanged);
        viewModel.getSelectedDay().observe(this, this::onDaySelected);
    }

    private void onMonthChanged(CalendarMonth calendarMonth) {
        if (calendarMonth != null) {
            calendarAdapter.updateData(calendarMonth.days);
            updateMonthYearDisplay(calendarMonth.year, calendarMonth.month);
        }
    }

    private void onDaySelected(CalendarDay day) {
        if (day != null) {
            calendarAdapter.setSelectedDay(day);
            updateSelectedDayInfo(day, null);
        }
    }

    private void setupCalendar() {
        calendarAdapter = new CalendarAdapter(this);
        CalendarAdapter.setupGridLayoutManager(rvCalendar);
        rvCalendar.setAdapter(calendarAdapter);
    }

    private void setupClickListeners() {
        btnPrevMonth.setOnClickListener(v -> viewModel.navigateToPreviousMonth());

        btnNextMonth.setOnClickListener(v -> viewModel.navigateToNextMonth());

        btnToday.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            viewModel.loadMonth(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1);
            CalendarDay todayCalendarDay = CalendarDay.fromDate(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH) + 1,
                    today.get(Calendar.DAY_OF_MONTH)
            );
            viewModel.selectDay(todayCalendarDay);
        });
    }

    @Override
    public void onDayClick(CalendarDay day) {
        viewModel.selectDay(day);
    }

    private void startRealtimeUpdate() {
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateCurrentDateDisplay();
                handler.postDelayed(this, 60000); // Update every minute
            }
        };
        handler.post(timeUpdateRunnable);
    }

    private void updateCurrentDateDisplay() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        // Format current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd 'tháng' MM, yyyy", new Locale("vi", "VN"));
        tvCurrentDate.setText(dateFormat.format(now.getTime()));

        // Get lunar date
        LunarCalendarUtil.LunarDate lunarDate = LunarCalendarUtil.getLunarDate(year, month, day);
        String lunarText = "Ngày " + lunarDate.day + " tháng " + lunarDate.month + " năm " + LunarCalendarUtil.getCanChi(lunarDate.year);
        tvLunarDate.setText(lunarText);

        // Get Can Chi for day
        String canChiDay = LunarCalendarUtil.getCanChiDay(lunarDate.jd);
        tvCanChi.setText("Ngày " + canChiDay);

        // Get Tiet Khi
        String tietKhi = LunarCalendarUtil.getTietKhi(month, day);
        if (tietKhi != null) {
            tvTietKhi.setText("Tiết khí: " + tietKhi);
            tvTietKhi.setVisibility(View.VISIBLE);
        } else {
            tvTietKhi.setVisibility(View.GONE);
        }

        // Get hour Can Chi
        int hour = now.get(Calendar.HOUR_OF_DAY);
        String hourCanChi = LunarCalendarUtil.getHourCanChi(hour, canChiDay);

        // If we're showing current time details, update them
        CalendarDay selectedDay = viewModel.getSelectedDay().getValue();
        if (selectedDay != null && selectedDay.isToday) {
            updateSelectedDayInfo(selectedDay, hourCanChi);
        }
    }

    private void updateMonthYearDisplay(int year, int month) {
        String[] monthNames = {
                "", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        };
        tvMonthYear.setText(monthNames[month] + ", " + year);
    }

    private void updateSelectedDayInfo(CalendarDay day, String hourCanChi) {
        // Main date info
        String dateText = "Thứ " + getDayOfWeek(day.solarYear, day.solarMonth, day.solarDay) + ", " +
                "ngày " + day.solarDay + " tháng " + day.solarMonth + " năm " + day.solarYear;
        tvSelectedDate.setText(dateText);

        // Lunar date info
        String lunarText = "Ngày " + day.lunarDay + " tháng " + day.lunarMonth + " năm " + LunarCalendarUtil.getCanChi(day.lunarYear);
        if (day.isLeapMonth) {
            lunarText += " (tháng nhuận)";
        }
        tvSelectedLunar.setText(lunarText);
        tvSelectedLunar.setVisibility(View.VISIBLE);

        // Can Chi info
        String canChiText = "Ngày " + day.canChi;
        if (day.isToday && hourCanChi != null) {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            String hourName = getHourName(hour);
            canChiText += " - Giờ " + hourCanChi + " (" + hourName + ")";
        }
        tvSelectedCanChi.setText(canChiText);
        tvSelectedCanChi.setVisibility(View.VISIBLE);

        // Holiday info
        if (day.isHoliday && day.holidayName != null) {
            tvSelectedHoliday.setText("🎉 " + day.holidayName);
            tvSelectedHoliday.setVisibility(View.VISIBLE);
        } else {
            tvSelectedHoliday.setVisibility(View.GONE);
        }

        // Tiet Khi info
        if (day.tietKhi != null) {
            if (tvSelectedHoliday.getVisibility() == View.VISIBLE) {
                tvSelectedHoliday.setText(tvSelectedHoliday.getText() + "\n🌸 Tiết khí: " + day.tietKhi);
            } else {
                tvSelectedHoliday.setText("🌸 Tiết khí: " + day.tietKhi);
                tvSelectedHoliday.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getDayOfWeek(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Chủ nhật";
            case Calendar.MONDAY: return "Hai";
            case Calendar.TUESDAY: return "Ba";
            case Calendar.WEDNESDAY: return "Tư";
            case Calendar.THURSDAY: return "Năm";
            case Calendar.FRIDAY: return "Sáu";
            case Calendar.SATURDAY: return "Bảy";
            default: return "";
        }
    }

    private String getHourName(int hour) {
        if (hour >= 23 || hour <= 0) return "Tý (23-01h)";
        else if (hour <= 2) return "Sửu (01-03h)";
        else if (hour <= 4) return "Dần (03-05h)";
        else if (hour <= 6) return "Mão (05-07h)";
        else if (hour <= 8) return "Thìn (07-09h)";
        else if (hour <= 10) return "Tỵ (09-11h)";
        else if (hour <= 12) return "Ngọ (11-13h)";
        else if (hour <= 14) return "Mùi (13-15h)";
        else if (hour <= 16) return "Thân (15-17h)";
        else if (hour <= 18) return "Dậu (17-19h)";
        else if (hour <= 20) return "Tuất (19-21h)";
        else return "Hợi (21-23h)";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeUpdateRunnable != null) {
            handler.removeCallbacks(timeUpdateRunnable);
        }
    }
}