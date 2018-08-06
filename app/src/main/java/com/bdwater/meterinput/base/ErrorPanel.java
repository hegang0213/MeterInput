package com.bdwater.meterinput.base;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bdwater.meterinput.R;

/**
 * Created by hegang on 16/6/28.
 */
public class ErrorPanel {
    public static final int NETWORK_ERROR = 0;
    public static final int GENERIC_ERROR = 401;

    private View root;
    private ImageView ic_network;
    private ImageView ic_error;
    private TextView error_message;
    private TextView error_message_detail;
    private Button retryButton;

    public Button getRetryButton() {
        return retryButton;
    }

    public ErrorPanel(View view) {
        root = view.findViewById(R.id.error_panel);
        ic_network = (ImageView)root.findViewById(R.id.ic_network);
        ic_error = (ImageView)root.findViewById(R.id.ic_error);
        error_message = (TextView)root.findViewById(R.id.error_message);
        error_message_detail = (TextView)root.findViewById(R.id.error_message_detail);
        retryButton = (Button)root.findViewById(R.id.error_retry_button);

        root.setVisibility(View.GONE);
    }
    public void show(int errorType, String message, String detail) {
        if(errorType == NETWORK_ERROR) {
            ic_network.setVisibility(View.VISIBLE);
            ic_error.setVisibility(View.GONE);
        } else if(errorType == GENERIC_ERROR) {
            ic_network.setVisibility(View.GONE);
            ic_error.setVisibility(View.VISIBLE);
        }

        if(message == null)
            error_message.setText(errorType == NETWORK_ERROR? "网络不稳定或不可用，请检查网络状况"
                    : "未知错误");
        else
            error_message.setText(message);

        if(detail == null)
            error_message_detail.setVisibility(View.INVISIBLE);
        else {
            error_message_detail.setText(detail);
            error_message_detail.setVisibility(View.VISIBLE);
        }
        root.setVisibility(View.VISIBLE);

    }
    public void hide() {
        root.setVisibility(View.GONE);
    }
}
