package com.abt.camera_control_view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }*/

    private static final String TAG = "MainActivity";
    private CameraControlView mCameraControlView;
    private CameraSwitchView mCustomViewScrolling;
    private WeekChooseView mScrollChooseView;

    // String mTitles[] = new String[] { "早餐前", "早餐后", "午餐前", "午餐后", "晚餐前", "晚餐后", "睡前" };
    String mTitles[] = new String[] { "早餐前", "早餐后" };

    private int picIds[] = new int[] {
            R.mipmap.time_bg_breakfastbefore, R.mipmap.time_bg_breakfastafter
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCustomViewScrolling = (CameraSwitchView) findViewById(R.id.custom_view_scrolling);
        mCustomViewScrolling.setOnCheckListener(new CameraSwitchView.OnCheckListener() {
            @Override
            public void itemOnCheckListener(boolean videoChecked) {
                Log.d(TAG, "itemOnCheckListener videoChecked = "+videoChecked);
            }
        });

        mScrollChooseView = (WeekChooseView) findViewById(R.id.scroll_choose_view);
        mScrollChooseView.setTitles(mTitles);
        mScrollChooseView.setPicIds(picIds);
        mScrollChooseView.setOnScrollEndListener(new WeekChooseView.OnScrollEndListener() {
            @Override
            public void currentPosition(int position) {
                Log.d(TAG, "当前 position = " + position + " " + mTitles[position]);
            }
        });

    }
}
