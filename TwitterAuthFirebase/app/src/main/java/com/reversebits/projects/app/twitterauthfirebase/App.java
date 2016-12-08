package com.reversebits.projects.app.twitterauthfirebase;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by reverseBits on 12/7/2016.
 */

public class App extends Application {

    /*
    TODO : Here enter API key and API Secret you get after creating application on https://apps.twitter.com
    */

    private static final String TWITTER_KEY = "PgbpUUFFu064fkNojoU8B5h0W";
    private static final String TWITTER_SECRET = "ALs9EaQYEDokDiFcNLIFkcHtXtIM6v2vJYrHqldBzeAIsKCubq";

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = App.this;
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(context, new Twitter(authConfig), new Crashlytics());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //added multidex to handle 64k memory issues.
        MultiDex.install(this);
    }
}
