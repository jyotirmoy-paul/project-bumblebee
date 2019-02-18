package com.android.mr_paul.sarwar;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mr_paul.sarwar.UtilityPackage.Constants;
import com.android.mr_paul.sarwar.UtilityPackage.DonationData;
import com.android.mr_paul.sarwar.UtilityPackage.LatLong;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdExtendedListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DonationDataPostingActivity extends AppCompatActivity implements LocationListener {

    LocationManager locationManager;
    String donationDescription, donationCategory, donationWorth, userNumber, donationOtherDetail, photoUri, donorName, currentDateTime, donorPhone, donorAddress, userRegTokenKey;
    ProgressDialog pd;
    LatLong latLong = null;
    StorageReference storageReference;
    UploadTask uploadTask;
    Bitmap userDonationCoverPhotoBitmap;

    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_data_posting);

        showInterstitialAd();

        pd = new ProgressDialog(DonationDataPostingActivity.this);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE);

        // get data from shared preference
        donorName = sharedPreferences.getString(Constants.USER_NAME,Constants.UNDEFINED);
        donorAddress = sharedPreferences.getString(Constants.USER_ADDRESS, Constants.UNDEFINED);
        donorPhone = sharedPreferences.getString(Constants.USER_PHONE_NUMBER,Constants.UNDEFINED);
        userRegTokenKey = sharedPreferences.getString(Constants.FIREBASE_TOKEN,Constants.UNDEFINED);

        // get data from intent
        final Intent intent = getIntent();
        donationDescription = intent.getStringExtra(Constants.DONATION_DESCRIPTION);
        donationWorth = intent.getStringExtra(Constants.DONATION_WORTH);
        donationOtherDetail = intent.getStringExtra(Constants.DONATION_OTHER_DETAIL);
        userNumber = intent.getStringExtra(Constants.DONATION_USER_NUMBER);
        photoUri = intent.getStringExtra(Constants.DONATION_PHOTO_URI);
        donationCategory = intent.getStringExtra(Constants.DONATION_CATEGORY);

        // get the current date
        DateFormat dateFormat = new SimpleDateFormat("HH:mm, dd MMM yyyy");
        currentDateTime = dateFormat.format(new Date());


        // referencing to the views
        ImageView coverPhoto = findViewById(R.id.cover_image);
        TextView donorNameDisplay = findViewById(R.id.donor_name);
        TextView currentDateTimeDisplay = findViewById(R.id.date_time);
        TextView categoryDisplay = findViewById(R.id.donation_category);
        TextView donationDescriptionDisplay = findViewById(R.id.donation_description);
        TextView otherDetailDisplay = findViewById(R.id.donation_other_details);
        TextView donorAddressDisplay = findViewById(R.id.donor_address);
        TextView donorPhoneNumber = findViewById(R.id.donor_contact_number);


        LinearLayout getLocationDisplay = findViewById(R.id.get_location);
        LinearLayout postDonationDetails = findViewById(R.id.post_donation_details);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        // set the donation Data to the users
        donorNameDisplay.setText(donorName);
        donorAddressDisplay.setText(donorAddress);
        currentDateTimeDisplay.setText(currentDateTime);
        categoryDisplay.setText(donationCategory);
        donationDescriptionDisplay.setText(donationDescription);
        otherDetailDisplay.setText(donationOtherDetail);
        donorPhoneNumber.setText(donorPhone);

        try{
            InputStream inputStream = getContentResolver().openInputStream(Uri.parse(photoUri));
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            userDonationCoverPhotoBitmap = getResizedBitmap(bmp,1000,600);
            coverPhoto.setImageBitmap(userDonationCoverPhotoBitmap);
            inputStream.close();
        }
        catch (Exception e){
            Log.i("Photo -->","Image not Found");
        }


        getLocationDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // try to get the location of the user
                if (ActivityCompat.checkSelfPermission(DonationDataPostingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DonationDataPostingActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    askForRequiredPermission();
                    return;
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, DonationDataPostingActivity.this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, DonationDataPostingActivity.this);
                pd.setMessage("Getting current Location...");
                pd.show();
            }
        });

        postDonationDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check for time period
                // users can donate only between 06:00 - 12:00 or between 13:00 - 18:00

                if(!isInTimePeriod()){

                    // show an alert dialog box, informing the user about the donation time period
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(DonationDataPostingActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(DonationDataPostingActivity.this);
                    }
                    builder.setTitle("Donation Not Available")
                            .setMessage("Please note that we accept donation requests between 06:00 - 12:00 and 13:00 - 18:00 hrs only!")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i("TAG","not a donation time");
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();


                    return;
                }


                if(latLong == null){
                    Toast.makeText(getApplicationContext(), "Please get your location first", Toast.LENGTH_SHORT).show();
                    return;
                }

                final ProgressDialog progressDialog = new ProgressDialog(DonationDataPostingActivity.this);
                progressDialog.setMessage("Sending your donation details...");
                progressDialog.show();

                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("donation_details").push();

                final String donationKey = databaseReference.getKey();

                storageReference = FirebaseStorage.getInstance().getReference().child(donationKey);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                userDonationCoverPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                uploadTask = storageReference.putBytes(baos.toByteArray());

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return storageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {

                            progressDialog.cancel();

                            DonationData donationData = new DonationData(donorName, donationCategory, donationKey, donorPhone, donationDescription, donationWorth, donationOtherDetail,
                                    task.getResult().toString(), latLong, donorAddress, currentDateTime, userRegTokenKey, Constants.STATUS_WAITING
                                    ,FirebaseAuth.getInstance().getCurrentUser().getUid());

                            databaseReference.child("donation_data").setValue(donationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    // show ad only after donation data has been uploaded successfully
                                    if(interstitialAd != null && interstitialAd.isAdLoaded()){

                                        interstitialAd.show();

                                        interstitialAd.setAdListener(new InterstitialAdExtendedListener() {
                                            @Override
                                            public void onInterstitialActivityDestroyed() {

                                            }

                                            @Override
                                            public void onInterstitialDisplayed(Ad ad) {

                                            }

                                            @Override
                                            public void onInterstitialDismissed(Ad ad) {

                                                Toast.makeText(getApplicationContext(), "You donation data has been submitted successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(DonationDataPostingActivity.this, MainActivity.class));
                                                DonationDataPostingActivity.this.finish();

                                            }

                                            @Override
                                            public void onError(Ad ad, AdError adError) {

                                            }

                                            @Override
                                            public void onAdLoaded(Ad ad) {

                                            }

                                            @Override
                                            public void onAdClicked(Ad ad) {

                                            }

                                            @Override
                                            public void onLoggingImpression(Ad ad) {

                                            }
                                        });

                                    }
                                    else{
                                        // if interstitial ad is not loaded, then just close this activity

                                        Toast.makeText(getApplicationContext(), "You donation data has been submitted successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(DonationDataPostingActivity.this, MainActivity.class));
                                        DonationDataPostingActivity.this.finish();

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(DonationDataPostingActivity.this, Constants.TRY_AGAIN_FAILURE, Toast.LENGTH_SHORT).show();
                                }
                            });



                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DonationDataPostingActivity.this, Constants.TRY_AGAIN_FAILURE, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, DonationDataPostingActivity.this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,DonationDataPostingActivity.this);

            pd.setMessage("Getting current Location...");
            pd.show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Location access is required", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onLocationChanged(Location location) {

        // we got the location, put the data in the server
        pd.cancel();

        if(location != null){
            latLong = new LatLong(location.getLatitude(), location.getLongitude());
            locationManager.removeUpdates(this);
            Toast.makeText(getApplicationContext(), "You can now post!", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), Constants.TRY_AGAIN_FAILURE, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {


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

    // time period checker method
    public Boolean isInTimePeriod(){
        return true; // TODO: comment this line, intended for testing purposes only
/*
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);

        return (hour >= 6 && hour < 12) || (hour >= 13 && hour < 18);
*/
    }


    // important utility method
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // creating a matrix for manupulation
        Matrix matrix = new Matrix();

        // resizing the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreating the bit map
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    void showInterstitialAd(){
        interstitialAd = new InterstitialAd(DonationDataPostingActivity.this, Constants.FACEBOOK_INTERSTITIAL_AD_KEY);
        interstitialAd.loadAd();

    }

}
