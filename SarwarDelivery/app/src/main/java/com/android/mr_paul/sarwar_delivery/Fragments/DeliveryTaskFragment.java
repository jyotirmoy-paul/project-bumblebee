package com.android.mr_paul.sarwar_delivery.Fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.mr_paul.sarwar_delivery.DonationDetailsActivity;
import com.android.mr_paul.sarwar_delivery.R;
import com.android.mr_paul.sarwar_delivery.UtilityPackage.DonationData;
import com.android.mr_paul.sarwar_delivery.UtilityPackage.DonationDataAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DeliveryTaskFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.delivery_task_fragment,container, false);

        final ListView listView = view.findViewById(R.id.listView);
        listView.setVisibility(View.GONE);
        final LinearLayout preLoading = view.findViewById(R.id.pre_loading);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("approved_donation_requests")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        final ArrayList<DonationData> list = new ArrayList<>();
        final DonationDataAdapter adapter = new DonationDataAdapter(getContext(), R.layout.donation_data_layout, list);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                DonationData donationData = dataSnapshot.getValue(DonationData.class);

                if(list.isEmpty()){
                    list.add(donationData);
                }
                else{
                    list.add(0, donationData);
                }

                adapter.notifyDataSetChanged();

                if(listView.getVisibility() == View.GONE){
                    listView.setVisibility(View.VISIBLE);
                    preLoading.setVisibility(View.GONE);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                String key = dataSnapshot.getValue(DonationData.class).getDonationKey();

                // search for that donation key in the array list and delete it, and notify the adapter
                for(int i=0;i<list.size();i++){
                    if (key.equals(list.get(i).getDonationKey())) {
                        // delete value at that index
                        list.remove(i);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }

                if(list.isEmpty()){
                    listView.setVisibility(View.GONE);
                    preLoading.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // open a new activity, showing detailed view of the donation details to the delivery agent
                DonationData donationData = list.get(position);
                Intent intent = new Intent(getContext(), DonationDetailsActivity.class);
                intent.putExtra("donationData",donationData);
                startActivity(intent);

            }
        });

        return view;
    }

}
