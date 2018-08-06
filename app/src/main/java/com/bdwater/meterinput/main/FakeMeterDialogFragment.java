package com.bdwater.meterinput.main;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
//import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bdwater.meterinput.CApplication;
import com.bdwater.meterinput.R;
import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.base.DoubleHelper;
import com.bdwater.meterinput.model.FakeMeter;
import com.bdwater.meterinput.model.FakeTitle;
import com.bdwater.meterinput.model.FakeWaterPrice;
import com.bdwater.meterinput.model.Meter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hegang on 16/6/21.
 */
public class FakeMeterDialogFragment extends DialogFragment {
    View mContentView;

    CApplication mApp;
    CurrentContext mCC;
    Double mCustomerPrice;

    ListView mFakeMeterListView;
    ProgressBar mProgressBar;

    View mAddNewView;
    View mActionView;
    Button mAddButton;
    Button mDeleteButton;
    Button mSubmitButton;
    Button mCancelButton;

    EditText mNameEditText;
    Spinner mNameSpinner;
    Spinner mPriceSpinner;
    EditText mPayEditText;
    EditText mWaterQuantity;

    FakeMeterArrayAdapter mAdapter;
    List<FakeMeter> mFakeMeters = new ArrayList<FakeMeter>();

    IMeterChangedNotifier mMeterChangedNotifier;
    int mPosition = -1;

    public void setMeterChangedNotifier(IMeterChangedNotifier notifier) {
        mMeterChangedNotifier = notifier;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.fullDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        WindowManager.LayoutParams params = getDialog().getWindow()
                .getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        getDialog().setCanceledOnTouchOutside(false);
        //getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setAttributes(params);

        mContentView = inflater.inflate(R.layout.content_meter_input_fake, container);

        mApp = AppUtils.App;
        mCC = mApp.getCurrentContext();

        // price of main meter, it maybe contains
        // level price likes "3.55/7.99/10", so you need
        // deal with this situation, gets which levels will be
        String priceString = mCC.getCurrentMeter().Price.split("/")[0];
        mCustomerPrice = Double.parseDouble(priceString);

        cloneFakeMeters();
        initContentView();

        return mContentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void cloneFakeMeters() {
        Meter meter = mCC.getCurrentMeter();
        for(int i = 0; i < meter.getFakeMeters().length; i++) {
            FakeMeter clone = FakeMeter.clone(meter.getFakeMeters()[i]);
            mFakeMeters.add(clone);
        }
    }

    private void initContentView() {
        mFakeMeterListView = (ListView)mContentView.findViewById(R.id.fakeMeterListView);
        mAdapter = new FakeMeterArrayAdapter(getContext(), mFakeMeters);
        mFakeMeterListView.setAdapter(mAdapter);
        mFakeMeterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                mAdapter.notifyDataSetChanged();
                mDeleteButton.setEnabled(position >= 0);
            }
        });

        mFakeMeterListView.setEnabled(true);

        mProgressBar = (ProgressBar)mContentView.findViewById(R.id.progressBar);

        mAddNewView = mContentView.findViewById(R.id.addnewView);
        mActionView = mContentView.findViewById(R.id.actionView);
        mAddButton = (Button)mContentView.findViewById(R.id.addButton);
        mDeleteButton = (Button)mContentView.findViewById(R.id.deleteButton);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.remove(mAdapter.getItem(mPosition));
                mDeleteButton.setEnabled(mAdapter.getCount() > 0);
            }
        });
        mDeleteButton.setEnabled(false);
        mSubmitButton = (Button)mContentView.findViewById(R.id.submitButton);
        mCancelButton = (Button)mContentView.findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != mMeterChangedNotifier) {
                    FakeMeter[] array = (FakeMeter[])mFakeMeters.toArray(new FakeMeter[mFakeMeters.size()]);
                    mMeterChangedNotifier.notifyFakeMeterChanged(array);
                    dismiss();
                }
            }
        });
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNew();
            }
        });

        mNameEditText = (EditText)mContentView.findViewById(R.id.nameEditText);
        mNameSpinner = (Spinner)mContentView.findViewById(R.id.nameSpinner);
        mNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mNameEditText.setText(((FakeTitle)mNameSpinner.getSelectedItem()).Title);
                ((TextView)parent.getChildAt(0)).setTextColor(Color.TRANSPARENT);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mPriceSpinner = (Spinner)mContentView.findViewById(R.id.priceSpinner);
        mPriceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(modifySource == PAY_SOURCE)
                    calculate();
                else
                    calculateByQty();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ((EditText)mContentView.findViewById(R.id.customerPriceEditText)).setText(mCustomerPrice.toString());
        mPayEditText = (EditText)mContentView.findViewById(R.id.payEditText);
        mWaterQuantity = (EditText)mContentView.findViewById(R.id.waterQuantityEditText);
        mWaterQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateByQty();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPayEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ArrayAdapter< FakeTitle > titleArrayAdapter = new ArrayAdapter<FakeTitle>(mContentView.getContext(),
                android.R.layout.simple_spinner_item, mCC.getFakeTitles());
        titleArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNameSpinner.setAdapter(titleArrayAdapter);

        ArrayAdapter<FakeWaterPrice> priceAdapter = new ArrayAdapter<FakeWaterPrice>(mContentView.getContext(),
                android.R.layout.simple_spinner_item, mCC.getFakeWaterPrices());
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //第四步：将适配器添加到下拉列表上
        mPriceSpinner.setAdapter(priceAdapter);

        if(mCC.getCurrentMeter().IsPaid) {
            // the meter is checked
            mAddNewView.setVisibility(View.INVISIBLE);
            mActionView.setVisibility(View.INVISIBLE);
        } else {
            // the meter is unchecked

        }

        mPriceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private boolean isCalculating = false;
    private int modifySource = PAY_SOURCE;
    private static final int PAY_SOURCE = 316;
    private static final int QTY_SOURCE = 10;
    private void calculate() {
        if(isCalculating) return;

        modifySource = PAY_SOURCE;
        isCalculating = true;
        try {
            double pay = 0d;
            int qty = 0;
            String s = mPayEditText.getText().toString();
            if (s.length() > 0) {
                pay = Double.parseDouble(s.toString());
                Double fakePrice = ((FakeWaterPrice) mPriceSpinner.getSelectedItem()).Price;
                if (pay > 0) {
                    fakePrice = DoubleHelper.sub(fakePrice, mCustomerPrice);
                    qty = (int) DoubleHelper.divide(pay, fakePrice);
                }
                else
                    qty = 0;

            }
            mWaterQuantity.setText(Integer.toString(qty));
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            isCalculating = false;
        }
    }
    private void calculateByQty() {
        if(isCalculating) return;

        modifySource = QTY_SOURCE;
        isCalculating = true;
        try {
            double pay = 0d;
            int qty = 0;
            String s = mWaterQuantity.getText().toString();
            if (s.length() > 0) {
                qty = Integer.parseInt(s.toString());
                Double fakePrice = ((FakeWaterPrice) mPriceSpinner.getSelectedItem()).Price;
                if (qty > 0)
                    pay = DoubleHelper.multiply(qty, DoubleHelper.sub(fakePrice, mCustomerPrice));
                else
                    pay = 0;

            }
            mPayEditText.setText(Double.toString(pay));
        }
        finally {
            isCalculating = false;
        }
    }
    private Boolean validateNewItem() {
        Boolean result = true;
        String s = mWaterQuantity.getText().toString();
        if(s.length() == 0)
            result = false;
        else if(Integer.parseInt(s) <= 0)
            result = false;

        if(!result) {
            mPayEditText.setError("错误的金额");
            mPayEditText.requestFocus();
        }
        return result;
    }
    private void addNew() {
        if(validateNewItem()) {
            FakeMeter fm = new FakeMeter();
            fm.Title = mNameEditText.getText().toString(); //((FakeTitle)mNameSpinner.getSelectedItem()).Title;

            FakeWaterPrice fwp = (FakeWaterPrice)mPriceSpinner.getSelectedItem();
            fm.WaterPriceID = fwp.WaterPriceID;
            fm.FakePrice = DoubleHelper.sub(fwp.Price, mCustomerPrice);
            fm.WaterQuantity = (double)Integer.parseInt(mWaterQuantity.getText().toString());
            fm.Pay = Double.parseDouble(mPayEditText.getText().toString());
            //mFakeMeters.add(fm);
            mAdapter.add(fm);
            mAdapter.notifyDataSetChanged();
        }
    }

    class FakeMeterArrayAdapter extends ArrayAdapter<FakeMeter> {
        LayoutInflater mInflater;

        public FakeMeterArrayAdapter(Context context, List<FakeMeter> objects) {
            super(context, R.layout.fake_list_item, objects);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView != null) {
                holder = (ViewHolder)convertView.getTag();
            } else {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.fake_list_item, null);

                holder.nameCheckBox = (CheckBox) convertView.findViewById(R.id.nameCheckBox);
                holder.priceAndQtyTextView = (TextView)convertView.findViewById(R.id.priceAndQtyTextView);
                holder.payTextView = (TextView)convertView.findViewById(R.id.payTextView);
                convertView.setTag(holder);
            }
            FakeMeter fake = getItem(position);
            holder.nameCheckBox.setText(fake.Title);
            holder.priceAndQtyTextView.setText(fake.WaterQuantity + " | " + fake.FakePrice);
            holder.payTextView.setText(fake.Pay.toString());

            holder.nameCheckBox.setChecked(position == mPosition);
            return convertView;
        }

        private class ViewHolder {
            CheckBox nameCheckBox;
            TextView priceAndQtyTextView;
            TextView payTextView;
        }
    }
}
