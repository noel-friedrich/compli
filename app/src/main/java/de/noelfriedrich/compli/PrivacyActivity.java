package de.noelfriedrich.compli;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

public class PrivacyActivity extends AppCompatActivity {

    TextView backButton;
    TableLayout allTimesTable;

    Button deleteDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        backButton = findViewById(R.id.privacy_back_btn);
        allTimesTable = findViewById(R.id.times_table);
        deleteDataButton = findViewById(R.id.delete_data_btn);

        Utilities.addActivityChangeListener(this, backButton, MainActivity.class);

        SharedPreferences timerSettings = getSharedPreferences(getString(R.string.options_timer_key), 0);
        SharedPreferences optionSettings = getSharedPreferences(getString(R.string.options_settings_key), 0);
        TimerDatabase.update(timerSettings);

        TimerDatabase.addTimesToTable(allTimesTable, this);

        Activity self = this;
        deleteDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimerDatabase.reset(timerSettings);
                Utilities.removeCurrTimer(self, timerSettings);
                Options.reset(optionSettings);
                allTimesTable.removeAllViews();
                TimerDatabase.addTimesToTable(allTimesTable, self);
            }
        });
    }

}