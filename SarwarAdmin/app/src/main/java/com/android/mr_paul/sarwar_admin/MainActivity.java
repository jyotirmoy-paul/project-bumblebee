package com.android.mr_paul.sarwar_admin;


import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.android.mr_paul.sarwar_admin.UtilityPackage.PageAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        // adding icons to the tabLayout
        int[] icons = new int[]{
                R.drawable.ic_news_feed, // news posting arena
                R.drawable.ic_new_donation_available, // donation collection waiting
                R.drawable.ic_delivery_task, // collected items
                R.drawable.ic_new_delivery_agent // application of new delivery agent
        };

        // create 4 new tabs and add the respective icons to them

        for(int icon : icons){
            tabLayout.addTab(tabLayout.newTab().setIcon(icon));
        }

        PageAdapter pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab){}
            @Override
            public void onTabReselected(TabLayout.Tab tab){}
        });

        viewPager.setCurrentItem(1); // set the new donation_available_section as default

    }

}
