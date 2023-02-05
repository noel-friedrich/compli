package de.noelfriedrich.compli;

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

    public String toString() {
        ArrayList<String> statements = new ArrayList<String>();
        if (daysRaw() == 1) statements.add(daysRaw() + " day");
        if (daysRaw() > 1) statements.add(daysRaw() + " days");
        if (hoursRaw() == 1) statements.add(hoursRaw() + " hour");
        if (hoursRaw() > 1) statements.add(hoursRaw() + " hours");
        if (minutesRaw() == 1) statements.add(minutesRaw() + " minute");
        if (minutesRaw() > 1) statements.add(minutesRaw() + " minutes");
        if (secondsRaw() == 1) statements.add(secondsRaw() + " second");
        if (secondsRaw() > 1) statements.add(secondsRaw() + " seconds");

        if (statements.size() == 0) return "0 seconds";
        if (statements.size() == 1) return statements.get(0);

        String out = "";
        for (int i = 0; i < statements.size(); i++) {
            out += statements.get(i);
            if (i < statements.size() - 2) {
                out += ", ";
            } else if (i < statements.size() - 1) {
                out += " and ";
            }
        }

        return out;
    }

    public long secondsRaw() {
        return this.totalSeconds % 60;
    }

    public long minutesRaw() {
        return this.totalMinutes() % 60;
    }

    public long hoursRaw() {
        return this.totalHours() % 24;
    }

    public long daysRaw() {
        return this.totalDays() % 60;
    }

    public String seconds() {
        return numToString(this.totalSeconds % 60, 2);
    }

    public String minutes() {
        return numToString(this.totalMinutes() % 60, 2);
    }

    public String hours() {
        return numToString(this.totalHours() % 24, 2);
    }

    public String days() {
        long days = this.totalDays();
        if (days > 99) {
            return "99+";
        } else {
            return numToString(days, 2);
        }
    }

}
