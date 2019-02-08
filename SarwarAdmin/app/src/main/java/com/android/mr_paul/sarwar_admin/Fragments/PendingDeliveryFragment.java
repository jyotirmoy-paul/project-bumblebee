package com.android.mr_paul.sarwar_admin.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.mr_paul.sarwar_admin.R;
import com.android.mr_paul.sarwar_admin.UtilityPackage.AvailableDonationDetailAdapter;
import com.android.mr_paul.sarwar_admin.UtilityPackage.AvailableDonationDetailClass;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PendingDeliveryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.pending_delivery_fragment, container, false);

        final ListView listView = view.findViewById(R.id.list_view);
        final LinearLayout preLoading = view.findViewById(R.id.pre_loading);

        final ArrayList<AvailableDonationDetailClass> list = new ArrayList<>();
        final AvailableDonationDetailAdapter adapter = new AvailableDonationDetailAdapter(getContext(), R.layout.available_donation_detail_layout, list);

        FirebaseDatabase.getInstance().getReference().child("donation_data_under_process")
                .addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                AvailableDonationDetailClass data = dataSnapshot.getValue(AvailableDonationDetailClass.class);

                if(list.isEmpty()){
                    list.add(data);
                }
                else{
                    list.add(0, data);
                }

                adapter.notifyDataSetChanged();

                if(listView.getVisibility() == View.GONE){
                    listView.setVisibility(View.VISIBLE);
                    preLoading.setVisibility(View.GONE);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                AvailableDonationDetailClass data = dataSnapshot.getValue(AvailableDonationDetailClass.class);

                // search for the donationKey in already existing arrayList

                for(int i = 0; i < list.size(); i++){
                    if(data.getDonationKey().equals(list.get(i).getDonationKey())){

                        // now update the previous data with already existing data and notify the adapter
                        list.set(i, data);
                        adapter.notifyDataSetChanged();
                        return;

                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                String key = dataSnapshot.getValue(AvailableDonationDetailClass.class).getDonationKey();

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

        return view;
    }



}






















