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
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.adapters.TaskDetailAdapter;
import com.example.planit.database.Contract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Label;
import model.Task;
import model.TaskPriority;

public class TaskDetailActivity extends AppCompatActivity {

    private static final String TAG = "TaskDetailActivity";

    private Task task;
    private Intent intent;
    private int taskPosition;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskdetail);

        intent = new Intent();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getIntent().hasExtra("task")) {

            //get the task with the id
            task = getTaskFromDatabase(getIntent().getIntExtra("task", -1));
            taskPosition = getIntent().getIntExtra("position", -1);

            List<Label> taskLabels = getTaskLabelsFromDatabase(task.getId());
            task.setLabels(taskLabels);

            //set activity title
            DateFormat dateFormat = new SimpleDateFormat("MMMM dd, YYYY");
            String dateString = dateFormat.format(task.getStartDate());
            setTitle(dateString);

            TextView title = findViewById(R.id.title_task_detail);
            TextView description = findViewById(R.id.description_task_detail);
            TextView priority = findViewById(R.id.priority_task_detail);
            checkBox = findViewById(R.id.checkbox_task_detail);
            TextView location = findViewById(R.id.location_task_detail);
            TextView label = findViewById(R.id.label_task_detail);
            TextView time = findViewById(R.id.time_task_detail);
            TextView reminder = findViewById(R.id.reminder_task_detail);

            title.setText(task.getTitle());

            checkBox.setChecked(task.getDone());

            if (task.getPriority() != null) {
                priority.setText(task.getPriority().getLabel());
            }

            if (task.getDescription() != null) {
                description.setText(task.getDescription());
            }

            if (task.getAddress() != null) {
                location.setText(task.getAddress());
            }

            if (task.getStartTime() != null) {
                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                String timeString = timeFormat.format(task.getStartTime());
                time.setText(timeString);
            }

            if (task.getReminderTime() != null) {
                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                String reminderString = timeFormat.format(task.getReminderTime());
                reminder.setText(reminderString);
            }

            //initialize RecyclerView
            RecyclerView recyclerView = findViewById(R.id.task_detail_recycle_view);
            recyclerView.setHasFixedSize(true);

            //set the adapter and layout manager
            TaskDetailAdapter adapter = new TaskDetailAdapter(this, task.getLabels());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            //check if task has labels
            if (task.getLabels().isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                label.setVisibility(View.VISIBLE);
            } else {
                label.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            //checkbox listener
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //update the state of the task
                    int rows = updateTaskStatusInDatabase(task.getId(), isChecked);

                    if (rows == 1) {
                        //do nothing
                    } else {
                        //revert the checkbox
                        buttonView.setChecked(!isChecked);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_task:
                openDeleteTaskDialog();
                break;
            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    void openDeleteTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_task_title);
        builder.setMessage(getResources().getString(R.string.delete_task_text) + " " + task.getTitle() + "?");
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteTaskLabelsFromDatabase(task.getId());
                int deletedRows = deleteTaskFromDatabase(task.getId());

                if (deletedRows == 1) {
                    intent.putExtra("deleted", true);
                    intent.putExtra("position", taskPosition);

                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
            }
        });

        builder.create().show();
    }

    //delete the task
    private int deleteTaskFromDatabase(Integer taskId) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + taskId);

        return getContentResolver().delete(taskUri, null, null);
    }

    //delete the task labels
    private int deleteTaskLabelsFromDatabase(Integer taskId) {
        String selection = "task = ?";
        String[] selectionArgs = new String[]{Integer.toString(taskId)};

        return getContentResolver().delete(Contract.TaskLabel.CONTENT_URI_TASK_LABEL, selection, selectionArgs);
    }

    //update the state of the task
    private int updateTaskStatusInDatabase(Integer taskId, Boolean isChecked) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + taskId);

        ContentValues values = new ContentValues();
        values.put(Contract.Task.COLUMN_DONE, isChecked ? 1 : 0);

        return getContentResolver().update(taskUri, values, null, null);
    }

    //get task with the id from the database
    private Task getTaskFromDatabase(Integer taskId) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + taskId);

        String[] allColumns = {Contract.Task.COLUMN_ID, Contract.Task.COLUMN_TITLE, Contract.Task.COLUMN_DESCRIPTION, Contract.Task.COLUMN_START_DATE,
                Contract.Task.COLUMN_START_TIME, Contract.Task.COLUMN_PRIORITY, Contract.Task.COLUMN_ADDRESS, Contract.Task.COLUMN_DONE, Contract.Task.COLUMN_REMINDER};

        Cursor cursor = getContentResolver().query(taskUri, allColumns, null, null, null);
        cursor.moveToFirst();

        Task task = new Task();
        task.setId(cursor.getInt(0));
        task.setTitle(cursor.getString(1));
        task.setDescription(cursor.getString(2));

        if (cursor.getString(3) == null) {
            task.setStartDate(null);
        } else {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = dateFormat.parse(cursor.getString(3));
                task.setStartDate(startDate);
            } catch (ParseException e) {
                task.setStartDate(null);
            }
        }

        if (cursor.getString(4) == null) {
            task.setStartTime(null);
        } else {
            try {
                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                Date startTime = timeFormat.parse(cursor.getString(4));
                task.setStartTime(startTime);
            } catch (ParseException e) {
                task.setStartTime(null);
            }
        }

        if (cursor.getString(5) != null) {
            task.setPriority(TaskPriority.valueOf(cursor.getString(5)));
        }

        task.setAddress(cursor.getString(6));
        task.setDone(cursor.getInt(7) == 1);

        if (cursor.getString(8) == null) {
            task.setReminderTime(null);
        } else {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                Date reminderTime = dateFormat.parse(cursor.getString(8));
                task.setReminderTime(reminderTime);
            } catch (ParseException e) {
                task.setReminderTime(null);
            }
        }

        cursor.close();

        return task;
    }

    //get labels of the task with the id from the database
    private List<Label> getTaskLabelsFromDatabase(Integer taskId) {
        List<Label> taskLabels = new ArrayList<>();

        Uri taskLabelsUri = Uri.parse(Contract.Label.CONTENT_URI_LABEL_TASK + "/" + taskId);

        String[] allColumns = {Contract.Label.COLUMN_ID, Contract.Label.COLUMN_NAME, Contract.Label.COLUMN_COLOR};

        Cursor cursor = getContentResolver().query(taskLabelsUri, allColumns, null, null, null);

        if (cursor.getCount() == 0) {
            //do nothing I guess
        } else {
            while (cursor.moveToNext()) {
                Label label = new Label();
                label.setId(cursor.getInt(0));
                label.setName(cursor.getString(1));
                label.setColor(cursor.getString(2));

                taskLabels.add(label);
            }
        }

        cursor.close();

        return taskLabels;
    }

    @Override
    public void finish() {
        //check if task status changed
        if (checkBox.isChecked() != task.getDone()) {
            intent.putExtra("changed", true);
            intent.putExtra("position", taskPosition);

            setResult(Activity.RESULT_OK, intent);
        }

        super.finish();
    }
}