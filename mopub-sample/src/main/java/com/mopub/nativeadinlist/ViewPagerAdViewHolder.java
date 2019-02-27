package com.mopub.nativeadinlist;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.mopub.nativeads.AdapterHelper;
import com.mopub.nativeads.ViewBinder;
import com.mopub.simpleadsdemo.R;

public class ViewPagerAdViewHolder {
    private RelativeLayout adContainer;
    private Context appContext;
    private AdapterHelper adapterHelper;
    private static final String TAG = "Native-Ad";

    ViewPagerAdViewHolder(Context appContext, View itemView) {
        adContainer = itemView.findViewById(R.id.li_adcontainer);
        this.appContext = appContext;
    }

    public void updateUI(int position, DataSource.ItemData data, MopubController.IAdStatusListener listener){


        if (data.isAd() && data.getNativeAd()!=null) {
            Log.e(TAG, " updateUI ad  " + position);
            //FIXME: Is this proper implementation?? Multiple Adapter Helpers ar created and we are passing position.
            adapterHelper = new AdapterHelper(appContext, position, 3); // When standalone, any range will be fine.
            View adView = adapterHelper.getAdView(null, null,  data.getNativeAd(), new ViewBinder.Builder(0).build());
            adContainer.removeAllViews();
            adContainer.addView(adView);
        }else{
            Log.e(TAG, " updateUI ad is not loaded " + position);
        }
        MopubController.getInstance(appContext).loadNativeAd(position,data, listener);


    }

}