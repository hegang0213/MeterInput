package com.bdwater.meterinput;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.base.NetworkUtils;
import com.bdwater.meterinput.model.Book;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.model.FakeTitle;
import com.bdwater.meterinput.model.FakeWaterPrice;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.model.User;
import com.bdwater.meterinput.model.WaterStatus;
import com.bdwater.meterinput.soap.ISoapAsyncTaskListener;
import com.bdwater.meterinput.soap.SoapAsyncTask;
import com.bdwater.meterinput.soap.SoapClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private SoapAsyncTask mAuthTask = null;

    // UI references.
    private EditText mUserNameView;
    private EditText mPasswordView;
    private Button mSignInButton;
    private View mProgressView;
    private View mLoginFormView;

    private CApplication mApp;
    private CurrentContext mCC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        String s = "你好，李某某，电话13903120200，其他内容";
        Pattern p = Pattern.compile("1[358]");
        Matcher m = p.matcher(s);
        if(m.find())
            System.out.printf("is found.");

        mApp = (CApplication)getApplication();
        mCC = mApp.getCurrentContext();
        mApp.loadSharedPreferences();
        NetworkUtils.setWebUrl();

        if(AppUtils.App == null)
            AppUtils.App = mApp;

        // Set up the login form.
        mUserNameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        if(mCC.getUser() != null) {
            mUserNameView.setText(mCC.getUser().Pinyin);
            mPasswordView.requestFocus();
        }

        mSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        AppUtils.closeSoftInput(LoginActivity.this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        /*if (mAuthTask != null) {
            return;
        }*/

        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        }
        else if (!isUserNameValid(username)) {
            mUserNameView.setError(getString(R.string.error_invalid_username));
            focusView = mUserNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            if(mCC.getCurrentBook() != null) {
                mAuthTask = new SoapAsyncTask(SoapClient.LOGIN_WITH_CURRENT_METHOD, listener);
                mAuthTask.execute(username, password, mCC.getCurrentBook().BookID);
            } else {
                mAuthTask = new SoapAsyncTask(SoapClient.LOGIN_METHOD, listener);
                mAuthTask.execute(username, password);
            }
        }
    }

    private boolean isUserNameValid(String email) {
        //TODO: Replace this with your own logic
        //return email.contains("@");
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//        }
        mUserNameView.setEnabled(!show);
        mPasswordView.setEnabled(!show);
        mSignInButton.setEnabled(!show);
    }

    void onLogonFailed() {
        showProgress(false);
        if(NetworkUtils.webUrlCount() - 1 > NetworkUtils.getCurrentUrlIndex()) {
            NetworkUtils.setWebUrl();
            attemptLogin();
        }
    }
    void onLogonSuccess() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    ISoapAsyncTaskListener listener = new ISoapAsyncTaskListener() {
        @Override
        public void onTaskSuccess(JSONObject obj) {
            try {
                // the user has been logon, get data
                JSONObject jsonObject = obj.getJSONObject("Data");

                // gets user info and puts it into current context
                CurrentContext cc = mApp.getCurrentContext();
                User user = new User();
                user.UserID = jsonObject.getString("UserID");
                user.Name = jsonObject.getString("Name");
                user.Pinyin = mUserNameView.getText().toString();
                cc.setUser(user);

                // get books of this user
                JSONArray jsonBooks = jsonObject.getJSONArray("Books");
                int length = jsonBooks.length();
                Book[] books = new Book[length];

                // gets id of current book
                Boolean isFound = false;
                String currentBookID = "";
                if(cc.getCurrentBook() != null)
                    currentBookID = cc.getCurrentBook().BookID;

                for(int i = 0; i < length; i++) {
                    JSONObject jsonBook = jsonBooks.getJSONObject(i);
                    Book book = new Book();
                    book.BookID = jsonBook.getString("BookID");
                    book.BookNo = jsonBook.getString("BookNo");
                    book.Title = jsonBook.getString("Title");
                    books[i] = book;

                    if(currentBookID.equals(book.BookID))
                        isFound = true;
                }

                // get meters of current book
                JSONArray jsonMeters = jsonObject.getJSONArray("Meters");
                length = jsonMeters.length();
                Meter[] meters = new Meter[length];
                for(int i = 0; i < length; i++) {
                    JSONObject jsonMeter = jsonMeters.getJSONObject(i);
                    meters[i] = Meter.parseSimple(jsonMeter);
                }
                cc.setMeters(meters);

                JSONArray jsonWaterStatus = jsonObject.getJSONArray("WaterStatus");
                length = jsonWaterStatus.length();
                WaterStatus[] waterStatuses = new WaterStatus[length];
                for(int i = 0; i < length; i++) {
                    WaterStatus ws = new WaterStatus();
                    ws.Title = jsonWaterStatus.getJSONObject(i).getString("Title");
                    waterStatuses[i] = ws;
                }
                cc.setWaterStatuses(waterStatuses);

                // get fake titles
                JSONArray jsonFakeTitles = jsonObject.getJSONArray("FakeTitles");
                length = jsonFakeTitles.length();
                FakeTitle[] fakeTitles = new FakeTitle[length];
                for (int i = 0; i < length; i++) {
                    FakeTitle ft = new FakeTitle();
                    ft.Title = jsonFakeTitles.getJSONObject(i).getString("Title");
                    fakeTitles[i] = ft;
                }
                cc.setFakeTitles(fakeTitles);

                // get fake prices
                JSONArray jsonFakePrices = jsonObject.getJSONArray("FakePrices");
                length = jsonFakePrices.length();
                FakeWaterPrice[] fakeWaterPrices = new FakeWaterPrice[length];
                for(int i = 0; i < length; i++) {
                    JSONObject jsonFakePrice = jsonFakePrices.getJSONObject(i);
                    FakeWaterPrice fwp = new FakeWaterPrice();
                    fwp.WaterPriceID = jsonFakePrice.getString("WaterPriceID");
                    fwp.Price = jsonFakePrice.getDouble("Price");
                    fakeWaterPrices[i] = fwp;
                }
                cc.setFakeWaterPrices(fakeWaterPrices);

                // select first book if no book was chosen by user
                cc.setBooks(books);
                if(books.length > 0 && !isFound)
                    cc.setCurrentBook(books[0]);

                // save user and current book for next login
                mApp.saveUser(user);
                if(cc.getCurrentBook() != null)
                    mApp.saveCurrentBook(cc.getCurrentBook());

                if(books.length > 0)
                    onLogonSuccess();
                else {
                    mUserNameView.setError(getString(R.string.error_invalid_username));
                    mUserNameView.requestFocus();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                onLogonFailed();
                AppUtils.showAlertDialog(LoginActivity.this, "错误", e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                onLogonFailed();
                AppUtils.showAlertDialog(LoginActivity.this, "错误", e.getMessage());
            }
        }

        @Override
        public void OnTaskFailed(Integer result, final String message) {

            mPasswordView.post(new Runnable() {
                @Override
                public void run() {
                    mPasswordView.setError(message);
                    onLogonFailed();
                }
            });

        }

        @Override
        public void onTaskException(Exception exception) {
            final String message = exception.getMessage();
            mPasswordView.post(new Runnable() {
                @Override
                public void run() {
                    onLogonFailed();
                    AppUtils.showAlertDialog(LoginActivity.this, "错误", message);
                }
            });


        }
    };

}

