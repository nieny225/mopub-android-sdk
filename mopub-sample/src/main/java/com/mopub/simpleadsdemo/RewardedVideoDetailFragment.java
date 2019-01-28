// Copyright 2018 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.simpleadsdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.GooglePlayServicesRewardedVideo;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideoManager.RequestParameters;
import com.mopub.mobileads.MoPubRewardedVideos;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.mopub.simpleadsdemo.Utils.hideSoftKeyboard;
import static com.mopub.simpleadsdemo.Utils.logToast;

public class RewardedVideoDetailFragment extends Fragment implements MoPubRewardedVideoListener {

    //AdMob - problem
//    public static final String MAIN_ADUNIT = "b066f1f396854604b42fadb33f9d4cca"; //ca-app-pub-2687325649049316/5470206929
//    public static final String BACKFILL_ADUNIT = "fa22094978944adfa143aa2b2f129ccd"; // ca-app-pub-2687325649049316/5657198740
//    public static final String BACKFILL_ADUNIT = "db91dfca1d5e4ae2a54e3fadf23bedeb"; //ca-app-pub-2687325649049316/5470206929
//    public static final String BACKFILL_ADUNIT = "b066f1f396854604b42fadb33f9d4cca"; // Queue the same ad unit


    public static final String TEST_DEVICE = "7115E6D528724F23A775699CCC2C563B";
//    //IronSource
//    public static final String MAIN_ADUNIT = "b720e869a892451d8815f0813c6cd342";
//    public static final String BACKFILL_ADUNIT = "885b4f0c25884d468da2c8d657b0d95b";
//    //Unity
    public static final String MAIN_ADUNIT = "8d2de5a1aa4a4b278a6c24ff4930a6e3";
    public static final String BACKFILL_ADUNIT = "20ac22127e13402d90662b77d2935467";
//    //FAN
//    public static final String MAIN_ADUNIT = "a8d5f5fff87c49b2a60c5df86ab4f447";
//    public static final String BACKFILL_ADUNIT = "82c3d266e084414c976e68def74136d5";
//    //Vungle
//    public static final String MAIN_ADUNIT = "3ab5b17178f84b5caba811528fc9c5c2";
//    public static final String BACKFILL_ADUNIT = "e26cf45489a040fba1219eb2feca7b76";


    public static final boolean TEST_RV_MULTIPLE = true;
    public static final int DEFAULT_RETRY_LIMIT = 20;
    public static final int DEFAULT_RETRY_DELAY_MS = 1000;

    public enum RewardedVideoStatus {
        EMPTY, LOADING, READY
    }

    private static boolean sRewardedVideoInitialized;

    // Include any custom event rewarded video classes, if available, for initialization.
    private static final List<String> sNetworksToInit = new LinkedList<>();
    @Nullable private Button mShowButton;
    @Nullable Map<String, RewardedVideoStatus> mAdUnitIdsMap = new HashMap<>();

    private Handler handler;
    private static int sRetryCount = 0;
    private static int sDelayMs = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final MoPubSampleAdUnit adConfiguration =
                MoPubSampleAdUnit.fromBundle(getArguments());
        final View view = inflater.inflate(R.layout.interstitial_detail_fragment, container, false);
        final DetailFragmentViewHolder views = DetailFragmentViewHolder.fromView(view);
        views.mKeywordsField.setText(getArguments().getString(MoPubListFragment.KEYWORDS_KEY, ""));
        views.mUserDataKeywordsField.setText(getArguments().getString(MoPubListFragment.USER_DATA_KEYWORDS_KEY, ""));
        hideSoftKeyboard(views.mKeywordsField);
        hideSoftKeyboard(views.mUserDataKeywordsField);

        if (!sRewardedVideoInitialized) {
            MoPubLog.d("call MoPub.initializeSdk()");
            MoPub.initializeSdk(getActivity(), new SdkConfiguration.Builder(
                    MAIN_ADUNIT)
                            .withNetworksToInit(sNetworksToInit)
                    .withMediationSettings(new GooglePlayServicesRewardedVideo.GooglePlayServicesMediationSettings(null, null, TEST_DEVICE))
                    .build(), null);
            sRewardedVideoInitialized = true;
        }
        MoPubRewardedVideos.setRewardedVideoListener(this);

        mAdUnitIdsMap.put(MAIN_ADUNIT, RewardedVideoStatus.EMPTY);
        if(TEST_RV_MULTIPLE) {
            mAdUnitIdsMap.put(BACKFILL_ADUNIT, RewardedVideoStatus.EMPTY);
        }

        views.mDescriptionView.setText(adConfiguration.getDescription());
        views.mAdUnitIdView.setText(mAdUnitIdsMap.keySet().toString());
        views.mLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                handler.removeCallbacksAndMessages(null);
                resetRetry();
                loadAd();

            }
        });
        mShowButton = views.mShowButton;
        mShowButton.setEnabled(false);
        mShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (String id : mAdUnitIdsMap.keySet()) {
                    if (MoPubRewardedVideos.hasRewardedVideo(id)) {
                        MoPubLog.d("MoPubRewardedVideos.hasRewardedVideo(): true");
                        MoPubRewardedVideos.showRewardedVideo(id);
                        break;
                    }
                }
            }
        });

        if (views.mCustomDataField != null) {
            views.mCustomDataField.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void loadAd() {
        for (String id : mAdUnitIdsMap.keySet()) {
            if (mAdUnitIdsMap.get(id) == RewardedVideoStatus.EMPTY) {
                MoPubRewardedVideos.loadRewardedVideo(id);
                mAdUnitIdsMap.put(id, RewardedVideoStatus.LOADING);
//                logToast(getActivity(), "Load ad: " + id);
            }
        }

    }

    private void retryLoadAd() {
        if (sRetryCount < DEFAULT_RETRY_LIMIT) {
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadAd();
                    sRetryCount++;
//                    logToast(getActivity(), "Retry load ad: " + sRetryCount + " delay: " + sDelayMs);
                }
            }, sDelayMs);
            sDelayMs *= 2;
        }
    }

    private void resetRetry() {
        sRetryCount = 0;
        sDelayMs = DEFAULT_RETRY_DELAY_MS;

//        logToast(getActivity(), "Reset retry");
    }

    private int getAvailableAdCount() {
        int availableAdCount = 0;
        MoPubLog.d("AdUnitIdsMap: " + mAdUnitIdsMap);
        for (String id : mAdUnitIdsMap.keySet()) {
            if (MoPubRewardedVideos.hasRewardedVideo(id)) {
                MoPubLog.d("Ad Unit: "+id+ " hasRewardedVideo: true");
                availableAdCount++;
            }
            else
                MoPubLog.d("Ad Unit: "+id+ " hasRewardedVideo: false");
        }
        return availableAdCount;

    }

    private void updateShowButtonStatus() {

        int availableAdCount = getAvailableAdCount();

        if (mShowButton != null){
            if (availableAdCount > 0)
                mShowButton.setEnabled(true);
            else
                mShowButton.setEnabled(false);

        }
        mShowButton.setText("Show (" + availableAdCount + ")");
    }

    @Override
    public void onDestroyView() {
//        MoPubRewardedVideos.setRewardedVideoListener(null);
        MoPubLog.d("onDestroyView()");
        super.onDestroyView();
    }

    // MoPubRewardedVideoListener implementation
    @Override
    public void onRewardedVideoLoadSuccess(@NonNull final String adUnitId) {
        MoPubLog.d("MoPubRewardedVideoListener onRewardedVideoLoadSuccess(): " + adUnitId);
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
            mAdUnitIdsMap.put(adUnitId, RewardedVideoStatus.READY);

            updateShowButtonStatus();

//            resetRetry();

//            logToast(getActivity(), "Rewarded video loaded: " + adUnitId);

        }
    }

    @Override
    public void onRewardedVideoLoadFailure(@NonNull final String adUnitId, @NonNull final MoPubErrorCode errorCode) {
        MoPubLog.d("onRewardedVideoLoadFailure(): " + adUnitId);
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
            mAdUnitIdsMap.put(adUnitId, RewardedVideoStatus.EMPTY);

//            retryLoadAd();

            updateShowButtonStatus();

//            logToast(getActivity(), String.format(Locale.US, "Rewarded video failed to load: %s " + adUnitId, errorCode.toString()));
        }
    }

    @Override
    public void onRewardedVideoStarted(@NonNull final String adUnitId) {
        MoPubLog.d("onRewardedVideoStarted(): " + adUnitId);
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
//            logToast(getActivity(), "Rewarded video started. " + adUnitId);
            updateShowButtonStatus();
        }
    }


    @Override
    public void onRewardedVideoPlaybackError(@NonNull final String adUnitId, @NonNull final MoPubErrorCode errorCode) {
        MoPubLog.d("onRewardedVideoPlaybackError(): " + adUnitId);
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
            mAdUnitIdsMap.put(adUnitId, RewardedVideoStatus.EMPTY);

//            retryLoadAd();

//            logToast(getActivity(), String.format(Locale.US, "Rewarded video playback error: %s " + adUnitId, errorCode.toString()));

            updateShowButtonStatus();
        }
    }

    @Override
    public void onRewardedVideoClicked(@NonNull final String adUnitId) {
        MoPubLog.d("onRewardedVideoClicked(): " + adUnitId);
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
//            logToast(getActivity(), "Rewarded video clicked. " + adUnitId);
        }
    }

    @Override
    public void onRewardedVideoClosed(@NonNull final String adUnitId) {
        MoPubLog.d("onRewardedVideoClosed(): " + adUnitId);
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
            mAdUnitIdsMap.put(adUnitId, RewardedVideoStatus.EMPTY);

//            loadAd();

//            logToast(getActivity(), "Rewarded video closed. " + adUnitId);

            updateShowButtonStatus();
        }


    }

    @Override
    public void onRewardedVideoCompleted(@NonNull final Set<String> adUnitIds,
            @NonNull final MoPubReward reward) {
        MoPubLog.d("onRewardedVideoCompleted(): " + adUnitIds);
        if (mAdUnitIdsMap.keySet().containsAll(adUnitIds)) {
//            logToast(getActivity(),
//                    String.format(Locale.US,
//                            "Rewarded video completed with reward  \"%d %s\" " + adUnitIds,
//                            reward.getAmount(),
//                            reward.getLabel()));
        }

    }

}
