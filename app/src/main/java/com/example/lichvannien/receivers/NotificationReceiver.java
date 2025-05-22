package com.example.lichvannien.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.lichvannien.utils.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int eventId = intent.getIntExtra("eventId", 0);
        String eventTitle = intent.getStringExtra("eventTitle");
        long eventDate = intent.getLongExtra("eventDate", 0);

        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.showNotification(eventId, eventTitle, eventDate);
    }
}
