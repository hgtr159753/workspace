package com.smsf.recordtrancharacter.view;

import android.os.CountDownTimer;
import android.widget.TextView;

/**
 * @Description: 倒计时
 * @Author: liys
 * @CreateDate: 2020/3/4 11:18
 */

public class VerificationTimer extends CountDownTimer{
    private TextView textView;
    private int one, tow;

    /**
     * @author millisInFuture 一共的时间
     * @author countDownInterval 几秒
     * @author textView 控件可以更换你所需要的控件
     * @author one onTick方法中的背景颜色        这两个都是用于控制控件背景的
     * @author tow  onFinish 方法中的背景颜色
     */
    public VerificationTimer(long millisInFuture, long countDownInterval, TextView textView) {
        super(millisInFuture, countDownInterval);
        this.textView = textView;
    }

    public VerificationTimer(long millisInFuture, long countDownInterval, TextView textView, int one, int tow) {
        super(millisInFuture, countDownInterval);
        this.textView = textView;
        this.one = one;
        this.tow = tow;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        //如果为空就不换背景颜色
        if (one != 0) {
            textView.setBackgroundResource(one);
        }
        textView.setText(millisUntilFinished / 1000 + "s");
        textView.setEnabled(false);
    }

    @Override
    public void onFinish() {
        //如果为空就不换背景颜色
        if (tow != 0) {
            textView.setBackgroundResource(tow);
        }
        textView.setEnabled(true);
        textView.setText("获取验证码");
    }
}
