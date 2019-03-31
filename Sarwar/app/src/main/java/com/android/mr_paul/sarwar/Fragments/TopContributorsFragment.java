package com.android.mr_paul.sarwar.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.mr_paul.sarwar.R;
import com.android.mr_paul.sarwar.UtilityPackage.Constants;
import com.android.mr_paul.sarwar.UtilityPackage.DonorData;
import com.android.mr_paul.sarwar.UtilityPackage.DonorDataAdapter;
import com.facebook.ads.InterstitialAd;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TopContributorsFragment extends Fragment {

    private int number_of_donor_data_to_fetch = 20; // just show the top 20 contributors
    private boolean isPresent = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.top_contributor_layout, container, false);

        final ArrayList<DonorData> donorDataList = new ArrayList<>();
        final DonorDataAdapter adapter = new DonorDataAdapter(getContext(), R.layout.donor_contribution_data_layout,donorDataList);

        final LinearLayout preLoading = view.findViewById(R.id.pre_loading);
        final ListView listView = view.findViewById(R.id.donor_contribution_data);
        final RelativeLayout topBar = view.findViewById(R.id.top_bar);
        final RelativeLayout buttonBar = view.findViewById(R.id.button_bar);

        showInterstitialAd();

        // this top contributors layout shows the top 30 donor's name and contributions
        Query queryData = FirebaseFirestore.getInstance().collection("donor_contribution_data")
                .orderBy("number", Query.Direction.DESCENDING).limit(number_of_donor_data_to_fetch);

        queryData.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                List donorList = queryDocumentSnapshots.getDocuments();

                if(!donorList.isEmpty()){
                    preLoading.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    topBar.setVisibility(View.VISIBLE);
                }

                for(int i = 0; i<donorList.size(); i++){

                    DocumentSnapshot documentSnapshot = (DocumentSnapshot) donorList.get(i);
                    Map<String, Object> mapData = documentSnapshot.getData();

                    // if the user is not present in the top donor's list, then show a bar in the button, showing user his info!
                    if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(mapData.get("uid"))){
                       isPresent = true;
                    }

                    donorDataList.add(new DonorData((String) mapData.get("name"), (Long) mapData.get("number"), (String)mapData.get("uid")));
                    adapter.notifyDataSetChanged();

                }

                if(!isPresent){
                    // then show the button layout, showing about the user data
                    buttonBar.setVisibility(View.VISIBLE);
                    final TextView myName = view.findViewById(R.id.my_name);
                    final TextView myNumber = view.findViewById(R.id.my_number);

                    FirebaseFirestore.getInstance().document("donor_contribution_data/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            Map<String, Object> mapData = documentSnapshot.getData();
                            String name = (String) mapData.get("name");
                            String number = Long.toString((long)mapData.get("number"));

                            myName.setText(name);
                            myNumber.setText(number);

                        }
                    });
                }

            }
        });

        listView.setAdapter(adapter);

        return view;

    }

    private void showInterstitialAd(){
        final InterstitialAd ad = new InterstitialAd(getContext(), Constants.FACEBOOK_INTERSTITIAL_AD_KEY);
        ad.loadAd();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(ad.isAdLoaded()){
                    ad.show();
                }

            }
        }, 10000 + new Random().nextInt(10000)); // wait for 10 - 20 seconds
    }

}












