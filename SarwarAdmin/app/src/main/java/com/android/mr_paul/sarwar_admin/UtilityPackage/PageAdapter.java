package com.android.mr_paul.sarwar_admin.UtilityPackage;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.android.mr_paul.sarwar_admin.Fragments.CollectedItemsFragment;
import com.android.mr_paul.sarwar_admin.Fragments.DeliveryAgentFragment;
import com.android.mr_paul.sarwar_admin.Fragments.PendingDeliveryFragment;

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
                return new CollectedItemsFragment();
            case 1:
                return new PendingDeliveryFragment();
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
