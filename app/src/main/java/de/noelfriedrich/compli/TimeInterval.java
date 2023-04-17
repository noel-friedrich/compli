package de.noelfriedrich.compli;

import android.content.Context;

import java.util.ArrayList;

public class TimeInterval {

    private int numLength;
    private long totalSeconds;

    public TimeInterval(long seconds) {
        this.totalSeconds = seconds;
        this.numLength = 2;
    }

    public static TimeInterval fromPassedTime(long time) {
        return new TimeInterval(Utilities.unixTimestamp() - time);
    }

    public void update(long seconds) {
        this.totalSeconds = seconds;
    }

    public long totalMinutes() {
        return this.totalSeconds / 60;
    }

    public long totalHours() {
        return this.totalMinutes() / 60;
    }

    public long totalDays() {
        return this.totalHours() / 24;
    }

    private String numToString(long n, int length) {
        String out = String.valueOf(n);
        while (out.length() < length) {
            out = "0" + out;
        }
        return out;
    }

    public String toString(Context context) {
        ArrayList<String> statements = new ArrayList<String>();
        if (daysRaw() == 1) statements.add(daysRaw() + " " + context.getString(R.string.day));
        if (daysRaw() > 1) statements.add(daysRaw() + " " + context.getString(R.string.days));
        if (hoursRaw() == 1) statements.add(hoursRaw() + " " + context.getString(R.string.hour));
        if (hoursRaw() > 1) statements.add(hoursRaw() + " " + context.getString(R.string.hours));
        if (minutesRaw() == 1) statements.add(minutesRaw() + " " + context.getString(R.string.minute));
        if (minutesRaw() > 1) statements.add(minutesRaw() + " " + context.getString(R.string.minutes));
        if (secondsRaw() == 1) statements.add(secondsRaw() + " " + context.getString(R.string.second));
        if (secondsRaw() > 1) statements.add(secondsRaw() + " " + context.getString(R.string.seconds));

        if (statements.size() == 0) return "0 " + context.getString(R.string.seconds);
        if (statements.size() == 1) return statements.get(0);

        String out = "";
        for (int i = 0; i < statements.size(); i++) {
            out += statements.get(i);
            if (i < statements.size() - 2) {
                out += ", ";
            } else if (i < statements.size() - 1) {
                out += " " + context.getString(R.string.and) + " ";
            }
        }

        return out;
    }

    boolean SCREENSHOT_MODE = false;

    public long secondsRaw() {
        if (SCREENSHOT_MODE) {
            return 2;
        }

        return this.totalSeconds % 60;
    }

    public long minutesRaw() {
        if (SCREENSHOT_MODE) {
            return 4;
        }

        return this.totalMinutes() % 60;
    }

    public long hoursRaw() {
        if (SCREENSHOT_MODE) {
            return 8;
        }

        return this.totalHours() % 24;
    }

    public long daysRaw() {
        if (SCREENSHOT_MODE) {
            return 16;
        }

        return this.totalDays() % 60;
    }

    public String seconds() {
        return numToString(this.secondsRaw(), 2);
    }

    public String minutes() {
        return numToString(this.minutesRaw(), 2);
    }

    public String hours() {
        return numToString(this.hoursRaw(), 2);
    }

    public String days() {
        if (SCREENSHOT_MODE) {
            return "16";
        }

        long days = this.totalDays();
        if (days > 99) {
            return "99+";
        } else {
            return numToString(days, 2);
        }
    }

}
