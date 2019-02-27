package com.mopub.nativeadinlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.simpleadsdemo.R;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MyRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements MopubController.IAdStatusListener {

    private static final int INFLATER_TYPE_AD = 1;
    private static final int INFLATER_TYPE_DEFAULT = 0;
    private static final String TAG = "RecyclerView Native-Ad";

    private Context context;
    private List<DataSource.ItemData> itemDataList;
    private LayoutInflater layoutInflater;
    private Map<Integer, DataSource.ItemData> adSlots;


    public MyRecyclerAdapter(Context context, List<DataSource.ItemData> data) {
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewtype) {
        switch (viewtype) {

            case INFLATER_TYPE_DEFAULT:

                View itemView = layoutInflater.inflate(R.layout.layout_viewpager_item, parent, false);
                return new  RecyclerItemViewHolder(itemView);
            case INFLATER_TYPE_AD:

                View adView = layoutInflater.inflate(R.layout.layout_viewpager_ad, parent, false);
                return  new RecyclerAdViewHolder(context,adView);

        }
        return null;
    }


    @Override
    public int getItemViewType(int position) {
        if (itemDataList == null || itemDataList.get(position) == null) return INFLATER_TYPE_DEFAULT;

        return (itemDataList.get(position).isAd())? INFLATER_TYPE_AD:INFLATER_TYPE_DEFAULT;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        switch (getItemViewType(position)){
            case INFLATER_TYPE_DEFAULT:
                ((RecyclerItemViewHolder)viewHolder).updateUI(position);
                break;
            case INFLATER_TYPE_AD:
                ((RecyclerAdViewHolder)viewHolder).updateUI(position,itemDataList.get(position),this);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return itemDataList != null ? itemDataList.size() : 0;
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

            notifyItemInserted(position);
        }else{
            Log.i(TAG, " onAdLoadSuccess not added item" + position);
            notifyItemChanged(position);
        }

    }

    @Override
    public void onAdLoadFailure(int position) {
        Log.i(TAG, " onAdLoadFailure " + position);
    }
}
