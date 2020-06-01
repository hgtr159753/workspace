package com.smsf.recordtrancharacter.view;

import android.app.Dialog;
import android.content.Context;

import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.smsf.recordtrancharacter.R;

import androidx.annotation.NonNull;


public class BuyVipDialog extends Dialog {

    private Context mContext;
    public static final int WRAP_CONTENT = WindowManager.LayoutParams.WRAP_CONTENT;
    private Window window;

    private LinearLayout zhifu_pay, weixin_pay;
    private Button zf_btn;

    public BuyVipDialog(@NonNull Context context) {
        super(context, R.style.vip_dialog);
        mContext = context;
        init();
    }

    private void init() {
        window = getWindow();
        window.setContentView(R.layout.buvip_dialog);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WRAP_CONTENT;
        wlp.height = WRAP_CONTENT;
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        zhifu_pay = findViewById(R.id.zhifu_pay);
        weixin_pay = findViewById(R.id.weixin_pay);
        zf_btn = findViewById(R.id.zf_btn);


    }


    public BuyVipDialog setAiPay(View.OnClickListener onBtnClickLiner) {
        if (onBtnClickLiner != null) {

            zhifu_pay.setOnClickListener(onBtnClickLiner);
        }
        return this;

    }

    public BuyVipDialog setWeChatPay(View.OnClickListener onBtnClickLiner) {
        if (onBtnClickLiner != null) {

            weixin_pay.setOnClickListener(onBtnClickLiner);
        }
        return this;
    }

    public BuyVipDialog setPay(View.OnClickListener onBtnClickLiner) {
        if (onBtnClickLiner != null) {
            zf_btn.setOnClickListener(onBtnClickLiner);
        }
        return this;
    }

}
