package com.netease.nim.demo.main.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.netease.nim.demo.R;

/**
 * Created by lsr on 2015/11/18.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DiscoverFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_discover, container, false);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onStart() {
        super.onStart();
    }

    /*
      * 函数名：initWidget()
      * 说明：用于初始化控件,包括ListView, linearlayout
      * */

}
