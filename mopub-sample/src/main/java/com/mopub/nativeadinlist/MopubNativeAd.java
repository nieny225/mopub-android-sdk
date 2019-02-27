package com.mopub.nativeadinlist;

import com.mopub.nativeads.NativeAd;

public class MopubNativeAd {

    String placemetnId;
    NativeAd mopubAd;

    public MopubNativeAd(String placemetnId) {
        this.placemetnId = placemetnId;
    }

    public String getPlacemetnId() {
        return placemetnId;
    }

    public void setPlacemetnId(String placemetnId) {
        this.placemetnId = placemetnId;
    }

    public NativeAd getMopubAd() {
        return mopubAd;
    }

    public void setMopubAd(NativeAd mopubAd) {
        this.mopubAd = mopubAd;
    }
}
