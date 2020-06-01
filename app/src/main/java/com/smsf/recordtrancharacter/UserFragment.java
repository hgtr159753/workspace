package com.smsf.recordtrancharacter;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.smsf.recordtrancharacter.Utils.ShareUtils;
import com.smsf.recordtrancharacter.Utils.SharedPUtils;
import com.smsf.recordtrancharacter.Utils.ToastUtils;
import com.smsf.recordtrancharacter.Utils.UserBean;
import com.smsf.recordtrancharacter.view.RoundImageView;

public class UserFragment extends Fragment implements View.OnClickListener {
    private View mRootView;
    private RelativeLayout yhxy_menu, ys_menu, lxkf_menu, sugges_menu;  //用户协议，隐私协议，联系客服,反馈
    private RelativeLayout goplay_vip; // VIP购买
    private RelativeLayout user_logn_menu;
    private LinearLayout user_login_success_menu;
    private TextView user_login_btn;  // 名称
    private RoundImageView user_img;  //头像
    private TextView user_name;  //名称
    private TextView vip_time;  //VIP到期时间
    private TextView mfcs_number;  //免费次数
    private boolean isLogin;  // 是否登录
    private Button close_login_btn; //退出登录


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.user_activity_layout, null, false);
        initView(v);
        return v;

    }


    public void initView(View v) {
        yhxy_menu = v.findViewById(R.id.yhxy_menu);
        ys_menu = v.findViewById(R.id.ys_menu);
        lxkf_menu = v.findViewById(R.id.lxkf_menu_btn);
        goplay_vip = v.findViewById(R.id.goplay_vip);
        sugges_menu = v.findViewById(R.id.sugges_menu);
        user_logn_menu = v.findViewById(R.id.user_logn_menu);
        user_login_btn = v.findViewById(R.id.user_login_btn);
        mfcs_number = v.findViewById(R.id.mfcs_number);
        user_login_success_menu = v.findViewById(R.id.user_login_success_menu);
        user_name = v.findViewById(R.id.user_name);
        user_img = v.findViewById(R.id.user_img);
        vip_time = v.findViewById(R.id.vip_time);
        close_login_btn = v.findViewById(R.id.close_login_btn);
        yhxy_menu.setOnClickListener(this);
        ys_menu.setOnClickListener(this);
        goplay_vip.setOnClickListener(this);
        sugges_menu.setOnClickListener(this);
        user_logn_menu.setOnClickListener(this);
        user_login_btn.setOnClickListener(this);
        close_login_btn.setOnClickListener(this);
        lxkf_menu.setOnClickListener(this);
        initUserData();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.yhxy_menu:
                // 用户协议
                Intent intent_private = new Intent();
                intent_private.setClass(getActivity(), CommonWebViewActivity.class);
                intent_private.putExtra("url", "http://qk.h5abc.com/content/agreement/user_agreement.html");
                startActivity(intent_private);
                break;
            case R.id.ys_menu:
                // 隐私协议
                Intent intent_agreement = new Intent();
                intent_agreement.setClass(getActivity(), CommonWebViewActivity.class);
                intent_agreement.putExtra("url", "http://qk.h5abc.com/content/agreement/privacy_agreement.html");
                startActivity(intent_agreement);
                break;
            case R.id.lxkf_menu_btn:
                // 联系客服
                if (ShareUtils.isQQClientAvailable(getActivity(), "com.tencent.mobileqq")) {
                    //跳转客服QQ界面
                    String url = "mqqwpa://im/chat?chat_type=wpa&uin=296521085";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    // 跳转前先判断Uri是否存在，如果打开一个不存在的Uri，App可能会崩溃
                    if (ShareUtils.isValidIntent(getActivity(), intent)) {
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getActivity(), "检查到您手机没有安装QQ客户端，请安装后使用该功能", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.goplay_vip:
                if (!isLogin) {
                    ToastUtils.showToast(getActivity(), "请先进行登录");
                    Intent intentlogin = new Intent(getActivity(), UserLoginActivity.class);
                    startActivity(intentlogin);
                    return;
                }
                Intent intent = new Intent(getActivity(), VipActivity.class);
                startActivityForResult(intent, 100);
                break;
            case R.id.sugges_menu:
                // 反馈
                Intent intentSugg = new Intent(getActivity(), SuggestionActivity.class);
                startActivity(intentSugg);
                break;
            case R.id.user_login_btn:
                // 登录
                Intent intentlogin = new Intent(getActivity(), UserLoginActivity.class);
                startActivity(intentlogin);
                break;
            case R.id.close_login_btn:
                //  退出登录
                close_login_btn.setVisibility(View.GONE);
                user_logn_menu.setVisibility(View.VISIBLE);
                user_login_success_menu.setVisibility(View.GONE);
                // 将本地保存置位false
                SharedPUtils.setUserSuccess(getActivity(), false);
                isLogin = false;
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        initUserData();
    }


    public void initUserData() {
        // 获取本地保存的用户信息
        UserBean.User user = SharedPUtils.getUserLogin(getActivity());
        if (user != null) {
            RequestOptions requestOptions = new RequestOptions().centerCrop();
            Glide.with(this).load(user.getHeadimgurl()).apply(requestOptions).into(user_img);
            user_name.setText(user.getNickname());
        }
        isLogin = SharedPUtils.getUserSuccess(getActivity());
        if (!isLogin) {
            vip_time.setText("未登录用户");
            user_logn_menu.setVisibility(View.VISIBLE);
            close_login_btn.setVisibility(View.GONE);
        } else {
            user_logn_menu.setVisibility(View.GONE);
            close_login_btn.setVisibility(View.VISIBLE);
            user_login_success_menu.setVisibility(View.VISIBLE);
            // 如果是登录用户首先判断是否是VIP
            boolean isVip = SharedPUtils.getIsVip(getActivity());
            if (isVip) {
                String expireTime = SharedPUtils.getRemainTime(getActivity());
                Log.d("mrs", "============expireTime=========" + expireTime);
                vip_time.setText("会员剩余时长: " + expireTime + "秒");
            } else {
                vip_time.setText("会员试用期: 已过期");
            }
        }
    }
}
