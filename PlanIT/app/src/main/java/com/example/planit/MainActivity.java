package com.example.planit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.planit.activities.ChatActivity;
import com.example.planit.activities.ProfileActivity;
import com.example.planit.activities.SettingsActivity;
import com.example.planit.activities.SignInActivity;
import com.example.planit.activities.SignUpActivity;
import com.example.planit.fragments.CalendarFragment;
import com.example.planit.fragments.HabitsOverviewFragment;
import com.example.planit.fragments.TeamsOverviewFragment;
import com.example.planit.mokaps.Mokap;
import com.example.planit.utils.SharedPreference;
import com.example.planit.utils.FragmentTransition;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import model.User;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawer;
    private GoogleSignInClient googleSignInClient;
    private NavigationView navigationView;
    private int currentMenuItem;
    private TextView loggedEmail;
    private TextView loggedName;
    private TextView loggedFirstChar;
    private LinearLayout profileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        String page = getIntent().getStringExtra("page");

        //google sign out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        if (SharedPreference.getLoggedEmail(MainActivity.this) == "") {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        } else {
            View header = navigationView.getHeaderView(0);
            loggedEmail = (TextView) header.findViewById(R.id.loggedEmail);
            loggedName = (TextView) header.findViewById(R.id.loggedName);
            loggedFirstChar = (TextView) header.findViewById(R.id.loggedFirstChar);

            profileLayout = (LinearLayout) header.findViewById(R.id.profileLayout);

            loggedEmail.setText(SharedPreference.getLoggedEmail(MainActivity.this));
            loggedName.setText(SharedPreference.getLoggedName(MainActivity.this).concat(" ").concat(SharedPreference.getLoggedLastName(MainActivity.this)));
            loggedFirstChar.setText(findLoggedUserName().substring(0, 1).concat(findLoggedUserLastName().substring(0, 1)));

            profileLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            });

            //if we enter the activity for the first time (not after rotating etc)
            if (savedInstanceState == null) {
                if (page == null) {
                    FragmentTransition.replaceFragment(this, CalendarFragment.newInstance(null), R.id.fragment_container, false);
                    navigationView.setCheckedItem(R.id.nav_calendar);
                    currentMenuItem = R.id.nav_calendar;
                }
            }

        }

        //update navigation drawer selection after back
        /*this.getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        Fragment current = getCurrentFragment();
                        if (current instanceof CalendarFragment) {
                            navigationView.setCheckedItem(R.id.nav_calendar);
                            currentMenuItem = R.id.nav_calendar;
                        } else if (current instanceof DailyPreviewFragment) {
                            navigationView.setCheckedItem(R.id.nav_calendar);
                            currentMenuItem = R.id.nav_calendar;
                        } else if (current instanceof TeamsFragment) {
                            navigationView.setCheckedItem(R.id.nav_teams);
                            currentMenuItem = R.id.nav_teams;
                        }
                    }
                });
        */
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //selected menu item id
        int id = item.getItemId();

        /*if (id == currentMenuItem) {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }*/

        switch (id) {
            case R.id.nav_calendar:
                FragmentTransition.replaceFragment(this, CalendarFragment.newInstance(null), R.id.fragment_container, false);
                break;
            case R.id.nav_habits:
                FragmentTransition.replaceFragment(this, HabitsOverviewFragment.newInstance(), R.id.fragment_container, false);
                break;
            case R.id.nav_teams:
                FragmentTransition.replaceFragment(this, TeamsOverviewFragment.newInstance(), R.id.fragment_container, false);
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                /*getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragment_container, PreferencesFragment.newInstance())
                        .addToBackStack(null)
                        .commit();*/
                break;
            case R.id.nav_signout:
                signOut();
                break;
            default:
        }

        currentMenuItem = id;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getCurrentFragment() instanceof TeamsOverviewFragment || getCurrentFragment() instanceof HabitsOverviewFragment) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransition.replaceFragment(this, CalendarFragment.newInstance(null), R.id.fragment_container, false);
                navigationView.setCheckedItem(R.id.nav_calendar);
                currentMenuItem = R.id.nav_calendar;
            } else {
                super.onBackPressed();
            }
        }
    }

    private void signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                SharedPreference.setLoggedEmail(getApplicationContext(), "");
                SharedPreference.setLoggedName(getApplicationContext(), "");
                SharedPreference.setLoggedColour(getApplicationContext(), "");
                SharedPreference.setLoggedLastName(getApplicationContext(), "");
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    public Fragment getCurrentFragment() {
        return this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    public String findLoggedUserName() {
        if (SharedPreference.getLoggedName(MainActivity.this) == null) {
            return "";
        }
        return SharedPreference.getLoggedName(MainActivity.this);
    }

    public String findLoggedUserLastName() {
        if (SharedPreference.getLoggedLastName(MainActivity.this) == null) {
            return "";
        }
        return SharedPreference.getLoggedLastName(MainActivity.this);
    }

}
