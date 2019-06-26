package paul.cipherresfeber.sarwardelivery.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import paul.cipherresfeber.sarwardelivery.R;
import paul.cipherresfeber.sarwardelivery.util.Constants;
import paul.cipherresfeber.sarwardelivery.models.UserInfo;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.Date;

public class ProfileUpdateActivity extends AppCompatActivity {

    String userName, userMobile, userAadharCardLink;
    Boolean isProfileCompleted;
    ProgressDialog pd;

    ImageView aadharCardView;
    LinearLayout aadharCardReqView;

    private int REQUEST_CODE = 100;

    Bitmap userAadharCardPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        // reference to the views
        TextView profilePicView = findViewById(R.id.profile_pic);
        final EditText userNameDisplay = findViewById(R.id.user_name);
        final EditText userMobileDisplay = findViewById(R.id.user_mobile_number);
        FloatingActionButton saveProfileDetail = findViewById(R.id.save_profile_details);
        aadharCardView = findViewById(R.id.aadhar_card_view);
        aadharCardReqView = findViewById(R.id.aadhar_card_req_view);

        pd = new ProgressDialog(ProfileUpdateActivity.this);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);


        final SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE);

        userName = sharedPreferences.getString(Constants.USER_NAME,Constants.UNDEFINED);
        userMobile = sharedPreferences.getString(Constants.USER_PHONE_NUMBER,Constants.UNDEFINED);
        userAadharCardLink = sharedPreferences.getString(Constants.USER_AADHAR_CARD_PHOTO_LINK, "-");
        isProfileCompleted = sharedPreferences.getBoolean(Constants.IS_PROFILE_COMPLETED,false);

        if(!isProfileCompleted){
            TextView completeProfilePopup = findViewById(R.id.complete_profile_popup);
            completeProfilePopup.setVisibility(View.VISIBLE);
        }
        else{
            // set on click listener on aadharImage View
            aadharCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // let the user choose his/her aadhar photo
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                    startActivityForResult(Intent.createChooser(intent,"Complete Action Using"), REQUEST_CODE);
                }
            });
        }

        userNameDisplay.setText(userName);
        userMobileDisplay.setText(userMobile);

        userMobileDisplay.setFocusable(false); // to ensure the delivery agent can't change phone number

        if("-".equals(userAadharCardLink)){
            // then show the aadhar card required view
            aadharCardReqView.setVisibility(View.VISIBLE);
            aadharCardView.setVisibility(View.GONE);

            aadharCardReqView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // let the user choose his/her aadhar photo
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                    startActivityForResult(Intent.createChooser(intent,"Complete Action Using"), REQUEST_CODE);
                }
            });

        }
        else{
            // show the aadhar card to the user
            aadharCardReqView.setVisibility(View.GONE);
            aadharCardView.setVisibility(View.VISIBLE);

            String aadharCardLink = sharedPreferences.getString(Constants.USER_AADHAR_CARD_PHOTO_LINK,Constants.UNDEFINED);
            Glide.with(this).load(Uri.parse(aadharCardLink)).into(aadharCardView);
        }

        profilePicView.setText(userName.toUpperCase().charAt(0) + "");

        // to update user info
        saveProfileDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String newUserName = userNameDisplay.getText().toString().trim();
                if(newUserName.length() < 5 || newUserName.length() > 20){
                    userNameDisplay.setError("5 - 20 chars only");
                    userNameDisplay.requestFocus();
                    return;
                }

                if("-".equals(userAadharCardLink)){
                    // means aadhar card is not present, show a toast message informing the user to upload aadhar card
                    Toast.makeText(ProfileUpdateActivity.this, "Please upload your aadhar card to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // start the updating process
                pd.setMessage("Updating...");
                pd.show();


                final String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("delivery_agent_data").child(userUID)
                        .child("user_info");

                // upload the aadhar card photo is necessary (for the first time)
                if(!isProfileCompleted){

                    final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("delivery_agent_aadhar_card")
                            .child(userUID);

                    // upload the aadhar card photo to firebase storage

                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    userAadharCardPhoto.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                    UploadTask uploadTask = storageReference.putBytes(baos.toByteArray());

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

                                SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
                                editor.putBoolean(Constants.IS_PROFILE_COMPLETED, true);
                                editor.putString(Constants.USER_AADHAR_CARD_PHOTO_LINK,task.getResult().toString());
                                editor.apply();

                                DateFormat format = new SimpleDateFormat("dd MMM yyyy");

                                // finally upload to the database
                                databaseReference.setValue(new UserInfo(userName,userMobile,task.getResult().toString(),Constants.YES,format.format(new Date()),Constants.NO));

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        pd.cancel();
                                        pd.dismiss();

                                        startActivity(new Intent(ProfileUpdateActivity.this, MainActivity.class));
                                        ProfileUpdateActivity.this.finish();

                                    }
                                }, 500); // wait for 500 ms before changing activity


                            }
                        }
                    });

                }
                else{

                    // check if the use has selected a new aadhar photo
                    if("content://".equals(userAadharCardLink.substring(0,10))){
                        // then the user has chosen a new photo, upload it

                        // alert the user about re-verification
                        AlertDialog.Builder builder;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(ProfileUpdateActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(ProfileUpdateActivity.this);
                        }

                        builder.setTitle("Update Aadhar Card?")
                                .setMessage("Are you sure, you want to change your aadhar picture, this might require a re-verification from the admin?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("delivery_agent_aadhar_card")
                                                .child(userUID);

                                        // upload the aadhar card photo to firebase storage

                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        userAadharCardPhoto.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                                        UploadTask uploadTask = storageReference.putBytes(baos.toByteArray());

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

                                                    SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
                                                    editor.putString(Constants.USER_AADHAR_CARD_PHOTO_LINK,task.getResult().toString());
                                                    editor.putString(Constants.USER_NAME, newUserName);
                                                    editor.apply();

                                                    databaseReference.child("name").setValue(newUserName);
                                                    databaseReference.child("aadharCardLink").setValue(task.getResult().toString());

                                                    // if a user upload his/her aadhar card, then change his verification status
                                                    databaseReference.child("isVerified").setValue(Constants.NO);

                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            pd.cancel();

                                                            startActivity(new Intent(ProfileUpdateActivity.this, MainActivity.class));
                                                            ProfileUpdateActivity.this.finish();

                                                        }
                                                    }, 500); // wait for 500 ms before changing activity

                                                }
                                            }
                                        });

                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                    }
                    else{

                        // just update the name
                        SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
                        editor.putString(Constants.USER_NAME, newUserName);
                        editor.apply();

                        databaseReference.child("name").setValue(newUserName).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                pd.cancel();
                                startActivity(new Intent(ProfileUpdateActivity.this, MainActivity.class));
                                ProfileUpdateActivity.this.finish();
                            }
                        });

                    }
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            Uri aadharPhoto = data.getData();
            userAadharCardLink = aadharPhoto.toString();

            try{
                InputStream inputStream = getContentResolver().openInputStream(aadharPhoto);
                Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                userAadharCardPhoto = getResizedBitmap(bmp,1000,500);
                aadharCardView.setImageBitmap(userAadharCardPhoto);

                aadharCardView.setVisibility(View.VISIBLE);
                aadharCardReqView.setVisibility(View.GONE);

                aadharCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // let the user choose his/her aadhar photo
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/jpeg");
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                        startActivityForResult(Intent.createChooser(intent,"Complete Action Using"), REQUEST_CODE);
                    }
                });


            }
            catch (Exception e){
                Log.i("Photo -->","Image not Found");
            }

        }
        else{
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

    }

    // important method
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
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

}
