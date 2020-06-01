package com.smsf.recordtrancharacter.wxapi;

import android.content.Intent;
import android.os.Bundle;

import com.smsf.recordtrancharacter.Utils.Conts;
import com.smsf.recordtrancharacter.Utils.ToastUtils;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import androidx.fragment.app.FragmentActivity;


public class WXEntryActivity extends FragmentActivity implements IWXAPIEventHandler {
    private IWXAPI api;
    public static BaseResp resp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, Conts.WECHAT_APP_ID, false);
        api.handleIntent(getIntent(), this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
        finish();
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        String result = "";

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = "登录成功";
                ToastUtils.showToast(this,result);
                if (resp != null) {
                    WXEntryActivity.resp = resp;
                }
                finish();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "发送取消";
                ToastUtils.showToast(this,result);
                WXEntryActivity.resp = null;
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "发送被拒绝";
                ToastUtils.showToast(this,result);
                WXEntryActivity.resp = null;
                finish();
                break;
            default:
                result = "发送返回";
                ToastUtils.showToast(this,result);
                WXEntryActivity.resp = null;
                finish();
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
        finish();
    }

}