package de.noelfriedrich.compli;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

class OptionType {
    public static int STRING = 1;
    public static int BOOLEAN = 2;
    public static int INTEGER = 3;
}

class Option {

    public String name;
    public String description;
    public int type;
    public String defaultValue;
    public String value;
    public String id;

    public Integer minNum = null;
    public Integer maxNum = null;

    public Option(String name, String description, int type, String defaultValue, String id) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.id = id;
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+");
    }

    public Option setIntMinMax(int min, int max) {
        minNum = min;
        maxNum = max;
        return this;
    }

    public boolean isGoodValue(String val) {
        if (this.type == OptionType.INTEGER) {
            if (!isNumeric(val))
                return false;
            int intVal = Integer.parseInt(val);
            if (minNum != null && minNum > intVal)
                return false;
            return maxNum == null || maxNum >= intVal;
        } else if (this.type == OptionType.BOOLEAN) {
            return (
                    val.equals("true") || val.equals("false")
                            || val.equals("0") || val.equals("1")
            );
        }

        return true;
    }

    public int getIntValue() {
        return Integer.parseInt(this.value);
    }

    public String getStringValue() {
        return this.value;
    }

    public void setBooleanValue(boolean newValue) {
        if (newValue) {
            this.value = "true";
        } else {
            this.value = "false";
        }
    }

    public boolean getBooleanValue() {
        return (
                this.value.equalsIgnoreCase("true")
                        || this.value.equals("1")
        );
    }

    public String getDescription() {
        String desc = description.replaceAll("<value>", value);
        for (Option option : Options.options) {
            String regex = "<value:" + option.id + ">";
            desc = desc.replaceAll(regex, option.value);
        }
        return desc;
    }

    public void set(String value, SharedPreferences settings) {
        this.value = value;
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(this.id, value);
        editor.commit();
    }

}

public class Options {

    public static Option[] options;

    public static void init(Context context) {
        options = new Option[]{
                new Option(
                        context.getString(R.string.setting_send_notification_header),
                        context.getString(R.string.setting_send_notification_text),
                        OptionType.BOOLEAN,
                        "true",
                        "send_notification"
                ),
                new Option(
                        context.getString(R.string.setting_notification_hours_header),
                        context.getString(R.string.setting_notification_hours_text),
                        OptionType.INTEGER,
                        "48",
                        "notification_hours"
                ).setIntMinMax(1, 1000),
                new Option(
                        context.getString(R.string.setting_full_graph_header),
                        context.getString(R.string.setting_full_graph_text),
                        OptionType.BOOLEAN,
                        "true",
                        "show_full_graph"
                ),
                new Option(
                    context.getString(R.string.setting_week_graph_header),
                    context.getString(R.string.setting_week_graph_text),
                    OptionType.BOOLEAN,
                    "false",
                    "show_week_graph"
                ),
                new Option(
                        context.getString(R.string.setting_month_graph_header),
                        context.getString(R.string.setting_month_graph_text),
                        OptionType.BOOLEAN,
                        "false",
                        "show_month_graph"
                )
        };
    }

    public static void reset(SharedPreferences settings) {
        for (Option option : options) {
            option.set(option.defaultValue, settings);
        }
    }

    public static void update(SharedPreferences settings) {
        for (Option option : options) {
            option.value = settings.getString(option.id, option.defaultValue);
        }
    }

    public static Option getOption(String id) {
        for (Option option : options) {
            if (option.id.equals(id)) {
                return option;
            }
        }
        return null;
    }

    public static void addOptionToTable(SharedPreferences settings, TableLayout tableLayout, Context context, Option option) {
        TableRow row = new TableRow(context);
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, 50);
        row.setLayoutParams(rowParams);

        GridLayout grid = new GridLayout(context);
        TableRow.LayoutParams gridParams = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 8.0f
        );
        grid.setColumnCount(1);
        grid.setLayoutParams(gridParams);

        TextView titleView = new TextView(context);
        titleView.setText(option.name);
        titleView.setTextSize(21);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        GridLayout.LayoutParams titleParams = new GridLayout.LayoutParams();
        titleView.setLayoutParams(titleParams);
        grid.addView(titleView);

        TextView descriptionView = new TextView(context);
        descriptionView.setText(option.getDescription());
        descriptionView.setTextSize(17);
        GridLayout.LayoutParams descriptionParams = new GridLayout.LayoutParams();
        descriptionView.setLayoutParams(descriptionParams);
        grid.addView(descriptionView);

        row.addView(grid);

        if (option.type == OptionType.INTEGER || option.type == OptionType.STRING) {
            EditText input = new EditText(context);
            input.setText(option.value);
            if (option.type == OptionType.INTEGER) {
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else if (option.type == OptionType.STRING) {
                input.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String value = input.getText().toString();
                    if (!option.isGoodValue(value))
                        return;
                    option.set(value, settings);
                    descriptionView.setText(option.getDescription());
                }
            });
            input.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 2.0f
            ));
            row.addView(input);
        } else if (option.type == OptionType.BOOLEAN) {
            Switch switchView = new Switch(context);
            switchView.setChecked(option.getBooleanValue());
            switchView.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 2.0f
            ));
            row.addView(switchView);
            switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    option.setBooleanValue(isChecked);
                    option.set(option.value, settings);
                }
            });
        }

        tableLayout.addView(row);
    }

    public static void addOptionsToTable(SharedPreferences s, TableLayout t, Context c) {
        for (Option option : options) {
            addOptionToTable(s, t, c, option);
        }
    }

}

