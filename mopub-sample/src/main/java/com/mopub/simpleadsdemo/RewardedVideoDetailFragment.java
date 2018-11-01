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

    private static boolean sRewardedVideoInitialized;

    // Include any custom event rewarded video classes, if available, for initialization.
    private static final List<String> sNetworksToInit = new LinkedList<>();

    @Nullable private Button mShowButton;
    @Nullable private String mAdUnitId;
    @Nullable Map<String, String> mAdUnitIdsMap;
    @Nullable private Map<String, MoPubReward> mMoPubRewardsMap;
    @Nullable private MoPubReward mSelectedReward;

    public interface RewardedVideoStatus {
        String EMPTY = "0";
        String LOADING = "1";
        String READY = "2";
    }

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
            MoPub.initializeSdk(getActivity(), new SdkConfiguration.Builder(
                            "44debba1b0ff484485ef6ebda98b67cb")
                            .withNetworksToInit(sNetworksToInit).build(), null);
            sRewardedVideoInitialized = true;
        }
        MoPubRewardedVideos.setRewardedVideoListener(this);

        mAdUnitId = adConfiguration.getAdUnitId();

        mAdUnitIdsMap = new HashMap<>();
        mAdUnitIdsMap.put("44debba1b0ff484485ef6ebda98b67cb", RewardedVideoStatus.EMPTY);
        mAdUnitIdsMap.put("d3b986d6f1bb4a589ec90fd8f79c86aa", RewardedVideoStatus.EMPTY);

        mMoPubRewardsMap = new HashMap<>();

        views.mDescriptionView.setText(adConfiguration.getDescription());
        views.mAdUnitIdView.setText(mAdUnitIdsMap.keySet().toString());
        views.mLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadAd();

//                if (mShowButton != null) {
//                    mShowButton.setEnabled(false);
//                }
            }
        });
        mShowButton = views.mShowButton;
        mShowButton.setEnabled(false);
        mShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mReadyAdUnitId = null;
                for (String id : mAdUnitIdsMap.keySet()) {
                    if (mAdUnitIdsMap.get(id) == RewardedVideoStatus.READY) {
                        mReadyAdUnitId = id;
                        break;
                    }
                }

                if (mReadyAdUnitId == null) {
                    return;
                }

                final String customData = (views.mCustomDataField != null)
                        ? views.mCustomDataField.getText().toString()
                        : null;

                MoPubRewardedVideos.showRewardedVideo(mReadyAdUnitId, customData);
            }
        });
        if (views.mCustomDataField != null) {
            views.mCustomDataField.setVisibility(View.VISIBLE);
        }

        return view;
    }

    public void loadAd() {
        for (String id : mAdUnitIdsMap.keySet()) {
            if (mAdUnitIdsMap.get(id) == RewardedVideoStatus.EMPTY) {
                MoPubRewardedVideos.loadRewardedVideo(id);
                mAdUnitIdsMap.put(id, RewardedVideoStatus.LOADING);
            }
        }

    }

    public int getAvailableAdCount() {
        int num = 0;
        for (String id : mAdUnitIdsMap.keySet()) {
            if (mAdUnitIdsMap.get(id) == RewardedVideoStatus.READY) {
                num++;
            }
        }
        MoPubLog.i("Nick getAvailableAdCount: "+ num);
        return num;

    }

    @Override
    public void onDestroyView() {
        MoPubRewardedVideos.setRewardedVideoListener(null);
        super.onDestroyView();
    }

    // MoPubRewardedVideoListener implementation
    @Override
    public void onRewardedVideoLoadSuccess(@NonNull final String adUnitId) {
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
            mAdUnitIdsMap.put(adUnitId, RewardedVideoStatus.READY);
            if (mShowButton != null && getAvailableAdCount() != 0) {
                mShowButton.setEnabled(true);
            }
            logToast(getActivity(), "Rewarded video loaded:" + getAvailableAdCount());

//            Set<MoPubReward> availableRewards = MoPubRewardedVideos.getAvailableRewards(adUnitId);
//
//            // If there are more than one reward available, pop up alert dialog for reward selection
//            if (availableRewards.size() > 1) {
//                final SelectRewardDialogFragment selectRewardDialogFragment
//                        = SelectRewardDialogFragment.newInstance();
//
//                // The user must select a reward from the dialog
//                selectRewardDialogFragment.setCancelable(false);
//
//                // Reset rewards mapping and selected reward
//                mMoPubRewardsMap.clear();
//                mSelectedReward = null;
//
//                // Initialize mapping between reward string and reward instance
//                for (MoPubReward reward : availableRewards) {
//                    mMoPubRewardsMap.put(reward.getAmount() + " " + reward.getLabel(), reward);
//                }
//
//                selectRewardDialogFragment.loadRewards(mMoPubRewardsMap.keySet()
//                        .toArray(new String[mMoPubRewardsMap.size()]));
//                selectRewardDialogFragment.setTargetFragment(this, 0);
//                selectRewardDialogFragment.show(getActivity().getSupportFragmentManager(),
//                        "selectReward");
//            }
        }
    }

    @Override
    public void onRewardedVideoLoadFailure(@NonNull final String adUnitId, @NonNull final MoPubErrorCode errorCode) {
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
            mAdUnitIdsMap.put(adUnitId, RewardedVideoStatus.EMPTY);
            loadAd();
            if (mShowButton != null && getAvailableAdCount() == 0) {
                mShowButton.setEnabled(false);
            }
            logToast(getActivity(), String.format(Locale.US, "Rewarded video failed to load: %s",
                    errorCode.toString()));
        }
    }

    @Override
    public void onRewardedVideoStarted(@NonNull final String adUnitId) {
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
            logToast(getActivity(), "Rewarded video started.");
            if (mShowButton != null && getAvailableAdCount() == 0) {
                mShowButton.setEnabled(false);
            }
        }
    }

    @Override
    public void onRewardedVideoPlaybackError(@NonNull final String adUnitId, @NonNull final MoPubErrorCode errorCode) {
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
            mAdUnitIdsMap.put(adUnitId, RewardedVideoStatus.EMPTY);
            loadAd();
            logToast(getActivity(), String.format(Locale.US, "Rewarded video playback error: %s",
                    errorCode.toString()));
            if (mShowButton != null && getAvailableAdCount() == 0) {
                mShowButton.setEnabled(false);
            }
        }
    }

    @Override
    public void onRewardedVideoClicked(@NonNull final String adUnitId) {
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
            logToast(getActivity(), "Rewarded video clicked.");
        }
    }

    @Override
    public void onRewardedVideoClosed(@NonNull final String adUnitId) {
        if (mAdUnitIdsMap.containsKey(adUnitId)) {
            mAdUnitIdsMap.put(adUnitId, RewardedVideoStatus.EMPTY);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadAd();
                }
            }, 100);

            logToast(getActivity(), "Rewarded video closed.");

            if (mShowButton != null && getAvailableAdCount() == 0) {
                mShowButton.setEnabled(false);
            }
        }


    }

    @Override
    public void onRewardedVideoCompleted(@NonNull final Set<String> adUnitIds,
            @NonNull final MoPubReward reward) {
        if (mAdUnitIdsMap.keySet().containsAll(adUnitIds)) {
            logToast(getActivity(),
                    String.format(Locale.US,
                            "Rewarded video completed with reward  \"%d %s\"",
                            reward.getAmount(),
                            reward.getLabel()));
        }

    }

//    public void selectReward(@NonNull String selectedReward) {
//        mSelectedReward = mMoPubRewardsMap.get(selectedReward);
//        MoPubRewardedVideos.selectReward(mAdUnitId, mSelectedReward);
//    }

//    public static class SelectRewardDialogFragment extends DialogFragment {
//        @NonNull private String[] mRewards;
//        @NonNull private String mSelectedReward;
//
//        public static SelectRewardDialogFragment newInstance() {
//            return new SelectRewardDialogFragment();
//        }
//
//        public void loadRewards(@NonNull String[] rewards) {
//            mRewards = rewards;
//        }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            AlertDialog dialog = new AlertDialog.Builder(getActivity())
//                    .setTitle("Select a reward")
//                    .setSingleChoiceItems(mRewards, -1, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            mSelectedReward = mRewards[which];
//                        }
//                    })
//                    .setPositiveButton("Select", null)
//                    .create();
//
//            // Overriding onShow() of dialog's OnShowListener() and onClick() of the Select button's
//            // OnClickListener() to prevent the dialog from dismissing upon any button click without
//            // selecting an item first.
//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    Button selectButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
//                    selectButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (mSelectedReward != null) {
//                                ((RewardedVideoDetailFragment) getTargetFragment())
//                                        .selectReward(mSelectedReward);
//                                dismiss();
//                            }
//                        }
//                    });
//                }
//            });
//
//            return dialog;
//        }
//    }
}
