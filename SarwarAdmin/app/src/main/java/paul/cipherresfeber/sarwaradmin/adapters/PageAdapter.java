package paul.cipherresfeber.sarwaradmin.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import paul.cipherresfeber.sarwaradmin.fragments.CollectedItemsFragment;
import paul.cipherresfeber.sarwaradmin.fragments.DeliveryAgentFragment;
import paul.cipherresfeber.sarwaradmin.fragments.PendingDeliveryFragment;

public class PageAdapter extends FragmentStatePagerAdapter {

    private int numberOfTabs;

    public PageAdapter(FragmentManager fm, int numberOfTabs){
        super(fm);
        this.numberOfTabs = numberOfTabs;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return new PendingDeliveryFragment();
            case 1:
                return new CollectedItemsFragment();
            case 2:
                return new DeliveryAgentFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

}
