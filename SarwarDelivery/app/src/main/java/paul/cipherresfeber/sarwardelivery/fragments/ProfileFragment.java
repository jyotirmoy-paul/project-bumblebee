package paul.cipherresfeber.sarwardelivery.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import paul.cipherresfeber.sarwardelivery.R;
import paul.cipherresfeber.sarwardelivery.activities.ProfileUpdateActivity;
import paul.cipherresfeber.sarwardelivery.util.Constants;
import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment,container, false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

        TextView profilePicView = view.findViewById(R.id.profile_pic);
        TextView userNameDisplay = view.findViewById(R.id.user_name);
        TextView userMobileDisplay = view.findViewById(R.id.user_mobile_number);
        FloatingActionButton editProfileDetail = view.findViewById(R.id.edit_profile_detail);
        ImageView aadharImageView = view.findViewById(R.id.aadhar_card_view);

        String userName = sharedPreferences.getString(Constants.USER_NAME, Constants.UNDEFINED);
        String userMobile = sharedPreferences.getString(Constants.USER_PHONE_NUMBER,Constants.UNDEFINED);
        String aadharCardLink = sharedPreferences.getString(Constants.USER_AADHAR_CARD_PHOTO_LINK,Constants.UNDEFINED);

        userNameDisplay.setText(userName);
        userMobileDisplay.setText(userMobile);
        Glide.with(this).load(Uri.parse(aadharCardLink)).into(aadharImageView);

        profilePicView.setText(userName.toUpperCase().charAt(0) + "");

        // set an onClick listener for editing the details in profile section
        editProfileDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start a new activity for editing profile
                startActivity(new Intent(getContext(), ProfileUpdateActivity.class));
                getActivity().finish();
            }
        });


        return view;
    }

}
