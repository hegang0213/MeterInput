package com.bdwater.meterinput.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.bdwater.meterinput.CApplication;
import com.bdwater.meterinput.R;
import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.soap.ISoapAsyncTaskListener;
import com.bdwater.meterinput.soap.SoapAsyncTask;
import com.bdwater.meterinput.soap.SoapClient;

import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by hegang on 16/9/9.
 */
public class MeterCommentDialogFragment extends DialogFragment {
    CApplication mApp = AppUtils.App;
    CurrentContext mCC = AppUtils.App.getCurrentContext();

    IMeterCommentChangedListener listener;

    View mContentView;
    //String comment;

    ProgressBar progressBar;
    EditText currentCommentEditText;
    EditText commentEditText;
    Button submitButton;
    Button closeButton;

    private SoapAsyncTask mTask;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL, R.style.fullDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        WindowManager.LayoutParams params = getDialog().getWindow()
                .getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().setAttributes(params);

        mContentView = inflater.inflate(R.layout.dialog_comment, container);

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

    void initContentView() {
        View view = mContentView;

        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        currentCommentEditText = (EditText)view.findViewById(R.id.currentCommentEditText);
        commentEditText = (EditText)view.findViewById(R.id.commentEditText);
        submitButton = (Button)view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(buttonListener);
        closeButton = (Button)view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        currentCommentEditText.setText(mCC.getCurrentMeter().CurrentComment);
        commentEditText.setText(mCC.getCurrentMeter().Comment);
    }


    public void setListener(IMeterCommentChangedListener l) {
        this.listener = l;
    }

    void onBeginning() {
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);
        closeButton.setEnabled(false);
    }
    void onCompleted() {
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                closeButton.setEnabled(true);
            }
        });

    }

    private Button.OnClickListener buttonListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            mTask = new SoapAsyncTask(SoapClient.UPDATE_METER_COMMENT_METHOD, taskListener);
            mTask.execute(mCC.getCurrentMeter().MeterID,
                    currentCommentEditText.getText().toString(),
                    commentEditText.getText().toString());
            onBeginning();
        }
    };
    private ISoapAsyncTaskListener taskListener = new ISoapAsyncTaskListener() {
        @Override
        public void onTaskSuccess(JSONObject obj) {
            onCompleted();

            if(listener != null)
                listener.OnChanged(currentCommentEditText.getText().toString(),
                        commentEditText.getText().toString());

            showSuccessMessage();
        }

        @Override
        public void OnTaskFailed(Integer result, String message) {
            onCompleted();
            showMessage(message);
        }

        @Override
        public void onTaskException(Exception exception) {
            onCompleted();
            showMessage(exception.getMessage());
        }
    };

    private void showSuccessMessage() {
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.success)
                        .setMessage(R.string.update_finish)
                        .create()
                        .show();
            }
        });
    }
    public void showMessage(final String message) {
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle("信息")
                        .setMessage(message)
                        .show();
            }
        });
    }


    // for external program to call, notify caller
    // that comment was changed
    public interface IMeterCommentChangedListener {
        void OnChanged(String currentComment, String comment);
    }

}
