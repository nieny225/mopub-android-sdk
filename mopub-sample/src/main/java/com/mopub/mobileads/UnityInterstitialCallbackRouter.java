package com.mopub.mobileads;

import com.mopub.common.logging.MoPubLog;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;

import java.util.HashMap;
import java.util.Map;

public class UnityInterstitialCallbackRouter implements IUnityAdsExtendedListener {
	private final Map<String, IUnityAdsExtendedListener> listeners = new HashMap<>();
	private String currentPlacementId;

	@Override
	public void onUnityAdsReady(String placementId) {
		MoPubLog.d("Unity Router: onUnityAdsReady: "+placementId);
		IUnityAdsExtendedListener listener = listeners.get(placementId);
		if (listener != null) {
			listener.onUnityAdsReady(placementId);
		}
	}

	@Override
	public void onUnityAdsStart(String placementId) {
		MoPubLog.d("Unity Router: onUnityAdsStart: "+placementId);
		IUnityAdsExtendedListener listener = listeners.get(placementId);
		if (listener != null) {
			listener.onUnityAdsStart(placementId);
		}
	}

	@Override
	public void onUnityAdsFinish(String placementId, UnityAds.FinishState finishState) {
		IUnityAdsExtendedListener listener = listeners.get(placementId);
		MoPubLog.d("Unity Router: onUnityAdsFinish: "+placementId);
		if (listener != null) {
			listener.onUnityAdsFinish(placementId, finishState);
		}
	}

	@Override
	public void onUnityAdsClick(String placementId) {
		IUnityAdsExtendedListener listener = listeners.get(placementId);
		if (listener != null) {
			listener.onUnityAdsClick(placementId);
		}
	}

	@Override
	public void onUnityAdsPlacementStateChanged(String placementId, UnityAds.PlacementState oldState, UnityAds.PlacementState newState) {
		MoPubLog.d("Unity Router: onUnityAdsPlacementStateChanged: "+placementId);
		IUnityAdsExtendedListener listener = listeners.get(placementId);
		if (listener != null) {
			listener.onUnityAdsPlacementStateChanged(placementId, oldState, newState);
		}
	}

	@Override
	public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String message) {
		IUnityAdsExtendedListener listener = listeners.get(currentPlacementId);
		if (listener != null) {
			listener.onUnityAdsError(unityAdsError, message);
		}
	}

	public void addListener(String placementId, IUnityAdsExtendedListener listener) {
		listeners.put(placementId, listener);
		MoPubLog.d("UnityRouter.getInterstitialRouter().addListener() for: "+placementId);
	}


	public void removeListener(String placementId) {
		listeners.remove(placementId);
		MoPubLog.d("UnityRouter.getInterstitialRouter().removeListener() for: "+placementId);
	}

	public void setCurrentPlacementId(String placementId) {
		currentPlacementId = placementId;
	}
}
