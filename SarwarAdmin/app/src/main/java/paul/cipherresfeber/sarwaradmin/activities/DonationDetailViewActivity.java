package paul.cipherresfeber.sarwaradmin.activities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;


import paul.cipherresfeber.sarwaradmin.R;
import paul.cipherresfeber.sarwaradmin.models.DonationData;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DonationDetailViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_detail_view);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("donation_details")
                .child(getIntent().getStringExtra("donationKey"));

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                DonationData donationData = dataSnapshot.getValue(DonationData.class);

                ImageView coverPhotoDisplay = findViewById(R.id.cover_image);
                TextView donorNameDisplay = findViewById(R.id.donor_name);
                TextView dateTimeDisplay = findViewById(R.id.date_time);
                TextView donationCategoryDisplay = findViewById(R.id.donation_category);
                TextView donationDescriptionDisplay = findViewById(R.id.donation_description);
                TextView donationOtherDetailDisplay = findViewById(R.id.donation_other_details);
                TextView donorAddressDisplay = findViewById(R.id.donor_address);
                TextView donorContactDisplay = findViewById(R.id.donor_contact_number);


                Glide.with(DonationDetailViewActivity.this).load(Uri.parse(donationData.getDonationMainPhotoUrl())).into(coverPhotoDisplay);
                donorNameDisplay.setText(donationData.getDonorName());
                dateTimeDisplay.setText(donationData.getDateTime());
                donationCategoryDisplay.setText(donationData.getDonationCategory());
                donationDescriptionDisplay.setText(donationData.getDonationItemDescription());
                donationOtherDetailDisplay.setText(donationData.getDonationOtherDetails());
                donorAddressDisplay.setText(donationData.getDonationUserAddress());
                donorContactDisplay.setText(donationData.getDonorContactNumber());

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
