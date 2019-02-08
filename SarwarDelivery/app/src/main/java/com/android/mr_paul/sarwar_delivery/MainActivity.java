package com.android.mr_paul.sarwar_delivery;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mr_paul.sarwar_delivery.Fragments.AboutFragment;
import com.android.mr_paul.sarwar_delivery.Fragments.DeliveryTaskFragment;
import com.android.mr_paul.sarwar_delivery.Fragments.FaqFragment;
import com.android.mr_paul.sarwar_delivery.Fragments.ProfileFragment;
import com.android.mr_paul.sarwar_delivery.Services.BackgroundLocationService;
import com.android.mr_paul.sarwar_delivery.UtilityPackage.Constants;
import com.android.mr_paul.sarwar_delivery.UtilityPackage.LatLong;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ref = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeliveryTaskFragment()).commit();
            getSupportActionBar().setTitle("Delivery Tasks");
            navigationView.setCheckedItem(R.id.nav_donate);
        }

        final SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE);

        String userName = sharedPreferences.getString(Constants.USER_NAME, Constants.UNDEFINED);
        String userDetail = sharedPreferences.getString(Constants.USER_PHONE_NUMBER, Constants.UNDEFINED);
        String firebaseToken = sharedPreferences.getString(Constants.FIREBASE_TOKEN, Constants.UNDEFINED);
        double latitude = Double.valueOf(sharedPreferences.getString(Constants.LATITUDE, "0"));
        double longitude = Double.valueOf(sharedPreferences.getString(Constants.LONGITUDE, "0"));

        // set the user name, profile picture and contact detail
        View headerView = navigationView.getHeaderView(0);
        TextView navUserNameDisplay = headerView.findViewById(R.id.user_name);
        TextView navUserDetailDisplay = headerView.findViewById(R.id.user_detail);
        TextView profileDisplay = headerView.findViewById(R.id.user_profile_pic);

        navUserNameDisplay.setText(userName);
        navUserDetailDisplay.setText(userDetail);
        profileDisplay.setText(userName.toUpperCase().charAt(0) + "");

        // clean till this point


        // updating the contact info of the delivery agent - this is done every time the user opens the MainActivity
        updateContactInfo(FirebaseAuth.getInstance().getCurrentUser().getUid(), firebaseToken, latitude, longitude);

        Button markAsAvailableBtn = findViewById(R.id.location_access);
        // set up the available button
        markAsAvailableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ask for location permission if not already given!
                askForRequiredPermission();
                // check if location permission is granted
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                if (sharedPreferences.getBoolean(Constants.IS_AVAILABLE, false)) {
                    // then put delivery agent as Unavailable
                    putModeUnavailable();

                } else {
                    // then put the delivery agent as un available
                    putModeAvailable();

                }
            }
        });

        // set the last setting
        if (sharedPreferences.getBoolean(Constants.IS_AVAILABLE, false)) {
            putModeAvailable();
        } else {
            putModeUnavailable();
        }

    }

    // this method puts the firebase token to the online database
    void updateContactInfo(String uid, String firebaseToken, double latitude, double longitude) {

        String status_string = Constants.NO;

        if (getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).getBoolean(Constants.IS_AVAILABLE, false)) {
            status_string = Constants.YES;
        }

        Map<String, Object> contactData = new HashMap<>();
        contactData.put("firebaseToken", firebaseToken);
        contactData.put("isAvailable", status_string);
        contactData.put("latLong", new LatLong(latitude, longitude));
        contactData.put("UID", uid);

        ref.child("delivery_agent_data").child(uid).child("contact_info").setValue(contactData);

    }


    void putModeAvailable(){

        // put the delivery agent to available mode
        getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit().putBoolean(Constants.IS_AVAILABLE, true).apply();

        TextView status = findViewById(R.id.availability_status);
        status.setText(Constants.status_available);
        status.setTextColor(getResources().getColor(R.color.deepGreen));
        status.setBackgroundColor(getResources().getColor(R.color.lightGreen));

        // start the background service
        ContextCompat.startForegroundService(MainActivity.this,new Intent(MainActivity.this,BackgroundLocationService.class));


        ref.child("delivery_agent_data").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("contact_info").child("isAvailable").setValue(Constants.YES);

    }

    void putModeUnavailable(){

        // put the delivery agent to unavailable mode

        getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit().putBoolean(Constants.IS_AVAILABLE, false).apply();

        TextView status = findViewById(R.id.availability_status);
        status.setText(Constants.status_unavailable);
        status.setTextColor(getResources().getColor(R.color.deepRed));
        status.setBackgroundColor(getResources().getColor(R.color.lightRed));

        // stop the background service
        stopService(new Intent(MainActivity.this, BackgroundLocationService.class));


        ref.child("delivery_agent_data").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("contact_info").child("isAvailable").setValue(Constants.NO);

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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new DeliveryTaskFragment()).commit();
                getSupportActionBar().setTitle("Delivery Tasks");
                break;
            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
                getSupportActionBar().setTitle("Profile");
                break;
            case R.id.nav_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new AboutFragment()).commit();
                getSupportActionBar().setTitle("About");
                break;
            case R.id.nav_faq:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new FaqFragment()).commit();
                getSupportActionBar().setTitle("FAQs");
                break;
            case R.id.nav_logout:
                // logout the current user and direct to the registration activity
                putModeUnavailable();
                SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,MODE_PRIVATE).edit();
                editor.putBoolean(Constants.IS_USER_LOGGED_IN,false);
                editor.apply();
                stopService(new Intent(MainActivity.this, BackgroundLocationService.class)); // stop the background service
                FirebaseAuth.getInstance().signOut(); // signing out

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                MainActivity.this.finish();

                break;
            case R.id.nav_email:
                // call an intent to Gmail app
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "sarwartechnicalhelp@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sarwar-Delivery Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
                break;

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void askForRequiredPermission() {

        // ask for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            // put the delivery agent to available mode
            putModeAvailable();

        }
        else{

            Toast.makeText(MainActivity.this, "Location access is required!", Toast.LENGTH_LONG).show();
        }

    }
}
