package com.reversebits.projects.app.twitterauthfirebase;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by TapanHP on 12/7/2016.
 */

public class App extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "PgbpUUFFu064fkNojoU8B5h0W";
    private static final String TWITTER_SECRET = "ALs9EaQYEDokDiFcNLIFkcHtXtIM6v2vJYrHqldBzeAIsKCubq";

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = App.this;
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(context, new Twitter(authConfig));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }
}
