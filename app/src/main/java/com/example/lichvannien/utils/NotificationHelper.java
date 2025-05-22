package com.example.lichvannien.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.lichvannien.R;
import com.example.lichvannien.model.Event;
import com.example.lichvannien.receivers.NotificationReceiver;
import com.example.lichvannien.notification.OngoingEventNotification;
import com.example.lichvannien.notification.UpcomingEventNotification;

import java.util.Calendar;

public class NotificationHelper {
    private static final String CHANNEL_ID = "EventReminder";
    private static final String CHANNEL_NAME = "Nhắc nhở sự kiện";
    private static final String CHANNEL_DESCRIPTION = "Thông báo cho các sự kiện trong lịch";

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }    public void scheduleNotification(Event event, String reminderText) {
        // Skip scheduling if reminder is "Không nhắc" (No reminder)
        if ("Không nhắc".equals(reminderText)) {
            return;
        }
        
        // Lên lịch thông báo theo thời gian được chọn
        scheduleReminderAtTime(event, reminderText);
        
        // Nếu đặt nhắc nhở trước thời gian sự kiện, thì cũng lên lịch thêm một thông báo khi sự kiện bắt đầu
        if (!"Khi sự kiện diễn ra".equals(reminderText)) {
            scheduleReminderAtTime(event, "Khi sự kiện diễn ra");
        }
    }
    
    private void scheduleReminderAtTime(Event event, String reminderText) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventTitle", event.getTitle());
        intent.putExtra("eventDate", event.getDate());
        intent.putExtra("reminderText", reminderText);
          // Add debug log to check what we're passing to the intent
        android.util.Log.d("NotificationHelper", "Scheduling notification with reminderText: " + reminderText);

        // Tạo requestCode khác nhau cho "Khi sự kiện diễn ra" và các thông báo khác
        int requestCode = (int) event.getId();
        if ("Khi sự kiện diễn ra".equals(reminderText)) {
            requestCode = requestCode + 10000; // Thêm 10000 để tránh trùng với các requestCode khác
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(event.getDate());
            calendar.setTime(date);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // Nếu là "Khi sự kiện diễn ra" thì set đúng giờ bắt đầu sự kiện
        if (reminderText != null && "Khi sự kiện diễn ra".equals(reminderText)) {
            if (!event.isAllDay() && event.getStartTime() != null && !event.getStartTime().isEmpty()) {
                try {
                    String[] hm = event.getStartTime().split(":");
                    int hour = Integer.parseInt(hm[0]);
                    int minute = Integer.parseInt(hm[1]);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                } catch (Exception ex) {
                    // Nếu lỗi thì giữ nguyên 00:00
                }
            } else {
                // All day event: giữ nguyên 00:00
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
            }
        } else if (reminderText != null) {
            switch (reminderText) {
                case "15 phút trước":
                    calendar.add(Calendar.MINUTE, -15);
                    break;
                case "30 phút trước":
                    calendar.add(Calendar.MINUTE, -30);
                    break;
                case "1 giờ trước":
                    calendar.add(Calendar.HOUR_OF_DAY, -1);
                    break;
                case "1 ngày trước":
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    break;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }    public void showNotification(int eventId, String title, String eventDate, String reminderText) {
        android.app.Notification notification;
        
        // Add debug log to verify the reminderText at notification time
        android.util.Log.d("NotificationHelper", "showNotification: eventId=" + eventId + 
                ", title=" + title + ", reminderText='" + reminderText + "'");
        
        // Check for exact match "Khi sự kiện diễn ra" for ongoing events
        if (reminderText != null && reminderText.equals("Khi sự kiện diễn ra")) {
            android.util.Log.d("NotificationHelper", "Using OngoingEventNotification");
            notification = OngoingEventNotification.build(context, title);
        } else {
            android.util.Log.d("NotificationHelper", "Using UpcomingEventNotification");
            notification = UpcomingEventNotification.build(context, title);
        }
        notificationManager.notify(eventId, notification);
    }

    /**
     * Hủy thông báo hiện tại cho một sự kiện
     * @param eventId ID của sự kiện
     */
    public void cancelNotification(long eventId) {
        // Hủy thông báo thông thường
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) eventId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Hủy thông báo "Khi sự kiện diễn ra" (có requestCode khác)
        Intent ongoingIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent ongoingPendingIntent = PendingIntent.getBroadcast(
                context,
                (int) eventId + 10000, // như đã thêm trong requestCode đặc biệt
                ongoingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.cancel(ongoingPendingIntent);
        
        // Xóa thông báo hiện tại nếu đang hiển thị
        notificationManager.cancel((int) eventId);
    }
}
