package com.mopub.nativeadinlist;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mopub.simpleadsdemo.R;

public  class ViewPagerItemViewHolder {
    private ImageView imageView;
    private TextView textView;
    ViewPagerItemViewHolder(View itemView) {
        imageView = itemView.findViewById(R.id.li_imageview);
        textView = itemView.findViewById(R.id.li_textview);

    }

    public void updateUI(int position){
        textView.setText(String.valueOf(position));
        Log.i("TAG", " data - " + position);
    }

}