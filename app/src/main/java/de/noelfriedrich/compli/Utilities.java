package de.noelfriedrich.compli;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

final public class Utilities {

    static public void addActivityChangeListener(Activity a, View v, Class cls) {
        // saves instance for later use in onClickListener
        // as they override the 'this' keyword
        // (looks sketchy but works)
        Activity self = a;

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changeActivity = new Intent(self, cls);
                a.startActivity(changeActivity);
            }
        });
    }

    static long unixTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    static public String timestampToString(long s) {
        Date date = new Date(s * 1000);
        return date.toString();
    }

    static public void sendNotification(Context context, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(context, context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.compli_logo)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        int randomNotificationID = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);
        notificationManager.notify(randomNotificationID, builder.build());
    }

    static public void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.app_name) + " Reminder Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(context.getString(R.string.app_name), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    static public long alarmSecondsLeft(Context context, SharedPreferences settings) {
        int notificationHours = Options.getOption("notification_hours").getIntValue();
        int notificationSeconds = notificationHours * 60 * 60; // seconds = hours * 60 * 60 = hours * 3600

        long lastTime = settings.getLong(context.getString(R.string.timer_timestamp_key), unixTimestamp());
        long difference = unixTimestamp() - lastTime;
        return notificationSeconds - difference;
    }

    static public void registerNotifyAlarm(Context context) {
        SharedPreferences optionsSettings = context.getSharedPreferences(
                context.getString(R.string.options_settings_key), 0);
        Options.update(optionsSettings);

        if (!Options.getOption("send_notification").getBooleanValue())
            return;

        SharedPreferences timerSettings = context.getSharedPreferences(
                context.getString(R.string.options_timer_key), 0);

        long notificationTime = (unixTimestamp() + alarmSecondsLeft(context, timerSettings)) * 1000;

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("action", "notify");
        // all pendingIntent's will be created with the same requestCode and will thus
        // override each other, cancelling a pending notification when pressing the button!
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        manager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);

        Log.i("alarm", "set alarm at " + notificationTime);
    }

    static public void removeCurrTimer(Context context, SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(context.getString(R.string.timer_timestamp_key));
    }

}
