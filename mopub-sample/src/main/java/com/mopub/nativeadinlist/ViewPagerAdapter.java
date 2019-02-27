package com.mopub.nativeadinlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.simpleadsdemo.R;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends PagerAdapter implements MopubController.IAdStatusListener {

    private static final int INFLATER_TYPE_AD = 1;
    private static final int INFLATER_TYPE_DEFAULT = 0;

    private static final String TAG = "Native-Ad";

    private Context context;
    private List<DataSource.ItemData> itemDataList;
    private LayoutInflater layoutInflater;
    private  Map<Integer, DataSource.ItemData> adSlots;
    public ViewPagerAdapter(Context context, List<DataSource.ItemData> data) {
        itemDataList = data;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        loadAndInsertAds();
    }

    /**
     * Data comes from server with position, ad information. we will request for ad. If ad load is success
     * Add to list and refresh when user visits adslot again (by swiping).
     */
    private void loadAndInsertAds(){
        adSlots = new LinkedHashMap<>();
        adSlots.put(1, new DataSource.ItemData(true,"91e4e8af17214dd5b45292f5d23d1705"));
        adSlots.put(4, new DataSource.ItemData(true,"91e4e8af17214dd5b45292f5d23d1705"));
        for (Map.Entry<Integer, DataSource.ItemData> entry : adSlots.entrySet()){
            MopubController.getInstance(context).loadNativeAd(entry.getKey(), entry.getValue(),this);
        }

    }




    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = null;
        if (itemDataList == null || itemDataList.get(position) == null) return itemView;
        DataSource.ItemData itemData = itemDataList.get(position);
        switch (getViewType(position)) {

            case INFLATER_TYPE_DEFAULT:
                Log.i(TAG, " instantiateItem item type" + position);

                itemView = layoutInflater.inflate(R.layout.layout_viewpager_item, container, false);
                ViewPagerItemViewHolder itemHolder = new ViewPagerItemViewHolder(itemView);
                itemHolder.updateUI(position);
                break;
            case INFLATER_TYPE_AD:
                Log.i(TAG, " instantiateItem ad type" + position);

                itemView = layoutInflater.inflate(R.layout.layout_viewpager_ad, container, false);
                ViewPagerAdViewHolder adHolder = new ViewPagerAdViewHolder(context,itemView);
                adHolder.updateUI(position,itemData,this);
                break;
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
    @Override
    public int getCount() {
        return itemDataList != null ? itemDataList.size() : 0;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public int getViewType(int position) {
        if (itemDataList == null || itemDataList.get(position) == null) return INFLATER_TYPE_DEFAULT;

        return (itemDataList.get(position).isAd())? INFLATER_TYPE_AD:INFLATER_TYPE_DEFAULT;
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    /**
     *  If ad item is not added then  Add it to list.
     *  if already available refresh the list
     * @param itemData
     * @param position
     */
    @Override
    public synchronized void onAdLoadSuccess(DataSource.ItemData itemData, int position) {
        Log.i(TAG, " onAdLoadSuccess " + position);

        if (adSlots.containsKey(position)){
            adSlots.remove(position);
            itemDataList.add(position, itemData);
            Log.i(TAG, " onAdLoadSuccess added item" + position);

            notifyDataSetChanged();
        }else{
            Log.i(TAG, " onAdLoadSuccess not added item" + position);
            //COMMENT: In out actual implementation we dont refresh entire view pager.
            notifyDataSetChanged();
        }

    }

    @Override
    public void onAdLoadFailure(int position) {
        Log.i(TAG, " onAdLoadFailure " + position);
    }




}
