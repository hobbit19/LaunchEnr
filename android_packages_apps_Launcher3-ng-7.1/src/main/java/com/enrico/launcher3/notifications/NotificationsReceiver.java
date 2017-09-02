package com.enrico.launcher3.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //when the service is killed we will receive a broadcast intent to restart it and keep it alive!
        Intent restartService = new Intent(context, NotificationsDotListener.class);
        context.startService(restartService);
    }
}
