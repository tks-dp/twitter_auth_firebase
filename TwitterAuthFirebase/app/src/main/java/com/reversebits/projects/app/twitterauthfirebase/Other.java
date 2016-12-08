package com.reversebits.projects.app.twitterauthfirebase;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by TapanHP on 12/8/2016.
 */

public class Other {

    public static void saveIntPref(Activity context, String key, int val) {
        SharedPreferences sharedPref = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, val);
        editor.commit();
    }

    public static void saveStringPref(Activity context, String key, String val) {
        SharedPreferences sharedPref = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public static int loadIntPref(Activity context, String key) {
        SharedPreferences sharedPref = context.getPreferences(Context.MODE_PRIVATE);
        int val = sharedPref.getInt(key, 0);
        return val;
    }

    public static String loadStringPref(Activity context, String key) {
        SharedPreferences sharedPref = context.getPreferences(Context.MODE_PRIVATE);
        String val = sharedPref.getString(key, null);
        return val;
    }

}
