package paul.cipherresfeber.sarwar.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import paul.cipherresfeber.sarwar.R;
import paul.cipherresfeber.sarwar.util.Constants;
import com.facebook.ads.InterstitialAd;

import java.util.Random;

public class AboutFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment,container, false);

        final InterstitialAd interstitialAd = new InterstitialAd(getContext(), Constants.FACEBOOK_INTERSTITIAL_AD_KEY);
        interstitialAd.loadAd();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(interstitialAd.isAdLoaded()){
                    interstitialAd.show();
                }

            }
        },10000 + new Random().nextInt(10000)); // 10 - 20 seconds later


        return view;
    }
}
