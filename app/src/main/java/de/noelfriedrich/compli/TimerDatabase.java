package de.noelfriedrich.compli;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import java.sql.Time;
import java.util.ArrayList;

public class TimerDatabase {

    public static String csvTimes = "";
    public static String settingsKey = "times_csv";

    public static void update(SharedPreferences settings) {
        csvTimes = settings.getString(settingsKey, "");
    }

    public static long[] getTimes() {
        if (csvTimes.equals("")) {
            return new long[0];
        }
        String[] timeStrings = csvTimes.split(",");
        long[] times = new long[timeStrings.length];
        for (int i = 0; i < timeStrings.length; i++) {
            times[i] = Long.parseLong(timeStrings[i]);
        }
        return times;
    }

    public static boolean statisticsAvailable() {
        return getTimes().length >= 2;
    }

    public static int averageInterval() {
        long[] times = getTimes();
        int sum = 0;
        for (int i = 1; i < times.length; i++) {
            sum += Math.abs(times[i] - times[i - 1]);
        }
        return (int) ((double) sum / (double) (times.length - 1));
    }

    public static String[] statistics() {
        String[] lines = new String[3];
        TimeInterval averageInterval = new TimeInterval(averageInterval());
        TimeInterval firstClickInterval = TimeInterval.fromPassedTime(getTimes()[0]);

        lines[0] = "The average time between compliments is " + averageInterval.toString() + ".";
        lines[1] = "Your first recorded compliment has been given " + firstClickInterval.toString() + " ago.";
        lines[2] = "In total, you have given " + getTimes().length + " compliments.";

        return lines;
    }

    public static void updateCsvTimes(SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(settingsKey, csvTimes);
        editor.apply();
    }

    public static void reset(SharedPreferences settings) {
        csvTimes = "";
        updateCsvTimes(settings);
    }

    public static void addTime(long time, SharedPreferences settings) {
        String strTime = String.valueOf(time);
        if (csvTimes.length() > 0) {
            csvTimes += ",";
        }
        csvTimes += strTime;
        updateCsvTimes(settings);
    }

    public static void addTimeToTable(TableLayout tableLayout, Context context, String time) {
        TextView textView = new TextView(context);
        textView.setText(time);
        textView.setTextSize(17);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setLayoutParams(new TableLayout.LayoutParams());
        tableLayout.addView(textView);
    }

    public static void addTimesToTable(TableLayout tableLayout, Context context) {
        for (long time : getTimes()) {
            addTimeToTable(tableLayout, context, Utilities.timestampToString(time));
        }
        if (getTimes().length == 0) {
            addTimeToTable(tableLayout, context, "<no timestamps stored>");
        }
    }

}
