package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.ISDemandOnlyInterstitialListener;
import com.mopub.common.LifecycleListener;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubLifecycleManager;
import com.mopub.common.logging.MoPubLog;

import java.util.Map;

public class IronSourceInterstitial extends CustomEventInterstitial implements ISDemandOnlyInterstitialListener {

    /**
     * private vars
     */

    // Configuration keys
    private static final String APPLICATION_KEY = "applicationKey";
    private static final String PLACEMENT_KEY = "placementName";
    private static final String INSTANCE_ID_KEY = "instanceId";
    private static final String MEDIATION_TYPE = "mopub";
    private static final String ADAPTER_VERSION = "300";


    // This is the instance id used inside ironSource SDK
    private String mInstanceId = "0";
    // This is the placement name used inside ironSource SDK
    private String mPlacementName = null;
    private static boolean mInitInterstitialSuccessfully;
    private static Handler sHandler;

    private static CustomEventInterstitialListener mMoPubListener;

    /**
     * Mopub API
     */

    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> map0, Map<String, String> serverExtras) {

        MoPubLifecycleManager.getInstance((Activity) context).addLifecycleListener(lifecycleListener);
        // Pass the user consent from the MoPub SDK to ironSource as per GDPR
        boolean canCollectPersonalInfo = MoPub.canCollectPersonalInformation();
        IronSource.setConsent(canCollectPersonalInfo);

        try {
            mMoPubListener = customEventInterstitialListener;
            sHandler = new Handler(Looper.getMainLooper());

            if (!(context instanceof Activity)) {
                // Context not an Activity context, log the reason for failure and fail the
                // initialization.
                MoPubLog.d("IronSource load interstitial must be called from an Activity context");
                sendMoPubInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                return;
            }

            String applicationKey = "";

            if (serverExtras != null) {
                if (serverExtras.get(APPLICATION_KEY) != null) {
                    applicationKey = serverExtras.get(APPLICATION_KEY);
                }

                if (serverExtras.get(PLACEMENT_KEY) != null) {
                    mPlacementName = serverExtras.get(PLACEMENT_KEY);
                }

                if (serverExtras.get(INSTANCE_ID_KEY) != null) {
                    if (!TextUtils.isEmpty(serverExtras.get(INSTANCE_ID_KEY))) {
                        mInstanceId = serverExtras.get(INSTANCE_ID_KEY);
                    }
                }
            } else {
                MoPubLog.d("serverExtras is null. Make sure you have entered ironSource's application and instance keys on the MoPub dashboard");
                sendMoPubInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }

            if (!TextUtils.isEmpty(applicationKey)) {
                initIronSourceSDK(((Activity) context), applicationKey);
                loadInterstitial();
            } else {
                MoPubLog.d("IronSource initialization failed, make sure that 'applicationKey' server parameter is added");
                sendMoPubInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
            }

        } catch (Exception e) {
            MoPubLog.d(e.toString());
            sendMoPubInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    protected void showInterstitial() {
        try {
            if (IronSource.isISDemandOnlyInterstitialReady(mInstanceId)) {
                if (TextUtils.isEmpty(mPlacementName)) {
                    IronSource.showISDemandOnlyInterstitial(mInstanceId);
                } else {
                    IronSource.showISDemandOnlyInterstitial(mInstanceId, mPlacementName);
                }
            } else {
                sendMoPubInterstitialFailed(MoPubErrorCode.NO_FILL);
            }
        } catch (Exception e) {
            MoPubLog.d(e.toString());
            sendMoPubInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    protected void onInvalidate() {
        mMoPubListener = null;
    }

    /**
     * Class Helper Methods
     **/

    private void initIronSourceSDK(Activity activity, String appKey) {
        IronSource.setISDemandOnlyInterstitialListener(this);

        if (!mInitInterstitialSuccessfully) {
            MoPubLog.d("IronSource initialization succeeded for Interstitial");
            IronSource.setMediationType(MEDIATION_TYPE + ADAPTER_VERSION);
            IronSource.initISDemandOnly(activity, appKey, IronSource.AD_UNIT.INTERSTITIAL);
            mInitInterstitialSuccessfully = true;
        }
    }

    private void loadInterstitial() {
        if (IronSource.isISDemandOnlyInterstitialReady(mInstanceId)) {
            onInterstitialAdReady(mInstanceId);
        } else {
            IronSource.loadISDemandOnlyInterstitial(mInstanceId);
        }
    }

    private void sendMoPubInterstitialFailed(final MoPubErrorCode errorCode) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMoPubListener != null) {
                    mMoPubListener.onInterstitialFailed(errorCode);
                }
            }
        });
    }

    private MoPubErrorCode getMoPubErrorMessage(IronSourceError ironSourceError) {
        if (ironSourceError == null) {
            return MoPubErrorCode.INTERNAL_ERROR;
        }
        switch (ironSourceError.getErrorCode()) {
            case IronSourceError.ERROR_CODE_NO_CONFIGURATION_AVAILABLE:
            case IronSourceError.ERROR_CODE_KEY_NOT_SET:
            case IronSourceError.ERROR_CODE_INVALID_KEY_VALUE:
            case IronSourceError.ERROR_CODE_INIT_FAILED:
                return MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
            case IronSourceError.ERROR_CODE_USING_CACHED_CONFIGURATION:
                return MoPubErrorCode.VIDEO_CACHE_ERROR;
            case IronSourceError.ERROR_CODE_NO_ADS_TO_SHOW:
                return MoPubErrorCode.NETWORK_NO_FILL;
            case IronSourceError.ERROR_CODE_GENERIC:
                return MoPubErrorCode.INTERNAL_ERROR;
            case IronSourceError.ERROR_NO_INTERNET_CONNECTION:
                return MoPubErrorCode.NO_CONNECTION;
            default:
                return MoPubErrorCode.UNSPECIFIED;
        }
    }

    /**
     * IronSource Interstitial Listener
     **/

    @Override
    public void onInterstitialAdReady(String instanceId) {
        MoPubLog.d("IronSource Interstitial loaded successfully for instance " + mInstanceId);
        if (!mInstanceId.equals(instanceId)) {
            return;
        }
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMoPubListener != null) {
                    mMoPubListener.onInterstitialLoaded();
                }
            }
        });
    }

    @Override
    public void onInterstitialAdLoadFailed(String instanceId, IronSourceError ironSourceError) {
        MoPubLog.d("IronSource Interstitial failed to load for instance " + mInstanceId + " Error: " + ironSourceError.getErrorMessage());

        if (!mInstanceId.equals(instanceId)) {
            return;
        }
        sendMoPubInterstitialFailed(getMoPubErrorMessage(ironSourceError));
    }

    @Override
    public void onInterstitialAdOpened(String instanceId) {
        MoPubLog.d("IronSource Interstitial opened ad for instance " + instanceId);

        sHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMoPubListener != null) {
                    mMoPubListener.onInterstitialShown();
                }
            }
        });
    }

    @Override
    public void onInterstitialAdClosed(String instanceId) {
        MoPubLog.d("IronSource Interstitial closed ad for instance " + instanceId);

        sHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMoPubListener != null) {
                    mMoPubListener.onInterstitialDismissed();
                }
            }
        });
    }

    @Override
    public void onInterstitialAdShowSucceeded(String instanceId) {
        // not in use in MoPub mediation (we use the onInterstitialAdOpened for saying that the ad was shown)
    }

    @Override
    public void onInterstitialAdShowFailed(String instanceId, IronSourceError ironSourceError) {
        MoPubLog.d("IronSource Interstitial failed to show for instance " + instanceId);
        sendMoPubInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
    }

    @Override
    public void onInterstitialAdClicked(String instanceId) {
        MoPubLog.d("IronSource Interstitial clicked ad for instance " + instanceId);
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMoPubListener != null) {
                    mMoPubListener.onInterstitialClicked();
                }
            }
        });
    }

    private static LifecycleListener lifecycleListener = new LifecycleListener() {
        @Override
        public void onCreate(@NonNull Activity activity) {
        }

        @Override
        public void onStart(@NonNull Activity activity) {
        }

        @Override
        public void onPause(@NonNull Activity activity) {
            IronSource.onPause(activity);
        }

        @Override
        public void onResume(@NonNull Activity activity) {
            IronSource.onResume(activity);
        }

        @Override
        public void onRestart(@NonNull Activity activity) {
        }

        @Override
        public void onStop(@NonNull Activity activity) {
        }

        @Override
        public void onDestroy(@NonNull Activity activity) {
        }

        @Override
        public void onBackPressed(@NonNull Activity activity) {
        }
    };
}