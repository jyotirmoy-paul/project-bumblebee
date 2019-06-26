package com.android.mr_paul.sarwar.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mr_paul.sarwar.R;
import com.android.mr_paul.sarwar.util.Constants;
import com.android.mr_paul.sarwar.models.UserInfo;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    CallbackManager mCallbackManager;

    ProgressDialog pd;
    SharedPreferences sharedPreferences;
    FirebaseAuth firebaseAuth;
    int GOOGLE_SIGN_IN = 1;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // if user is logged in directly go to the main activity
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        if(sharedPreferences.getBoolean(Constants.IS_USER_LOGGED_IN,false)){
            startActivity(new Intent(LoginActivity.this, SplashScreenActivity.class));
            LoginActivity.this.finish();
        }

        ImageView googleSignIn = findViewById(R.id.google_login);
        Button phoneSignIn = findViewById(R.id.sign_up);
        TextView forgotPassword = findViewById(R.id.forgot_password);

        pd = new ProgressDialog(LoginActivity.this);

        firebaseAuth = FirebaseAuth.getInstance();


        // handle google sign in
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Please Wait...");
                pd.show();
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
            }
        });


        //phoneNumber sign in
        phoneSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText userPhoneDisplay = findViewById(R.id.user_phone_number);
                EditText userPasswordDisplay = findViewById(R.id.user_password);

                String userPhone = userPhoneDisplay.getText().toString().trim();
                String userPassword = userPasswordDisplay.getText().toString().trim();


                if(userPhone.length() != 10){
                    userPhoneDisplay.setError("Invalid Phone Number");
                    userPhoneDisplay.requestFocus();
                    return;
                }

                try{
                    Long.parseLong(userPhone);
                }
                catch (Exception e){
                    userPhoneDisplay.setError("Invalid Phone Number");
                    userPhoneDisplay.requestFocus();
                    return;
                }

                if(userPassword.length() < 6 || userPassword.length() > 15){
                    userPasswordDisplay.setError("6 - 15 chars only");
                    userPasswordDisplay.requestFocus();
                    return;
                }

                pd.setMessage("Please Wait...");
                pd.show();

                // authenticate the details

                String virtualEmail = userPhone + Constants.EMAIL_DOMAIN;

                FirebaseAuth.getInstance().signInWithEmailAndPassword(virtualEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            // find all the user related details stored in the database, and write to the shared preference
                            updateSharedPreference(FirebaseAuth.getInstance().getCurrentUser().getUid(), Constants.PHONE_SIGNED_IN);

                        }
                        else{
                            pd.cancel();
                            Toast.makeText(LoginActivity.this, "Invalid credentials!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });

        // handle create an account
        findViewById(R.id.create_an_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                LoginActivity.this.finish();
            }
        });


        // for google sign Im
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        // set on click listener for forgetting password
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });


        // handling facebook Login
        mCallbackManager = CallbackManager.Factory.create();
        ImageView facebookLogin = findViewById(R.id.facebook_login);

        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pd.setMessage("Please wait...");
                pd.show();

                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this,
                        Arrays.asList("email","public_profile"));
            }
        });

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this,
                        "Facebook Login Cancelled", Toast.LENGTH_SHORT).show();
                pd.cancel();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this,
                        "Something went wrong!", Toast.LENGTH_SHORT).show();
                pd.cancel();
            }
        });

        TextView privacyPolicyView = findViewById(R.id.privacy_policy);
        privacyPolicyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://sarwar-09-12-2018.firebaseapp.com/"));
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                pd.cancel();
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }

        mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();

                            if(isNew){
                                UserInfo userInfo = new UserInfo(firebaseAuth.getCurrentUser().getDisplayName(),
                                        firebaseAuth.getCurrentUser().getEmail(),"-","-",Constants.NO);

                                FirebaseDatabase.getInstance().getReference().child("donor_data").child(Constants.GOOGLE_SIGNED_IN)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("user_info").setValue(userInfo);


                                Map<String, Object> defaultData = new HashMap<>();
                                defaultData.put("name", firebaseAuth.getCurrentUser().getDisplayName());
                                defaultData.put("number", 0);
                                FirebaseFirestore.getInstance().document("donor_contribution_data/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .set(defaultData);

                            }

                            updateSharedPreference(firebaseAuth.getCurrentUser().getUid(),Constants.GOOGLE_SIGNED_IN);

                        }
                        else {
                            pd.cancel();

                            // display an error message to the user
                            Toast.makeText(LoginActivity.this, "Sign in failed!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    // handling facebook login
    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();

                            if(isNew){
                                UserInfo userInfo = new UserInfo(firebaseAuth.getCurrentUser().getDisplayName(),
                                        "-","-","-",Constants.NO);

                                FirebaseDatabase.getInstance().getReference().child("donor_data").child(Constants.FACEBOOK_SIGNED_IN)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("user_info").setValue(userInfo);


                                Map<String, Object> defaultData = new HashMap<>();
                                defaultData.put("name", firebaseAuth.getCurrentUser().getDisplayName());
                                defaultData.put("number","0");
                                FirebaseFirestore.getInstance().document("donor_contribution_data/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .set(defaultData);

                            }

                            updateSharedPreference(firebaseAuth.getCurrentUser().getUid(),Constants.FACEBOOK_SIGNED_IN);

                        }
                        else {

                            pd.cancel();
                            Toast.makeText(LoginActivity.this,
                                    "Facebook Login Failed!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


    // this method is used to update the user related info to the shared preference
    public void updateSharedPreference(String uid, final String mode_of_sign_in){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("donor_data").child(mode_of_sign_in)
                .child(uid);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();

                editor.putString(Constants.USER_NAME, userInfo.getName());
                editor.putString(Constants.USER_EMAIL, userInfo.getEmail());
                editor.putString(Constants.USER_PHONE_NUMBER, userInfo.getPhoneNumber());
                editor.putString(Constants.USER_ADDRESS, userInfo.getAddress());

                if(Constants.YES.equals(userInfo.getIsProfileCompleted())){
                    editor.putBoolean(Constants.IS_PROFILE_COMPLETED,true);
                }
                else{
                    editor.putBoolean(Constants.IS_PROFILE_COMPLETED,false);
                }

                editor.putBoolean(Constants.IS_USER_LOGGED_IN,true);


                if(mode_of_sign_in.equals(Constants.GOOGLE_SIGNED_IN)){
                    editor.putBoolean(Constants.IS_GMAIL_SIGNED_IN,true);
                    editor.putBoolean(Constants.IS_PHONE_SIGNED_IN,false);
                    editor.putBoolean(Constants.IS_FACEBOOK_SIGNED_IN,false);
                }
                else if(mode_of_sign_in.equals(Constants.PHONE_SIGNED_IN)){
                    editor.putBoolean(Constants.IS_GMAIL_SIGNED_IN,false);
                    editor.putBoolean(Constants.IS_PHONE_SIGNED_IN,true);
                    editor.putBoolean(Constants.IS_FACEBOOK_SIGNED_IN,false);
                }
                else{
                    editor.putBoolean(Constants.IS_GMAIL_SIGNED_IN,false);
                    editor.putBoolean(Constants.IS_PHONE_SIGNED_IN,false);
                    editor.putBoolean(Constants.IS_FACEBOOK_SIGNED_IN,true);
                }

                editor.apply();

                pd.cancel();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                LoginActivity.this.finish();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}

