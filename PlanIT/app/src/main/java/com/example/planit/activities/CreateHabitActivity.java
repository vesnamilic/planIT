package com.example.planit.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.planit.R;

import java.util.Calendar;

public class CreateHabitActivity extends AppCompatActivity {

    private LinearLayout pickDaysLayout;
    private LinearLayout pickWeeksLayout;
    private LinearLayout goalAmountLayout;
    private Button reminderButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_habit);
        setTitle("Create Habit");
        // toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar_habit_create);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        // getting layouts
        pickDaysLayout = findViewById(R.id.habit_create_daily_pick_days);
        pickWeeksLayout = findViewById(R.id.habit_create_daily_pick_weeks);
        goalAmountLayout = findViewById(R.id.habit_create_goal_amount_layout);
        this.reminderButton = findViewById(R.id.reminder_button);

        this.reminderButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateHabitActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        reminderButton.setText( selectedHour + ":" + selectedMinute);
                        reminderButton.setBackground(ContextCompat.getDrawable(CreateHabitActivity.this, R.drawable.circle_primary));
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                Toast.makeText(this, R.string.habit_created, Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onRadioButtonFrequencyClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.habit_create_daily:
                if (checked) {
                    pickDaysLayout.setVisibility(View.VISIBLE);
                    pickWeeksLayout.setVisibility(View.GONE);
                }

                break;
            case R.id.habit_create_weekly:
                if (checked) {
                    pickDaysLayout.setVisibility(View.GONE);
                    pickWeeksLayout.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    public void onRadioButtonGoalClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();


        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.habit_create_goal_all:
                if (checked)
                    goalAmountLayout.setVisibility(View.GONE);
                break;
            case R.id.habit_create_goal_amount:
                if (checked)
                    goalAmountLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

}
