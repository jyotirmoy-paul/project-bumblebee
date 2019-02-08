package com.android.mr_paul.sarwar.UtilityPackage;


import android.content.Context;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.android.mr_paul.sarwar.R;

import java.util.ArrayList;

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

        CardView parentLayout = convertView.findViewById(R.id.parent_layout);
        ImageView donationCategoryPic = convertView.findViewById(R.id.donation_category_pic);
        TextView agentNameDisplay = convertView.findViewById(R.id.agent_name);
        Button callBtn = convertView.findViewById(R.id.call_btn);
        LinearLayout subParentLayout = convertView.findViewById(R.id.sub_parent_layout);
        TextView donationCollected = convertView.findViewById(R.id.donation_collection_confirmation);

        final AvailableDonationDetailClass data = getItem(position);

        setCategoryImage(donationCategoryPic, data.getDonationCategory());

        if(Constants.COLLECTED.equals(data.getDonationStatus())){

            // means donation has been collected!
            parentLayout.setCardBackgroundColor(getContext().getResources().getColor(R.color.lightGreen));
            subParentLayout.setVisibility(View.GONE);
            donationCollected.setText("Donation collected by " + data.getDeliveryAgentName() + ". Thank You!");
            donationCollected.setVisibility(View.VISIBLE);
            donationCollected.setTextColor(getContext().getResources().getColor(R.color.deepGreen));

        }
        else{
            parentLayout.setCardBackgroundColor(getContext().getResources().getColor(R.color.lightRed));
            subParentLayout.setVisibility(View.VISIBLE);
            donationCollected.setVisibility(View.GONE);
            agentNameDisplay.setText(data.getDeliveryAgentName());
        }



        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + data.getDeliveryAgentNumber()));
                getContext().startActivity(intent);

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
