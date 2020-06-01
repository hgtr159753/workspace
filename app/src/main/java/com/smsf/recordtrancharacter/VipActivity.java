package com.smsf.recordtrancharacter;

import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.smsf.recordtrancharacter.Utils.ApiUtils;
import com.smsf.recordtrancharacter.Utils.AppUtils;
import com.smsf.recordtrancharacter.Utils.ByVipAdapter;
import com.smsf.recordtrancharacter.Utils.Conts;
import com.smsf.recordtrancharacter.Utils.HttpResoneBean;
import com.smsf.recordtrancharacter.Utils.SharedPUtils;
import com.smsf.recordtrancharacter.Utils.StatusBarUtil;
import com.smsf.recordtrancharacter.Utils.UserBean;
import com.smsf.recordtrancharacter.Utils.VipPrices;
import com.smsf.recordtrancharacter.Utils.WeChatResponse;
import com.smsf.recordtrancharacter.view.BuyVipDialog;
import com.smsf.recordtrancharacter.Utils.BASEactivity;

import com.smsf.recordtrancharacter.wxapi.WXPayEntryActivity;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @Description: VIP页面
 * @Author: Mr
 * @CreateDate: 2020/3/2 15:07
 */
public class VipActivity extends BASEactivity implements View.OnClickListener {

    private ImageView iv_back; // 返回
    private TextView toolbar_title;  // 标题
    private TextView text_line_1, text_line_2, text_line_3, text_line_4;
    private LinearLayout month_menu, half_year_menu, year_menu, allleft_menu; // 会员选项布局
    private Button buy_vip_btn; // 购买
    private TextView select_text;  //选择需要显示的金额
    private RecyclerView recycler_view;  // 价格适配器
    private List<VipPrices.Prices> vipPrices_list = new ArrayList<>();
    private ImageView user_img;  //用户头像
    private TextView user_name;  // 用户昵称
    private TextView vip_time;  // VIP时长
    private VipPrices.Prices select_vipPrices;  //选择的充值商品
    private String userToken;  //用户token
    private boolean isOnItem; // 表示第一次是否点击选项
    /**
     * 微信支付类
     */
    private IWXAPI api;

    private int pay_Type = 2;  //默认选中微信 1是支付宝2是微信
    private BuyVipDialog buyVipDialog;

    private String tranNumber;  // 订单号
    private static final int SDK_PAY_FLAG = 1;
    @Override
    public int getLayoutId() {
        StatusBarUtil.setStatuBar(this, getResources().getColor(R.color.title_bar_bg));
        return R.layout.activity_vip_layout;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        iv_back = findViewById(R.id.iv_back);
        iv_back.setVisibility(View.VISIBLE);
        toolbar_title = findViewById(R.id.toolbar_title);
        text_line_1 = findViewById(R.id.text_line_1);
        text_line_2 = findViewById(R.id.text_line_2);
        text_line_3 = findViewById(R.id.text_line_3);
        text_line_4 = findViewById(R.id.text_line_4);
        month_menu = findViewById(R.id.month_menu);
        half_year_menu = findViewById(R.id.half_year_menu);
        year_menu = findViewById(R.id.year_menu);
        allleft_menu = findViewById(R.id.allleft_menu);
        buy_vip_btn = findViewById(R.id.buy_vip_btn);
        select_text = findViewById(R.id.select_text);
        recycler_view = findViewById(R.id.recycler_view);
        user_img = findViewById(R.id.user_img);
        user_name = findViewById(R.id.user_name);
        vip_time = findViewById(R.id.vip_time);

    }

    @Override
    protected void initData() {
        api = WXAPIFactory.createWXAPI(this, null);
        api.registerApp(Conts.WECHAT_APP_ID);
        getSelectVip();
        toolbar_title.setText("VIP会员");
        // 获取本地保存的用户信息
        UserBean.User user = SharedPUtils.getUserLogin(this);
        if (user != null) {
            RequestOptions requestOptions = new RequestOptions().centerCrop();
            Glide.with(this).load(user.getHeadimgurl()).apply(requestOptions).into(user_img);
            user_name.setText(user.getNickname());
            userToken = user.getToken();
        }
        boolean isLogin = SharedPUtils.getUserSuccess(this);
        if (!isLogin) {
            vip_time.setText("未登录用户");
        } else {
            // 如果是登录用户首先判断是否是VIP
            boolean isVip = SharedPUtils.getIsVip(this);
            if (isVip) {
                String expireTime = SharedPUtils.getRemainTime(this);
                vip_time.setText("会员剩余时长: " + expireTime+"秒");
            } else {
                vip_time.setText("会员试用期: 已过期");
            }
        }
      /*  text_line_1.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        text_line_2.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        text_line_3.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        text_line_4.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);*/
    }

    @Override
    protected void initListener() {
        iv_back.setOnClickListener(this);
        month_menu.setOnClickListener(this);
        half_year_menu.setOnClickListener(this);
        year_menu.setOnClickListener(this);
        allleft_menu.setOnClickListener(this);
        buy_vip_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.half_year_menu:
                // 半年度会员
                half_year_menu.setBackground(getResources().getDrawable(R.drawable.vip_bg_background));
                year_menu.setBackground(null);
                month_menu.setBackground(null);
                allleft_menu.setBackground(null);
                break;
            case R.id.year_menu:
                // 年度会员
                year_menu.setBackground(getResources().getDrawable(R.drawable.vip_bg_background));
                half_year_menu.setBackground(null);
                month_menu.setBackground(null);
                allleft_menu.setBackground(null);
                break;
            case R.id.month_menu:
                // 月度会员
                month_menu.setBackground(getResources().getDrawable(R.drawable.vip_bg_background));
                half_year_menu.setBackground(null);
                year_menu.setBackground(null);
                allleft_menu.setBackground(null);
                break;
            case R.id.allleft_menu:
                // 终身会员
                allleft_menu.setBackground(getResources().getDrawable(R.drawable.vip_bg_background));
                half_year_menu.setBackground(null);
                year_menu.setBackground(null);
                month_menu.setBackground(null);
                break;
            case R.id.buy_vip_btn:
                Wx_Pay();
        }
    }


    // 微信支付
    public void Wx_Pay() {
        // 判断选中对象是否为空有没有点击列表
        if (vipPrices_list != null && !isOnItem) {
            //  没有点击列表直接点击的支付默认按集合列表get（0）来确认
            select_vipPrices = vipPrices_list.get(0);
        }
        if (select_vipPrices == null)
            return;
        HashMap<String, String> params = new HashMap<>();
        params.put("goodsId", select_vipPrices.getGoodsId());
        params.put("pkgname", AppUtils.getPackageName(this));
        params.put("goodsName", select_vipPrices.getGoodsName());
        params.put("token", userToken);
        params.put("money", select_vipPrices.getPriceNow()+"");
        OkHttpUtils.post().url(Conts.WXVIPPAY)
                .params(params)
                .build().execute(new StringCallback() {
            @Override
            public void onError(okhttp3.Call call, Exception e, int id) {
                Log.d("mrs", "============Wx_PayonError===========");
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d("mrs", "============onResponse===========" + response);
                // 登录成功
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    Log.d("mrs", "============onResponse===========" + response);
                    WeChatResponse weChatResponse = gson.fromJson(response, WeChatResponse.class);
                    if (weChatResponse != null) {
                        final WeChatResponse.WeChatBean weChatBean = gson.fromJson(weChatResponse.getDetail(), WeChatResponse.WeChatBean.class);
                        if (weChatBean != null) {
                            tranNumber = weChatBean.getPrepayid();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    PayReq req = new PayReq();
                                    req.appId = weChatBean.getAppid();
                                    req.partnerId = weChatBean.getPartnerid();
                                    req.prepayId = weChatBean.getPrepayid();
                                    req.packageValue = "Sign=WXPay";
                                    req.nonceStr = weChatBean.getNoncestr();
                                    req.timeStamp = String.valueOf(weChatBean.getTimestamp());
                                    req.sign = weChatBean.getSign();
                                    api.sendReq(req);
                                }
                            }).start();
                        }
                    }
                }
            }
        });

    }


    /**
     * 获取VIP购买列表
     */
    public void getSelectVip() {
        HashMap<String, String> params = new HashMap<>();
        params.put("appVersion", AppUtils.getVersionName(this));
        params.put("pkgname", AppUtils.getPackageName(this));
        OkHttpUtils.post().url(Conts.GETPRICES)
                .params(params)
                .build().execute(new StringCallback() {
            @Override
            public void onError(okhttp3.Call call, Exception e, int id) {
                Log.d("mrs", "============onError===========");
            }

            @Override
            public void onResponse(String response, int id) {
                //Log.d("mrs", "============onResponse===========" + response);
                // 登录成功
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    VipPrices vipPrices = gson.fromJson(response, VipPrices.class);
                    if (vipPrices != null && vipPrices.getCode() == 200) {
                        // 获取成功
                        try {
                            if (vipPrices.getDetail() != null) {
                                vipPrices_list = vipPrices.getDetail();
                                LinearLayoutManager layoutManager = new LinearLayoutManager(VipActivity.this);
                                recycler_view.setLayoutManager(layoutManager);
                                ByVipAdapter byVipAdapter = new ByVipAdapter(VipActivity.this, vipPrices_list);
                                select_text.setText(vipPrices_list.get(0).getPriceNow()+"");
                                recycler_view.setAdapter(byVipAdapter);
                                byVipAdapter.setOnItemClickListener(new ByVipAdapter.OnItemClickListener() {
                                    @Override
                                    public void onClick(int position) {
                                        // 点击item事件
                                        isOnItem = true;
                                        select_vipPrices = vipPrices_list.get(position);
                                        select_text.setText(select_vipPrices.getPriceNow() + "元");
                                    }
                                });
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (WXPayEntryActivity.paySuccess) {
            /*if (buyVipDialog != null) {
                buyVipDialog.dismiss();
            }*/
            // 查询订单是否成功

            getQueryTran();

        }

    }

    public void getQueryTran() {
        HashMap<String, String> params = new HashMap<>();
        params.put("transno", tranNumber);
        OkHttpUtils.post().url(Conts.QUERYTRAN)
                .params(params)
                .build().execute(new StringCallback() {
            @Override
            public void onError(okhttp3.Call call, Exception e, int id) {
                Log.d("mrs", "============onError===========");
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d("mrs", "============onResponse===========" + response);
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    HttpResoneBean httpResoneBean = gson.fromJson(response, HttpResoneBean.class);
                    if (httpResoneBean.getCode() == 200) {
                        // 保存VIP日期
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.DAY_OF_MONTH, +select_vipPrices.getVipDays());
                        String res = simpleDateFormat.format(c.getTime());
                        // 将到期时间保存
                        long remain=Integer.parseInt(SharedPUtils.getRemainTime(getApplicationContext()));
//                        remain+=200;
                        if(select_vipPrices.getGoodsName().equals("充值1小时"))
                            remain+=60*60;
                        else if(select_vipPrices.getGoodsName().equals("充值10分钟"))
                            remain+=600;
                        else if(select_vipPrices.getGoodsName().equals("充值5小时"))
                            remain+=5*60*60;
                        else if(select_vipPrices.getGoodsName().equals("充值10小时"))
                            remain+=10*60*60;
                        SharedPUtils.setRemainTime(getApplication(),remain);
                        SharedPUtils.setVipExpire(VipActivity.this, res);
                        SharedPUtils.setIsVip(VipActivity.this, true);
                        WXPayEntryActivity.paySuccess = false;
                        vip_time.setText("会员剩余时长: " + remain);
                        ApiUtils.report(VipActivity.this, "微信支付成功");
                        finish();
                    }
                }
            }
        });
    }

}
