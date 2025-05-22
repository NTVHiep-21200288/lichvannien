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
    }

    public void scheduleNotification(Event event) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventTitle", event.getTitle());
        intent.putExtra("eventDate", event.getDate());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) event.getId(), // Cast to int
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Đặt thông báo trước sự kiện 15 phút
        Calendar calendar = Calendar.getInstance();
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(event.getDate());
            calendar.setTime(date);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        calendar.add(Calendar.MINUTE, -15);

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
    }

    public void showNotification(int eventId, String title, long eventDate) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText("Sự kiện sẽ diễn ra trong 15 phút nữa")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(eventId, builder.build());
    }
}
