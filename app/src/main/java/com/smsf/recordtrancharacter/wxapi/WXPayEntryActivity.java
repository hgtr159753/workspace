package com.smsf.recordtrancharacter.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.smsf.recordtrancharacter.Utils.Conts;
import com.smsf.recordtrancharacter.Utils.ToastUtils;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;


/**
 * 微信支付返回接口
 * */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

	private IWXAPI api;

	public static boolean paySuccess = false; 
	/**
	 * 提示支付
	 * */
	private String errResult = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		api = WXAPIFactory.createWXAPI(this, Conts.WECHAT_APP_ID);
		api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if (resp.errCode == 0) {
				errResult = "支付成功";
				paySuccess = true;
			} else if (resp.errCode == -1) {
				errResult = "出现异常,请重新尝试";
				paySuccess = false;
			} else if (resp.errCode == -2) {
				errResult = "支付中断";
				paySuccess = false;
			}
			ToastUtils.showToast(this,errResult);
			finish();
		}
	}

}
