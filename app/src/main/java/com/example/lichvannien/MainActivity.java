package com.example.lichvannien;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lichvannien.adapter.CalendarAdapter;
import com.example.lichvannien.adapter.EventAdapter;
import com.example.lichvannien.model.CalendarDay;
import com.example.lichvannien.model.CalendarMonth;
import com.example.lichvannien.utils.LunarCalendarUtil;
import com.example.lichvannien.viewmodel.CalendarViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.example.lichvannien.database.EventDbHelper;
import com.example.lichvannien.model.Event;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.OnDayClickListener {    private static final int ADD_EVENT_REQUEST_CODE = 1001;
    private static final int EDIT_EVENT_REQUEST_CODE = 1002;
    
    private CalendarViewModel viewModel;
    private CalendarAdapter calendarAdapter;
    private EventAdapter currentEventsAdapter;
    private EventAdapter selectedEventsAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeUpdateRunnable;

    // Views
    private TextView tvCurrentDate;
    private TextView tvLunarDate;
    private TextView tvCanChi;
    private TextView tvMonthYear;
    private TextView tvSelectedDate;
    private TextView tvSelectedLunar;
    private TextView tvSelectedCanChi;    
    private TextView tvSelectedHoliday;
    private RecyclerView rvCalendar;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private ImageButton btnToday;
    private FloatingActionButton fabAddEvent;
    
    // Event views
    private TextView tvCurrentEventsHeader;
    private RecyclerView rvCurrentEvents;
    private TextView tvNoCurrentEvents;
    private TextView tvSelectedEventsHeader;
    private RecyclerView rvSelectedEvents;
    private TextView tvNoSelectedEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupViewModel();
        setupCalendar();
        setupClickListeners();
        startRealtimeUpdate();

        // Thêm listener cho adapter sự kiện
        currentEventsAdapter.setOnEventClickListener((event) -> showEventOptionsDialog(event));
        selectedEventsAdapter.setOnEventClickListener((event) -> showEventOptionsDialog(event));

        // Load current month
        Calendar today = Calendar.getInstance();
        viewModel.loadMonth(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1);
    }    private void showEventOptionsDialog(Event event) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(event.getTitle())
            .setItems(new CharSequence[]{"Chỉnh sửa", "Xóa"}, (dialog, which) -> {
                if (which == 0) {
                    // Chỉnh sửa sự kiện
                    Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                    intent.putExtra("EDIT_EVENT_ID", event.getId());
                    // Bắt đầu activity để chỉnh sửa và chờ kết quả
                    startActivityForResult(intent, EDIT_EVENT_REQUEST_CODE);
                } else if (which == 1) {
                    // Xóa sự kiện
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa sự kiện này?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            EventDbHelper dbHelper = new EventDbHelper(this);
                            dbHelper.deleteEvent(event.getId());
                            // Refresh lại danh sách sự kiện
                            CalendarDay selectedDay = viewModel.getSelectedDay().getValue();
                            if (selectedDay != null) {
                                loadEventsForSelectedDay(selectedDay.solarYear, selectedDay.solarMonth, selectedDay.solarDay);
                                loadEventsForCurrentDay(selectedDay.solarYear, selectedDay.solarMonth, selectedDay.solarDay);
                                // Cập nhật lại view cho tháng hiện tại
                                CalendarMonth currentMonth = viewModel.getCurrentMonth().getValue();
                                if (currentMonth != null) {
                                    loadEventsForMonth(currentMonth.year, currentMonth.month);
                                }
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
                }
            })
            .setNegativeButton("Đóng", null)
            .show();
    }

    private void initViews() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvLunarDate = findViewById(R.id.tvLunarDate);
        tvCanChi = findViewById(R.id.tvCanChi);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedLunar = findViewById(R.id.tvSelectedLunar);
        tvSelectedCanChi = findViewById(R.id.tvSelectedCanChi);
        tvSelectedHoliday = findViewById(R.id.tvSelectedHoliday);
        rvCalendar = findViewById(R.id.rvCalendar);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnToday = findViewById(R.id.btnToday);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        
        // Event views
        tvCurrentEventsHeader = findViewById(R.id.tvCurrentEventsHeader);
        rvCurrentEvents = findViewById(R.id.rvCurrentEvents);
        tvNoCurrentEvents = findViewById(R.id.tvNoCurrentEvents);
        tvSelectedEventsHeader = findViewById(R.id.tvSelectedEventsHeader);
        rvSelectedEvents = findViewById(R.id.rvSelectedEvents);
        tvNoSelectedEvents = findViewById(R.id.tvNoSelectedEvents);
        
        // Setup event adapters
        currentEventsAdapter = new EventAdapter(this);
        selectedEventsAdapter = new EventAdapter(this);
        
        // Setup event RecyclerViews
        EventAdapter.setupLinearLayoutManager(rvCurrentEvents);
        EventAdapter.setupLinearLayoutManager(rvSelectedEvents);
        rvCurrentEvents.setAdapter(currentEventsAdapter);
        rvSelectedEvents.setAdapter(selectedEventsAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        viewModel.getCurrentMonth().observe(this, this::onMonthChanged);
        viewModel.getSelectedDay().observe(this, this::onDaySelected);
    }    private void onMonthChanged(CalendarMonth calendarMonth) {
        if (calendarMonth != null) {
            android.util.Log.d("MainActivity", "Month changed: " + calendarMonth.month + "/" + calendarMonth.year + " with " + calendarMonth.days.size() + " days");
            calendarAdapter.updateData(calendarMonth.days);
            updateMonthYearDisplay(calendarMonth.year, calendarMonth.month);
            
            // Load events for this month
            loadEventsForMonth(calendarMonth.year, calendarMonth.month);
        } else {
            android.util.Log.e("MainActivity", "CalendarMonth is null");
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
    }    private void setupClickListeners() {
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
          fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
            
            // Truyền ngày đã chọn hiện tại (nếu có)
            CalendarDay selectedDay = viewModel.getSelectedDay().getValue();
            if (selectedDay != null) {
                intent.putExtra("YEAR", selectedDay.solarYear);
                intent.putExtra("MONTH", selectedDay.solarMonth);
                intent.putExtra("DAY", selectedDay.solarDay);
            }
            
            startActivityForResult(intent, ADD_EVENT_REQUEST_CODE);
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
    }    private void updateCurrentDateDisplay() {
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

        // Get hour Can Chi
        int hour = now.get(Calendar.HOUR_OF_DAY);
        String hourCanChi = LunarCalendarUtil.getHourCanChi(hour, canChiDay);

        // If we're showing current time details, update them
        CalendarDay selectedDay = viewModel.getSelectedDay().getValue();
        if (selectedDay != null && selectedDay.isToday) {
            updateSelectedDayInfo(selectedDay, hourCanChi);
        }
        
        // Load events for current day
        loadEventsForCurrentDay(year, month, day);
    }

    private void updateMonthYearDisplay(int year, int month) {
        String[] monthNames = {
                "", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        };
        tvMonthYear.setText(monthNames[month] + ", " + year);
    }    private void updateSelectedDayInfo(CalendarDay day, String hourCanChi) {
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
        
        // Load events for selected day
        loadEventsForSelectedDay(day.solarYear, day.solarMonth, day.solarDay);
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
    }    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null &&
            (data.getBooleanExtra("EVENT_ADDED", false) || data.getBooleanExtra("EVENT_UPDATED", false))) {

            int year = data.getIntExtra("YEAR", -1);
            int month = data.getIntExtra("MONTH", -1);
            int day = data.getIntExtra("DAY", -1);

            if (year > 0 && month > 0 && day > 0) {
                // Làm mới tháng
                CalendarMonth currentMonth = viewModel.getCurrentMonth().getValue();
                if (currentMonth != null) {
                    loadEventsForMonth(currentMonth.year, currentMonth.month);
                }
                // Làm mới ngày được chọn
                CalendarDay eventDay = CalendarDay.fromDate(year, month, day);
                viewModel.selectDay(eventDay);
                loadEventsForSelectedDay(year, month, day);

                // Nếu là hôm nay thì làm mới luôn danh sách hôm nay
                Calendar now = Calendar.getInstance();
                int todayYear = now.get(Calendar.YEAR);
                int todayMonth = now.get(Calendar.MONTH) + 1;
                int todayDay = now.get(Calendar.DAY_OF_MONTH);
                if (year == todayYear && month == todayMonth && day == todayDay) {
                    loadEventsForCurrentDay(todayYear, todayMonth, todayDay);
                }
            }
        }
    }
    
    private void loadEventsForMonth(int year, int month) {
        // Create EventDbHelper
        EventDbHelper dbHelper = new EventDbHelper(this);
        
        // Create a map to store events by day
        List<CalendarDay> days = calendarAdapter.getDays();
        if (days == null || days.isEmpty()) {
            return;
        }
        
        // Get current calendar month
        CalendarMonth currentMonth = viewModel.getCurrentMonth().getValue();
        if (currentMonth == null) {
            return;
        }
        
        // Get all events for this month
        List<Event> monthEvents = dbHelper.getEventsByMonth(year, month);
        
        // Mark days with events
        boolean hasUpdates = false;
        for (CalendarDay day : days) {
            boolean hadEvents = day.hasEvents;
            day.hasEvents = false;
            
            // Check if this day has events
            for (Event event : monthEvents) {
                if (event.getYear() == day.solarYear && 
                    event.getMonth() == day.solarMonth && 
                    event.getDay() == day.solarDay) {
                    day.hasEvents = true;
                    break;
                }
            }
            
            // Track if anything changed
            if (hadEvents != day.hasEvents) {
                hasUpdates = true;
            }
        }
        
        // Update UI if needed
        if (hasUpdates) {
            calendarAdapter.notifyDataSetChanged();
        }
    }
    
    /**
     * Loads and displays events for the current day
     */
    private void loadEventsForCurrentDay(int year, int month, int day) {
        // Create EventDbHelper
        EventDbHelper dbHelper = new EventDbHelper(this);
        
        // Get events for the current day
        List<Event> todayEvents = dbHelper.getEventsByDate(year, month, day);
        
        // Update UI based on whether there are events
        if (todayEvents != null && !todayEvents.isEmpty()) {
            // Show header and RecyclerView
            tvCurrentEventsHeader.setVisibility(View.VISIBLE);
            rvCurrentEvents.setVisibility(View.VISIBLE);
            tvNoCurrentEvents.setVisibility(View.GONE);
            
            // Update adapter with events
            currentEventsAdapter.updateData(todayEvents);
        } else {
            // Show "no events" message
            tvCurrentEventsHeader.setVisibility(View.VISIBLE);
            rvCurrentEvents.setVisibility(View.GONE);
            tvNoCurrentEvents.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Loads and displays events for the selected day
     */
    private void loadEventsForSelectedDay(int year, int month, int day) {
        // Create EventDbHelper
        EventDbHelper dbHelper = new EventDbHelper(this);
        
        // Get events for the selected day
        List<Event> selectedDayEvents = dbHelper.getEventsByDate(year, month, day);
        
        // Update UI based on whether there are events
        if (selectedDayEvents != null && !selectedDayEvents.isEmpty()) {
            // Show header and RecyclerView
            tvSelectedEventsHeader.setVisibility(View.VISIBLE);
            rvSelectedEvents.setVisibility(View.VISIBLE);
            tvNoSelectedEvents.setVisibility(View.GONE);
            
            // Update adapter with events
            selectedEventsAdapter.updateData(selectedDayEvents);
        } else {
            // Show "no events" message
            tvSelectedEventsHeader.setVisibility(View.VISIBLE);
            rvSelectedEvents.setVisibility(View.GONE);
            tvNoSelectedEvents.setVisibility(View.VISIBLE);
        }
    }
}