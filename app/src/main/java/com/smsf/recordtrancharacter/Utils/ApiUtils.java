package com.smsf.recordtrancharacter.Utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;

/**
 * @Description: 打点工具类
 * @Author: Mr
 * @CreateDate: 2020/3/5 14:25
 */

public class ApiUtils {

    static final String URL_API_REPORT = "http://47.104.12.170/";


    public static void report(Context context, final String clickName) {
        HashMap<String, String> params = new HashMap<>();
        String imie = AppUtils.getIMEI(context);
        if (TextUtils.isEmpty(imie)) {
            if (!TextUtils.isEmpty(SharedPUtils.getAppIMIE(context)) && SharedPUtils.getAppIMIE(context).length() > 0) {
                imie = SharedPUtils.getAppIMIE(context);
            } else {
                imie = AppUtils.randomWord();
                SharedPUtils.setAppIMIE(context, imie);
            }
        }
        params.put("deviceId", imie);
        params.put("appVersion", AppUtils.getVersionName(context));
        params.put("pkgName", AppUtils.getPackageName(context));
        params.put("clickName", clickName);
        OkHttpUtils.get().url(URL_API_REPORT)
                .params(params)
                .build().execute(new StringCallback() {
            @Override
            public void onError(okhttp3.Call call, Exception e, int id) {
                Log.d("mrs", "=======================打点:" + clickName + "失败");
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d("mrs", "=======================打点:" + clickName + "成功");

            }
        });
    }
}
