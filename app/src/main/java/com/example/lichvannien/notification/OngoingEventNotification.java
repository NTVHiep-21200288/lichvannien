package com.example.lichvannien.notification;

import android.app.Notification;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import com.example.lichvannien.R;

public class OngoingEventNotification {
    public static Notification build(Context context, String title) {
        return new NotificationCompat.Builder(context, "EventReminder")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText("Sự kiện đang diễn ra!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
    }
}
