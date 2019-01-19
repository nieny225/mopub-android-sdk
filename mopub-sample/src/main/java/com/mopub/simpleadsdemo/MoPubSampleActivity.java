// Copyright 2018 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.simpleadsdemo;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.WebView;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.ConsentDialogListener;
import com.mopub.common.privacy.ConsentStatus;
import com.mopub.common.privacy.ConsentStatusChangeListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.common.util.DeviceUtils;
import com.mopub.mobileads.FacebookAdvancedBidder;
import com.mopub.mobileads.MoPubErrorCode;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.mopub.common.Constants.UNUSED_REQUEST_CODE;

public class MoPubSampleActivity extends FragmentActivity {
    private static final List<String> REQUIRED_DANGEROUS_PERMISSIONS = new ArrayList<>();

    static {
        REQUIRED_DANGEROUS_PERMISSIONS.add(ACCESS_COARSE_LOCATION);
        REQUIRED_DANGEROUS_PERMISSIONS.add(WRITE_EXTERNAL_STORAGE);
    }

    // Sample app web views are debuggable.
    static {
        setWebDebugging();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void setWebDebugging() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    private MoPubListFragment mMoPubListFragment;
    private Intent mDeeplinkIntent;
    @Nullable
    PersonalInfoManager mPersonalInfoManager;

    @Nullable
    private ConsentStatusChangeListener mConsentStatusChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MoPub-Nick", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        List<String> permissionsToBeRequested = new ArrayList<>();
        for (String permission : REQUIRED_DANGEROUS_PERMISSIONS) {
            if (!DeviceUtils.isPermissionGranted(this, permission)) {
                permissionsToBeRequested.add(permission);
            }
        }

        // Request dangerous permissions
        if (!permissionsToBeRequested.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToBeRequested.toArray(
                    new String[permissionsToBeRequested.size()]), UNUSED_REQUEST_CODE);
        }

        // Set location awareness and precision globally for your app:
        MoPub.setLocationAwareness(MoPub.LocationAwareness.TRUNCATED);
        MoPub.setLocationPrecision(4);

        if (savedInstanceState == null) {
            createMoPubListFragment(getIntent());
        }

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder("facfc5e6d36d45329fbaf228e2cd4a36")
                .withAdvancedBidder(FacebookAdvancedBidder.class).build();
        MoPub.initializeSdk(this, sdkConfiguration, initSdkListener());


        mConsentStatusChangeListener = initConsentChangeListener();
        mPersonalInfoManager = MoPub.getPersonalInformationManager();
        if (mPersonalInfoManager != null) {
            mPersonalInfoManager.subscribeConsentStatusChangeListener(mConsentStatusChangeListener);
        }

        // Intercepts all logs including Level.FINEST so we can show a toast
        // that is not normally user-facing. This is only used for native ads.
        LoggingUtils.enableCanaryLogging(this);
    }

    @Override
    protected void onDestroy() {
        if (mPersonalInfoManager != null) {
            // unsubscribe or memory leak will occur
            mPersonalInfoManager.unsubscribeConsentStatusChangeListener(mConsentStatusChangeListener);
        }
        mConsentStatusChangeListener = null;
        super.onDestroy();
    }


    private void createMoPubListFragment(@NonNull final Intent intent) {
        if (findViewById(R.id.fragment_container) != null) {
            mMoPubListFragment = new MoPubListFragment();
            mMoPubListFragment.setArguments(intent.getExtras());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mMoPubListFragment).commit();

            mDeeplinkIntent = intent;
        }
    }

    @Override
    public void onNewIntent(@NonNull final Intent intent) {
        mDeeplinkIntent = intent;
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        if (mMoPubListFragment != null && mDeeplinkIntent != null) {
            mMoPubListFragment.addAdUnitViaDeeplink(mDeeplinkIntent.getData());
            mDeeplinkIntent = null;
        }
    }

    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {

            @Override
            public void onInitializationFinished() {
                Utils.logToast(MoPubSampleActivity.this, "SDK initialized.");
                Log.d("MoPub-Nick", "onInitializationFinished(): SDK initialized");
                if (mPersonalInfoManager != null && mPersonalInfoManager.shouldShowConsentDialog()) {
                    mPersonalInfoManager.loadConsentDialog(initDialogLoadListener());
                }
            }
        };
    }

    private ConsentStatusChangeListener initConsentChangeListener() {
        return new ConsentStatusChangeListener() {

            @Override
            public void onConsentStateChange(@NonNull ConsentStatus oldConsentStatus,
                                             @NonNull ConsentStatus newConsentStatus,
                                             boolean canCollectPersonalInformation) {
                Utils.logToast(MoPubSampleActivity.this, "Consent: " + newConsentStatus.name());
                if (mPersonalInfoManager != null && mPersonalInfoManager.shouldShowConsentDialog()) {
                    mPersonalInfoManager.loadConsentDialog(initDialogLoadListener());
                }
            }
        };
    }

    private ConsentDialogListener initDialogLoadListener() {
        return new ConsentDialogListener() {

            @Override
            public void onConsentDialogLoaded() {
                if (mPersonalInfoManager != null) {
                    mPersonalInfoManager.showConsentDialog();
                }
            }

            @Override
            public void onConsentDialogLoadFailed(@NonNull MoPubErrorCode moPubErrorCode) {
                Utils.logToast(MoPubSampleActivity.this, "Consent dialog failed to load.");
            }
        };
    }
}
