package de.noelfriedrich.compli;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

public class OptionsActivity extends AppCompatActivity {

    TableLayout optionsTable;
    TextView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        optionsTable = findViewById(R.id.options_table);
        backButton = findViewById(R.id.settings_back_btn);

        Utilities.addActivityChangeListener(this, backButton, MainActivity.class);

        SharedPreferences settings = getSharedPreferences(getString(R.string.options_settings_key), 0);
        Options.update(settings);
        Options.addOptionsToTable(settings, optionsTable, this);
    }
}