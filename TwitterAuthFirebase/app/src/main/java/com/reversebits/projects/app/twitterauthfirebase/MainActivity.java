package com.reversebits.projects.app.twitterauthfirebase;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;


/**
 * Created by reverseBits on 12/7/2016.
 */

public class MainActivity extends AppCompatActivity {

    private TwitterLoginButton loginButton;
    private TwitterAuthClient authClient;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String email;
    private static final String TAG = "FirebaseAuth";

    private ImageView ivActivityProfileClose;
    private LinearLayout llActivityProfileLogin;
    private TwitterLoginButton twitterLoginButton;
    private LinearLayout llActivityProfileLoggedin;
    private CircleImageView ivActivityProfilePic;
    private TextView tvActivityProfileName;
    private TextView tvActivityProfileMail;
    private ImageView ivActivityProfileLogout;

    private int isUserLogin;

    private String user_name, user_email, user_pic;
    private TwitterSession session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isUserLogin == 0)
            mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isUserLogin == 0) {
            if (mAuthListener != null) {
                mAuth.removeAuthStateListener(mAuthListener);
            }
        }
    }


    private void init() {
        ivActivityProfileClose = (ImageView) findViewById(R.id.iv_activity_profile_close);
        ivActivityProfileClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        llActivityProfileLogin = (LinearLayout) findViewById(R.id.ll_activity_profile_login);
        llActivityProfileLoggedin = (LinearLayout) findViewById(R.id.ll_activity_profile_loggedin);

        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);

        ivActivityProfilePic = (CircleImageView) findViewById(R.id.iv_activity_profile_pic);
        tvActivityProfileName = (TextView) findViewById(R.id.tv_activity_profile_name);
        tvActivityProfileMail = (TextView) findViewById(R.id.tv_activity_profile_mail);
        ivActivityProfileLogout = (ImageView) findViewById(R.id.iv_activity_profile_logout);


        isUserLogin = Other.loadIntPref(this, PrefKey.USER_LOGIN_STATUS);

        if (isUserLogin == 1) {
            //launch profile screen

            manageProfile(1);

            ivActivityProfileLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logoutTwitter();
                }
            });

        } else {

            manageProfile(0);

        }

    }

    private void manageProfile(int val) {

        if (val == 1) {
            llActivityProfileLogin.setVisibility(View.GONE);
            llActivityProfileLoggedin.setVisibility(View.VISIBLE);

            tvActivityProfileName.setText(Other.loadStringPref(this, PrefKey.USER_NAME));
            tvActivityProfileMail.setText(Other.loadStringPref(this, PrefKey.USER_EMAIL));

            Glide.with(this).load(Other.loadStringPref(this, PrefKey.USER_PIC)).fitCenter().into(ivActivityProfilePic);

        } else {
            llActivityProfileLoggedin.setVisibility(View.GONE);
            llActivityProfileLogin.setVisibility(View.VISIBLE);
            manageTwitterFirebaseAuth();
        }


    }

    private void manageTwitterFirebaseAuth() {

        // Initialize twitter auth client
        authClient = new TwitterAuthClient();

        // Initialize Firebase Auth object
        mAuth = FirebaseAuth.getInstance();

        //Initialize login button
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);

        /*Initialize Firebase Auth Listner
          It will triggered whenever user's auth state change.*/

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        //Added callback on button, It will manage login and twitter session
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d("TwitterKit", "Login with Twitter successfully");
                session = result.data;


                getEmailFromTwitter(session);


            }

            @Override
            public void failure(TwitterException exception) {
                Log.e("TwitterKit", "Login with Twitter failure", exception);
            }
        });


    }

    //STEP : Asking for user Email address
    private void getEmailFromTwitter(final TwitterSession session) {

        Call<User> user = TwitterCore.getInstance().getApiClient().getAccountService().verifyCredentials(true, false);
        user.enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> userResult) {

                user_pic = userResult.data.profileImageUrl.replace("_normal", "");


                Log.e("pic", "success: " + user_pic);
            }

            @Override
            public void failure(TwitterException exc) {
                Log.d("TwitterKit", "Verify Credentials Failure", exc);
            }
        });


        authClient.requestEmail(session, new Callback<String>() {
            @Override
            public void success(Result<String> result) {
                //Here returned result will be email string
                Log.d("EmailRequest", "Got email from twitter successfully");
                email = result.data;
                handleTwitterSession(session, email);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.e("EmailRequest", "Got Exception after asking for email", exception);
                getEmailFromTwitter(session);
            }
        });
    }


    //This method will do Login/Signup in Firebase after comlete twitter authentication
    private void handleTwitterSession(TwitterSession session, final String email) {
        Log.d(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> authResultTask) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + authResultTask.isSuccessful());

                        //Here adding email address of user to firebase by updateEmail method
                        authResultTask.getResult().getUser().updateEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            user_name = authResultTask.getResult().getUser().getDisplayName();
                                            user_email = authResultTask.getResult().getUser().getEmail();
//                                            user_pic = authResultTask.getResult().getUser().getPhotoUrl().toString();
                                            managePref(1);
                                        }
                                    }
                                });

                        if (!authResultTask.isSuccessful()) {
                            Log.e(TAG, "signInWithCredential", authResultTask.getException());
                        }

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }


    public void logoutTwitter() {
        TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (twitterSession != null) {
            ClearCookies(getApplicationContext());
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        }
        FirebaseAuth.getInstance().signOut();
        managePref(0);

    }

    public static void ClearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }


    private void managePref(int val) {
        if (val == 1) {
            Other.saveIntPref(MainActivity.this, PrefKey.USER_LOGIN_STATUS, 1);
            Other.saveStringPref(MainActivity.this, PrefKey.USER_EMAIL, user_email);
            Other.saveStringPref(MainActivity.this, PrefKey.USER_NAME, user_name);
            Other.saveStringPref(MainActivity.this, PrefKey.USER_PIC, user_pic);

            Log.e("USER", " name : " + Other.loadStringPref(this, PrefKey.USER_NAME) + " : " +
                    "email : " + Other.loadStringPref(this, PrefKey.USER_EMAIL) + " : " +
                    " :  pic : " + Other.loadStringPref(this, PrefKey.USER_PIC));

            manageProfile(1);


        } else {
            Other.saveIntPref(MainActivity.this, PrefKey.USER_LOGIN_STATUS, 0);
            Other.saveStringPref(MainActivity.this, PrefKey.USER_EMAIL, null);
            Other.saveStringPref(MainActivity.this, PrefKey.USER_PIC, null);
            Other.saveStringPref(MainActivity.this, PrefKey.USER_NAME, null);

            Log.e("USER", " name : " + Other.loadStringPref(this, PrefKey.USER_NAME) + " : " +
                    "email : " + Other.loadStringPref(this, PrefKey.USER_EMAIL) + " : " +
                    " :  pic : " + Other.loadStringPref(this, PrefKey.USER_PIC));
            manageProfile(0);
        }
    }

}

