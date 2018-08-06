package com.bdwater.meterinput.metertask;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bdwater.meterinput.R;

public class MeterTaskActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    Fragment[] fragments;
    MeterTaskFragmentPageAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager)findViewById(R.id.viewPager);
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);

        fragments = new Fragment[] {
                new MeterTaskActivityFragment(),
                new MeterTaskListFragment()
        };

        String[] tabStrings = new String[] {
                "维修申请","未完成的申请"
        };

        pageAdapter = new MeterTaskFragmentPageAdapter(this, getSupportFragmentManager(), tabStrings, fragments);
        viewPager.setAdapter(pageAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
    }

}
