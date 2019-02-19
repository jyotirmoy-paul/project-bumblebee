package com.android.mr_paul.sarwar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mr_paul.sarwar.UtilityPackage.Constants;
import com.android.mr_paul.sarwar.UtilityPackage.UserInfo;
import com.bumptech.glide.Glide;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.CopyOnWriteArrayList;

public class ProfileUpdateActivity extends AppCompatActivity {

    String userName, userEmail, userAddress, userMobile;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        pd = new ProgressDialog(ProfileUpdateActivity.this);
        pd.setMessage("Please wait...");


        final SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

        if(!sharedPreferences.getBoolean(Constants.IS_PROFILE_COMPLETED,false)){
            TextView completeProfilePopup = findViewById(R.id.complete_profile_popup);
            completeProfilePopup.setVisibility(View.VISIBLE);
        }
        else{
            // show banner ad only if the user has already completed his/her profile
            showBannerAd();
        }

        // referencing to the views
        TextView profilePicView = findViewById(R.id.profile_pic);
        final EditText userNameDisplay = findViewById(R.id.user_name);
        final EditText userEmailDisplay = findViewById(R.id.user_email);
        final EditText userAddressDisplay = findViewById(R.id.user_address);
        final EditText userMobileDisplay = findViewById(R.id.user_mobile_number);
        FloatingActionButton editProfileDetail = findViewById(R.id.save_profile_details);


        userName = sharedPreferences.getString(Constants.USER_NAME, Constants.UNDEFINED);
        userEmail = sharedPreferences.getString(Constants.USER_EMAIL,Constants.UNDEFINED);
        userAddress = sharedPreferences.getString(Constants.USER_ADDRESS,Constants.UNDEFINED);
        userMobile = sharedPreferences.getString(Constants.USER_PHONE_NUMBER,Constants.UNDEFINED);
        final Boolean isGoogleSignedIn = sharedPreferences.getBoolean(Constants.IS_GMAIL_SIGNED_IN,false);
        final Boolean isPhoneSignedIn = sharedPreferences.getBoolean(Constants.IS_PHONE_SIGNED_IN,false);

        // check if either of userName, userEmail, userMobile or userAddress is "-", then put the hint of that textview as "-"
        if("-".equals(userName))
            userNameDisplay.setHint("-");
        else
            userNameDisplay.setText(userName);

        if("-".equals(userEmail))
            userEmailDisplay.setHint("-");
        else
            userEmailDisplay.setText(userEmail);

        if("-".equals(userMobile))
            userMobileDisplay.setHint("-");
        else
            userMobileDisplay.setText(userMobile);

        if("-".equals(userAddress))
            userAddressDisplay.setHint("-");
        else
            userAddressDisplay.setText(userAddress);



        // user can't edit info that they used for signing in
        if(isGoogleSignedIn){
            userEmailDisplay.setFocusable(false);
        }
        else if(isPhoneSignedIn){
            userMobileDisplay.setFocusable(false);
        }

        // finally show the profile pic
        profilePicView.setText(userName.toUpperCase().charAt(0) + "");


        // after the user fills in detail, update the user profile in server as well as locally
        editProfileDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check if internet connection is available or not
                if(!isNetworkAvailable()){
                    Toast.makeText(ProfileUpdateActivity.this, Constants.TRY_AGAIN_FAILURE, Toast.LENGTH_SHORT).show();
                    return;
                }

                // check for the validation of user name, user email, user mobile and address

                String newUserName = userNameDisplay.getText().toString().trim();
                if(newUserName.length() < 5 || newUserName.length() > 20){
                    userNameDisplay.setError("5 - 20 chars only");
                    userNameDisplay.requestFocus();
                    return;
                }

                String newAddress = userAddressDisplay.getText().toString().trim();
                if(newAddress.length() < 10 || newAddress.length() > 200){
                    userAddressDisplay.setError("10 - 200 chars only");
                    userAddressDisplay.requestFocus();
                    return;
                }

                String newMobileNumber = userMobileDisplay.getText().toString().trim();
                if(newMobileNumber.length() != 10){
                    userMobileDisplay.setError("Invalid Phone Number, length should be 10");
                    userMobileDisplay.requestFocus();
                    return;
                }

                String newEmail = userEmailDisplay.getText().toString().trim();
                if(!isValidEmail(newEmail)){
                    userEmailDisplay.setError("Invalid Email");
                    userEmailDisplay.requestFocus();
                    return;
                }


                // user entered values are checked
                pd.setMessage("Updating...");
                pd.show();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String modeOfSignIn;
                if(isGoogleSignedIn){
                    modeOfSignIn = Constants.GOOGLE_SIGNED_IN;
                }
                else if(isPhoneSignedIn){
                    modeOfSignIn = Constants.PHONE_SIGNED_IN;
                }
                else{
                    modeOfSignIn = Constants.FACEBOOK_SIGNED_IN;
                }

                // edit the profile data locally
                editor.putString(Constants.USER_NAME,newUserName);
                editor.putString(Constants.USER_PHONE_NUMBER,newMobileNumber);
                editor.putString(Constants.USER_EMAIL,newEmail);
                editor.putString(Constants.USER_ADDRESS,newAddress);
                editor.putBoolean(Constants.IS_PROFILE_COMPLETED,true);
                editor.apply();


                // edit the profile data in firebase database
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("donor_data").child(modeOfSignIn)
                        .child(userUid).child("user_info");

                UserInfo userInfo = new UserInfo(newUserName,newEmail,newMobileNumber,newAddress, Constants.YES);

                databaseReference.setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.cancel();

                        startActivity(new Intent(ProfileUpdateActivity.this, MainActivity.class));
                        ProfileUpdateActivity.this.finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileUpdateActivity.this, Constants.NETWORK_ERROR, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });


    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    // show banner ad section is defined here
    void showBannerAd(){
        AdView adView = new AdView(ProfileUpdateActivity.this, Constants.FACEBOOK_BANNER_AD_KEY, AdSize.BANNER_HEIGHT_50);

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
