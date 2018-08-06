package com.bdwater.meterinput.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bdwater.meterinput.R;

/**
 * Created by hegang on 16/6/15.
 */
public class MeterFragmentPageAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private String[] mTabTitles; // = new String[] {"抄表", "欠费信息", "详细信息"};
    private Fragment[] mFragments;

    public MeterFragmentPageAdapter(Context context, FragmentManager fm, String[] tabTitles, Fragment[] fragments) {
        super(fm);
        mFragments = fragments;
        mContext = context;
        mTabTitles = tabTitles;

    }

    @Override
    public Fragment getItem(int position) {
        return mFragments[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles[position];
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }


}
