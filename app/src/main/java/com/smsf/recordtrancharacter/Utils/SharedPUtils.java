package com.smsf.recordtrancharacter.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import anet.channel.util.Utils;

import static anet.channel.util.Utils.context;


/**
 * @Description: 本地数据库保存工具类
 * @Author: Mr
 * @CreateDate: 2020/2/14 14:50
 */

public class SharedPUtils {

    public final static String APP_INFOR = "watermark";

    /**
     * 保存定位信息
     */
    public static void setLoactionAddStr(Context context, String addStr) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("addStr", addStr);
        editor.commit();
    }

    /***
     * 获取当前定位信息
     * */
    public static String getLoactionAddStr(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if (sp != null) {
            String str = sp.getString("addStr", "");
            return str;
        }
        return null;
    }


    /**
     * 保存是否登录
     */
    public static void setUserSuccess(Context context, boolean isLogin) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isLogin", isLogin);
        editor.commit();
    }

    /***
     * 获取是否登录
     * */
    public static boolean getUserSuccess(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if (sp != null) {
            boolean result = sp.getBoolean("isLogin", false);
            return result;
        }
        return false;
    }

    /**
     * 保存是否登录
     */
    public static void setMenuShow(Context context, boolean isLogin) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("menuShow", isLogin);
        editor.commit();
    }

    /***
     * 获取是否登录
     * */
    public static boolean getMenuShow(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if (sp != null) {
            boolean result = sp.getBoolean("menuShow", false);
            return result;
        }
        return false;
    }


    /**
     * 保存用户登录状态
     */
    public static void setUserLogin(Context context, UserBean userBean) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", userBean.getDetail().getToken());
        editor.putString("name", userBean.getDetail().getNickname());
        editor.putString("imguri", userBean.getDetail().getHeadimgurl());
        editor.commit();
    }


    /***
     * 获取当前用户登录状态
     * */
    public static UserBean.User getUserLogin() {
        return getUserLogin();
    }

    /***
     * 获取当前用户登录状态
     * */
    public static UserBean.User getUserLogin(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if (sp != null) {
            String name = sp.getString("name", "");
            String token = sp.getString("token", "");
            String imguri = sp.getString("imguri", "");
            if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(token)) {
                UserBean.User user = new UserBean.User();
                user.setNickname(name);
                user.setToken(token);
                user.setHeadimgurl(imguri);
                return user;
            }
        }
        return null;
    }


    /**
     * 保存APPImel
     */
    public static void setAppIMIE(Context context, String imie) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("imie", imie);
        editor.commit();
    }

    /***
     * 获取当前APPImel
     * */
    public static String getAppIMIE(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if (sp != null) {
            String appid = sp.getString("imie", "");
            return appid;
        }
        return null;
    }


    /**
     * 保存VIP到期时间
     */
    public static void setVipExpire(Context context, String time) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("expireTime", time);
        editor.commit();
    }

    /***
     * 获取当前用户VIP到期时间
     * */
    public static String getVipExpire(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if (sp != null) {
            String time = sp.getString("expireTime", "");
            return time;
        }
        return null;
    }

    /**
     * 保存是否是VIP
     */
    public static void setIsVip(Context context, boolean isVip) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isVip", isVip);
        editor.commit();
    }

    /***
     * 获取是否是VIP
     * */
    public static boolean getIsVip(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if (sp != null) {
            boolean isVip = sp.getBoolean("isVip", false);
            return isVip;
        }
        return false;
    }

    /**
     * 保存客服
     */
    public static void setWxCustom(Context context, String kfCode) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("kfCode", kfCode);
        editor.commit();
    }

    /***
     * 获取客服
     * */
    public static String getWxCustom(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if (sp != null) {
            String kfCode = sp.getString("kfCode", "");
            return kfCode;
        }
        return null;
    }
//设置vip剩余秒数
    public static void setRemainTime(Context context,long time)
    {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("timecount", String.valueOf(time));
        editor.commit();
    }
//获取vip剩余秒数
    public static String getRemainTime(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if(sp!=null) {
            String time = sp.getString("timecount", "0");
            return time;
        }
       return "-1";
    }
    //判断是否初次安装
    public static boolean getfirsttiem(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if(sp!=null) {
            boolean time = sp.getBoolean("firstTime",true);
            return time;
        }
        return true;
    }
    //设置初次安装
    public static void setfirsttiem(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if(sp!=null) {

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("firstTime", false);
            editor.commit();
        }
    }

    //设置初次安装
    public static void setfirsttiemf(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(APP_INFOR, Context.MODE_PRIVATE);
        if(sp!=null) {
            String time = sp.getString("first", "0");
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("firstTime", Boolean.parseBoolean("0"));
            editor.commit();
        }
    }
}
