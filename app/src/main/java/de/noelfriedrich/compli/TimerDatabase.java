package de.noelfriedrich.compli;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.sql.Array;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Stack;
import java.util.TreeMap;

public class TimerDatabase {

    public static String csvTimes = "";
    public static String settingsKey = "times_csv";

    public static void update(SharedPreferences settings) {
        csvTimes = settings.getString(settingsKey, "");
    }

    public static String getStreak() {
        int minDays = 1000;
        while (true) {
            TreeMap<LocalDate, Integer> complimentsPerDay = toComplimentsPerDay(minDays);
            Integer[] compliments = complimentsPerDay.values().toArray(new Integer[0]);
            int streak = 0;
            for (int i = compliments.length - 1; i >= 0; i--) {
                if (compliments[i] == 0) {
                    return Integer.toString(streak);
                } else {
                    streak++;
                }
            }
            minDays += 1000;

            if (minDays > 100000) {
                return "99999+";
            }
        }
    }

    public static TreeMap<LocalDate, Integer> toComplimentsPerDay(int minDays) {
        long[] times = getTimes();
        ArrayList<Long> timeDeque = new ArrayList<Long>();
        for (int i = 0; i < times.length; i++) {
            timeDeque.add(times[i]);
        }

        TreeMap<LocalDate, Integer> count = new TreeMap<LocalDate, Integer>();

        LocalDate start;
        if (times.length == 0) {
            start = LocalDate.now();
        } else {
            start = new Date(times[0] * 1000).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        LocalDate end = LocalDate.now().plusDays(1);

        while (ChronoUnit.DAYS.between(start, end) < minDays) {
            start = start.minusDays(1);
        }

        final int dayInMillis = 1000 * 60 * 60 * 24;
        for (LocalDate currDate = start; currDate.isBefore(end); currDate = currDate.plusDays(1))  {
            long currDateMillis = currDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            currDateMillis += dayInMillis;

            if (timeDeque.size() == 0) {
                count.put(currDate, 0);
                continue;
            }

            int tempCount = 0;
            while (timeDeque.get(0) * 1000 < currDateMillis) {
                tempCount++;
                timeDeque.remove(0);
                if (timeDeque.size() == 0)
                    break;
            }

            count.put(currDate, tempCount);
        }

        return count;
    }

    public static String csvFromTimes(long[] times) {
        String out = "";
        for (int i = 0; i < times.length; i++) {
            out += Long.toString(times[i]);
            if (i != times.length - 1) {
                out += ",";
            }
        }
        return out;
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

    public static String[] statistics(Context context) {
        String[] lines = new String[3];
        TimeInterval averageInterval = new TimeInterval(averageInterval());
        TimeInterval firstClickInterval = TimeInterval.fromPassedTime(getTimes()[0]);

        lines[0] = context.getString(R.string.statistic_average).replace("X", averageInterval.toString(context));
        lines[1] = context.getString(R.string.statistic_first).replace("X", firstClickInterval.toString(context));
        lines[2] = context.getString(R.string.statistic_total).replace("X", Integer.toString(getTimes().length));

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

    public static void popLast(SharedPreferences settings) {
        long[] times = getTimes();
        if (times.length == 0)
            return;

        long[] newTimes = new long[times.length - 1];
        for (int i = 0; i < times.length - 1; i++) {
            newTimes[i] = times[i];
        }
        csvTimes = csvFromTimes(newTimes);
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
