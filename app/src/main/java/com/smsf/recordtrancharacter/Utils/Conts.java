package com.smsf.recordtrancharacter.Utils;

/**
 * @Description: API接口
 * @Author: Mr
 * @CreateDate: 2020/3/4 11:49
 */

public class Conts {

    public static final String WECHAT_APP_ID = "wxc94e5bb7501bc899";

    // 域名
    public static final String HTTP_URI = "http://cs.snmi.cn/";
    //  微信登陆
    public static final String WX_LOGIN = HTTP_URI + "User/WXLogin";
    // 获取VIP列表
    public static final String GETPRICES = HTTP_URI + "pay/getprices";
    // 微信支付
    public static final String WXVIPPAY = HTTP_URI + "pay/wxvippay";
    // 查询
    public static final String QUERYTRAN = HTTP_URI + "pay/QueryTran";
    // 支付宝支付
    public static final String ALIVIPPAY = HTTP_URI + "pay/AliVIPPay";
    // 意见反馈
    public static final String BASTURL = "http://118.190.166.164:96/api/User/Feedback";
}
