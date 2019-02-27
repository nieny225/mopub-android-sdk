package com.mopub.nativeadinlist;

import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.NativeAd;
import com.mopub.network.AdLoader;

import java.util.ArrayList;
import java.util.List;

public class DataSource {

    public static  class ItemData {

        private String title;

        private boolean isAd = false;

        private String extraData; // For ads it will be placement id.


        private NativeAd nativeAd;

        private MoPubNative mopubAdRequestObject;

        private long lastLoadedTimeStamp;

        public MoPubNative getMopubAdRequestObject() {
            return mopubAdRequestObject;
        }

        public void setMopubAdRequestObject(MoPubNative mopubAdRequestObject) {
            this.mopubAdRequestObject = mopubAdRequestObject;
        }

        public ItemData(){

        }
        public ItemData(String title){
            this.title = title;
        }

        public ItemData(boolean isAd, String extraData){
            this.extraData = extraData;
            this.isAd = isAd;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setAd(boolean ad) {
            isAd = ad;
        }

        public boolean isAd() {
            return isAd;
        }

        public void setExtraData(String extraData) {
            this.extraData = extraData;
        }

        public String getExtraData() {
            return extraData;
        }

        public NativeAd getNativeAd() {
            return nativeAd;
        }

        public void setNativeAd(NativeAd nativeAd) {
            this.nativeAd = nativeAd;
            this.lastLoadedTimeStamp = System.currentTimeMillis();
        }

        public boolean isRefreshable(){
            return ((System.currentTimeMillis()- lastLoadedTimeStamp) >=2000);
        }
    }

    public  static List<ItemData> generateData(int size){

        List<ItemData>  list = new ArrayList<>(size);
        for (int index = 1; index<=size; index++){
            list.add(new ItemData("Item Data " + index));
        }
        return list;
    }
}
