package pt.ipleiria.estg.dei.sentinel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private TextView tvHeaderEmail;
    private SharedPreferences sharedPref;
    private NavigationView navigationView;

    protected static final String PREFERENCES_FILE_NAME = "pt.ipleiria.estg.dei.sentinel.SHARED_PREFERENCES";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        /*THE STRINGS IN THE CONSTRUCTOR ARE HELPFUL FOR BLIND PEOPLE WHO NEED TEXT TO SPEECH*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) {
//            tvHeaderEmail.setText("Not logged in");

                    /*DISPLAYS LOGIN AND REGISTER BUTTONS*/
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_login).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_register).setVisible(true);


                } else {
                    /*DISPLAYS LOGIN AND REGISTER BUTTONS*/
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_register).setVisible(false);

                    //  tvHeaderEmail.setText(currentUser.getEmail());

                }
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        sharedPref = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);



        /*SWITCHS TO DASHBOARD FRAGMENT*/
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DashboardFragment()).commit();


    }


    @Override
    protected void onStart() {
        super.onStart();

        /*CHECKS USER SAVED PREFERENCE TO KEEP SIGNED IN */
//        tvHeaderEmail = navigationView.getHeaderView(R.id.nav_view).findViewById(R.id.nav_email);


        /*CHANGES EMAIL ACCORDING TO USER LOGIN*/



    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_dashboard:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DashboardFragment()).commit();
                break;

            case R.id.nav_login:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LoginFragment()).commit();
                break;

            case R.id.nav_register:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new RegisterFragment()).commit();
                break;

            case R.id.nav_logout:
                signOut();

                /*SEND DATA ABOUT SIGNOUT DO DASHBOARD ACITVITY*/
               /* Bundle bundle = new Bundle();
                bundle.putString("signout", "Logged out");
                DashboardFragment dashboardFragment = new DashboardFragment();
                dashboardFragment.setArguments(bundle);*/

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DashboardFragment()).commit();

        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void signOut() {
        sharedPref.edit().putBoolean("keep_signed_in", false).commit();
        FirebaseAuth.getInstance().signOut();
    }

    /* IF USER PREFERENCE KEEP SIGNED IN IS FALSE, SIGNOUT USER BEFORE APP CLOSURE*/
    @Override
    public void onStop() {
        super.onStop();
        if (!sharedPref.getBoolean("keep_signed_in", false)) {
            signOut();
        }
    }


}

