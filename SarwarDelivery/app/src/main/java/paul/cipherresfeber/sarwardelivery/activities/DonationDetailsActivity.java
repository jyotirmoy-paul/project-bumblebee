package paul.cipherresfeber.sarwardelivery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import paul.cipherresfeber.sarwardelivery.R;
import paul.cipherresfeber.sarwardelivery.util.Constants;
import paul.cipherresfeber.sarwardelivery.models.DonationData;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

public class DonationDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_details);

        Intent intent = getIntent();
        final DonationData donationData = (DonationData) intent.getSerializableExtra("donationData");

        ImageView coverPhotoDisplay = findViewById(R.id.cover_image);
        TextView donorNameDisplay = findViewById(R.id.donor_name);
        TextView dateTimeDisplay = findViewById(R.id.date_time);
        TextView donationCategoryDisplay = findViewById(R.id.donation_category);
        TextView donationDescriptionDisplay = findViewById(R.id.donation_description);
        TextView donationOtherDetailDisplay = findViewById(R.id.donation_other_details);
        TextView donorAddressDisplay = findViewById(R.id.donor_address);
        TextView donorContactDisplay = findViewById(R.id.donor_contact_number);


        Glide.with(DonationDetailsActivity.this).load(Uri.parse(donationData.getDonationMainPhotoUrl())).into(coverPhotoDisplay);
        donorNameDisplay.setText(donationData.getDonorName());
        dateTimeDisplay.setText(donationData.getDateTime());
        donationCategoryDisplay.setText(donationData.getDonationCategory());
        donationDescriptionDisplay.setText(donationData.getDonationItemDescription());
        donationOtherDetailDisplay.setText(donationData.getDonationOtherDetails());
        donorAddressDisplay.setText(donationData.getDonationUserAddress());
        donorContactDisplay.setText(donationData.getDonorContactNumber());

        findViewById(R.id.mark_donation_as_collected).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                final ProgressDialog pd = new ProgressDialog(DonationDetailsActivity.this);
                pd.setMessage("Marking donation as collected...");
                pd.show();

                FirebaseDatabase.getInstance().getReference().child("donation_data_under_process").child(donationData.getDonationKey())
                        .child("donationStatus").setValue(Constants.COLLECTED).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.cancel();
                        Toast.makeText(DonationDetailsActivity.this, "Donation marked as collected!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.cancel();
                        Toast.makeText(DonationDetailsActivity.this,
                                "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}
