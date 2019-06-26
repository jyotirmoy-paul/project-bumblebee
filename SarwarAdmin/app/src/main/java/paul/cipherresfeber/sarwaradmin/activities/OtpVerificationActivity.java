package paul.cipherresfeber.sarwaradmin.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import paul.cipherresfeber.sarwaradmin.R;
import paul.cipherresfeber.sarwaradmin.util.Constants;
import paul.cipherresfeber.sarwaradmin.models.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class OtpVerificationActivity extends AppCompatActivity {

    private String phoneVerificationId;
    ProgressDialog pd;
    FirebaseAuth fbAuth;

    String userName, userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        pd = new ProgressDialog(OtpVerificationActivity.this);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);

        fbAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        phoneVerificationId = intent.getStringExtra("otp");
        userName = intent.getStringExtra("userName");
        userPhone = intent.getStringExtra("userPhone");


        TextView textView = findViewById(R.id.phone_number);
        textView.setText("OTP sent to +91 " + userPhone);

    }

    public void verifyCode(View V) {

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

                        pd.cancel();

                        if (task.isSuccessful()) {
                            // write to the shared preference and firebase database

                            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(Constants.USER_NAME, userName);
                            editor.putString(Constants.USER_PHONE_NUMBER, userPhone);
                            editor.putBoolean(Constants.IS_USER_LOGGED_IN, true);
                            editor.apply();

                            FirebaseDatabase.getInstance().getReference().child("admin_data")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("user_info")
                                    .setValue(new UserInfo(userName,userPhone, sharedPreferences.getString(Constants.FIREBASE_TOKEN, Constants.UNDEFINED),
                                            FirebaseAuth.getInstance().getCurrentUser().getUid()));

                            // finally call intent to the main activity
                            startActivity(new Intent(OtpVerificationActivity.this, MainActivity.class));
                            OtpVerificationActivity.this.finish();

                        }
                        else {
                            Toast.makeText(OtpVerificationActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
