package com.example.planit.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.planit.R;
import com.example.planit.database.Contract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import model.Habit;

public class HabitDetailsActivity extends AppCompatActivity {

    private Habit habit;
    private Integer habitId;
    private Integer index;
    private Intent intent;
    private TextView titleView;
    private TextView descriptionView;
    private TextView habitNumberOfDaysTextView;
    private Switch switchDone;
    private Boolean changed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_habit_details);

        // toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar_habit);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.titleView = findViewById(R.id.habits_overview_recycle_view_name);
        this.descriptionView = findViewById(R.id.habit_details_description);
        this.habitNumberOfDaysTextView = findViewById(R.id.habits_overview_recycle_view_num_days);
        this.switchDone = findViewById(R.id.habit_done);


        this.intent = new Intent();
        this.changed = false;

        // check if habit exists
        if (getIntent().hasExtra("habitId")) {
            this.habitId = getIntent().getIntExtra("habitId", -1);
            this.index = getIntent().getIntExtra("index", -1);
            if (this.habitId != null && this.habitId != -1) {
                // switch on checked listener

            }
        }

    }

    public int deleteTodayHabitFulfillment() {
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        String selection = Contract.HabitFulfillment.COLUMN_HABIT_ID + " = ? and " + Contract.HabitFulfillment.COLUMN_DATE + " = ?";
        String[] selectionArgs = new String[]{Integer.toString(habitId), format.format(new Date())};

        return getContentResolver().delete(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT, selection, selectionArgs);
    }

    public Uri addHabitFulfillment() {
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        ContentValues values = new ContentValues();
        values.put(Contract.HabitFulfillment.COLUMN_DATE, format.format(new Date()));
        values.put(Contract.HabitFulfillment.COLUMN_HABIT_ID, habitId);
        return getContentResolver().insert(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT, values);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.habitId != null && this.habitId != -1) {
            //get the habit information from the database
            this.getHabitInfo();
            //set activity title
            this.setTitle(habit.getTitle());
            this.titleView.setText(habit.getTitle());
            this.descriptionView.setText(habit.getDescription());
            this.habitNumberOfDaysTextView.setText(habit.getTotalNumberOfDays().toString());
            this.switchDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        Uri resultUri = addHabitFulfillment();
                        if (resultUri != null) {
                            habit.setTotalNumberOfDays(habit.getTotalNumberOfDays() + 1);
                            changed = true;
                        } else {
                            switchDone.setChecked(false);
                        }
                    } else {
                        int numberOfDeletedRows = deleteTodayHabitFulfillment();
                        if (numberOfDeletedRows > 0) {
                            habit.setTotalNumberOfDays(habit.getTotalNumberOfDays() - 1);
                            changed = true;
                        } else {
                            switchDone.setChecked(true);
                        }
                    }
                    habitNumberOfDaysTextView.setText(habit.getTotalNumberOfDays().toString());
                }
            });

        }
    }

    private void getHabitInfo() {
        Uri uri = Uri.parse(Contract.Habit.CONTENT_URI_HABIT + "/" + this.habitId);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToNext()) {
            Integer id = (Integer) cursor.getInt(cursor.getColumnIndex(Contract.Habit.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(Contract.Habit.COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndex(Contract.Habit.COLUMN_DESCRIPTION));
            Uri uriFulfillment = Uri.parse(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT + "/" + Contract.Habit.TABLE_NAME + "/" + id);
            Cursor cursorFulfillment = getContentResolver().query(uriFulfillment, null, null, null, null);
            if (cursorFulfillment.getCount() != 0)
                while (cursorFulfillment.moveToNext()) {
                    String dateString = cursorFulfillment.getString(cursorFulfillment.getColumnIndex(Contract.HabitFulfillment.COLUMN_DATE));
                    DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                    if (dateString.toString().equals(format.format(new Date()))) {
                        this.switchDone.setChecked(true);
                        break;
                    }
                }

            habit = new Habit();
            habit.setId(id);
            habit.setTitle(title);
            habit.setTotalNumberOfDays(cursorFulfillment.getCount());
            habit.setDescription(description);
            cursorFulfillment.close();
        }
        cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.habit_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_task:
                openDialog();
                break;
            case R.id.menu_edit_task:
                Intent intent = new Intent(this, CreateHabitActivity.class);
                intent.putExtra("habitId", habitId);
                startActivityForResult(intent, 4);
                break;
            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        int deletedRows = deleteHabitFromDB();

                        if (deletedRows > 0) {
                            intent.putExtra("deleted", true);
                            intent.putExtra("index", index);

                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    private int deleteHabitFromDB() {

        this.deleteHabitFulfillmentByHabit();

        if (this.habit.getNumberOfDays() == null) {
            this.deleteHabitDays();
        }

        Uri uri = Uri.parse(Contract.Habit.CONTENT_URI_HABIT + "/" + habit.getId());
        return getContentResolver().delete(uri, null, null);
    }

    private void deleteHabitFulfillmentByHabit() {
        Uri uri = Uri.parse(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT + "/" + Contract.Habit.TABLE_NAME + "/" + habit.getId());
        getContentResolver().delete(uri, null, null);
    }

    private void deleteHabitDays() {
        Uri uri = Uri.parse(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN + "/" + Contract.Habit.TABLE_NAME + "/" + habit.getId());
        getContentResolver().delete(uri, null, null);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // habit is changed
        if (requestCode == 4) {
            if (resultCode == Activity.RESULT_OK) {
                Boolean updated = data.getBooleanExtra("updated", false);

                if (updated) {
                    intent.putExtra("updated", true);
                    intent.putExtra("index", this.index);
                    intent.putExtra("habitId", habit.getId());
                    setResult(Activity.RESULT_OK, intent);
                }
            }
        }
    }

    @Override
    public void finish() {
        if (changed) {
            intent.putExtra("done", true);
            intent.putExtra("index", index);
            intent.putExtra("habitId", habit.getId());
            intent.putExtra("totalDays", habit.getTotalNumberOfDays());
            setResult(Activity.RESULT_OK, intent);
        }

        super.finish();
    }


}
