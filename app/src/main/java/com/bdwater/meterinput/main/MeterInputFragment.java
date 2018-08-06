package com.bdwater.meterinput.main;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bdwater.meterinput.CApplication;
import com.bdwater.meterinput.R;
import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.base.DoubleHelper;
import com.bdwater.meterinput.base.ErrorPanel;
import com.bdwater.meterinput.model.FakeMeter;
import com.bdwater.meterinput.model.FakeWaterPrice;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.base.MeterEditMode;
import com.bdwater.meterinput.base.NumberUtils;
import com.bdwater.meterinput.model.WaterStatus;
import com.bdwater.meterinput.soap.ISoapAsyncTaskListener;
import com.bdwater.meterinput.soap.SoapAsyncTask;
import com.bdwater.meterinput.soap.SoapClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeterInputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeterInputFragment extends Fragment implements IMeterChangedNotifier {
    CApplication mApp;
    CurrentContext mCC;

    private MeterEditMode mMeterEditMode = MeterEditMode.NoCheck;

    private String mMeterIDString;
    private Boolean mIsDataChanged = false;
    private Boolean mIsLoaded = false;
    private SoapAsyncTask mTask;

    List<View> noFakeSections = new ArrayList<View>();
    List<View> fakeSections = new ArrayList<View>();

    private ProgressBar progressBar;
    private View contentView;
    private View actionView;

    private View fakeIndicatorView;
    private View maintenceView;

    // no fake meter controls
    private EditText customerNoEditText;
    private EditText meterPositionEditText;
    private EditText waterStatusEditText;
    private EditText nameEditText;
    private EditText lastDateEditText;
    private EditText lastValueEditText;
    private EditText currentDateEditText;
    private EditText currentValueEditText;

    private CheckBox baseValueCheckBox;
    private Switch baseValueSwitch;
    private EditText baseValueEditText;
    private CheckBox isSerialMeterCheckBox;
    private EditText serialValueEditText;
    private EditText waterQuantityEditText;
    private EditText currentPayEditText;
    private EditText payBeforeEditText;

    private View waterStatusView;
    private Spinner waterStatusSpinner;
    private EditText currentCommentEditText;

    private EditText fakeListTotalPayTextEdit;
    private Button fakeListButton;
    private Button submitButton;

    private EditText commentEditText;
    private Button editCommentButton;

    DatePickerDialog datePickerDialog;

    FakeMeterControl fakeMeterControl;
    ErrorPanel errorPanel;

    public MeterInputFragment() {
        // Required empty public constructor
        mApp = AppUtils.App;
        mCC = mApp.getCurrentContext();
    }

    // TODO: Rename and change types and number of parameters
    public static MeterInputFragment newInstance() {
        MeterInputFragment fragment = new MeterInputFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    SimpleDateFormat dateFormatter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meter_input, container, false);

        errorPanel = new ErrorPanel(view);
        errorPanel.getRetryButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        TableLayout tl = (TableLayout)view.findViewById(R.id.tableLayout);
        for(int i = 0; i < tl.getChildCount(); i++) {
            View child = tl.getChildAt(i);
            Object tag = child.getTag();
            if(null == tag)
                continue;
            if(tag.toString().equals("NoFakeMeterSection"))
                noFakeSections.add(child);
            else
                fakeSections.add(child);
        }

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE);
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getContext(), null, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setCanceledOnTouchOutside(true);
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(datePickerDialog.getDatePicker().getYear(),
                        datePickerDialog.getDatePicker().getMonth(),
                        datePickerDialog.getDatePicker().getDayOfMonth());
                currentDateEditText.setText(dateFormatter.format(newDate.getTime()));
            }
        });
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        contentView = view.findViewById(R.id.contentView);
        actionView = view.findViewById(R.id.actionView);

        progressBar.setVisibility(View.GONE);
        if(!mIsLoaded) {
            contentView.setVisibility(View.GONE);
            actionView.setVisibility(View.GONE);
        }

        fakeIndicatorView = view.findViewById(R.id.fakeIndicatorView);

        customerNoEditText = (EditText)view.findViewById(R.id.customNoEditText);
        meterPositionEditText = (EditText)view.findViewById(R.id.meterPositionEditText);
        waterStatusEditText = (EditText)view.findViewById(R.id.waterStatusEditText);
        nameEditText = (EditText)view.findViewById(R.id.nameEditText);
        lastDateEditText = (EditText)view.findViewById(R.id.lastDateEditText);
        lastValueEditText = (EditText)view.findViewById(R.id.lastValueEditText);
        currentDateEditText = (EditText)view.findViewById(R.id.currentDateEditText);
        currentDateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                datePickerDialog.show();
                return false;
            }
        });
        currentValueEditText = (EditText) view.findViewById(R.id.currentValueEditText);
        currentValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCC.getCurrentMeter().CurrentValue = NumberUtils.toInt(s.toString(), 0);
                calculate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        currentValueEditText.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    currentValueEditText.selectAll();
                else {
                    if(currentValueEditText.getText().toString().trim().equals(""))
                        currentValueEditText.setText("0");
                    //calculate();
                }
            }
        });

        baseValueCheckBox = (CheckBox)view.findViewById(R.id.baseValueCheckBox);
        baseValueCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                baseValueSwitch.setEnabled(isChecked);
                baseValueEditText.setEnabled(isChecked);

                if(isChecked) {
                    mCC.getCurrentMeter().MeterStatus = baseValueSwitch.isChecked() ? 2 : 1;
                    baseValueEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                }
                else
                    mCC.getCurrentMeter().MeterStatus = 0;
                calculate();
            }
        });
        baseValueSwitch = (Switch)view.findViewById(R.id.baseValueSwitch);
        baseValueSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCC.getCurrentMeter().MeterStatus = isChecked ? 2: 1;
                //calculate();
            }
        });
        baseValueEditText = (EditText)view.findViewById(R.id.baseValueEditText);
        baseValueEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        baseValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCC.getCurrentMeter().BaseValue = NumberUtils.toInt(s.toString(), 0);
                calculate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        baseValueEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    if(baseValueEditText.getText().toString().trim().equals(""))
                        baseValueEditText.setText("0");
            }
        });

        isSerialMeterCheckBox = (CheckBox)view.findViewById(R.id.isSerialMeterCheckBox);
        isSerialMeterCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCC.getCurrentMeter().IsSerialMeter = isChecked;
                serialValueEditText.setEnabled(isChecked);
                if(isChecked)
                    serialValueEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                calculate();
            }
        });
        serialValueEditText = (EditText)view.findViewById(R.id.serialValueEditText);
        serialValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCC.getCurrentMeter().SerialValue = NumberUtils.toInt(s.toString(), 0);
                calculate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        serialValueEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(serialValueEditText.getText().toString().trim().equals(""))
                    serialValueEditText.setText("0");
            }
        });

        waterQuantityEditText = (EditText)view.findViewById(R.id.waterQuantityEditText);
        currentPayEditText = (EditText)view.findViewById(R.id.currentPayEditText);

        payBeforeEditText = (EditText)view.findViewById(R.id.payBeforeEditText);

        submitButton = (Button)view.findViewById(R.id.submitButton);
        fakeListTotalPayTextEdit = (EditText)view.findViewById(R.id.fakeTotalPayEditText);
        fakeListButton = (Button) view.findViewById(R.id.fakeButton);
        fakeListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FakeMeterDialogFragment fmDialog = new FakeMeterDialogFragment();
                fmDialog.setMeterChangedNotifier((IMeterChangedNotifier)MeterInputFragment.this);
                fmDialog.show(getActivity().getSupportFragmentManager(), "EditNameDialog");
            }
        });

        waterStatusView = view.findViewById(R.id.waterStatusView);
        waterStatusSpinner = (Spinner)view.findViewById(R.id.waterStatusSpinner);
        final ArrayAdapter<WaterStatus> waterStatusAdapter = new ArrayAdapter<WaterStatus>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                mCC.getWaterStatuses());
        //mCC.getFakeWaterPricesGreatThen(Double.valueOf(mCC.getCurrentMeter().Price)));
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        waterStatusAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        waterStatusSpinner.setAdapter(waterStatusAdapter);
        waterStatusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(waterStatusSpinner.getTag() == null) { // view.getTag() == null) {
                    waterStatusSpinner.setTag(true); //view.setTag(true);
                    return;
                }

                String title = ((WaterStatus)waterStatusAdapter.getItem(position)).Title;
                //Meter meter = mCC.getCurrentMeter();
                setCurrentCommentByTitle(position == 0? "": title);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        currentCommentEditText = (EditText)view.findViewById(R.id.currentCommentEditText);
        currentCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 0)
                    mCC.getCurrentMeter().CurrentComment = s.toString();
                else
                    mCC.getCurrentMeter().CurrentComment = "";
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // just for comment
        commentEditText = (EditText)view.findViewById(R.id.commentEditText);
        editCommentButton = (Button)view.findViewById(R.id.editCommentButton);
        final MeterCommentDialogFragment.IMeterCommentChangedListener listener = new MeterCommentDialogFragment.IMeterCommentChangedListener() {
            @Override
            public void OnChanged(String currentComment, String comment) {
                currentCommentEditText.setText(currentComment);
                commentEditText.setText(comment);
                mCC.getCurrentMeter().CurrentComment = currentComment;
                mCC.getCurrentMeter().Comment = comment;
            }
        };
        editCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MeterCommentDialogFragment fmDialog = new MeterCommentDialogFragment();
                //fmDialog.setComment(mCC.getCurrentMeter().Comment);
                fmDialog.setListener(listener);
                fmDialog.show(getActivity().getSupportFragmentManager(), "");

            }
        });

        fakeIndicatorView.setVisibility(View.INVISIBLE);
        maintenceView = view.findViewById(R.id.maintenceView);
        maintenceView.setVisibility(View.GONE);

        submitButton = (Button)view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        fakeMeterControl = new FakeMeterControl();
        fakeMeterControl.fakeLastDateEditText = (EditText)view.findViewById(R.id.fakeLastDateEditText);
        fakeMeterControl.fakeCurrentDateEditText = (EditText)view.findViewById(R.id.fakeCurrentDateEditText);
        fakeMeterControl.fakeWaterPriceSpinner = (Spinner)view.findViewById(R.id.fakeWaterPriceSpinner);
        fakeMeterControl.fakeWaterQuantityEditText = (EditText)view.findViewById(R.id.fakeWaterQuantityEditText);
        fakeMeterControl.fakePriceEditText = (EditText)view.findViewById(R.id.fakePriceEditText);
        fakeMeterControl.fakeMainMeterPriceEditText = (EditText)view.findViewById(R.id.fakeMainMeterPriceEditText);
        fakeMeterControl.fakePayEditText = (EditText)view.findViewById(R.id.fakePayEditText);
        fakeMeterControl.fakeCommentEditText = (EditText)view.findViewById(R.id.fakeCommentEditText);
        ArrayAdapter<FakeWaterPrice> priceAdapter = new ArrayAdapter<FakeWaterPrice>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                mCC.getFakeWaterPrices());
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        priceAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //第四步：将适配器添加到下拉列表上
        fakeMeterControl.fakeWaterPriceSpinner.setAdapter(priceAdapter);
        fakeMeterControl.fakeWaterQuantityEditText.addTextChangedListener(fakeMeterControl.qtyListener);
        fakeMeterControl.fakeWaterPriceSpinner.setOnItemSelectedListener(fakeMeterControl.spinnerListener);
        fakeMeterControl.fakeCurrentDateEditText.setOnTouchListener(fakeMeterControl.currentDateTouch);

        if(mIsDataChanged)
            load();

        return view;
    }
    private void setCurrentCommentByTitle(String title) {
        String current = currentCommentEditText.getText().toString();
        String content = title == "" ? "": title + " - ";
        if(current.length() == 0)
            currentCommentEditText.setText(content);
        else {
            int index = current.indexOf('-');
            if(index > 0) {
                current = current.substring(index + 1);
                if(current.startsWith(" ")) {
                    current = current.substring(1);
                }
            }

            currentCommentEditText.setText(content + current);
        }

    }
    // update UIs' value from meter
    private void updateUI(Meter meter) {
        customerNoEditText.setText(meter.CustomerNo);
        meterPositionEditText.setText(meter.MeterPosition);
        switch (meter.WaterStatus) {
            case 0:
                waterStatusEditText.setText(R.string.meter_unchecked);
                break;
            case 1:
                waterStatusEditText.setText(R.string.meter_has_checked);
                break;
            default:
                waterStatusEditText.setText(meter.WaterStatus.toString());
                break;
        }

        nameEditText.setText(meter.Name);
        lastDateEditText.setText(meter.LastDate);
        lastValueEditText.setText(meter.LastValue.toString());
        currentDateEditText.setText(meter.CurrentDate);
        currentValueEditText.setText(meter.CurrentValue.toString());
        payBeforeEditText.setText(meter.PayBefore);

        switch (meter.MeterStatus) {
            case 0:
                baseValueCheckBox.setChecked(false);
                break;
            case 1:
                baseValueSwitch.setChecked(false);
                baseValueCheckBox.setChecked(true);
                break;
            case 2:
                baseValueSwitch.setChecked(true);
                baseValueCheckBox.setChecked(true);
                break;
        }
        baseValueEditText.setText(meter.BaseValue.toString());

        isSerialMeterCheckBox.setChecked(meter.IsSerialMeter);
        serialValueEditText.setText(meter.SerialValue.toString());
        waterQuantityEditText.setText(meter.WaterQuantity.toString() + "  (" + meter.Price + ")");
        currentPayEditText.setText(meter.CurrentPay.toString());
        if(meter.WaterStatus == 0)
            currentPayEditText.setText("提交后获取金额");

        waterStatusSpinner.setTag(null);
        String[] cm = meter.CurrentComment.split("-");
        int waterStatusIndexOf = 1;
        if(cm.length > 1)
           waterStatusIndexOf = mCC.indexOfWaterStatuses(cm[0].trim());
        waterStatusSpinner.setSelection(waterStatusIndexOf);

        currentCommentEditText.setText(meter.CurrentComment);
        maintenceView.setVisibility(meter.MeterTaskStatus == 0? View.GONE: View.VISIBLE);
        commentEditText.setText(meter.Comment);

        updateFakePayUI(meter.getFakePay(), meter.getFakeMeters().length);

        mIsDataChanged = false;

        fakeMeterControl.fakeLastDateEditText.setText(meter.LastDate);
        fakeMeterControl.fakeCurrentDateEditText.setText(meter.CurrentDate);
        if(meter.IsFake && meter.getFakeMeters().length == 0) {
            // no checked
            FakeMeter fm = FakeMeter.createForOnlyFake();
            fm.Price = meter.Price;
            meter.setFakeMeters(new FakeMeter[] { fm });
        }
        if(meter.IsFake) {
            FakeMeter fm = meter.getFakeMeters()[0];
            fakeMeterControl.preventCalculate = true;
            fakeMeterControl.fakeWaterPriceSpinner.setSelection(mCC.indexOfFakeWaterPrice(fm.WaterPriceID));
            fakeMeterControl.fakePriceEditText.setText(fm.FakePrice.toString());
            fakeMeterControl.fakeMainMeterPriceEditText.setText(meter.Price);
            fakeMeterControl.fakePayEditText.setText(fm.Pay.toString());

            // place this control at end, because this control will cause calculate
            fakeMeterControl.fakeWaterQuantityEditText.setText(String.valueOf(fm.WaterQuantity));
            fakeMeterControl.fakeCommentEditText.setText("");
            fakeMeterControl.preventCalculate = false;
        }

        if(meter.IsPaid)
            mMeterEditMode = MeterEditMode.HasPaid;
        else if (meter.WaterStatus == 0)
            mMeterEditMode = MeterEditMode.NoCheck;
        else
            mMeterEditMode = MeterEditMode.HasChecked;
        setEditModeState();
    }
    ISoapAsyncTaskListener mTaskListener = new ISoapAsyncTaskListener() {

        @Override
        public void onTaskSuccess(JSONObject obj) {
            Meter meter = mCC.getCurrentMeter();
            try {
                JSONObject jsonMeter = obj.getJSONObject("Data");
                Meter.Load(meter, jsonMeter);

                updateUI(meter);
                if(bySubmit)
                    new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.success)
                        .setMessage(R.string.update_finish)
                        .create()
                        .show();
            } catch (JSONException e) {
                e.printStackTrace();
                loaded();
                showMessage(e.getMessage());

            } catch (Exception e) {
                e.printStackTrace();
                loaded();
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
            setEditModeState();
        }

        @Override
        public void onTaskException(Exception exception) {
            final Exception ex = exception;
            errorPanel.getRetryButton().post(new Runnable() {
                @Override
                public void run() {
                    loaded();
                    if(ex instanceof IOException) {
                        errorPanel.show(ErrorPanel.NETWORK_ERROR, null, ex.getMessage());
                    } else {
                        showMessage(ex.getMessage());
                    }
                }
            });
        }
    };

    public void notifyDataChanged() {
        String meterID = "";
        if(mCC.getCurrentMeter() == null) return;
        if(mCC.getCurrentMeter().MeterID.equals(mMeterIDString)) return;

        meterID = mCC.getCurrentMeter().MeterID;
        mIsDataChanged = true;
        mMeterIDString = meterID;

        load();
    }
    @Override
    public void notifyFakeMeterChanged(FakeMeter[] fakeMeters) {
        mCC.getCurrentMeter().setFakeMeters(fakeMeters);
        updateFakePayUI(mCC.getCurrentMeter().getFakePay(), fakeMeters.length);
    }

    // updates UI's value of sum of sub fake meter
    private void updateFakePayUI(Double fakePay, int fakeMeterCount) {
        fakeListTotalPayTextEdit.setText(fakePay.toString());
        if(!mCC.getCurrentMeter().IsFake)
            ((TextInputLayout)fakeListTotalPayTextEdit.getParent()).setHint("补差金额" + "(表数：" + fakeMeterCount + ")");
    }

    public void showMessage(String message) {
        new AlertDialog.Builder(getActivity())
                .setTitle("信息")
                .setMessage(message)
                .show();
    }
    // loads current meter
    public void load() {
        if(!isAdded()) return;
        if(!mIsLoaded || mIsDataChanged)
            loadMeter(mMeterIDString);
    }
    public void refresh() {
        if(!isAdded()) return;

        loadMeter(mMeterIDString);
    }
    private void loadMeter(String meterIDString) {
        if(mMeterIDString == null) return;

        if(mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING)
            mTask.cancel(true);

        bySubmit = false;
        loading();

        mTask = new SoapAsyncTask(SoapClient.GET_METER_BYID_METHOD, mTaskListener);
        mTask.execute(meterIDString);
    }
    private void loading() {
        progressBar.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
        actionView.setVisibility(View.GONE);
    }
    private void loaded() {
        bySubmit = false;
        mIsLoaded = true;

        progressBar.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);

        errorPanel.hide();
    }

    private boolean bySubmit;
    private void updateMeter() throws JSONException {
        bySubmit = true;
        loading();

        String jsonString = mCC.getCurrentMeter().toJsonUpdateMeter();
        mTask = new SoapAsyncTask(SoapClient.UPDATE_METER_METHOD, mTaskListener);
        mTask.execute(jsonString);

        Log.d("MeterInput", jsonString);
    }
    private void updateFakeMeter() throws JSONException {
        bySubmit = true;
        loading();

        String jsonString = mCC.getCurrentMeter().toJsonFakeMeter();
        mTask = new SoapAsyncTask(SoapClient.UPDATE_FAKE_METER_METHOD, mTaskListener);
        mTask.execute(jsonString);

        Log.d("MeterInput", jsonString);

    }

    private void disableAll() {
        // current value
        currentDateEditText.setEnabled(false);
        currentValueEditText.setEnabled(false);
        currentValueEditText.setError(null);
        //setEditableInteger(currentValueEditText, false);

        // base value
        baseValueCheckBox.setEnabled(false);
        baseValueSwitch.setEnabled(false);
        baseValueEditText.setEnabled(false);
        //setEditableInteger(baseValueEditText, false);

        // serial value
        isSerialMeterCheckBox.setEnabled(false);
        serialValueEditText.setEnabled(false);

        waterStatusView.setVisibility(View.GONE);
        waterStatusSpinner.setEnabled(false);
        currentCommentEditText.setEnabled(false);

        //setEditableInteger(serialValueEditText, false);

        // no fake
        fakeIndicatorView.setVisibility(View.INVISIBLE);
        fakeListTotalPayTextEdit.setEnabled(false);
        fakeListButton.setEnabled(false);

        // no action
        actionView.setVisibility(View.GONE);

        // fake meter mode
        fakeMeterControl.fakeWaterPriceSpinner.setEnabled(false);
        TextView tv = (TextView)fakeMeterControl.fakeWaterPriceSpinner.getSelectedView();
        if(tv != null) tv.setError(null);
        fakeMeterControl.fakeWaterQuantityEditText.setEnabled(false);
        fakeMeterControl.fakeWaterQuantityEditText.setError(null);
        fakeMeterControl.fakeLastDateEditText.setVisibility(View.GONE);
        fakeMeterControl.fakeCurrentDateEditText.setEnabled(false);
    }

    private void setEditModeState() {
        if(mMeterEditMode == MeterEditMode.HasPaid)
            setReadonlyState();
        else
            setCheckEditState();

        showOrHideFakeMeterSections();
    }
    private void setReadonlyState() {
        disableAll();

        showOrHideFakeMeterSections();

    }
    private void setCheckEditState() {
        disableAll();

        currentDateEditText.setEnabled(false);
        currentValueEditText.setEnabled(true);
        baseValueCheckBox.setEnabled(true);
        isSerialMeterCheckBox.setEnabled(true);

        waterStatusView.setVisibility(View.VISIBLE);
        waterStatusSpinner.setEnabled(true);
        currentCommentEditText.setEnabled(true);

        actionView.setVisibility(View.VISIBLE);

        // fake meter mode
        fakeMeterControl.fakeWaterPriceSpinner.setEnabled(true);
        fakeMeterControl.fakeWaterQuantityEditText.setEnabled(true);
        fakeMeterControl.fakeCurrentDateEditText.setEnabled(false);

        fakeListButton.setEnabled(true);
        showOrHideFakeMeterSections();
    }

    private void showOrHideFakeMeterSections() {
        // show or hide fake control
        if(mCC.getCurrentMeter().IsFake) {
            fakeIndicatorView.setVisibility(View.VISIBLE);
            fakeMeterControl.fakeLastDateEditText.setVisibility(View.VISIBLE);
            for(View child: fakeSections) { child.setVisibility(View.VISIBLE); }
            for(View child: noFakeSections) { child.setVisibility(View.GONE); }
            fakeListButton.setVisibility(View.GONE);
        } else {
            fakeIndicatorView.setVisibility(View.GONE);
            for(View child: fakeSections) { child.setVisibility(View.GONE); }
            for(View child: noFakeSections) { child.setVisibility(View.VISIBLE); }
            fakeListButton.setVisibility(View.VISIBLE);
        }
    }

    private void calculate() {
        Meter meter = mCC.getCurrentMeter();
        int qty = 0;
        if(meter.MeterStatus == 0)
            qty = meter.CurrentValue - meter.LastValue;
        else
            qty = meter.CurrentValue + meter.BaseValue;

        if(meter.IsSerialMeter)
            qty = qty - meter.SerialValue;

        meter.WaterQuantity = qty;
        waterQuantityEditText.setText(String.valueOf(qty) + " | ¥" + meter.Price );
        //currentPayEditText.setText("¥" + DoubleHelper.multiply(qty, Double.parseDouble(meter.Price)));
    }
    private boolean validate() {
        boolean result = true;
        Meter meter = mCC.getCurrentMeter();
        if(meter.IsFake) {
            ((TextView)fakeMeterControl.fakeWaterPriceSpinner.getSelectedView()).setError(null);
            fakeMeterControl.fakeWaterQuantityEditText.setError(null);

            FakeMeter fm = meter.getFakeMeters()[0];
            if(fm.FakePrice <= 0) {
                ((TextView)fakeMeterControl.fakeWaterPriceSpinner.getSelectedView()).setError("错误的水价");
                return false;
            }
            if(fm.WaterQuantity == 0) {
                fakeMeterControl.fakeWaterQuantityEditText.setError("水量必须大于零");
                return false;
            }

        } else {
            if(meter.WaterQuantity <= 0) {
                currentValueEditText.setError("错误：计算的水量小于零");
                return false;
            }

        }
        return result;
    }
    private void submit() {

        // validate user input
        if(!validate()) return;

        // close soft input
        AppUtils.closeSoftInput(getActivity());

        try {
            Meter meter = mCC.getCurrentMeter();
            //meter.CurrentComment = currentCommentEditText.getText().toString();
            if(mCC.getCurrentMeter().IsFake) {
                updateFakeMeter();
            }
            else {
                //final boolean[] allow = {false};
                if(mCC.getCurrentMeter().WaterQuantity > mCC.getCurrentMeter().AverageWater) {
                    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setMessage("您当前数据输入的水量已超出此用户最近时期月用水，是否仍然要提交抄表信息？");
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "仍然继续", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //allow[0] = true;
                            try {
                                MeterInputFragment.this.updateMeter();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else {
                    updateMeter();
                }
//                else
//                    allow[0] = true;
//                if(allow[0])
//                    updateMeter();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MeterInputFragment", e.getMessage());
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("MeterInputFragment","onStart");

        if(!mIsLoaded && mMeterIDString != null) {
            Log.d("MeterInputFragment","lazy load...");
            refresh();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MeterInputFragment","onDestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("MeterInputFragment","onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("MeterInputFragment","onDetach");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MeterInputFragment","onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MeterInputFragment","onResume");
    }

    // about only fake meter's control
    class FakeMeterControl {
        EditText fakeLastDateEditText;
        EditText fakeCurrentDateEditText;

        Spinner fakeWaterPriceSpinner;
        EditText fakeWaterQuantityEditText;
        EditText fakePriceEditText;
        EditText fakeMainMeterPriceEditText;
        EditText fakePayEditText;
        EditText fakeCommentEditText;

        boolean preventCalculate = false;

        private DatePickerDialog fakeCurrentDatePickDialog;
        private SimpleDateFormat dateFormatter;
        public FakeMeterControl() {
            this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE);
            Calendar newCalendar = Calendar.getInstance();
            this.fakeCurrentDatePickDialog = new DatePickerDialog(getContext(), null, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
            this.fakeCurrentDatePickDialog.setCanceledOnTouchOutside(true);
            this.fakeCurrentDatePickDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(fakeCurrentDatePickDialog.getDatePicker().getYear(),
                            fakeCurrentDatePickDialog.getDatePicker().getMonth(),
                            fakeCurrentDatePickDialog.getDatePicker().getDayOfMonth());
                    fakeCurrentDateEditText.setText(dateFormatter.format(newDate.getTime()));
                }
            });
            this.fakeCurrentDatePickDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }

        EditText.OnTouchListener currentDateTouch = new EditText.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fakeCurrentDatePickDialog.show();
                return false;
            }
        };

        EditText.OnFocusChangeListener qtyFocusChangedListener = new EditText.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!mCC.getCurrentMeter().IsFake) return;

                if(hasFocus && fakeWaterQuantityEditText.getText().toString().trim().equals(""))
                    fakeWaterQuantityEditText.setText("0");
            }
        };
        TextWatcher qtyListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!mCC.getCurrentMeter().IsFake) return;

                mCC.getCurrentMeter().getFakeMeters()[0].WaterQuantity = NumberUtils.toDouble(s.toString(), 0);
                calculate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!mCC.getCurrentMeter().IsFake) return;

                calculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        // calculate fake price and pay
        public void calculate() {
            if(preventCalculate) return;

            FakeMeter fm = mCC.getCurrentMeter().getFakeMeters()[0];
            FakeWaterPrice fwp = (FakeWaterPrice)fakeWaterPriceSpinner.getSelectedItem();
            fm.WaterPriceID = fwp.WaterPriceID;
            fm.FakePrice = DoubleHelper.sub(fwp.Price, Double.valueOf(mCC.getCurrentMeter().Price));
            fm.Pay = DoubleHelper.multiply(fm.FakePrice, fm.WaterQuantity);

            fakePriceEditText.setText(fm.FakePrice.toString());
            fakePayEditText.setText(fm.Pay.toString());
        }
    }
}
