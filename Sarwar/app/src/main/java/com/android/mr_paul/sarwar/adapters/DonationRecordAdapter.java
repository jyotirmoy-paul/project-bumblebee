package com.android.mr_paul.sarwar.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.mr_paul.sarwar.fragments.DonateFragment;
import com.android.mr_paul.sarwar.R;

import java.util.ArrayList;

public class DonationRecordAdapter extends ArrayAdapter<DonateFragment.DonationRecordsClass> {

    public DonationRecordAdapter(Context context, int layout, ArrayList<DonateFragment.DonationRecordsClass> list){
        super(context, layout, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.donation_record_layout,parent, false);
        }

        ImageView graphicAsset = convertView.findViewById(R.id.graphic_asset);
        TextView textView = convertView.findViewById(R.id.donation_type);

        DonateFragment.DonationRecordsClass donationRecordsClass = getItem(position);

        graphicAsset.setImageResource(donationRecordsClass.getPicture_resource());
        textView.setText(donationRecordsClass.getName());


        return convertView;
    }
}
