package com.android.mr_paul.sarwar;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mr_paul.sarwar.Fragments.AboutFragment;
import com.android.mr_paul.sarwar.Fragments.DonateFragment;
import com.android.mr_paul.sarwar.Fragments.FaqFragment;
import com.android.mr_paul.sarwar.Fragments.MyDonationsFragment;
import com.android.mr_paul.sarwar.Fragments.NewsFeedFragment;
import com.android.mr_paul.sarwar.Fragments.ProfileFragment;
import com.android.mr_paul.sarwar.Fragments.TopContributorsFragment;
import com.android.mr_paul.sarwar.UtilityPackage.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.facebook.ads.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isNetworkAvailable()){
            Toast.makeText(this, "Please connect to the internet!", Toast.LENGTH_LONG).show();
        }

        // this method is used to show banner ad
        showBannerAd();

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        Boolean isProfileCompleted = sharedPreferences.getBoolean(Constants.IS_PROFILE_COMPLETED,false);

        if(!isProfileCompleted){
            startActivity(new Intent(MainActivity.this, ProfileUpdateActivity.class));
            MainActivity.this.finish();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new DonateFragment()).commit();
            getSupportActionBar().setTitle("Select Category");
            navigationView.setCheckedItem(R.id.nav_donate);
        }

        // set the user name, profile picture and contact detail
        View headerView = navigationView.getHeaderView(0);
        TextView navUserNameDisplay = headerView.findViewById(R.id.user_name);
        TextView navUserDetailDisplay = headerView.findViewById(R.id.user_detail);
        TextView profileDisplay = headerView.findViewById(R.id.user_profile_pic);


        String userName = sharedPreferences.getString(Constants.USER_NAME,Constants.UNDEFINED);
        Boolean isGoogleSignedIn = sharedPreferences.getBoolean(Constants.IS_GMAIL_SIGNED_IN, false);

        String userDetail;

        if(isGoogleSignedIn){
            userDetail = sharedPreferences.getString(Constants.USER_EMAIL,Constants.UNDEFINED);
        }
        else{
            userDetail = sharedPreferences.getString(Constants.USER_PHONE_NUMBER,Constants.UNDEFINED);
        }

        // set display
        navUserNameDisplay.setText(userName);
        navUserDetailDisplay.setText(userDetail);

        profileDisplay.setText(userName.toUpperCase().charAt(0) + "");

    }


    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){
            case R.id.nav_donate:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DonateFragment()).commit();
                getSupportActionBar().setTitle("Select Category");
                break;
            case R.id.nav_news_feed:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NewsFeedFragment()).commit();
                getSupportActionBar().setTitle("News Feed");
                break;
            case R.id.nav_my_donations:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyDonationsFragment()).commit();
                getSupportActionBar().setTitle("Current Donations");
                break;
            case R.id.nav_top_contributors:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TopContributorsFragment()).commit();
                getSupportActionBar().setTitle("Top Contributors");
                break;
            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                getSupportActionBar().setTitle("Profile");
                break;
            case R.id.nav_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
                getSupportActionBar().setTitle("About Us");
                break;
            case R.id.nav_faq:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FaqFragment()).commit();
                getSupportActionBar().setTitle("FAQs");
                break;
            case R.id.nav_logout:
                // logout the current user and direct to the registration activity
                FirebaseAuth.getInstance().signOut();
                SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,MODE_PRIVATE).edit();
                editor.putBoolean(Constants.IS_USER_LOGGED_IN,false);
                editor.putBoolean(Constants.IS_PROFILE_COMPLETED, false); // so that a different account can be opened in this device with new details
                editor.apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                MainActivity.this.finish();
                break;
            case R.id.nav_share:
                // let the user share the app
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Sarwar - Donation that matters");
                    String sAux = "\nSarwar is a great application for donation to the needy! Try it out here: \n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=com.android.mr_paul.sarwar";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Choose one"));
                } catch(Exception e) {
                    Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_rate:
                Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                }
                catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
                }
                break;
            case R.id.nav_email:
                // call an intent to Gmail app
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "sarwartechnicalhelp@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sarwar Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // show banner ad section is defined here
    void showBannerAd(){
        AdView adView = new AdView(MainActivity.this, Constants.FACEBOOK_BANNER_AD_KEY, AdSize.BANNER_HEIGHT_50);

        LinearLayout adContainer = findViewById(R.id.banner_container);
        adContainer.addView(adView);

        adView.loadAd();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
