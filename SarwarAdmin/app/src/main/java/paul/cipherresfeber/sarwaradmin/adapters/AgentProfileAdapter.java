package paul.cipherresfeber.sarwaradmin.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import paul.cipherresfeber.sarwaradmin.R;
import paul.cipherresfeber.sarwaradmin.util.Constants;
import paul.cipherresfeber.sarwaradmin.models.DeliveryAgentProfile;


public class AgentProfileAdapter extends ArrayAdapter<DeliveryAgentProfile> {

    public AgentProfileAdapter(Context c, int resource, ArrayList<DeliveryAgentProfile> list){
        super(c, resource, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null){
            convertView = inflater.inflate(R.layout.agent_profile_layout,parent, false);
        }

        // reference the views
        final CardView cardView = convertView.findViewById(R.id.card_view);
        TextView profilePicDisplay = convertView.findViewById(R.id.profile_pic);
        TextView userNameDisplay = convertView.findViewById(R.id.user_name);
        TextView creationDateDisplay = convertView.findViewById(R.id.account_creation_date);
        final ImageView verificationStatusDisplay = convertView.findViewById(R.id.verification_status);
        TextView phoneNumberDisplay = convertView.findViewById(R.id.phone_number);
        ImageView aadharCardDisplay = convertView.findViewById(R.id.aadhar_card_image);
        Button callBtn = convertView.findViewById(R.id.call_btn);
        final Button markAsVerifiedBtn = convertView.findViewById(R.id.mark_verified_btn);

        final DeliveryAgentProfile agentProfile = getItem(position);

        userNameDisplay.setText(agentProfile.getName());
        profilePicDisplay.setText(agentProfile.getName().toUpperCase().charAt(0) + "");
        creationDateDisplay.setText(agentProfile.getCreationDate());
        phoneNumberDisplay.setText(agentProfile.getPhoneNumber());
        Glide.with(getContext()).load(Uri.parse(agentProfile.getAadharCardLink())).into(aadharCardDisplay);

        // update the UI in accordance to the user being verified or not
        if(Constants.YES.equals(agentProfile.getIsVerified())){
            cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.lightGreen));
            verificationStatusDisplay.setImageResource(R.drawable.ic_verified);

            // here disable the markAsVerifiedBtn
            markAsVerifiedBtn.setVisibility(View.GONE);
        }
        else{
            // if the delivery user account is not verified

            // set the background red
            cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.lightRed));

            // set the verification status as crossed
            verificationStatusDisplay.setImageResource(R.drawable.ic_not_verified);
            markAsVerifiedBtn.setVisibility(View.VISIBLE);
        }



        // finally set up the call, image and markAsVerified buttons

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + agentProfile.getPhoneNumber()));
                getContext().startActivity(intent);
            }
        });

        markAsVerifiedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = agentProfile.getName() + " will now be able to collect donation requests and will become a part of Sarwar family. Are you sure you want to confirm the verification?\n(This can't be undone)";

                // show an dialog box asking admin to confirm the verification of the delivery agent
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }
                builder.setTitle("Confirm Verification?")
                        .setMessage(message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // complete the verification of the delivery agent

                                final ProgressDialog pd = new ProgressDialog(getContext());
                                pd.setCancelable(false);
                                pd.setCanceledOnTouchOutside(false);

                                pd.setMessage("Please wait...");

                                FirebaseDatabase.getInstance().getReference().child("delivery_agent_data").child(agentProfile.getDeliveryAgentUID())
                                        .child("user_info").child("isVerified").setValue(Constants.YES).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        pd.cancel();

                                        cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.lightGreen));
                                        verificationStatusDisplay.setImageResource(R.drawable.ic_verified);

                                        // here disable the markAsVerifiedBtn
                                        markAsVerifiedBtn.setVisibility(View.GONE);

                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                Log.i("VerificationStatus: ", "Verification Cancelled!");
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        return convertView;

    }
}
