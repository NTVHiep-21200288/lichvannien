package com.example.lichvannien;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lichvannien.database.EventDbHelper;
import com.example.lichvannien.model.Event;
import com.example.lichvannien.utils.NotificationHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private TextView tvEventDate;
    private TextInputEditText edtEventTitle, edtStartTime, edtEndTime, edtEventNote;
    private AutoCompleteTextView actvEventType, actvReminder;
    private SwitchMaterial switchAllDay;
    private Button btnChangeDate, btnCancel, btnSaveEvent;
    private EventDbHelper dbHelper;
    
    private Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private NotificationHelper notificationHelper;    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_event);
        
        // Khởi tạo database helper
        dbHelper = new EventDbHelper(this);
        
        // Kiểm tra và yêu cầu quyền thông báo
        checkNotificationPermission();
        
        initViews();
        
        // Nhận dữ liệu ngày từ Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("YEAR")) {
            int year = intent.getIntExtra("YEAR", -1);
            int month = intent.getIntExtra("MONTH", -1);
            int day = intent.getIntExtra("DAY", -1);
            
            if (year > 0 && month > 0 && day > 0) {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month - 1); // Calendar tháng bắt đầu từ 0
                selectedDate.set(Calendar.DAY_OF_MONTH, day);
            }
        }
        
        // Khởi tạo NotificationHelper
        notificationHelper = new NotificationHelper(this);
        
        setupInitialValues();
        setupListeners();
        setupDropdowns();
    }
    
    private void initViews() {
        tvEventDate = findViewById(R.id.tvEventDate);
        edtEventTitle = findViewById(R.id.edtEventTitle);
        edtStartTime = findViewById(R.id.edtStartTime);
        edtEndTime = findViewById(R.id.edtEndTime);
        edtEventNote = findViewById(R.id.edtEventNote);
        actvEventType = findViewById(R.id.actvEventType);
        actvReminder = findViewById(R.id.actvReminder);
        switchAllDay = findViewById(R.id.switchAllDay);
        btnChangeDate = findViewById(R.id.btnChangeDate);
        btnCancel = findViewById(R.id.btnCancel);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
    }
    
    private void setupInitialValues() {
        // Hiển thị ngày hiện tại
        updateDateDisplay();
        
        // Giờ mặc định
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, 1);
        now.set(Calendar.MINUTE, 0);
        edtStartTime.setText(timeFormat.format(now.getTime()));
        
        now.add(Calendar.HOUR_OF_DAY, 1);
        edtEndTime.setText(timeFormat.format(now.getTime()));
    }
    
    private void setupListeners() {
        // Chọn ngày
        btnChangeDate.setOnClickListener(v -> showDatePicker());
        
        // Chọn giờ bắt đầu và kết thúc
        edtStartTime.setOnClickListener(v -> showTimePicker(true));
        edtEndTime.setOnClickListener(v -> showTimePicker(false));
        
        // Switch Cả ngày
        switchAllDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            edtStartTime.setEnabled(!isChecked);
            edtEndTime.setEnabled(!isChecked);
        });
        
        // Nút Hủy
        btnCancel.setOnClickListener(v -> finish());
        
        // Nút Lưu
        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }
    
    private void setupDropdowns() {
        // Thiết lập loại sự kiện
        String[] eventTypes = {"Cá nhân", "Công việc", "Gia đình", "Kỷ niệm", "Lễ hội", "Khác"};
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, eventTypes);
        actvEventType.setAdapter(eventTypeAdapter);
        actvEventType.setText(eventTypes[0], false);
        
        // Thiết lập nhắc nhở
        String[] reminderOptions = {"Không nhắc", "Khi sự kiện diễn ra", "15 phút trước", "30 phút trước", 
                "1 giờ trước", "1 ngày trước"};
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, reminderOptions);
        actvReminder.setAdapter(reminderAdapter);
        actvReminder.setText(reminderOptions[0], false);
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void showTimePicker(boolean isStartTime) {
        Calendar timeToShow = Calendar.getInstance();
        String currentTime = isStartTime ? edtStartTime.getText().toString() : edtEndTime.getText().toString();
        
        try {
            timeToShow.setTime(timeFormat.parse(currentTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    Calendar newTime = Calendar.getInstance();
                    newTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    newTime.set(Calendar.MINUTE, minute);
                    
                    if (isStartTime) {
                        edtStartTime.setText(timeFormat.format(newTime.getTime()));
                    } else {
                        edtEndTime.setText(timeFormat.format(newTime.getTime()));
                    }
                },
                timeToShow.get(Calendar.HOUR_OF_DAY),
                timeToShow.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }
    
    private void updateDateDisplay() {
        // Tạo tên của ngày trong tuần (Thứ Hai, Thứ Ba,...)
        String dayOfWeek;
        switch (selectedDate.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                dayOfWeek = "Thứ Hai";
                break;
            case Calendar.TUESDAY:
                dayOfWeek = "Thứ Ba";
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = "Thứ Tư";
                break;
            case Calendar.THURSDAY:
                dayOfWeek = "Thứ Năm";
                break;
            case Calendar.FRIDAY:
                dayOfWeek = "Thứ Sáu";
                break;
            case Calendar.SATURDAY:
                dayOfWeek = "Thứ Bảy";
                break;
            case Calendar.SUNDAY:
                dayOfWeek = "Chủ Nhật";
                break;
            default:
                dayOfWeek = "";
        }
        
        String formattedDate = dayOfWeek + ", " + 
                selectedDate.get(Calendar.DAY_OF_MONTH) + " tháng " + 
                (selectedDate.get(Calendar.MONTH) + 1) + ", " + 
                selectedDate.get(Calendar.YEAR);
        
        tvEventDate.setText(formattedDate);
    }
      private void saveEvent() {
        // Kiểm tra tiêu đề rỗng
        String title = edtEventTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề sự kiện", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Lấy các giá trị
        String eventType = actvEventType.getText().toString();
        String startTime = edtStartTime.getText().toString();
        String endTime = edtEndTime.getText().toString();
        boolean isAllDay = switchAllDay.isChecked();
        String reminder = actvReminder.getText().toString();
        String note = edtEventNote.getText().toString().trim();
        
        // Tạo đối tượng sự kiện
        Event event = new Event();
        event.setTitle(title);
        event.setEventType(eventType);
        event.setYear(selectedDate.get(Calendar.YEAR));
        event.setMonth(selectedDate.get(Calendar.MONTH) + 1); // Calendar tháng bắt đầu từ 0
        event.setDay(selectedDate.get(Calendar.DAY_OF_MONTH));
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setAllDay(isAllDay);
        event.setReminder(reminder);
        event.setNote(note);
          // Lưu sự kiện vào database
        long eventId = dbHelper.addEvent(event);
        
        if (eventId != -1) {
            event.setId((int) eventId);
            // Lên lịch thông báo cho sự kiện
            notificationHelper.scheduleNotification(event);
            
            // Set result và kết thúc Activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("EVENT_ADDED", true);
            resultIntent.putExtra("YEAR", event.getYear());
            resultIntent.putExtra("MONTH", event.getMonth());
            resultIntent.putExtra("DAY", event.getDay());
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu sự kiện", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Đã được cấp quyền thông báo
                Toast.makeText(this, "Đã được cấp quyền thông báo", Toast.LENGTH_SHORT).show();
            } else {
                // Quyền thông báo bị từ chối
                Toast.makeText(this, "Thông báo sẽ không hoạt động do không được cấp quyền", Toast.LENGTH_LONG).show();
            }
        }
    }
}
