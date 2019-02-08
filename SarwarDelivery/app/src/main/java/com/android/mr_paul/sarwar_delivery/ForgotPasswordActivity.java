package com.android.mr_paul.sarwar_delivery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
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

import com.android.mr_paul.sarwar_delivery.UtilityPackage.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends AppCompatActivity {


    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private FirebaseAuth fbAuth;

    EditText userPhoneDisplay;
    Button sendOtpButton;
    EditText enterOtpDisplay;
    Button verifyOtpButton;
    TextView showPasswordDisplay;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        pd = new ProgressDialog(this);

        fbAuth = FirebaseAuth.getInstance();

        showPasswordDisplay = findViewById(R.id.show_user_password);
        verifyOtpButton = findViewById(R.id.verify_otp);
        enterOtpDisplay = findViewById(R.id.enter_otp);
        sendOtpButton = findViewById(R.id.send_otp);
        userPhoneDisplay = findViewById(R.id.user_phone_number);

        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userPhone = userPhoneDisplay.getText().toString().trim();

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

                pd.setMessage("Please Wait...");
                pd.show();

                FirebaseAuth.getInstance().sendPasswordResetEmail(userPhone + Constants.EMAIL_DOMAIN).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        pd.cancel();

                        if(task.isSuccessful()){
                            setUpVerificationCallbacks();
                            PhoneAuthProvider.getInstance().verifyPhoneNumber("+91" + userPhone,60, TimeUnit.SECONDS, ForgotPasswordActivity.this, verificationCallbacks);

                            pd.setMessage("Sending OTP...");
                            pd.show();
                        }
                        else{
                            try{
                                throw task.getException();
                            }
                            catch (Exception e){

                                // show a dialog box, informing the user is not registered!

                                AlertDialog.Builder builder;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder = new AlertDialog.Builder(ForgotPasswordActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                                } else {
                                    builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                                }
                                builder.setTitle("You are not Registered!")
                                        .setMessage("Hey, your phone number is not registered with us, please check your number and try again")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.i("TAG","not a registered user");
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();

                            }
                        }
                    }
                });



            }
        });

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        Toast.makeText(ForgotPasswordActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();
                        pd.cancel();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

                        Toast.makeText(ForgotPasswordActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                        phoneVerificationId = verificationId;
                        pd.cancel();

                    }
                };
    }

    public void verifyCode() {


        String code = enterOtpDisplay.getText().toString().trim();

        if(code.isEmpty()){
            enterOtpDisplay.setError("Can't be empty!");
            enterOtpDisplay.requestFocus();
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

                            String uid = fbAuth.getCurrentUser().getUid();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                    .child(Constants.PHONE_SIGNED_IN).child(uid);

                            reference.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    String userPassword = dataSnapshot.getValue(String.class);

                                    // sign out the user
                                    fbAuth.signOut();
                                    showPasswordDisplay.setText(userPassword);
                                    pd.cancel();
                                    Toast.makeText(ForgotPasswordActivity.this,
                                            "Please retrieve your password", Toast.LENGTH_SHORT).show();

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


                        } else {

                            pd.cancel();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(ForgotPasswordActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

}
