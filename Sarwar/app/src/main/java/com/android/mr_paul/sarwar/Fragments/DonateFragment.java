package com.android.mr_paul.sarwar.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.mr_paul.sarwar.DonationActivity;
import com.android.mr_paul.sarwar.R;
import com.android.mr_paul.sarwar.UtilityPackage.Constants;
import com.android.mr_paul.sarwar.UtilityPackage.DonationRecordAdapter;

import java.util.ArrayList;

public class DonateFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.donate_fragment,container, false);

        GridView gridView = view.findViewById(R.id.grid_view);

        final ArrayList<DonationRecordsClass> recordList = new ArrayList<>();
        DonationRecordAdapter donationRecordAdapter = new DonationRecordAdapter(getContext(), R.layout.donation_record_layout, recordList);

        recordList.add(new DonationRecordsClass("Drinks",R.drawable.drinks_graphic_asset));
        recordList.add(new DonationRecordsClass("Electronics",R.drawable.electronics_graphic_asset));
        recordList.add(new DonationRecordsClass("Food",R.drawable.food_graphic_asset));
        recordList.add(new DonationRecordsClass("Education",R.drawable.education_graphic_asset));
        recordList.add(new DonationRecordsClass("Clothes",R.drawable.cloths_graphic_asset));
        recordList.add(new DonationRecordsClass("Fruits",R.drawable.fruits_graphic_asset));
        recordList.add(new DonationRecordsClass("Money",R.drawable.money_graphic_asset));
        recordList.add(new DonationRecordsClass("Others",R.drawable.other_graphic_asset));


        // open donation detail activity after user chooses a category of donation
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // donation of drinks, food or fruits will be unavailable for first few days!
                if("Drinks".equals(recordList.get(position).getName())){
                    Toast.makeText(getContext(), "Sorry, donation of 'Drinks' is currently unavailable!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if("Food".equals(recordList.get(position).getName())){
                    Toast.makeText(getContext(), "Sorry, donation of 'Food' is currently unavailable!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if("Fruits".equals(recordList.get(position).getName())){
                    Toast.makeText(getContext(), "Sorry, donation of 'Fruits' is currently unavailable!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getContext(), DonationActivity.class);
                intent.putExtra(Constants.ITEM_NAME,recordList.get(position).getName());
                intent.putExtra(Constants.ITEM_ID,recordList.get(position).getPicture_resource());
                startActivity(intent);
            }
        });

        gridView.setAdapter(donationRecordAdapter);
        return view;
    }

    public class DonationRecordsClass{
        public String name;
        public int picture_resource;

        public DonationRecordsClass(String name, int picture_resource){
            this.name = name;
            this.picture_resource = picture_resource;
        }

        public int getPicture_resource(){
            return picture_resource;
        }

        public String getName(){
            return name;
        }

    }

}
