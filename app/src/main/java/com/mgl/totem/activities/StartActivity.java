package com.mgl.totem.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.mgl.totem.R;
import com.mgl.totem.base.TotemApplication;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = StartActivity.class.getName();
    private SpassFingerprint mSpassFingerprint;
    private SpassFingerprint.IdentifyListener listener;
    private Spass mSpass;
    private boolean isFeatureEnabled;
    private SpassFingerprint.RegisterListener mRegisterListener;
    private int tries = 0;

    @InjectView(R.id.scanImage)
    ImageView scanFinger;
    private String userUniqueId;
    private Runnable startActivityRunnable;

    @Override
    protected void onResume() {
        super.onResume();

        scanFinger.setImageDrawable(getDrawable(R.drawable.fingerprint));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ButterKnife.inject(this);
        ((TotemApplication) getApplication()).inject(this);

        startActivityRunnable = new Runnable() {
            public void run() {
                Intent main = new Intent(StartActivity.this, MainActivity.class);
                main.putExtra(MainActivity.EXTRA_USER_ID, userUniqueId);
                startActivity(main);
            }
        };

        listener = new SpassFingerprint.IdentifyListener() {
            @Override
            public void onFinished(int eventStatus) {
                Log.d(TAG, "identify finished : reason=" + getEventStatusName(eventStatus));
                int FingerprintIndex = 0;
                String uniqueId = "none";
                try {
                    FingerprintIndex = mSpassFingerprint.getIdentifiedFingerprintIndex();
                    if (mSpassFingerprint.getRegisteredFingerprintUniqueID().get(FingerprintIndex) != null) {
                        uniqueId = mSpassFingerprint.getRegisteredFingerprintUniqueID().get(FingerprintIndex).toString();
                    }
                } catch (IllegalStateException ise) {
                    Log.d(TAG,ise.getMessage());
                }
                if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                    Log.d(TAG, "onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex + " ID: " + uniqueId);
                    userUniqueId = uniqueId;
                    scanFinger.setImageDrawable(getDrawable(R.drawable.greentick));
                    Handler handler = new Handler();
                    handler.postDelayed(startActivityRunnable, 1500);
                } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                    Log.d(TAG,"onFinished() : Password authentification Success");
                } else {
                    Log.d(TAG,"onFinished() : Authentification Fail for identify");
                }

                tries = 0;
            }

            @Override
            public void onReady() {
                Log.d(TAG, "identify state is ready");
            }

            @Override
            public void onStarted() {
                Log.d(TAG, "User touched fingerprint sensor!");
                Log.d(TAG, "tries: " + tries);
                if (tries > 1) {
                    Log.d(TAG, "Should register cause it does not exist");
                    mSpassFingerprint.cancelIdentify();
                    Intent register = new Intent(StartActivity.this, RegisterActivity.class);
                    startActivity(register);
                } else {
                    tries++;
                }
            }
        };

        mSpass = new Spass();
        try {
            mSpass.initialize(StartActivity.this);
        } catch (SsdkUnsupportedException e) {
            // Error handling
        } catch (UnsupportedOperationException e){
            // Error handling
        }
        isFeatureEnabled = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
        if(isFeatureEnabled){
            mSpassFingerprint = new SpassFingerprint(StartActivity.this);
            Log.d(TAG, "YAY!! SUPPORTED!!");
        } else {
            Log.d(TAG, "Fingerprint Service is not supported in the device.");
        }

        scanFinger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpassFingerprint.startIdentifyWithDialog(StartActivity.this, listener, false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
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

    private static String getEventStatusName(int eventStatus) {
        switch (eventStatus) {
            case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                return "STATUS_AUTHENTIFICATION_SUCCESS";
            case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                return "STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS";
            case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                return "STATUS_TIMEOUT";
            case SpassFingerprint.STATUS_SENSOR_FAILED:
                return "STATUS_SENSOR_ERROR";
            case SpassFingerprint.STATUS_USER_CANCELLED:
                return "STATUS_USER_CANCELLED";
            case SpassFingerprint.STATUS_QUALITY_FAILED:
                return "STATUS_QUALITY_FAILED";
            case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
                return "STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE";
            case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
            default:
                return "STATUS_AUTHENTIFICATION_FAILED";
        }
    }
}
