package com.smsf.recordtrancharacter;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.smsf.recordtrancharacter.Utils.ApiUtils;
import com.smsf.recordtrancharacter.Utils.AppUtils;
import com.smsf.recordtrancharacter.Utils.BASEactivity;
import com.smsf.recordtrancharacter.Utils.Conts;
import com.smsf.recordtrancharacter.Utils.SharedPUtils;
import com.smsf.recordtrancharacter.Utils.StatusBarUtil;
import com.smsf.recordtrancharacter.Utils.ToastUtils;
import com.smsf.recordtrancharacter.Utils.UserBean;
import com.smsf.recordtrancharacter.view.MSDialog;
import com.smsf.recordtrancharacter.view.VerificationTimer;
import com.smsf.recordtrancharacter.wxapi.WXEntryActivity;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * @Description: 登录页面
 * @Author: Mr
 * @CreateDate: 2020/3/4 10:18
 */

public class UserLoginActivity extends BASEactivity implements View.OnClickListener {

    private TextView toolbar_title;  //标题
    private TextView save_btn;   //跳过
    private TextView code_btn;  //验证码
    private VerificationTimer verificationTimer;  //倒计时
    private RelativeLayout wechat_login;  //微信登录
    private boolean sIsWXAppInstalledAndSupported;
    private IWXAPI wxApi;
    private MSDialog msDialog;
    private Button login_btn;  //登录按钮
    private String isLoaderActivity;  //获取那个页面显示的

    @Override
    public int getLayoutId() {
        StatusBarUtil.setStatuBar(this, getResources().getColor(R.color.title_bar_bg));
        return R.layout.activity_userlogin;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        toolbar_title = findViewById(R.id.toolbar_title);
        save_btn = findViewById(R.id.save_btn);
        code_btn = findViewById(R.id.code_btn);
        wechat_login = findViewById(R.id.wechat_login);
        login_btn = findViewById(R.id.login_btn);

    }

    @Override
    protected void initData() {
        toolbar_title.setText("登录");
        save_btn.setText("跳过");
        save_btn.setVisibility(View.VISIBLE);
        code_btn.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        code_btn.getPaint().setAntiAlias(true);//抗锯齿
        verificationTimer = new VerificationTimer(60000, 1000, code_btn);
        initWXLogin();
        isLoaderActivity = getIntent().getStringExtra("isLoaderActivity");
    }

    @Override
    protected void initListener() {
        save_btn.setOnClickListener(this);
        code_btn.setOnClickListener(this);
        wechat_login.setOnClickListener(this);
        login_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.save_btn:
                MobclickAgent.onEvent(this, "Login_Skip_btn");
                ApiUtils.report(this, "登录页面跳过");
                if (!TextUtils.isEmpty(isLoaderActivity)) {
                    if ("splash".equals(isLoaderActivity)) {
                        Intent intent = new Intent(this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    finish();
                }
                break;
            case R.id.code_btn:
                verificationTimer.start();
                break;
            case R.id.wechat_login:
                // 微信登录
                WXLogin();
//                MobclickAgent.onEvent(this, "Login_Wechat_btn");
//                ApiUtils.report(this, "微信登录");
                break;
            case R.id.login_btn:
                // 登录按钮
                MobclickAgent.onEvent(this, "Login_btn");
                ApiUtils.report(this, "登录按钮");

                break;
        }
    }

    /**
     * 初始化微信
     */
    private void initWXLogin() {
        wxApi = WXAPIFactory.createWXAPI(this, null, true);
        wxApi.registerApp(Conts.WECHAT_APP_ID);
        sIsWXAppInstalledAndSupported = wxApi.isWXAppInstalled()
                && wxApi.isWXAppSupportAPI();
    }

    /**
     * 登录微信
     */
    private void WXLogin() {
        if (sIsWXAppInstalledAndSupported) {
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "sym";
            wxApi.sendReq(req);
        } else {
            msDialog = new MSDialog(this);
            msDialog.setCanceledOnTouchOutside(true);
            String dialmsg = "未安装微信,是否马上下载";
            msDialog.setMessage(dialmsg);
            msDialog.setPositiveButton(getResources().getString(R.string.ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 调用自带浏览器
                            Intent mIntent = new Intent();
                            mIntent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse("http://weixin.qq.com");
                            mIntent.setData(content_url);
                            startActivity(mIntent);
                            msDialog.dismiss();
                        }
                    });
            msDialog.setNegativeButton(getResources()
                    .getString(R.string.cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    msDialog.dismiss();

                }
            });
            msDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //微信返回,填充用户信息
        BaseResp resp = WXEntryActivity.resp;
        Log.d("ddd", String.valueOf(resp));
        if (resp != null) {
            WXEntryActivity.resp = null;
            if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
                // code返回
                String weixinCode = ((SendAuth.Resp) resp).code;
                if (!TextUtils.isEmpty(weixinCode)) {
                    // 传参操作
                    Log.d("mrs", "============返回Code==========" + weixinCode);
                    Wx_Login(weixinCode);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wxApi.detach();
        if (wxApi != null) {
            wxApi = null;
        }
    }

    /**
     * 微信登录
     */
    public void Wx_Login(String wx_userCode) {
        HashMap<String, String> params = new HashMap<>();
        params.put("appVersion", AppUtils.getVersionName(this));
        String androidId = AppUtils.getAndroidId(this);
        params.put("deviceId", androidId);
        params.put("appChannel", AnalyticsConfig.getChannel(this));
        Log.d("mrs", "======aa==========" + AppUtils.getPackageName(this));
        params.put("pkgname", AppUtils.getPackageName(this));
        params.put("Code", wx_userCode);
        OkHttpUtils.post().url(Conts.WX_LOGIN)
                .params(params)
                .build().execute(new StringCallback() {
            @Override
            public void onError(okhttp3.Call call, Exception e, int id) {
                Log.d("mrs", "============onError===========");
                ToastUtils.showToast(UserLoginActivity.this, "登录失败,请重新登录");
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d("mrs", "============onResponse===========" + response);
                // 登录成功
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    UserBean userBean = gson.fromJson(response, UserBean.class);
                    if (userBean != null && userBean.getCode() == 200) {
                        // 登录成功保存用户信息
                        SharedPUtils.setUserSuccess(UserLoginActivity.this, true);
                        SharedPUtils.setUserLogin(UserLoginActivity.this, userBean);
                        // 记录首次注册时间获取当前日期时分秒
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.DAY_OF_MONTH, + 3);
                        String res = simpleDateFormat.format(c.getTime());
                        // 将到期时间保存
                        SharedPUtils.setVipExpire(UserLoginActivity.this, res);
                        SharedPUtils.setIsVip(UserLoginActivity.this, true);
                        finish();
                    } else {
                        ToastUtils.showToast(UserLoginActivity.this, "登录失败,请重新登录");
                    }
                }
            }
        });
    }

}
