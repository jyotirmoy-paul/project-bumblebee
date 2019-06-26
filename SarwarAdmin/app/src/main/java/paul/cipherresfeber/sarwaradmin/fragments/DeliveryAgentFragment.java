package paul.cipherresfeber.sarwaradmin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import paul.cipherresfeber.sarwaradmin.R;
import paul.cipherresfeber.sarwaradmin.adapters.AgentProfileAdapter;
import paul.cipherresfeber.sarwaradmin.models.DeliveryAgentProfile;
import paul.cipherresfeber.sarwaradmin.models.DeliveryAgentUserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DeliveryAgentFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.delivery_agent_fragment, container, false);

        final ListView listView = view.findViewById(R.id.list_view);
        final LinearLayout preLoading = view.findViewById(R.id.pre_loading);

        final ArrayList<DeliveryAgentProfile> list = new ArrayList<>();
        final AgentProfileAdapter adapter = new AgentProfileAdapter(getActivity(), R.layout.agent_profile_layout, list);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("delivery_agent_data"); // ref where all delivery agent data are stored
        databaseReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                DeliveryAgentUserInfo userInfo = dataSnapshot.child("user_info").getValue(DeliveryAgentUserInfo.class);

                String isVerified = dataSnapshot.child("user_info").child("isVerified").getValue(String.class);
                String uid = dataSnapshot.child("contact_info").child("UID").getValue(String.class);

                DeliveryAgentProfile deliveryAgentProfile = new DeliveryAgentProfile(userInfo.getName(),userInfo.getPhoneNumber(),
                        userInfo.getAadharCardLink(), isVerified, uid, userInfo.getCreationDate());

                if(list.isEmpty()){
                    list.add(deliveryAgentProfile);
                }
                else{
                    list.add(0, deliveryAgentProfile);
                }

                adapter.notifyDataSetChanged();

                if(listView.getVisibility() == View.GONE){
                    listView.setVisibility(View.VISIBLE);
                    preLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                // update if the verification status of the user changed!

                DeliveryAgentUserInfo userInfo = dataSnapshot.child("user_info").getValue(DeliveryAgentUserInfo.class);

                String isVerified = dataSnapshot.child("user_info").child("isVerified").getValue(String.class);
                String uid = dataSnapshot.child("contact_info").child("UID").getValue(String.class);

                DeliveryAgentProfile deliveryAgentProfile = new DeliveryAgentProfile(userInfo.getName(),userInfo.getPhoneNumber(),
                        userInfo.getAadharCardLink(), isVerified, uid, userInfo.getCreationDate());

                for(int i = 0; i < list.size(); i++){
                    if(list.get(i).getDeliveryAgentUID().equals(deliveryAgentProfile.getDeliveryAgentUID())){
                        list.set(i,deliveryAgentProfile);
                    }
                }


            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        listView.setAdapter(adapter);

        return view;
    }

}
