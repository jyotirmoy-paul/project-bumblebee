package com.android.mr_paul.sarwar_delivery.Services;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.mr_paul.sarwar_delivery.MainActivity;
import com.android.mr_paul.sarwar_delivery.R;
import com.android.mr_paul.sarwar_delivery.UtilityPackage.Constants;
import com.android.mr_paul.sarwar_delivery.UtilityPackage.LatLong;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import static com.android.mr_paul.sarwar_delivery.UtilityPackage.App.CHANNEL_ID;

public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    // important global variables
    GoogleApiClient mGoogleApiClient;
    LocationCallback mLocationCallbacks;
    LocationRequest mLocationRequest;



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // do nothing
    }

    @Override
    public void onConnectionSuspended(int i) {
        // do nothing
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // get my current location
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000 + new Random().nextInt(10000)); // update location every (10 - 20) seconds

        // now if we have the authority to look into user's current location, do update get it
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest,mLocationCallbacks,null);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        // the following code needs to be run only once in the entire lifecycle of this service class
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // setting the locationCallback functionality
        mLocationCallbacks = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if(locationResult == null){
                    return;
                }

                Location location = locationResult.getLastLocation();
                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();

                // write location to the shared preference
                SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
                editor.putString(Constants.LATITUDE, Double.toString(latitude));
                editor.putString(Constants.LONGITUDE, Double.toString(longitude));
                editor.apply();

                // write to the cloud database
                FirebaseDatabase.getInstance().getReference().child("delivery_agent_data").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("contact_info").child("latLong").setValue(new LatLong(latitude, longitude));
            }
        };
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // to keep the activity alive in foreground, show an notification, stating the app status
        Intent notificationIntent = new Intent(this,MainActivity.class);
        Intent[] listOfIntents = new Intent[1];
        listOfIntents[0] = notificationIntent;
        PendingIntent pendingIntent = PendingIntent.getActivities(
                this,0,listOfIntents,0);
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Your location is being monitored")
                .setSmallIcon(R.drawable.ic_location)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1,notification);

        // finally connect the googleApiClient
        if(this.mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }

        return START_STICKY; // this will ensure the auto-start of the service
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
