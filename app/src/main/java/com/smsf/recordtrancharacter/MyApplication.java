package com.smsf.recordtrancharacter;

import android.app.Application;

import com.tencent.bugly.Bugly;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "eb4edb03a01ff225a76a2d65c88bbba2");
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        UMConfigure.setLogEnabled(true);
        MobclickAgent.setCatchUncaughtExceptions(true);
        UMConfigure.setEncryptEnabled(true);
        // 腾讯热升级获取版本号
        String channelName = AnalyticsConfig.getChannel(this);
        Bugly.setAppChannel(this, channelName);
        // 腾讯Bugly
        Bugly.init(getApplicationContext(), "ebc80bb2c3", false);
    }

}
