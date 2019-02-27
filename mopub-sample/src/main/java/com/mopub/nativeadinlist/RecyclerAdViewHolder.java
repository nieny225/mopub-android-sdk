package com.mopub.nativeadinlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.mopub.nativeads.AdapterHelper;
import com.mopub.nativeads.ViewBinder;
import com.mopub.simpleadsdemo.R;

public class RecyclerAdViewHolder extends RecyclerView.ViewHolder {
    private RelativeLayout adContainer;
    private Context appContext;
    private AdapterHelper adapterHelper;
    private static final String TAG = "RecyclerView Native-Ad";

    RecyclerAdViewHolder(Context appContext, View itemView) {
        super(itemView);
        adContainer = itemView.findViewById(R.id.li_adcontainer);
        this.appContext = appContext;
        //FIXME: Is this proper implementation?? only one AdapterHelper is created. with start position as 0.
        adapterHelper = new AdapterHelper(appContext, 0, 3);
    }

    public void updateUI(int position, DataSource.ItemData data, MopubController.IAdStatusListener listener){


        if (data.isAd() && data.getNativeAd()!=null) {
            Log.e(TAG, " updateUI ad  " + position);
            //FIXME: Is this proper implementation?? Only one adapter helper is created and using its method to get Adview.
            View adView = adapterHelper.getAdView(null, null,  data.getNativeAd(), new ViewBinder.Builder(0).build());
            adContainer.removeAllViews();
            adContainer.addView(adView);
        }else{
            Log.e(TAG, " updateUI ad is not loaded " + position);
        }

        //COMMENT: for refresh ad purpose
        MopubController.getInstance(appContext).loadNativeAd(position,data, listener);


    }

}