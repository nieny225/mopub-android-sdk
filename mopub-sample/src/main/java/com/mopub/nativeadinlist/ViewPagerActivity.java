package com.mopub.nativeadinlist;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

import com.mopub.simpleadsdemo.R;

import java.util.List;

/**
 * Mopub Native ad View pager and Recycler view implementations using Manual Integration way.
 *
 *
 */

public class ViewPagerActivity extends FragmentActivity {
    ViewPager viewPager;
    RecyclerView recyclerView;
    List<DataSource.ItemData> viewPagerData,recyclerViewData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        viewPager = this.findViewById(R.id.viewpager);
        viewPager.setClipToPadding(false);
        viewPager.setPadding(dpToPx(13) , 0, dpToPx(47), 0);
        viewPager.setPageMargin(dpToPx(2));

        recyclerView = this.findViewById(R.id.recyclerview);
        //COMMENT: If we dont add this delay sdk will not be initialized and always ad load will fail.

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPagerData =  DataSource.generateData(10);
                recyclerViewData =  DataSource.generateData(10);
                // viewPager.setAdapter(new ViewPagerAdapter(getBaseContext(),viewPagerData));
                 //COMMENT: Recycler view implementation.
                 recyclerView.setAdapter(new MyRecyclerAdapter(getBaseContext(),recyclerViewData));
            }
        },2000);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearAds();
    }

    public void clearAds(){

        if (recyclerViewData!=null){
            for (DataSource.ItemData itemData : recyclerViewData) {
                if (itemData.isAd() && itemData.getNativeAd()!=null){
                    //Destroy each native ad object.
                    itemData.getNativeAd().destroy();
                    itemData.setNativeAd(null);
                    itemData.setMopubAdRequestObject(null);
                }
            }
        }


        if (viewPagerData!=null){
            for (DataSource.ItemData itemData : viewPagerData) {
                if (itemData.isAd() && itemData.getNativeAd()!=null){
                    itemData.getNativeAd().destroy();
                    itemData.setNativeAd(null);
                    itemData.setMopubAdRequestObject(null);
                }
            }
        }
    }
}
