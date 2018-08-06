package com.bdwater.meterinput;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bdwater.meterinput.base.NetworkUtils;
import com.bdwater.meterinput.main.WaterLogFragment;
import com.bdwater.meterinput.metertask.MeterTaskActivity;
import com.bdwater.meterinput.model.Book;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.base.DrawableUtils;
import com.bdwater.meterinput.base.IReturnValueListener;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.main.CustomerBillFragment;
import com.bdwater.meterinput.main.CustomerFragment;
import com.bdwater.meterinput.main.IMeterChangedNotifier;
import com.bdwater.meterinput.main.MeterFragmentPageAdapter;
import com.bdwater.meterinput.main.MeterInputFragment;
import com.bdwater.meterinput.main.MeterSearchPanel;
import com.bdwater.meterinput.update.UpdateService;

public class MainActivity extends AppCompatActivity {
        /*implements NavigationView.OnNavigationItemSelectedListener {*/

    CApplication mApp;
    CurrentContext mCC;

    DrawerLayout mDrawLayout;

    // main navigation
    NavigationView mNavigationMain;
    TextView mCurrentBookView;
    TextView mCurrentMeterView;
    Button mMaintainApplyButton;

    // right navigation
    NavigationView mNavigationRight;
    ViewPager mViewPagerOfRight;
    Fragment[] mFragmentsOfRight;

    // main content
    TabLayout mTabLayout;
    ViewPager mViewPagerOfMain;
    MeterFragmentPageAdapter mMeterFragmentPageAdapter;
    Fragment[] mFragmentsOfMain;

    // meter search panel
    MenuItem searchItem;
    SearchView searchView;
    MeterSearchPanel meterSearchPanel;

    UpdateService updateService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApp = (CApplication)getApplication();
        mCC = mApp.getCurrentContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawLayout.setDrawerListener(toggle);
        toggle.syncState();

        initNavigationMain();
        initNavigationRight();

        initMainContent();
        initMeterSearchPanel();

        refreshBookAndMeterTitle();

        checkUpdate();
    }
    private void checkUpdate() {
        updateService = new UpdateService(MainActivity.this);
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    updateService.getUpdateInfo();
                    if(updateService.isNeedUpdate() == true) {
                        Log.i("update", "is need update");
                        handler.sendEmptyMessage(0);
                    } else
                        Log.i("update", "no need update");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("update", e.getMessage());
                }
            }
        }).start();
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateService.showUpdateDialog();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        notifyFragmentsMeterChanged();
    }

    // init main navigation
    private void initNavigationMain() {
        String versionName = NetworkUtils.getVersionName(MainActivity.this);

        mNavigationMain = (NavigationView) findViewById(R.id.nav_view);

        ((TextView)mNavigationMain.findViewById(R.id.username)).setText(mCC.getUser().Name + " [" + NetworkUtils.getLineName() + "]");
        ((TextView)mNavigationMain.findViewById(R.id.loginname)).setText("[@" + mCC.getUser().Pinyin + "]  V" + versionName);

        mCurrentBookView = (TextView)mNavigationMain.findViewById(R.id.currentBook);
        mCurrentBookView.setOnClickListener(mButtonClickListener);
        mCurrentMeterView = (TextView)mNavigationMain.findViewById(R.id.currentMeter);
        mCurrentMeterView.setOnClickListener(mButtonClickListener);

        mMaintainApplyButton = (Button)mNavigationMain.findViewById(R.id.maintainApplyButton);
        mMaintainApplyButton.setOnClickListener(mMaintainButtonClickListener);
    }
    // init right navigation
    private void initNavigationRight() {
        mNavigationRight = (NavigationView)findViewById(R.id.nav_right_view);
        int width = getResources().getDisplayMetrics().widthPixels - 100;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams)mNavigationRight.getLayoutParams();
        params.width = width;
        mNavigationRight.setLayoutParams(params);

        mViewPagerOfRight = (ViewPager)mNavigationRight.findViewById(R.id.viewPager);
        mFragmentsOfRight = new Fragment[2];
        BookListFragment bf = new BookListFragment();
        bf.setListener(mReturnValueListener);
        mFragmentsOfRight[0] = bf;
        MeterListFragment mf = new MeterListFragment();
        mf.setListener(mReturnValueListener);
        mFragmentsOfRight[1] = mf;
        RightViewFragmentPageAdapter fragmentPageAdapter =
                new RightViewFragmentPageAdapter(getSupportFragmentManager(), mFragmentsOfRight);
        mViewPagerOfRight.setAdapter(fragmentPageAdapter);
        mViewPagerOfRight.setOnTouchListener(new ViewPager.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });
    }
    // tab layout, viewpager and fragments
    private void initMainContent() {
        mTabLayout = (TabLayout)findViewById(R.id.tabLayout);
        mViewPagerOfMain = (ViewPager)findViewById(R.id.viewPagerMain);
        mViewPagerOfMain.setOffscreenPageLimit(4);

        // create fragments
        mFragmentsOfMain = new Fragment[] {
                new MeterInputFragment(),
                new WaterLogFragment(),
                new CustomerBillFragment(),
                new CustomerFragment()
        };

        // create page adapter
        String[] tabTitles = new String[]{
                this.getString(R.string.tab_meter_input),
                this.getString(R.string.tab_water_log),
                this.getString(R.string.tab_customer_bill),
                this.getString(R.string.tab_detail)
        };
        mMeterFragmentPageAdapter = new MeterFragmentPageAdapter(this, getSupportFragmentManager(),
                tabTitles, mFragmentsOfMain);
        mViewPagerOfMain.setAdapter(mMeterFragmentPageAdapter);
        mViewPagerOfMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((IMeterChangedNotifier)mFragmentsOfMain[position]).notifyDataChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.setupWithViewPager(mViewPagerOfMain);
    }
    private void initMeterSearchPanel() {
        meterSearchPanel = new MeterSearchPanel(this);
        meterSearchPanel.setRoot(findViewById(R.id.meter_search_panel));
        meterSearchPanel.setProgressBar((ProgressBar)findViewById(R.id.meter_search_panel_progressBar));
        meterSearchPanel.setListView((ListView)findViewById(R.id.meter_search_panel_listView));
        meterSearchPanel.setOnMeterClickListener(new MeterSearchPanel.OnMeterClickedListener() {
            @Override
            public void onClick(Meter meter) {
                searchView.clearFocus();
                searchView.setIconified(true);
                searchView.onActionViewCollapsed();
                meterSearchPanel.dismiss();

                showMeter(meter);
            }
        });
    }

    // notify fragments selection of meter has changed
    private void notifyFragmentsMeterChanged() {
        ((IMeterChangedNotifier)mFragmentsOfMain[mViewPagerOfMain.getCurrentItem()]).notifyDataChanged();
    }
    // refresh title of book and meter in left drawer
    private void refreshBookAndMeterTitle() {
        Book book = mCC.getCurrentBook();
        if(null != book)
            mCurrentBookView.setText(book.Title);

        Meter meter = mCC.getCurrentMeter();
        if(null != meter)
            mCurrentMeterView.setText(meter.CustomerNo + " - " + meter.Name);
        else
            mCurrentMeterView.setText("选择抄表的用户");

    }
    // show book navigation in left drawer
    private void showNavigationBook() {
        mViewPagerOfRight.setCurrentItem(0);
        mDrawLayout.openDrawer(GravityCompat.END);
        ((BookListFragment)mFragmentsOfRight[0]).syncCheckState();
    }
    // show meter navigation in right drawer
    private void showNavigationMeter(Book book) {
        mDrawLayout.openDrawer(GravityCompat.END);
        ((MeterListFragment)mFragmentsOfRight[1]).loadByBook(book);
        mViewPagerOfRight.setCurrentItem(1);
    }
    // loads meter in main content
    private void showMeter(Meter meter) {
        Book book = mCC.getBookById(meter.BookID);

        // set current book and current meter
        mCC.setCurrentMeter(meter);
        mCC.setCurrentBook(book);

        // save book and meter into profile
        mApp.saveCurrentBook(book);
        mApp.saveCurrentMeter(meter);

        // closes drawer
        if(mDrawLayout.isDrawerOpen(GravityCompat.END))
            mDrawLayout.closeDrawer(GravityCompat.END);

        refreshBookAndMeterTitle();

        // notifies fragment selection of meter has changed
        notifyFragmentsMeterChanged();
    }

    // listener of return value for selection of book or meter in
    // right drawer
    IReturnValueListener mReturnValueListener = new IReturnValueListener() {
        @Override
        public void onReturnValue(Object obj) {
            if(obj instanceof Book) {
                showNavigationMeter((Book)obj);
            } else if (obj instanceof Meter) {
                Meter meter = (Meter)obj;
                showMeter(meter);
            }
        }
    };

    // listener of click for current book or meter in
    // left drawer
    Button.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.currentBook:
                    showNavigationBook();
                    break;
                case R.id.currentMeter:
                    showNavigationMeter(mCC.getCurrentBook());
                    break;
            }
            mDrawLayout.closeDrawer(GravityCompat.START);
        }
    };
    Button.OnClickListener mMaintainButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mCC.getCurrentMeter() == null) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("尚未选择用户，无水表可进行故障申请，请选择后重试")
                        .show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, MeterTaskActivity.class);
            startActivity(intent);
        }
    };

    @Override
    public void onBackPressed() {
        if(mDrawLayout.isDrawerOpen(GravityCompat.END) && mViewPagerOfRight.getCurrentItem() == 1) {
            showNavigationBook();
        } else if (mDrawLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawLayout.closeDrawer(GravityCompat.END);
        } else if(mDrawLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawLayout.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("确认")
                    .setMessage("您是否要确认退出？")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
    }

    Menu mMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        menu.findItem(R.id.menu_navigate).setIcon(DrawableUtils.getIcon(this, R.drawable.ic_navigate));

        // previous meter menu
        final MenuItem prev = menu.findItem(R.id.menu_previous);
        prev.setVisible(false);
        prev.getActionView().findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(prev);
            }
        });

        // next meter menu
        final MenuItem next = menu.findItem(R.id.menu_next);
        next.setVisible(false);
        next.getActionView().findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(next);
            }
        });

        // search view menu
        searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView)searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_query_hint));
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meterSearchPanel.show();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                meterSearchPanel.dismiss();
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                meterSearchPanel.submitSearch(query);
                return false;
            }

            String queryString = "";
            @Override
            public boolean onQueryTextChange(String newText) {
                queryString = newText;
                final String delayString = newText;
                meterSearchPanel.getRoot().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(delayString.equals(queryString))
                            meterSearchPanel.quickSearch(delayString);
                    }
                }, 500);

                return false;
            }
        });

        return true;
    }

    boolean menuNavigateExpanded = false;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_navigate) {
            menuNavigateExpanded = !menuNavigateExpanded;
            if(menuNavigateExpanded) {
                // expands search view
                mMenu.findItem(R.id.menu_previous).setVisible(true);
                mMenu.findItem(R.id.menu_next).setVisible(true);
                item.setIcon(DrawableUtils.getIcon(this, android.R.drawable.ic_menu_close_clear_cancel));
                searchItem.setVisible(false);
            } else {
                mMenu.findItem(R.id.menu_previous).setVisible(false);
                mMenu.findItem(R.id.menu_next).setVisible(false);
                // collapses search view
                item.setIcon(DrawableUtils.getIcon(this, R.drawable.ic_navigate));
                searchItem.setVisible(true);
            }
            getSupportActionBar().setDisplayShowTitleEnabled(!menuNavigateExpanded);

            return true;
        } else if(id == R.id.menu_previous) {
            Meter meter = mCC.getPrevMeter();
            if(null == meter)
                Toast.makeText(this, R.string.reached_first_meter, Toast.LENGTH_SHORT).show();
            else
                showMeter(meter);
        } else if(id == R.id.menu_next) {
            Meter meter = mCC.getNextMeter();
            if(null == meter)
                Toast.makeText(this, R.string.reached_last_meter, Toast.LENGTH_SHORT).show();
            else
                showMeter(meter);
        }

        return super.onOptionsItemSelected(item);
    }

}
