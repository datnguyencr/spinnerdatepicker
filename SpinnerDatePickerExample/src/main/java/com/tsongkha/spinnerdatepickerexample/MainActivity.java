package com.tsongkha.spinnerdatepickerexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import com.tsongkha.spinnerdatepicker.CustomDatePickerDialog;
import com.tsongkha.spinnerdatepicker.CustomTimePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;
import com.tsongkha.spinnerdatepicker.SpinnerTimePickerDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by rawsond on 25/08/17.
 */

public class MainActivity extends AppCompatActivity {

    TextView dateTextView;
    Button dateButton;
    Button timeButton;
    SimpleDateFormat simpleDateFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dateButton = (Button) findViewById(R.id.set_date_button);
        dateTextView = (TextView) findViewById(R.id.date_textview);
        simpleDateFormat = new SimpleDateFormat("dd MM yyyy", Locale.US);
        dateButton.setOnClickListener(view -> showDate(1980, 0, 1, R.style.DatePickerSpinner));

        timeButton = (Button) findViewById(R.id.set_time_button);
        timeButton.setOnClickListener(view -> showTime());
    }


    @VisibleForTesting
    void showDate(int year, int monthOfYear, int dayOfMonth, int spinnerTheme) {
        CustomDatePickerDialog dialog = new SpinnerDatePickerDialogBuilder()
                .context(MainActivity.this)
                .callback((view, year1, monthOfYear1, dayOfMonth1) -> {
                    Calendar calendar = new GregorianCalendar(year1, monthOfYear1, dayOfMonth1);
                    dateTextView.setText(simpleDateFormat.format(calendar.getTime()));
                })
                .onCancel(view -> dateTextView.setText(R.string.cancelled))
                .defaultDate(year, monthOfYear, dayOfMonth)
                .build();
        dialog.show(getSupportFragmentManager(), "SpinnerDatePickerDialog");
    }

    @VisibleForTesting
    void showTime() {
        CustomTimePickerDialog dialog;
        dialog = new SpinnerTimePickerDialogBuilder()
                .context(MainActivity.this)
                .callback((view, hour, minute) -> {

                })
                .onCancel(view -> {

                })
                .build();
        dialog.show(getSupportFragmentManager(), "SpinnerDatePickerDialog");
    }

}