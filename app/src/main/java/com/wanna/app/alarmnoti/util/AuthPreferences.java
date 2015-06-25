package com.wanna.app.alarmnoti.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthPreferences {
    private static final String KEY_USER = "user";
    private static final String KEY_TOKEN = "SyncToken";
    private static final String KEY_NEXT_TOKEN = "nextSyncToken";

    private SharedPreferences preferences;

    public AuthPreferences(Context context) {
        preferences = context
                .getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public void setUser(String user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER, user);
        editor.commit();
    }

    public void setToken(String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, password);
        editor.commit();
    }

    public void setNextToken(String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_NEXT_TOKEN, password);
        editor.commit();
    }

    public String getUser() { return preferences.getString(KEY_USER, null); }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getNextToken() {
        return preferences.getString(KEY_NEXT_TOKEN, null);
    }
}
