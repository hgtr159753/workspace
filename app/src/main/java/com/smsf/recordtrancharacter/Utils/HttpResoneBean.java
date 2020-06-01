package com.smsf.recordtrancharacter.Utils;

import java.io.Serializable;

/**
 * @Author: Mr
 * @CreateDate: 2020/3/23 11:52
 */

public class HttpResoneBean implements Serializable {

    public int getCode() {
        return Code;
    }

    public void setCode(int code) {
        Code = code;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    private int Code;
    private String Msg;
}
