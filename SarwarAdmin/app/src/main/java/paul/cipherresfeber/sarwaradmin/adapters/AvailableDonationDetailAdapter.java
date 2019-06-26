package paul.cipherresfeber.sarwaradmin.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import paul.cipherresfeber.sarwaradmin.R;
import paul.cipherresfeber.sarwaradmin.models.AvailableDonationDetailClass;
import paul.cipherresfeber.sarwaradmin.util.Constants;

public class AvailableDonationDetailAdapter extends ArrayAdapter<AvailableDonationDetailClass> {

    public AvailableDonationDetailAdapter(Context c, int resource, ArrayList<AvailableDonationDetailClass> list){
        super(c, resource, list);
    }



    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            convertView = inflater.inflate(R.layout.available_donation_detail_layout, parent, false);
        }

        // reference to the views
        final LinearLayout parentLayout = convertView.findViewById(R.id.parent_layout);
        final LinearLayout buttonParentLayout = convertView.findViewById(R.id.button_parent_layout);

        ImageView categoryImageView = convertView.findViewById(R.id.category_image);

        TextView donorNameDisplay = convertView.findViewById(R.id.donorNameDisplay);
        TextView donorContactDisplay = convertView.findViewById(R.id.donorNumerDisplay);

        TextView agentNameDisplay = convertView.findViewById(R.id.deliveryAgentNameDisplay);
        TextView agentNumberDisplay = convertView.findViewById(R.id.deliveryAgentNumberDisplay);

        TextView estimatedDistance = convertView.findViewById(R.id.estimated_distance);

        Button approveBtn = convertView.findViewById(R.id.approve_btn);
        Button rejectBtn = convertView.findViewById(R.id.reject_btn);

        ImageView mainDonationPhoto = convertView.findViewById(R.id.main_donation_photo);

        final TextView confirmationStatus = convertView.findViewById(R.id.confirmation_status);

        final AvailableDonationDetailClass data = getItem(position);

        // method to set the referenced category image for the donation category
        setCategoryImage(categoryImageView, data.getDonationCategory());

        donorNameDisplay.setText(data.getDonorName());
        donorContactDisplay.setText(data.getDonorContactNumber());

        agentNameDisplay.setText(data.getDeliveryAgentName());
        agentNumberDisplay.setText(data.getDeliveryAgentNumber());

        double distance = BigDecimal.valueOf(data.getDistanceInKm()).setScale(5, RoundingMode.HALF_UP).doubleValue();
        estimatedDistance.setText("Estimated Distance: " + distance*1000 + " m"); // show distance in m

        if(Constants.YES.equals(data.getDonationConfirm())){
            // then color the background light green and remove the confirmations button
            parentLayout.setBackgroundColor(getContext().getResources().getColor(R.color.lightGreen));
            buttonParentLayout.setVisibility(View.GONE);
        }

        if(Constants.YES.equals(data.getDonationConfirm())){
            // change ui if donation status is confirmed
            parentLayout.setBackgroundColor(getContext().getResources().getColor(R.color.lightGreen));
            buttonParentLayout.setVisibility(View.GONE);
            confirmationStatus.setText(Constants.APPROVED);
            confirmationStatus.setVisibility(View.VISIBLE);
            confirmationStatus.setTextColor(getContext().getResources().getColor(R.color.deepGreen));
        }
        else if(Constants.NO.equals(data.getDonationConfirm())){
            // change ui if donation been rejected!
            parentLayout.setBackgroundColor(getContext().getResources().getColor(R.color.lightRed));
            buttonParentLayout.setVisibility(View.GONE);
            confirmationStatus.setText(Constants.REJECTED);
            confirmationStatus.setVisibility(View.VISIBLE);
            confirmationStatus.setTextColor(getContext().getResources().getColor(R.color.deepRed));
        }
        else{
            parentLayout.setVisibility(View.VISIBLE);
            parentLayout.setBackgroundColor(getContext().getResources().getColor(R.color.lightRed));
            confirmationStatus.setVisibility(View.GONE);
        }

        Glide.with(getContext()).load(Uri.parse(data.getDonationMainPhoto())).into(mainDonationPhoto);

        // now handle the button clicks!

        approveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ask for the confirmation of ADMIN approval
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }
                builder.setTitle("Confirm Approval?")
                        .setMessage("Are you sure, you want to approve the Donation?\n(This can't be undone)")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                final ProgressDialog pd = new ProgressDialog(getContext());
                                pd.setCancelable(false);
                                pd.setCanceledOnTouchOutside(false);

                                pd.setMessage("Please wait...");

                                FirebaseDatabase.getInstance().getReference().child("donation_data_under_process").child(data.getDonationKey())
                                        .child("donationConfirm").setValue(Constants.YES).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        pd.cancel();


                                        parentLayout.setBackgroundColor(getContext().getResources().getColor(R.color.lightGreen));
                                        buttonParentLayout.setVisibility(View.GONE);
                                        confirmationStatus.setText(Constants.APPROVED);
                                        confirmationStatus.setVisibility(View.VISIBLE);
                                        confirmationStatus.setTextColor(getContext().getResources().getColor(R.color.deepGreen));

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
        });

        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ask for the confirmation of rejection
                AlertDialog.Builder builder;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }

                builder.setTitle("Confirm Rejection?")
                        .setMessage("Are you sure, you want to reject the Donation?\n(This can't be undone)")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                final ProgressDialog pd = new ProgressDialog(getContext());
                                pd.setMessage("Please wait...");

                                FirebaseDatabase.getInstance().getReference().child("donation_data_under_process").child(data.getDonationKey())
                                        .child("donationConfirm").setValue(Constants.NO).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        parentLayout.setBackgroundColor(getContext().getResources().getColor(R.color.lightRed));
                                        buttonParentLayout.setVisibility(View.GONE);
                                        confirmationStatus.setText(Constants.REJECTED);
                                        confirmationStatus.setVisibility(View.VISIBLE);
                                        confirmationStatus.setTextColor(getContext().getResources().getColor(R.color.deepRed));

                                        // now delete all the data from everywhere

                                        // delete the main photo
                                        FirebaseStorage.getInstance().getReferenceFromUrl(data.getDonationMainPhoto()).delete();

                                        // delete from donation_details
                                        FirebaseDatabase.getInstance().getReference().child("donation_details").child(data.getDonationKey()).removeValue();
                                        FirebaseDatabase.getInstance().getReference().child("donation_data_under_process").child(data.getDonationKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                pd.cancel();
                                                Toast.makeText(getContext(), "Donation Details successfully deleted!", Toast.LENGTH_SHORT).show();

                                            }
                                        });

                                    }
                                });

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing!

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        return convertView;
    }

    void setCategoryImage(ImageView imageView, String category){

        if("Drinks".equals(category)){
            imageView.setImageResource(R.drawable.drinks_graphic_asset);
        }
        else if("Electronics".equals(category)){
            imageView.setImageResource(R.drawable.electronics_graphic_asset);
        }
        else if("Food".equals(category)){
            imageView.setImageResource(R.drawable.food_graphic_asset);
        }
        else if("Education".equals(category)){
            imageView.setImageResource(R.drawable.education_graphic_asset);
        }
        else if("Clothes".equals(category)){
            imageView.setImageResource(R.drawable.cloths_graphic_asset);
        }
        else if("Fruits".equals(category)){
            imageView.setImageResource(R.drawable.fruits_graphic_asset);
        }
        else{
            imageView.setImageResource(R.drawable.other_graphic_asset);
        }

    }

}













