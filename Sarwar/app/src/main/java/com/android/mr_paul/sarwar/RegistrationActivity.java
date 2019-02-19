package com.android.mr_paul.sarwar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.mr_paul.sarwar.UtilityPackage.Constants;
import com.android.mr_paul.sarwar.UtilityPackage.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {


    String userPhone, userPassword, userName;

    ProgressDialog pd;

    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        pd = new ProgressDialog(this);

        fbAuth = FirebaseAuth.getInstance();

        Button sendOtp = findViewById(R.id.send_otp);

        // take in the user phone number and verify it using otp
        sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isNetworkAvailable()){
                    Toast.makeText(RegistrationActivity.this, Constants.TRY_AGAIN_FAILURE, Toast.LENGTH_SHORT).show();
                    return;
                }

                EditText userNameDisplay = findViewById(R.id.user_name);
                EditText userPhoneDisplay = findViewById(R.id.user_phone_number);
                EditText userPasswordDisplay = findViewById(R.id.user_password);

                userPhone = userPhoneDisplay.getText().toString().trim();
                userPassword = userPasswordDisplay.getText().toString().trim();
                userName = userNameDisplay.getText().toString().trim();

                if(userName.length() < 5 || userName.length() > 20){
                    userNameDisplay.setError("5 - 20 chars only");
                    userNameDisplay.requestFocus();
                    return;
                }


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

                setUpVerificationCallbacks();

                PhoneAuthProvider.getInstance().verifyPhoneNumber("+91" + userPhone,60, TimeUnit.SECONDS, RegistrationActivity.this, verificationCallbacks);

                pd.setMessage("Sending OTP...");
                pd.show();


            }
        });


        // verify otp button
        findViewById(R.id.verify_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    Toast.makeText(RegistrationActivity.this, Constants.TRY_AGAIN_FAILURE, Toast.LENGTH_SHORT).show();
                    return;
                }
                verifyCode();
            }
        });

    }

    private void setUpVerificationCallbacks() {

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        pd.cancel();
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(RegistrationActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();
                        pd.cancel();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

                        Toast.makeText(RegistrationActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                        phoneVerificationId = verificationId;
                        pd.cancel();

                    }
                };
    }

    public void verifyCode() {

        EditText codeText = findViewById(R.id.otp);

        String code = codeText.getText().toString().trim();

        if(code.isEmpty()){
            codeText.setError("Can't be empty!");
            codeText.requestFocus();
            return;
        }

        pd.setMessage("Please wait...");
        pd.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            // write the password to the database ----> in case the user forgets the password, he/she can retrieve the password
                            FirebaseDatabase.getInstance().getReference().child(Constants.PHONE_SIGNED_IN)
                                    .child(fbAuth.getCurrentUser().getUid()).child("password").setValue(userPassword);

                            // the phone number is validated, store it to the shared preference and create an email
                            fbAuth.signOut();

                            String virtualUserEmail = userPhone + Constants.EMAIL_DOMAIN;

                            // now create an account for email and password sign in
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(virtualUserEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {


                                    if(task.isSuccessful()){

                                        pd.cancel();

                                        UserInfo userInfo = new UserInfo(userName,"-",userPhone,"-",Constants.NO);

                                        FirebaseDatabase.getInstance().getReference().child("donor_data").child("phone_signed_in")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("user_info").setValue(userInfo);

                                        Map<String, Object> defaultData = new HashMap<>();
                                        defaultData.put("name", userName);
                                        defaultData.put("number","0");
                                        FirebaseFirestore.getInstance().document("donor_contribution_data/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .set(defaultData).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegistrationActivity.this, Constants.TRY_AGAIN_FAILURE, Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        AlertDialog.Builder builder;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            builder = new AlertDialog.Builder(RegistrationActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                                        } else {
                                            builder = new AlertDialog.Builder(RegistrationActivity.this);
                                        }
                                        builder.setTitle("Hurray Account created!")
                                                .setMessage("Congrats, your account has been created. You can now login using your credentials")
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        // redirect to the login page
                                                        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                                        RegistrationActivity.this.finish();

                                                    }
                                                })
                                                .setIcon(R.drawable.ic_account_created)
                                                .show();

                                    }
                                    else{

                                        try{
                                            throw task.getException();
                                        }
                                        catch (FirebaseAuthUserCollisionException e){

                                            // show dialog box, informing user about already existing user account
                                            AlertDialog.Builder builder;
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                builder = new AlertDialog.Builder(RegistrationActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                                            } else {
                                                builder = new AlertDialog.Builder(RegistrationActivity.this);
                                            }
                                            builder.setTitle("Account Already Exists")
                                                    .setMessage("Hey! Seems like you already have an account, try logging in.")
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            // redirect to the login page
                                                            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                                            RegistrationActivity.this.finish();

                                                        }
                                                    })
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .show();


                                        }
                                        catch (Exception e){
                                            Toast.makeText(RegistrationActivity.this,
                                                    "Something went wrong!", Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                }
                            });



                        } else {

                            pd.cancel();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(RegistrationActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
