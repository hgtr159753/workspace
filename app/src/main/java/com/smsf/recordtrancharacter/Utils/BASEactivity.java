package com.smsf.recordtrancharacter.Utils;

/**
 * @Description: java类作用描述
 * @Author: liys
 * @CreateDate: 2020/5/19 17:06
 */

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

/**
 * @Description: 父类
 * @Author: Mr
 * @CreateDate: 2020/2/11 9:34
 */

public abstract class BASEactivity extends FragmentActivity {

    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //设置布局内容
        setContentView(getLayoutId());
        //初始化控件
        initViews(savedInstanceState);
        //初始化数据
        initData();
        //初始化事件
        initListener();
    }


    public abstract int getLayoutId();

    protected abstract void initViews(Bundle savedInstanceState);

    protected abstract void initData();

    protected abstract void initListener();


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
