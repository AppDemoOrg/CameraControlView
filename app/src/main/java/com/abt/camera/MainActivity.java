package com.abt.camera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.abt.camera.view.CameraSwitchView;
import com.abt.camera.view.WeekChooseView;
import com.orhanobut.logger.Logger;

public class MainActivity extends AppCompatActivity {

    private CameraSwitchView mCameraSwitchView;
    private WeekChooseView mWeekChooseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initCameraController();
        initCameraSwitcher();
        initWeekChooser();
    }

    private void initWeekChooser() {
        mWeekChooseView = (WeekChooseView) findViewById(R.id.week_choose_view);
        mWeekChooseView.setTitles(new String[]{"晚上", "早上"});
        mWeekChooseView.setPicIds(new int[]{R.mipmap.time_bg_breakfastbefore, R.mipmap.time_bg_breakfastafter});
        mWeekChooseView.setOnScrollEndListener(new WeekChooseView.OnScrollEndListener() {
            @Override
            public void currentPosition(int position) {
                Logger.d("当前 position = " + position);
            }
        });
    }

    private void initCameraSwitcher() {
        mCameraSwitchView = (CameraSwitchView) findViewById(R.id.camera_switch_view);
        mCameraSwitchView.setOnCheckListener(new CameraSwitchView.OnCheckListener() {
            @Override
            public void itemOnCheckListener(boolean videoChecked) {
                Logger.d("itemOnCheckListener videoChecked = " + videoChecked);
            }
        });
    }

}
