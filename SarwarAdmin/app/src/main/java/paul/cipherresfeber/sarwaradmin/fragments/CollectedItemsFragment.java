package paul.cipherresfeber.sarwaradmin.fragments;

import android.content.Intent;
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

import paul.cipherresfeber.sarwaradmin.R;
import paul.cipherresfeber.sarwaradmin.activities.DonationDetailViewActivity;

import paul.cipherresfeber.sarwaradmin.models.AvailableDonationDetailClass;
import paul.cipherresfeber.sarwaradmin.adapters.CollectedDonationsAdapter;
import paul.cipherresfeber.sarwaradmin.util.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CollectedItemsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.collected_item_fragment, container, false);

        final ListView listView = view.findViewById(R.id.list_view);
        final LinearLayout preLoading = view.findViewById(R.id.pre_loading);

        final ArrayList<AvailableDonationDetailClass> list = new ArrayList<>();
        final CollectedDonationsAdapter adapter = new CollectedDonationsAdapter(getContext(), R.layout.collected_donations_layout, list);

        FirebaseDatabase.getInstance().getReference().child("donation_data_under_process")
                .addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        AvailableDonationDetailClass data = dataSnapshot.getValue(AvailableDonationDetailClass.class);
                        if(Constants.YES.equals(data.getDonationConfirm())){

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
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        AvailableDonationDetailClass data = dataSnapshot.getValue(AvailableDonationDetailClass.class);
                        if(Constants.COLLECTED.equals(data.getDonationStatus())){
                            // means delivery agent has confirmed the pick up of the item!
                            // update in the app
                            String donationKey = data.getDonationKey();
                            for(int i = 0 ;i < list.size(); i++){
                                 if(donationKey.equals(list.get(i).getDonationKey())){
                                     // this current item has been updated, update it in the list and break
                                     list.set(i, data);
                                     adapter.notifyDataSetChanged();
                                     break;
                                 }
                            }

                            if(listView.getVisibility() == View.GONE){
                                listView.setVisibility(View.VISIBLE);
                                preLoading.setVisibility(View.GONE);
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
                            preLoading.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
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

                Intent intent = new Intent(getContext(), DonationDetailViewActivity.class);
                intent.putExtra("donationKey", list.get(position).getDonationKey());
                startActivity(intent);

            }
        });

        return view;
    }

}
