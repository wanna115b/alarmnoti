package com.wanna.app.alarmnoti.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.util.AuthPreferences;


public class MainActivity extends Activity {
    private final String TAG = "MainActivity";
    private final String SCOPE = "https://www.googleapis.com/auth/calendar.readonly";
    private final int AUTHORIZATION_CODE = 1993;
    private final int ACCOUNT_CODE = 1601;

    private Button mStartBt;

    private AuthPreferences mAuthPreferences;
    private AccountManager mAccountManager;

    /*
    private WebView mWebview;
    private final String CLIENT_ID = "529681893968-uaut43seh2q1ic8t4duhvmeu4uh3vml1.apps.googleusercontent.com";
    private final String SERVER_API_KEY = "AIzaSyDHLiOGJqWfEV2S5Q9Msi4URZW7c8vrkSU";
    private final String BROWSER_API_KEY = "AIzaSyAmAGZGpH-bgEnkO2QdOIDswSdZMv2i3OE";
    private final String ANDROID_API_KEY = "AIzaSyBBPMrdEG1UYn2G5oVDbK7Z0trMEdHwNBc";
    private final String CLIENT_SECRET = "AIzaSyAjUxttilG-MEu768Z8z_ACPtwbIKpHbEo";
    private final String ENDPOINT_URL = "https://www.googleapis.com/tasks/v1/users/@me/lists";
    private final String REDIRECT_URI = "http://localhost";
    private final String AUTHENTICATION_REQUEST_URI = "https://accounts.google.com/o/oauth2/auth?" + "client_id=" + CLIENT_ID
            + "&redirect_uri=" + REDIRECT_URI + "&scope=" + SCOPE + "&response_type=code" + "&access_type=offline";
    private final String TOKEN_REQUEST_URL = "https://accounts.google.com/o/oauth2/token";
    private final String TOKEN_REQUEST_HEADER = "POST /oauth2/v3/token HTTP/1.1\n" + "Host: www.googleapis.com\n"
            + "Content-Type: application/x-www-form-urlencoded\n\n";
    // private final String TOKEN_REQUEST_HEADER = "Host: www.googleapis.com\n" + "Content-Type: application/x-www-form-urlencoded\n\n";
    // private final String TOKEN_REQUEST_HEADER = "Host: www.googleapis.com\n\n";
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartBt = (Button) findViewById(R.id.start);
        mStartBt.setOnClickListener(v -> {
            if (mAuthPreferences.getUser() != null
                    && mAuthPreferences.getToken() != null) {
                doCoolAuthenticatedStuff();
            } else {
                chooseAccount();
            }
        });

        mAccountManager = AccountManager.get(this);
        mAuthPreferences = new AuthPreferences(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == AUTHORIZATION_CODE) {
                requestToken();
            } else if (requestCode == ACCOUNT_CODE) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                mAuthPreferences.setUser(accountName);

                // invalidate old tokens which might be cached. we want a fresh one, which is guaranteed to work
                invalidateToken();
                requestToken();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doCoolAuthenticatedStuff() {
        Intent intent = new Intent(MainActivity.this, AlarmListActivity.class);
        startActivity(intent);
    }

    private void chooseAccount() {
        Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
        startActivityForResult(intent, ACCOUNT_CODE);
    }

    private void requestToken() {
        Account userAccount = null;
        String user = mAuthPreferences.getUser();
        for (Account account : mAccountManager.getAccountsByType("com.google")) {
            if (account.name.equals(user)) {
                userAccount = account;
                break;
            }
        }

        mAccountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, this, new OnTokenAcquired(), null);
    }

    private void invalidateToken() {
        AccountManager accountManager = AccountManager.get(this);
        accountManager.invalidateAuthToken("com.google", mAuthPreferences.getToken());
        mAuthPreferences.setToken(null);
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                Bundle bundle = result.getResult();
                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, AUTHORIZATION_CODE);
                } else {
                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    mAuthPreferences.setToken(token);
                    doCoolAuthenticatedStuff();
                }
            } catch (Exception e) {
                e.printStackTrace();
                //throw new RuntimeException(e);
            }
        }
    }
}
