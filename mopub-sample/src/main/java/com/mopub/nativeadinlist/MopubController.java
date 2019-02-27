package com.mopub.nativeadinlist;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.nativeads.FacebookAdRenderer;
import com.mopub.nativeads.GooglePlayServicesAdRenderer;
import com.mopub.nativeads.MediaViewBinder;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.MoPubVideoNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.ViewBinder;
import com.mopub.simpleadsdemo.R;

import java.util.EnumSet;

public class MopubController {

    private Context appContext;
    private static final String TAG = "Native-Ad";

    public interface IAdStatusListener {
        void onAdLoadSuccess(DataSource.ItemData itemData, int position);

        void onAdLoadFailure( int position);
    }

    private static volatile  MopubController  INSTANCE;

    private MopubController(Context appContext) {
        Log.i(TAG, " constructor - start");
        SdkConfiguration configuration = new SdkConfiguration.Builder(
                "91e4e8af17214dd5b45292f5d23d1705").build();
        MoPub.initializeSdk(appContext, configuration, new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                Log.i(TAG, " constructor - onInitializationFinished");

            }
        });
        Log.i(TAG, " constructor - end");

    }

    public synchronized static MopubController getInstance(Context appContext){
        if (INSTANCE==null) {
            INSTANCE = new MopubController(appContext);
            INSTANCE.appContext = appContext;
        }
        return INSTANCE;

    }

    /**
     *
     * - Request new ad once ad is loaded/failed send callback with data.
     *
     * - If MoPubNative already available, reuse it for refreshing ad(If refresh time is met.).
     *
     *
     * @param position
     * @param adData
     * @param listener
     */

    public void loadNativeAd(final int position, final  DataSource.ItemData adData, final IAdStatusListener listener) {
        Log.i(TAG, " getNativeAd - start");
        // Setting desired assets on your request helps native ad networks and bidders
        // provide higher-quality ads.
        final EnumSet<RequestParameters.NativeAdAsset> desiredAssets = EnumSet.of(
                RequestParameters.NativeAdAsset.TITLE,
                RequestParameters.NativeAdAsset.TEXT,
                RequestParameters.NativeAdAsset.ICON_IMAGE,
                RequestParameters.NativeAdAsset.MAIN_IMAGE,
                RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT,
                RequestParameters.NativeAdAsset.STAR_RATING
        );

        RequestParameters  requestParameters = new RequestParameters.Builder()
                .desiredAssets(desiredAssets)
                .build();

       if (adData.getMopubAdRequestObject()==null){

           ViewBinder staticViewBinder = new ViewBinder.Builder(R.layout.native_ad_list_item)
                   .titleId(R.id.native_title)
                   .textId(R.id.native_text)
                   .mainImageId(R.id.native_main_image)
                   .iconImageId(R.id.native_icon_image)
                   .callToActionId(R.id.native_cta)
                   .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
                   .build();

           MediaViewBinder videoViewBinder = new MediaViewBinder.Builder(R.layout.video_ad_list_item)
                   .titleId(R.id.native_title)
                   .textId(R.id.native_text)
                   .mediaLayoutId(R.id.native_media_layout)
                   .iconImageId(R.id.native_icon_image)
                   .callToActionId(R.id.native_cta)
                   .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
                   .build();

           FacebookAdRenderer.FacebookViewBinder fbViewBinder = new FacebookAdRenderer.FacebookViewBinder.Builder(R.layout.fb_native_ad_list_item)
                   .titleId(R.id.native_title)
                   .textId(R.id.native_text)
                   .mediaViewId(R.id.native_media_layout)
                   .adIconViewId(R.id.native_icon_image)
                   .callToActionId(R.id.native_cta)
                   .adChoicesRelativeLayoutId(R.id.native_privacy_information_icon_image)
                   .build();

           // Set up a renderer for a admob and facebook native ad.
           final GooglePlayServicesAdRenderer googlePlayServicesAdRenderer = new GooglePlayServicesAdRenderer(videoViewBinder);
           final FacebookAdRenderer facebookAdRenderer = new FacebookAdRenderer(fbViewBinder);

           // Set up a renderer for a mopub static native ad.
           final MoPubStaticNativeAdRenderer moPubStaticNativeAdRenderer = new MoPubStaticNativeAdRenderer(staticViewBinder);
           // Set up a renderer for a mopub video native ad.
           final MoPubVideoNativeAdRenderer moPubVideoNativeAdRenderer = new MoPubVideoNativeAdRenderer(videoViewBinder);


           MoPubNative moPubNative = new MoPubNative(appContext, adData.getExtraData(), new MoPubNative.MoPubNativeNetworkListener() {
               @Override
               public void onNativeLoad(final NativeAd nativeAd) {
                   Log.d(TAG, "getNativeAd - Native ad  loaded." + position);
                   // Set the native event listeners (onImpression, and onClick).
                   nativeAd.setMoPubNativeEventListener(new NativeAd.MoPubNativeEventListener() {
                       @Override
                       public void onImpression(View view) {
                           Log.d(TAG, "getNativeAd - Native ad recorded an impression.");
                       }

                       @Override
                       public void onClick(View view) {
                           Log.d(TAG, "getNativeAd - Native ad recorded a click.");
                       }
                   });
                   adData.setNativeAd(nativeAd);
                   listener.onAdLoadSuccess(adData, position);

               }

               @Override
               public void onNativeFail(NativeErrorCode errorCode) {
                   Log.d(TAG, "getNativeAd - Native ad failed to load with error: " + errorCode.toString());
                   listener.onAdLoadFailure(position);
               }
           });

           //Register networks renders first before registering mopub's
           moPubNative.registerAdRenderer(googlePlayServicesAdRenderer);
           moPubNative.registerAdRenderer(facebookAdRenderer);
           moPubNative.registerAdRenderer(moPubStaticNativeAdRenderer);
           moPubNative.registerAdRenderer(moPubVideoNativeAdRenderer);

           adData.setMopubAdRequestObject(moPubNative);
           moPubNative.makeRequest(requestParameters);
           Log.d(TAG, "getNativeAd - new ad is requested " );

       }else if(adData.isRefreshable()) {
           Log.d(TAG, "getNativeAd - ad is refreshing " );
            adData.getMopubAdRequestObject().makeRequest(requestParameters);
        }else {
           Log.w(TAG, "getNativeAd - ad cannot be  refreshed " );
       }

    }
}
