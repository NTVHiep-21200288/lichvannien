package com.example.lichvannien.notification;

import android.app.Notification;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import com.example.lichvannien.R;

public class UpcomingEventNotification {
    public static Notification build(Context context, String title) {
        return new NotificationCompat.Builder(context, "EventReminder")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText("Sự kiện sẽ diễn ra trong thời gian tới")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
    }
}
