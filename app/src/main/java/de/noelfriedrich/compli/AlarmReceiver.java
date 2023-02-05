package de.noelfriedrich.compli;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");

        Log.i("alarm", "received alarm with action \"" + action + "\"");

        if (action == null) {
            return;
        }

        if (action.equals("notify")) {
            Utilities.createNotificationChannel(context);
            Utilities.sendNotification(
                    context,
                    context.getString(R.string.notification_title),
                    context.getString(R.string.notification_description)
            );
        }
    }

}
