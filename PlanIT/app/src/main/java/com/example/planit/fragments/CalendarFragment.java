package com.example.planit.fragments;

import com.example.planit.R;
import com.example.planit.activities.ChatActivity;
import com.example.planit.activities.EditTaskActivity;
import com.example.planit.activities.TeamDetailActivity;
import com.example.planit.database.Contract;
import com.example.planit.utils.EventDecorator;
import com.example.planit.utils.FragmentTransition;
import com.example.planit.utils.SharedPreference;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";

    private ArrayList<CalendarDay> eventDates = new ArrayList<>();
    private MaterialCalendarView calendarView;
    private Integer teamId;
    private FloatingActionButton floatingActionButton;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static CalendarFragment newInstance(Integer team, Integer position, String teamName) {
        CalendarFragment fragment = new CalendarFragment();

        if (team != null) {
            Bundle args = new Bundle();
            args.putInt("SELECTED_TEAM", team);
            args.putInt("POSITION", position);
            args.putString("TEAM_NAME", teamName);

            fragment.setArguments(args);
        }

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        //set activity title
        if (getArguments() != null) {

            teamId = getArguments().getInt("SELECTED_TEAM");
            String teamName = getArguments().getString("TEAM_NAME");

            getActivity().setTitle(teamName);

            setHasOptionsMenu(true);
        } else {
            getActivity().setTitle(R.string.personal);
        }

        calendarView = view.findViewById(R.id.calendar_view);
        calendarView.setDateSelected(CalendarDay.today(), true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean showWeekDays = sharedPreferences.getBoolean("pref_week_days", true);
        String startDayOfTheWeek = sharedPreferences.getString("pref_start_day", "3");

        calendarView.state().edit().setShowWeekDays(showWeekDays);

        //update the size of the calendar tiles
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            calendarView.setTileSizeDp(view.getHeight() / 8);
        }

        //get dates with tasks for the current month
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        getTaskDates(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR), teamId);

        //decorator for marking dates with events
        calendarView.addDecorator(new EventDecorator(getResources().getColor(R.color.colorPrimary), eventDates));

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                widget.setDateSelected(date, false);
                widget.setDateSelected(CalendarDay.today(), true);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, date.getYear());
                cal.set(Calendar.MONTH, date.getMonth() - 1);
                cal.set(Calendar.DAY_OF_MONTH, date.getDay());
                Long dateInMilliseconds = cal.getTimeInMillis();

                //go to DailyPreviewFragment for the selected date
                FragmentTransition.replaceFragment(getActivity(), DailyPreviewFragment.newInstance(dateInMilliseconds, teamId), R.id.fragment_container, true);
            }
        });

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                getTaskDates(date.getMonth(), date.getYear(), teamId);

                calendarView.removeDecorators();
                calendarView.addDecorator(new EventDecorator(getResources().getColor(R.color.colorPrimary), eventDates));
            }
        });

        //floating action button for creating a new task
        floatingActionButton = view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditTaskActivity.class);
                intent.putExtra("team", teamId);
                getActivity().startActivityForResult(intent, 1);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(teamId != null && !isNetworkAvailable()){
            floatingActionButton.setEnabled(false);
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
        }
        else {
            floatingActionButton.setEnabled(true);
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.team_calendar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_chat:
                intent = new Intent(getActivity(), ChatActivity.class);

                if (getArguments() != null) {
                    Integer teamId = getArguments().getInt("SELECTED_TEAM");
                    intent.putExtra("team", teamId);
                }

                getActivity().startActivity(intent);
                break;
            case R.id.menu_team_details:
                intent = new Intent(getActivity(), TeamDetailActivity.class);

                if (getArguments() != null) {
                    Integer teamId = getArguments().getInt("SELECTED_TEAM");
                    Integer position = getArguments().getInt("POSITION");

                    intent.putExtra("team", teamId);
                    intent.putExtra("position", position);
                }

                getActivity().startActivityForResult(intent, 6);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get dates with tasks for the specific month of the specific year
     *
     * @param month shown in the calendar
     * @param year  shown in the calendar
     */
    private void getTaskDates(int month, int year, Integer teamId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean showTeamTasks = sharedPreferences.getBoolean("pref_assigned_team_tasks", false);

        String monthString = Integer.toString(month);
        if (monthString.length() < 2) {
            monthString = "0" + monthString;
        }

        String date = year + "-" + monthString + "-01";
        String[] allColumns = {"distinct " + Contract.Task.COLUMN_START_DATE};

        String selection = Contract.Task.COLUMN_START_DATE + " between date(?) and date(?, '+1 month', '-1 day')";
        String[] selectionArgs;
        //for personal calendar
        if (teamId == null) {
            selection += " and (" + Contract.Task.COLUMN_TEAM + " is null";
            //show team tasks assigned to the logged in user as well
            if (showTeamTasks) {
                Integer id = SharedPreference.getLoggedId(getActivity());

                selection += " or " + Contract.Task.COLUMN_USER + "= ?)";
                selectionArgs = new String[]{date, date, id.toString()};
            }
            //show only personal task assigned to the logged in user
            else {
                selection += ")";
                selectionArgs = new String[]{date, date};
            }
        }
        //for team calendar
        else {
            selection += " and " + Contract.Task.COLUMN_TEAM + " = ?";
            selectionArgs = new String[]{date, date, Integer.toString(teamId)};
        }

        Cursor cursor = getActivity().getContentResolver().query(Contract.Task.CONTENT_URI_TASK, allColumns, selection, selectionArgs, null);

        eventDates.clear();

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                try {
                    Date eventDate = dateFormat.parse(cursor.getString(0));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(eventDate);

                    eventDates.add(CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
                } catch (ParseException e) {
                    //do nothing?
                }

            }
        }

        cursor.close();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
