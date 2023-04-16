package de.noelfriedrich.compli;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private Handler buttonHandler = new Handler();
    private int handlerIntervalMs = 1000;

    SharedPreferences settings;
    TimeInterval currInterval;

    TextView secondsView;
    TextView minutesView;
    TextView hoursView;
    TextView daysView;

    TextView descriptionView;
    TextView streakView;
    TextView statisticsTextView;

    Button complimentButton;
    Button openOptionsButton;
    Button openHelpButton;
    Button openPrivacyButton;

    TableLayout primaryContentLayout;

    boolean undoComplimentMode = false;
    Long tempTimestamp = null;

    void updateTimerSetting(long newTime) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(getString(R.string.timer_timestamp_key), newTime);
        editor.apply();
    }

    long getTimerSetting() {
        return settings.getLong(
                getString(R.string.timer_timestamp_key),
                Utilities.unixTimestamp());
    }

    long calcSecondsPassed() {
        return Utilities.unixTimestamp() - getTimerSetting();
    }

    void resetTimer() {
        updateTimerSetting(Utilities.unixTimestamp());
    }

    void updateInterval() {
        currInterval.update(calcSecondsPassed());
    }

    void updateViews() {
        updateInterval();

        secondsView.setText(currInterval.seconds());
        minutesView.setText(currInterval.minutes());
        hoursView.setText(currInterval.hours());
        daysView.setText(currInterval.days());
    }

    void updateStatistics() {
        TimerDatabase.update(settings);
        if (TimerDatabase.statisticsAvailable()) {
            String newStatistics = "";
            for (String line : TimerDatabase.statistics(this)) {
                newStatistics += line + "\n\n";
            }
            newStatistics = newStatistics.substring(0, newStatistics.length() - 2);
            statisticsTextView.setText(newStatistics);
        } else {
            statisticsTextView.setText(R.string.default_statistics);
        }

        updateGraphs();
    }

    void updateStreak() {
        if (Options.getOption("show_streak").getBooleanValue()) {
            streakView.setVisibility(View.VISIBLE);
            String streak = TimerDatabase.getStreak();
            String streakString = getString(R.string.streak_output).replace("X", streak);
            streakView.setText(streakString);
        } else {
            streakView.setVisibility(View.GONE);
        }
    }

    void updateDescription() {
        updateOptions();

        assert Options.getOption("notification_hours") != null;
        String hours = Options.getOption("notification_hours").value;
        String description = getString(R.string.mainpage_description)
                .replaceAll("\\$\\{hours\\}", hours);

        descriptionView.setText(description);
    }

    StatisticsView addStatisticView() {
        StatisticsView statisticsView = new StatisticsView(this);
        primaryContentLayout.addView(statisticsView);
        TableLayout.LayoutParams params = (TableLayout.LayoutParams) statisticsView.getLayoutParams();
        params.setMargins(0, 50, 0, 0);
        statisticsView.post(new Runnable() {
            @Override
            public void run() {
                float aspectRatio = 16f / 9f;
                int height = (int) (statisticsView.getWidth() * (1f / aspectRatio));
                statisticsView.setMinimumHeight(height);
            }
        });
        return statisticsView;
    }

    void showWeekStatistic(StatisticsView statisticsView) {
        statisticsView.setTitle(getString(R.string.setting_week_graph_header));
        TreeMap<LocalDate, Integer> dayCompliments = TimerDatabase.toComplimentsPerDay(7);

        if (dayCompliments.size() < 7) {
            return;
        }

        String[] labels = new String[7];
        int[] statisticsData = new int[7];
        for (int i = 0; i < 7; i++) {
            int index = dayCompliments.size() - (7 - i);
            LocalDate localDate = dayCompliments.keySet().toArray(new LocalDate[0])[index];
            int compliments = dayCompliments.get(localDate);
            int weekDayIndex = localDate.getDayOfWeek().getValue();
            String weekDayName = Utilities.weekdayToShortString(this, weekDayIndex);

            labels[i] = weekDayName;
            statisticsData[i] = compliments;
        }

        statisticsView.setLabels(labels);
        statisticsView.setData(statisticsData);

        statisticsView.post(new Runnable() {
            @Override
            public void run() {
                statisticsView.invalidate();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Options.init(this);
        setContentView(R.layout.activity_main);

        secondsView = findViewById(R.id.seconds_output);
        minutesView = findViewById(R.id.minutes_output);
        hoursView = findViewById(R.id.hours_output);
        daysView = findViewById(R.id.days_output);
        statisticsTextView = findViewById(R.id.statistics_text);
        descriptionView = findViewById(R.id.description_text);
        streakView = findViewById(R.id.streak_output);

        complimentButton = findViewById(R.id.compliment_btn);
        openHelpButton = findViewById(R.id.open_help_btn);
        openOptionsButton = findViewById(R.id.open_settings_btn);
        openPrivacyButton = findViewById(R.id.open_privacy_btn);

        primaryContentLayout = findViewById(R.id.primary_content);

        Utilities.addActivityChangeListener(this, openHelpButton, HelpActivity.class);
        Utilities.addActivityChangeListener(this, openOptionsButton, OptionsActivity.class);
        Utilities.addActivityChangeListener(this, openPrivacyButton, PrivacyActivity.class);

        settings = getSharedPreferences(getString(R.string.options_timer_key), 0);

        if (getTimerSetting() == -1) {
            resetTimer();
        }

        currInterval = new TimeInterval(calcSecondsPassed());

        loop();

        complimentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (undoComplimentMode) {
                    undoCompliment();
                } else {
                    giveCompliment();
                }
            }
        });

        updateStatistics();
        updateDescription();
        updateStreak();

        TimerDatabase.update(settings);
        addStatistics();
    }

    StatisticsView weekStatisticView = null;

    void updateOptions() {
        SharedPreferences opts = getSharedPreferences(getString(R.string.options_settings_key), 0);
        Options.update(opts);
    }

    void addStatistics() {
        updateOptions();

        if (Options.getOption("show_week_graph").getBooleanValue()) {
            weekStatisticView = addStatisticView();
            showWeekStatistic(weekStatisticView);
        }
    }

    void updateGraphs() {
        if (weekStatisticView != null) {
            showWeekStatistic(weekStatisticView);
        }
    }

    void giveCompliment() {
        tempTimestamp = getTimerSetting();

        resetTimer();
        updateViews();
        Utilities.createNotificationChannel(this);
        Utilities.registerNotifyAlarm(this);
        TimerDatabase.addTime(Utilities.unixTimestamp(), settings);
        updateStatistics();

        complimentButton.setText(R.string.undo_compliment_btn);
        undoComplimentMode = true;

        updateOptions();
        int undoSeconds = Options.getOption("undo_seconds").getIntValue();
        // remove all previous delayed callbacks:
        buttonHandler.removeCallbacksAndMessages(null);
        buttonHandler.postDelayed(this::resetButton, undoSeconds * 1000);
    }

    void undoCompliment() {
        if (tempTimestamp == null || !undoComplimentMode) {
            return;
        }

        TimerDatabase.popLast(settings);
        updateTimerSetting(tempTimestamp);
        tempTimestamp = null;
        updateViews();
        updateStatistics();
        resetButton();
        // TODO: remove current notification
    }

    void resetButton() {
        if (!undoComplimentMode) {
            return;
        }

        complimentButton.setText(R.string.give_compliment_btn);
        undoComplimentMode = false;
    }

    void loop() {
        updateViews();
        updateStatistics();
        updateStreak();

        handler.postDelayed(this::loop, handlerIntervalMs);
    }
}