package pt.ipleiria.estg.dei.sentinel.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.R;
import pt.ipleiria.estg.dei.sentinel.fragments.DashboardFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.LoginFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.RegisterFragment;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MBVIEW_REQUEST_CODE = 1;
    private DrawerLayout drawer;
    private TextView tvHeaderEmail;
    private SharedPreferences sharedPref;
    private NavigationView navigationView;
    private  Configuration configuration;

    public Boolean LOGGED_IN=false;


    protected static final String PREFERENCES_FILE_NAME = "pt.ipleiria.estg.dei.sentinel.SHARED_PREFERENCES";


    /*TWITTER API*/

    private static SharedPreferences mSharedPreferences;
    private static Twitter twitter;
    private static RequestToken requestToken;
    private AccessToken accessToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /*GETS URL FROM INTENT*/
        Uri uri = getIntent().getData();
        if(!isTwitterLoggedInAlready()){
            /*IF USER STARTED THE APP, NO URL EXISTS*/
            if (uri != null && uri.toString().startsWith(Constants.CALLBACKURL)) {

                /*GETS VERIFIER FROM URL*/
                final String verifier = uri.getQueryParameter(Constants.URL_TWITTER_OAUTH_VERIFIER);

                try {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                accessToken = twitter.getOAuthAccessToken(
                                        requestToken, verifier);

                                SharedPreferences.Editor e = mSharedPreferences.edit();


                                // After getting access token, access token secret
                                // store them in application preferences
                                e.putString(Constants.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                                e.putString(Constants.PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
                                // Store login status - true
                                e.putBoolean(Constants.PREF_KEY_TWITTER_LOGIN, true);
                                e.commit(); // save changes

                                startActivity(new Intent(MainActivity.this, TwitterPop_Activity.class));


                                Log.v("accessToken", accessToken.getToken());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    });
                    thread.start();


                } catch (Exception e) {
                    Log.v("Sentinel", "Erro => " + e.getMessage());
                }
            }

        }


        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvHeaderEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_email);


        /*THE STRINGS IN THE CONSTRUCTOR ARE HELPFUL FOR BLIND PEOPLE WHO NEED TEXT TO SPEECH*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) {
                    tvHeaderEmail.setText("Not logged in");

                    /*DISPLAYS LOGIN AND REGISTER BUTTONS*/
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_login).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_register).setVisible(true);


                } else {
                    /*DISPLAYS LOGIN AND REGISTER BUTTONS*/
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_register).setVisible(false);

                    tvHeaderEmail.setText(currentUser.getEmail());

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


    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(Constants.PREF_KEY_TWITTER_LOGIN, false);
    }

    public void loginToTwitter() {
        // Check if already logged in
        if(!isTwitterLoggedInAlready()) {


            // Start new thread for activity (you can't do too much work on the UI/Main thread.
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {


                    // Setup builder
                    ConfigurationBuilder builder = new ConfigurationBuilder();
                    // Get key and secret from Constants.java
                    builder.setOAuthConsumerKey(Constants.API_KEY);
                    builder.setOAuthConsumerSecret(Constants.API_SECRET);

                    // Build
                    configuration = builder.build();

                    TwitterFactory factory = new TwitterFactory(configuration);
                    twitter = factory.getInstance();

                    try {
                        requestToken = twitter
                                .getOAuthRequestToken(Constants.CALLBACKURL);
                        MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                .parse(requestToken.getAuthenticationURL())));

                        LOGGED_IN = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        }else{
            startActivity(new Intent(MainActivity.this,TwitterPop_Activity.class));
        }


    }

    public static void tweet(String tweet,Context context){
        Handler success = new Handler() {
            public void handleMessage(Message msg){
                if(msg.what == 0){
                    Toast.makeText(context, "Tweet sent successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        Handler error = new Handler() {
            public void handleMessage(Message msg){
                if(msg.what == 0){
                    Toast.makeText(context, "An error occurred while trying to send the Tweet!", Toast.LENGTH_SHORT).show();
                }
            }
        };


        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    twitter.updateStatus(tweet);
                    success.sendEmptyMessage(0);

                } catch (Exception e) {
                    e.printStackTrace();
                    error.sendEmptyMessage(0);
                }
            }
        });
        thread.start();


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

