package com.bdwater.meterinput.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bdwater.meterinput.CApplication;
import com.bdwater.meterinput.R;
import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.model.FakeMeter;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.soap.ISoapAsyncTaskListener;
import com.bdwater.meterinput.soap.SoapAsyncTask;
import com.bdwater.meterinput.soap.SoapClient;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomerFragment extends Fragment implements IMeterChangedNotifier {

    CApplication mApp;
    CurrentContext mCC;
    MeterLoadState meterLoadState = new MeterLoadState();
    SoapAsyncTask task;

    SwipeRefreshLayout refreshLayout;
    ScrollView scrollView;

    // customer
    TextView customerNo;
    TextView name;
    TextView address;
    TextView customerCategoryTitle;
    TextView phone;
    TextView deposit;
    TextView departmentCode;
    RatingBar starGradeRatingBar;
    RatingBar markGradeRatingBar;

    View VATPanel;
    TextView accountBank;
    TextView accounts;
    TextView taxNum;

    // meter
    TextView peoples;
    TextView bookTitle;
    TextView cardIndex;
    TextView waterPriceTitle;
    TextView meterPosition;
    TextView meterCaliber;
    TextView installDate;

    TextView totalQty;
    TextView firstLevelQty;
    TextView secondLevelQty;
    TextView thirdLevelQty;

    public CustomerFragment() {
        // Required empty public constructor
        mApp = AppUtils.App;
        mCC = mApp.getCurrentContext();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_customer, container, false);
        refreshLayout = (SwipeRefreshLayout)v;
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });

        scrollView = (ScrollView)v.findViewById(R.id.root);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                if(scrollY == 0)
                    refreshLayout.setEnabled(true);
                else
                    refreshLayout.setEnabled(false);
            }
        });

        customerNo = (TextView)v.findViewById(R.id.customerNo);
        name = (TextView)v.findViewById(R.id.name);
        address= (TextView)v.findViewById(R.id.address);
        customerCategoryTitle = (TextView)v.findViewById(R.id.customerCategoryTitle);
        phone = (TextView)v.findViewById(R.id.phone);
        deposit = (TextView)v.findViewById(R.id.deposit);
        departmentCode = (TextView)v.findViewById(R.id.departmentCode);
        starGradeRatingBar = (RatingBar)v.findViewById(R.id.starGradeRatingBar);
        markGradeRatingBar = (RatingBar)v.findViewById(R.id.markGradeRatingBar);

        VATPanel = v.findViewById(R.id.VATPanel);
        accountBank = (TextView)v.findViewById(R.id.accountBank);
        accounts = (TextView)v.findViewById(R.id.accounts);
        taxNum = (TextView)v.findViewById(R.id.taxNum);

        peoples = (TextView)v.findViewById(R.id.peoples);
        bookTitle = (TextView)v.findViewById(R.id.bookTitle);
        cardIndex = (TextView)v.findViewById(R.id.cardIndex);
        waterPriceTitle = (TextView)v.findViewById(R.id.waterPriceTitle);
        meterPosition = (TextView)v.findViewById(R.id.meterPosition);
        meterCaliber = (TextView)v.findViewById(R.id.meterCaliber);
        installDate = (TextView)v.findViewById(R.id.installDate);

        totalQty = (TextView)v.findViewById(R.id.totalQty);
        firstLevelQty = (TextView)v.findViewById(R.id.firstLevelQty);
        secondLevelQty = (TextView)v.findViewById(R.id.secondLevelQty);
        thirdLevelQty = (TextView)v.findViewById(R.id.thirdLevelQty);

        return v;
    }

    ISoapAsyncTaskListener listener = new ISoapAsyncTaskListener() {
        @Override
        public void onTaskSuccess(JSONObject obj) {
            try
            {
                JSONObject jc = obj.getJSONObject("Data");
                Meter meter = mCC.getCurrentMeter();
                customerNo.setText("用户编号：" + meter.CustomerNo);
                name.setText(meter.Name);
                address.setText(jc.getString("Address"));
                customerCategoryTitle.setText("[" + jc.getString("CustomerCategoryTitle") + "]");
                phone.setText("电话：" + jc.getString("Phone"));
                deposit.setText("余额：" + jc.getString("Deposit"));
                departmentCode.setText("部门代码：" + jc.getString("DepartmentCode"));
                starGradeRatingBar.setRating(jc.getInt("StarGrade"));
                markGradeRatingBar.setRating(jc.getInt("MarkGrade"));

                accountBank.setText("开户银行：" + jc.getString("AccountBank"));
                accounts.setText("账号：" + jc.getString("Accounts"));
                taxNum.setText("税号：" + jc.getString("TaxNum"));
                VATPanel.setVisibility(jc.getBoolean("IsVAT") ? View.VISIBLE: View.GONE);

                peoples.setText("人数：" + jc.getString("Peoples"));
                bookTitle.setText("抄表薄：" + mCC.getCurrentBook().Title);
                cardIndex.setText("卡片编号：" + meter.CardIndex);
                waterPriceTitle.setText("用水性质：" + jc.getString("WaterPriceTitle"));
                meterPosition.setText("水表位置：" + jc.getString("MeterPosition"));
                meterCaliber.setText("水表口径：" + jc.getString("MeterCaliber"));
                installDate.setText("安装时间：" + jc.getString("InstallDate"));

                totalQty.setText("总用水量：" + jc.getString("TotalQty") + " ["
                    + (jc.getBoolean("IsOnlyFirstPrice") ? "不执行阶梯水价": "执行阶梯水价") + "]");
                firstLevelQty.setText("第一阶水量：" + jc.getString("FirstLevelQty"));
                secondLevelQty.setText("第二阶水量：" + jc.getString("SecondLevelQty"));
                thirdLevelQty.setText("第三阶水量：" + jc.getString("ThirdLevelQty"));

            } catch (JSONException e) {
                e.printStackTrace();
                showMessage(e.getMessage());
            }
            finally {
                loaded();
            }
        }

        @Override
        public void OnTaskFailed(Integer result, String message) {
            loaded();
            showMessage(message);
        }

        @Override
        public void onTaskException(Exception exception) {
            loaded();
            showMessage(getString(R.string.network_error));
        }
    };

    @Override
    public void notifyDataChanged() {
        Meter meter = mCC.getCurrentMeter();
        if(meter == null) return;
        if(meter.MeterID.equals(meterLoadState.MeterID)) return;

        meterLoadState.MeterID = meter.MeterID;
        meterLoadState.IsDataChanged = true;
        this.load();
    }

    @Override
    public void notifyFakeMeterChanged(FakeMeter[] fakeMeters) {

    }

    private void load() {
        if(!this.isAdded()) return;

        refreshLayout.setRefreshing(true);
        executeTask();
    }
    private void loading() {

    }
    private  void loaded() {
        meterLoadState.IsDataChanged = false;
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
            }
        });
        Log.d("Customer", "loaded");

    }
    private void showMessage(final String message) {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void executeTask() {
        loading();

        Log.d("Customer", "executeTasks");

        task = new SoapAsyncTask(SoapClient.GET_CUSTOMER_BY_METER_ID_METHOD, listener);
        task.execute(meterLoadState.MeterID);
    }

}
