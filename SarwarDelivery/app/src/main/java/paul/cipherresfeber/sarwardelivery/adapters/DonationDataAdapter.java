package paul.cipherresfeber.sarwardelivery.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import paul.cipherresfeber.sarwardelivery.R;
import paul.cipherresfeber.sarwardelivery.models.DonationData;

public class DonationDataAdapter extends ArrayAdapter<DonationData> {

    public DonationDataAdapter(Context context, int resource, ArrayList<DonationData> list){
        super(context, resource, list);
    }


    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null){
            convertView = inflater.inflate(R.layout.donation_data_layout, parent, false);
        }

        // reference to the views
        TextView profilePicDisplay = convertView.findViewById(R.id.profile_pic);
        TextView donorNameDisplay = convertView.findViewById(R.id.donor_name);
        TextView donationTimeStampDisplay = convertView.findViewById(R.id.donation_time_stamp);

        TextView donationCategoryDisplay = convertView.findViewById(R.id.donation_category);
        TextView donationDescriptionDisplay = convertView.findViewById(R.id.donation_description);

        Button callBtn = convertView.findViewById(R.id.call_btn);
        Button getDirectionBtn = convertView.findViewById(R.id.get_direction_btn);

        ImageView mainPhotoDisplay = convertView.findViewById(R.id.donation_cover_image);

        final DonationData donationData = getItem(position);

        profilePicDisplay.setText(donationData.getDonorName().toUpperCase().charAt(0) + "");
        donorNameDisplay.setText(donationData.getDonorName());
        donationTimeStampDisplay.setText(donationData.getDateTime());

        donationCategoryDisplay.setText(donationData.getDonationCategory());

        String description = donationData.getDonationItemDescription();
        if(description.length() > 100){
            description = description.substring(0, 100) + "...";
        }
        donationDescriptionDisplay.setText(description);

        Glide.with(getContext()).load(Uri.parse(donationData.getDonationMainPhotoUrl())).into(mainPhotoDisplay);

        // set the button click listeners
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // call intent to dialer app
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + donationData.getDonorContactNumber()));
                getContext().startActivity(intent);

            }
        });

        getDirectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destAddress = donationData.getLatLong().getLatitude() + "," + donationData.getLatLong().getLongitude();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + destAddress));
                getContext().startActivity(intent);
            }
        });


        return convertView;

    }
}
