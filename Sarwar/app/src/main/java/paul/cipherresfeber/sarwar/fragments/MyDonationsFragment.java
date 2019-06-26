package paul.cipherresfeber.sarwar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import paul.cipherresfeber.sarwar.R;
import paul.cipherresfeber.sarwar.adapters.AvailableDonationDetailAdapter;
import paul.cipherresfeber.sarwar.models.AvailableDonationDetailClass;
import paul.cipherresfeber.sarwar.util.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class MyDonationsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.my_donations_fragment,container, false);

        final String token = getContext().getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getString(Constants.FIREBASE_TOKEN,Constants.UNDEFINED);

        final LinearLayout preLoading = view.findViewById(R.id.pre_loading);
        final ListView listView = view.findViewById(R.id.current_donations);

        final ArrayList<AvailableDonationDetailClass> list = new ArrayList<>();
        final AvailableDonationDetailAdapter adapter = new AvailableDonationDetailAdapter(getContext(), R.layout.available_donation_detail_layout, list);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("donation_data_under_process");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                AvailableDonationDetailClass data = dataSnapshot.getValue(AvailableDonationDetailClass.class);

                String donorToken = data.getDonorToken();
                String donationConfirm = data.getDonationConfirm();


                if("-".equals(donationConfirm)){
                    // the donation is still being judged by the admin
                    return;
                }

                if(token.equals(donorToken)){

                    if(list.isEmpty()){
                        list.add(data);
                    }
                    else{
                        list.add(0, data);
                    }

                    preLoading.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                String donationKey = dataSnapshot.getValue(AvailableDonationDetailClass.class).getDonationKey();

                for(int i=0;i<list.size();i++){
                    if(donationKey.equals(list.get(i).getDonationKey())){
                        list.set(i, dataSnapshot.getValue(AvailableDonationDetailClass.class));
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                String donationKey = dataSnapshot.getValue(AvailableDonationDetailClass.class).getDonationKey();

                for(int i=0;i<list.size();i++){
                    if(donationKey.equals(list.get(i).getDonationKey())){
                        list.remove(i);
                        adapter.notifyDataSetChanged();
                        break;
                    }
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
