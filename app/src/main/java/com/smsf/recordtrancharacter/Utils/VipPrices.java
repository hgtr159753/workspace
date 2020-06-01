package com.smsf.recordtrancharacter.Utils;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: VIP 实体类
 * @Author: Mr
 * @CreateDate: 2020/3/19 10:12
 */

public class VipPrices implements Serializable {


    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    public int getCode() {
        return Code;
    }

    public void setCode(int code) {
        Code = code;
    }

    public List<Prices> getDetail() {
        return Detail;
    }

    public void setDetail(List<Prices> detail) {
        Detail = detail;
    }

    private String Msg;
    private int Code;
    private List<Prices> Detail;


    public class Prices{


        public String getDiscountDescription() {
            return discountDescription;
        }

        public void setDiscountDescription(String discountDescription) {
            this.discountDescription = discountDescription;
        }

        public String getGoodsDescription() {
            return goodsDescription;
        }

        public void setGoodsDescription(String goodsDescription) {
            this.goodsDescription = goodsDescription;
        }

        public String getGoodsId() {
            return goodsId;
        }

        public void setGoodsId(String goodsId) {
            this.goodsId = goodsId;
        }

        public String getGoodsName() {
            return goodsName;
        }

        public void setGoodsName(String goodsName) {
            this.goodsName = goodsName;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public double getPriceNow() {
            return priceNow;
        }

        public void setPriceNow(int priceNow) {
            this.priceNow = priceNow;
        }

        public int getVipDays() {
            return vipDays;
        }

        public void setVipDays(int vipDays) {
            this.vipDays = vipDays;
        }

        private String discountDescription;
        private String goodsDescription;
        private String goodsId;
        private String goodsName;
        private double price;
        private double priceNow;
        private int vipDays;


    }




}
