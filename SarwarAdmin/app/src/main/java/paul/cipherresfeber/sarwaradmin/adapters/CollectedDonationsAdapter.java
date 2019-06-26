package paul.cipherresfeber.sarwaradmin.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import paul.cipherresfeber.sarwaradmin.R;
import paul.cipherresfeber.sarwaradmin.models.AvailableDonationDetailClass;
import paul.cipherresfeber.sarwaradmin.util.Constants;


public class CollectedDonationsAdapter extends ArrayAdapter<AvailableDonationDetailClass> {

    private AvailableDonationDetailClass data;

    public CollectedDonationsAdapter(Context c, int resources, ArrayList<AvailableDonationDetailClass> list){
        super(c, resources, list);
    }

    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null){
            convertView = inflater.inflate(R.layout.collected_donations_layout, parent, false);
        }

        TextView deliveryAgentNameDisplay = convertView.findViewById(R.id.deliveryAgentNameDisplay);
        TextView deliveryAgentNumberDisplay = convertView.findViewById(R.id.deliveryAgentContactDisplay);
        TextView donationCategoryDisplay = convertView.findViewById(R.id.donationCategoryDisplay);
        ImageView donationCoverPhotoDisplay = convertView.findViewById(R.id.donation_cover_photo);

        TextView donationStatusDisplay = convertView.findViewById(R.id.donation_status);
        Button deleteDonationDetailsBtn = convertView.findViewById(R.id.delete_donation_details_btn);

        CardView parentLayout = convertView.findViewById(R.id.parent_layout);

        data = getItem(position);

        deliveryAgentNameDisplay.setText(data.getDeliveryAgentName());
        deliveryAgentNumberDisplay.setText(data.getDeliveryAgentNumber());
        donationCategoryDisplay.setText(data.getDonationCategory());

        // show the donation cover photo
        Glide.with(getContext()).load(Uri.parse(data.getDonationMainPhoto())).into(donationCoverPhotoDisplay);

        String donationStatus = data.getDonationStatus();
        if(Constants.COLLECTED.equals(donationStatus)){
            // donation has been collected, change the text and show the delete donation button
            donationStatusDisplay.setText("Collected");
            donationStatusDisplay.setTextColor(getContext().getResources().getColor(R.color.deepGreen));
            parentLayout.setCardBackgroundColor(getContext().getResources().getColor(R.color.lightGreen));

            deleteDonationDetailsBtn.setVisibility(View.VISIBLE);
            deleteDonationDetailsBtn.setOnClickListener(listener);
        }
        else{
            // means the donation is still to be picked up!
            donationStatusDisplay.setText("Waiting for pickup....");
            donationStatusDisplay.setTextColor(getContext().getResources().getColor(R.color.deepRed));
            parentLayout.setCardBackgroundColor(getContext().getResources().getColor(R.color.lightRed));
            deleteDonationDetailsBtn.setVisibility(View.GONE);
        }



        return convertView;

    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }

            builder.setTitle("Confirm Deletion?")
                    .setMessage("Are you sure, you want to confirm the donation and delete all the related data?\n(This can't be undone)")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            final ProgressDialog pd = new ProgressDialog(getContext());
                            pd.setMessage("Deleting donation data...");
                            pd.show();

                            // write to the firestore about donor's contribution
                            writeToFirestore(data.getDonorUID());

                            FirebaseStorage.getInstance().getReferenceFromUrl(data.getDonationMainPhoto()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    // after the deletion of main cover photo, delete all the instances of the donation data available in all three places
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                                    // remove from approved donation requests
                                    databaseReference.child("approved_donation_requests").child(data.getDeliveryAgentUID())
                                            .child(data.getDonationKey()).removeValue();
                                    // remove from donation data under_process
                                    databaseReference.child("donation_data_under_process").child(data.getDonationKey()).removeValue();

                                    // remove donation data from donation_details section
                                    databaseReference.child("donation_details").child(data.getDonationKey()).removeValue();

                                    pd.cancel();

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
    };

    private void writeToFirestore(String uid){

        final DocumentReference db = FirebaseFirestore.getInstance().document("donor_contribution_data/" + uid);
        db.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Map<String, Object> prevData = documentSnapshot.getData();
                long number = (long) prevData.get("number");

                Map<String, Object> newData = new HashMap<>();
                newData.put("name", data.getDonorName());
                newData.put("number", number + 1); // increment the donation number by one

                db.update(newData);

            }
        });
    }
}
