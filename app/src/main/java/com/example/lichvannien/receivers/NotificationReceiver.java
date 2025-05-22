package com.example.lichvannien.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.lichvannien.utils.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
      @Override
    public void onReceive(Context context, Intent intent) {
        int eventId = intent.getIntExtra("eventId", 0);
        String eventTitle = intent.getStringExtra("eventTitle");
        String eventDate = intent.getStringExtra("eventDate");
        String reminderText = intent.getStringExtra("reminderText");
        
        // Make sure reminderText is not null to avoid NullPointerException
        if (reminderText == null) {
            reminderText = "";
        }
        
        // Debug log to verify what reminderText we received
        Log.d(TAG, "onReceive: eventId=" + eventId + ", title=" + eventTitle + ", reminderText='" + reminderText + "'");

        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.showNotification(eventId, eventTitle, eventDate, reminderText);
    }
}
