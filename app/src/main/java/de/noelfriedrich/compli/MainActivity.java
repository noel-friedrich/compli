package de.noelfriedrich.compli;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private int handlerIntervalMs = 1000;

    SharedPreferences settings;
    TimeInterval currInterval;

    TextView secondsView;
    TextView minutesView;
    TextView hoursView;
    TextView daysView;

    TextView descriptionView;

    TextView statisticsTextView;

    Button complimentButton;
    Button openOptionsButton;
    Button openHelpButton;
    Button openPrivacyButton;

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
        }
    }

    void updateDescription() {
        SharedPreferences opts = getSharedPreferences(getString(R.string.options_settings_key), 0);
        Options.update(opts);

        assert Options.getOption("notification_hours") != null;
        String hours = Options.getOption("notification_hours").value;
        String description = getString(R.string.mainpage_description)
                .replaceAll("\\$\\{hours\\}", hours);

        descriptionView.setText(description);
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

        complimentButton = findViewById(R.id.compliment_btn);
        openHelpButton = findViewById(R.id.open_help_btn);
        openOptionsButton = findViewById(R.id.open_settings_btn);
        openPrivacyButton = findViewById(R.id.open_privacy_btn);

        Utilities.addActivityChangeListener(this, openHelpButton, HelpActivity.class);
        Utilities.addActivityChangeListener(this, openOptionsButton, OptionsActivity.class);
        Utilities.addActivityChangeListener(this, openPrivacyButton, PrivacyActivity.class);

        settings = getSharedPreferences(getString(R.string.options_timer_key), 0);

        if (getTimerSetting() == -1) {
            resetTimer();
        }

        currInterval = new TimeInterval(calcSecondsPassed());

        loop();

        Context context = this;
        complimentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
                updateViews();
                Utilities.createNotificationChannel(context);
                Utilities.registerNotifyAlarm(context);
                TimerDatabase.addTime(Utilities.unixTimestamp(), settings);
                updateStatistics();
            }
        });

        updateStatistics();
        updateDescription();
    }

    void loop() {
        updateViews();
        handler.postDelayed(this::loop, handlerIntervalMs);
    }
}