package com.android.mr_paul.sarwar_admin.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.android.mr_paul.sarwar_admin.MainActivity;
import com.android.mr_paul.sarwar_admin.R;
import com.android.mr_paul.sarwar_admin.UtilityPackage.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class FirebaseInstanceService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        // save the token in the shared preference
        getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit().putString(Constants.FIREBASE_TOKEN,s).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // show an notification to the user
        showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

    }

    // helper method for building up the notification
    private void showNotification(String title, String body){

        // setting up pending intent
        Intent notificationIntent = new Intent(this,MainActivity.class);
        Intent[] listOfIntents = new Intent[1];
        listOfIntents[0] = notificationIntent;
        PendingIntent pendingIntent = PendingIntent.getActivities(this,0,listOfIntents,0);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // create a notification channel if android version is above or equal to OREO
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){


            NotificationChannel notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, "Notification",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription("Sarwar Channel");
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100,500,100});



            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setContentInfo("Info");

        // finally, show the notification to the user
        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }

}
