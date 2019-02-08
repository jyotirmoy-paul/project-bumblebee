package com.android.mr_paul.sarwar_admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mr_paul.sarwar_admin.UtilityPackage.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    ProgressDialog pd;
    String userName, userPhone;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).getBoolean(Constants.IS_USER_LOGGED_IN, false)){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            LoginActivity.this.finish();
        }

        pd = new ProgressDialog(LoginActivity.this);

        findViewById(R.id.verify_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText userPhoneDisplay = findViewById(R.id.user_phone_number);
                EditText userNameDisplay = findViewById(R.id.user_name);

                userName = userNameDisplay.getText().toString().trim();
                userPhone = userPhoneDisplay.getText().toString().trim();

                // check for the validity of phone number
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

                // check for the validity of user name
                if(userName.length() < 5 || userName.length() > 20){
                    userNameDisplay.setError("5 - 20 chars only");
                    userNameDisplay.requestFocus();
                    return;
                }

                setUpVerificationCallbacks();

                PhoneAuthProvider.getInstance().verifyPhoneNumber("+91" + userPhone,60, TimeUnit.SECONDS, LoginActivity.this, verificationCallbacks);

                pd.setMessage("Sending OTP...");
                pd.show();
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
                        Toast.makeText(LoginActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();
                        pd.cancel();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

                        pd.cancel();

                        // open the otp verification activity
                        Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("otp",verificationId);
                        intent.putExtra("userName", userName);
                        intent.putExtra("userPhone", userPhone);
                        startActivity(intent);

                    }
                };
    }

}
