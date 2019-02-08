package com.android.mr_paul.sarwar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mr_paul.sarwar.UtilityPackage.Constants;
import com.android.mr_paul.sarwar.UtilityPackage.DonationData;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.nio.charset.MalformedInputException;

public class DonationActivity extends AppCompatActivity {

    Uri mainPhotoUri = null;
    int REQUEST_CODE = 100;

    ImageView uploadAPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        showBannerAd();

        View materialDonationLayout = findViewById(R.id.material_donation_layout);
        View monetaryDonationLayout = findViewById(R.id.monetary_donation_layout);

        Intent intent = getIntent();
        String categoryOfDonation = intent.getStringExtra(Constants.ITEM_NAME);
        int categoryResourceId = intent.getIntExtra(Constants.ITEM_ID,-1);

        // initial informative views -- referencing
        TextView donationCategoryName = findViewById(R.id.donation_category);
        TextView donationCategoryDetail = findViewById(R.id.donation_category_detail);
        ImageView donationCategoryPicture = findViewById(R.id.donation_category_pic);

        donationCategoryDetail.setText(getDonationCategoryDetail(categoryResourceId));
        donationCategoryName.setText(categoryOfDonation);
        donationCategoryPicture.setImageResource(categoryResourceId);

        if(categoryResourceId == R.drawable.money_graphic_asset){
            monetaryDonationLayout.setVisibility(View.VISIBLE);
            materialDonationLayout.setVisibility(View.GONE);
        }
        else{
            materialDonationLayout.setVisibility(View.VISIBLE);
            monetaryDonationLayout.setVisibility(View.GONE);

            runMaterialDonationCodes(materialDonationLayout, categoryOfDonation);

        }
    }

    private void runMaterialDonationCodes(View view, final String categoryOfDonation){

        final EditText userContactNumber = view.findViewById(R.id.contact_number);
        final EditText donationDescriptionInput = view.findViewById(R.id.donation_description);
        final EditText donationWorthInput = view.findViewById(R.id.donation_worth);
        final EditText otherDetailInput = view.findViewById(R.id.other_detail);
        uploadAPhoto = view.findViewById(R.id.upload_a_photo);
        Button sendDonationDetails = view.findViewById(R.id.send_donation_details);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE);


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // put the default contact number in the UI, user can change it with his/her latest contact number
        String userNumber = firebaseUser.getPhoneNumber();
        if(userNumber != null && !userNumber.isEmpty()){
            userContactNumber.setText(userNumber);
        }
        else{
            userContactNumber.setText(sharedPreferences.getString(Constants.USER_PHONE_NUMBER,Constants.UNDEFINED));
        }

        // click a photo
        uploadAPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Complete Action Using"),REQUEST_CODE);
            }
        });

        // send donation button
        sendDonationDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userNumber = userContactNumber.getText().toString().trim();
                String donationDescription = donationDescriptionInput.getText().toString().trim();
                String donationWorth = donationWorthInput.getText().toString().trim();
                String otherDetail = otherDetailInput.getText().toString().trim();

                if(userNumber.length() != 10){
                    userContactNumber.setError("Invalid Number");
                    userContactNumber.requestFocus();
                    return;
                }

                try{
                    Long.parseLong(userNumber);
                }
                catch (Exception e){
                    userContactNumber.setError("Invalid Number");
                    userContactNumber.requestFocus();
                    return;
                }

                if(donationDescription.isEmpty()){
                    donationDescription = "---";
                }

                if(donationWorth.isEmpty()){
                    donationWorth = "---";
                }

                if(otherDetail.isEmpty()){
                    otherDetail = "---";
                }

                if(mainPhotoUri == null){
                    Toast.makeText(getApplicationContext(), "A photo is mandatory!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(DonationActivity.this, DonationDataPostingActivity.class);
                intent.putExtra(Constants.DONATION_DESCRIPTION,donationDescription);
                intent.putExtra(Constants.DONATION_USER_NUMBER,userNumber);
                intent.putExtra(Constants.DONATION_WORTH,donationWorth);
                intent.putExtra(Constants.DONATION_OTHER_DETAIL,otherDetail);
                intent.putExtra(Constants.DONATION_PHOTO_URI,mainPhotoUri.toString());
                intent.putExtra(Constants.DONATION_CATEGORY,categoryOfDonation);

                startActivity(intent);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            mainPhotoUri = data.getData();

            try{
                InputStream inputStream = getContentResolver().openInputStream(mainPhotoUri);
                Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                bmp = getResizedBitmap(bmp,500,500);
                uploadAPhoto.setImageBitmap(bmp);

            }
            catch (Exception e){
                Log.i("Photo -->","Image not Found");
            }

        }
        else{
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

    }

    private String getDonationCategoryDetail(int resourceId){

        String detail;

        switch (resourceId){

            case R.drawable.drinks_graphic_asset:
                detail = "Donate packaged water bottle, cold drinks, fruit juice or any other such items";
                break;
            case R.drawable.electronics_graphic_asset:
                detail = "Donate your old TV, electronic kettle, iron, extension chords or any electronic gadgets";
                break;
            case R.drawable.food_graphic_asset:
                detail = "Donate surplus cooked food or dry food items, excess veggies or snacks";
                break;
            case R.drawable.education_graphic_asset:
                detail = "Donate your old books, color box, pencil, pen, bag or any other stationery item";
                break;
            case R.drawable.cloths_graphic_asset:
                detail = "Donate your old clothes, blanket, mosquito net, shoes, sweater, anything";
                break;
            case R.drawable.fruits_graphic_asset:
                detail = "Donate excess fruits or veggies before they get wasted";
                break;
            case R.drawable.money_graphic_asset:
                detail = "Apart from materialistic donations, help poor families with monetary support";
                break;
            case R.drawable.other_graphic_asset:
                detail = "You have something else to donate? Feel free to uplift their life in your own way";
                break;
            default:
                detail = Constants.UNDEFINED;
                break;
        }

        return detail;

    }

    // important utility function
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

    // show banner ad section is defined here
    void showBannerAd(){
        AdView adView = new AdView(DonationActivity.this, Constants.FACEBOOK_BANNER_AD_KEY, AdSize.BANNER_HEIGHT_50);

        LinearLayout adContainer = findViewById(R.id.banner_container);
        adContainer.addView(adView);

        adView.loadAd();

    }

}
