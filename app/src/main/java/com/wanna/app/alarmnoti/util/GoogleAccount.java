package com.wanna.app.alarmnoti.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by m on 2015-06-23.
 */
public class GoogleAccount {
    private final String SCOPE = "https://www.googleapis.com/auth/calendar.readonly";
    private final int AUTHORIZATION_CODE = 1993;
    private final int ACCOUNT_CODE = 1601;

    private AuthPreferences mAuthPreferences;
    private AccountManager mAccountManager;
    Activity mActivity;
    private AuthenticatedStuff mAuthenticatedStuff;

    public interface AuthenticatedStuff {
        void doCoolAuthenticatedStuff();
    }

    public GoogleAccount(Activity activity, AuthenticatedStuff as) {
        mActivity = activity;
        mAuthenticatedStuff = as;

        mAccountManager = AccountManager.get(activity);
        mAuthPreferences = new AuthPreferences(activity);
    }

    public void checkAccount() {
        if (mAuthPreferences.getUser() != null
                && mAuthPreferences.getToken() != null) {
            mAuthenticatedStuff.doCoolAuthenticatedStuff();
        } else {
            chooseAccount();
        }
    }

    public void result(int requestCode, int resultCode, Intent data) {
        if (resultCode == mActivity.RESULT_OK) {
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

    private void chooseAccount() {
        Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
        mActivity.startActivityForResult(intent, ACCOUNT_CODE);
    }

    public void requestToken() {
        Account userAccount = null;
        String user = mAuthPreferences.getUser();
        for (Account account : mAccountManager.getAccountsByType("com.google")) {
            if (account.name.equals(user)) {
                userAccount = account;
                break;
            }
        }

        mAccountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, mActivity, new OnTokenAcquired(), null);
    }

    private void invalidateToken() {
        AccountManager accountManager = AccountManager.get(mActivity);
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
                    mActivity.startActivityForResult(launch, AUTHORIZATION_CODE);
                } else {
                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    mAuthPreferences.setToken(token);
                    mAuthenticatedStuff.doCoolAuthenticatedStuff();
                }
            } catch (Exception e) {
                e.printStackTrace();
                //throw new RuntimeException(e);
            }
        }
    }
}
