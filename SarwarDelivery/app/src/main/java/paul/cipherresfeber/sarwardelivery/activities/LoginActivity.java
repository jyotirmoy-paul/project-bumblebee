package paul.cipherresfeber.sarwardelivery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import paul.cipherresfeber.sarwardelivery.R;
import paul.cipherresfeber.sarwardelivery.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import paul.cipherresfeber.sarwardelivery.models.UserInfo;

public class LoginActivity extends AppCompatActivity {

    ProgressDialog pd;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // if user is logged in directly go to the main activity
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE);

        if(sharedPreferences.getBoolean(Constants.IS_USER_LOGGED_IN,false)){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            LoginActivity.this.finish();
            return;
        }

        pd = new ProgressDialog(LoginActivity.this);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);


        // reference to the views
        Button phoneSignIn = findViewById(R.id.sign_up_btn);
        TextView forgotPassword = findViewById(R.id.forgot_password);

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
                FirebaseAuth.getInstance().signInWithEmailAndPassword(virtualEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // find all the user related details stored in the database, and write to the shared preference
                            updateSharedPreference(FirebaseAuth.getInstance().getCurrentUser().getUid());
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
        findViewById(R.id.create_an_account_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                LoginActivity.this.finish();
            }
        });

        // handle forgot password
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

    }

    // this method is used to update the user related info to the shared preference
    public void updateSharedPreference(String uid){

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("delivery_agent_data").child(uid);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                final UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();

                editor.putString(Constants.USER_NAME, userInfo.getName());
                editor.putString(Constants.USER_PHONE_NUMBER, userInfo.getPhoneNumber());
                editor.putString(Constants.USER_AADHAR_CARD_PHOTO_LINK, userInfo.getAadharCardLink());


                if(Constants.YES.equals(userInfo.getIsProfileCompleted())){
                    editor.putBoolean(Constants.IS_PROFILE_COMPLETED, true);
                }
                else{
                    editor.putBoolean(Constants.IS_PROFILE_COMPLETED, false);
                }

                editor.putBoolean(Constants.IS_USER_LOGGED_IN, true);
                editor.apply();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        pd.cancel();
                        pd.dismiss();

                       if(Constants.YES.equals(userInfo.getIsProfileCompleted())){
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            LoginActivity.this.finish();
                       }
                       else{
                           startActivity(new Intent(LoginActivity.this, ProfileUpdateActivity.class));
                           LoginActivity.this.finish();
                       }

                    }
                }, 500); // wait for 500 ms before changing activity

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
