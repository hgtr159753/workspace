package com.smsf.recordtrancharacter.Utils;

import android.content.Context;
import android.graphics.Paint;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smsf.recordtrancharacter.R;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


/**
 * @Description: VIP价格适配器
 * @Author: Mr
 * @CreateDate: 2020/3/19 10:04
 */

public class ByVipAdapter extends RecyclerView.Adapter<ByVipAdapter.ViewHolder> {

    private List<VipPrices.Prices> vipPrices_list = new ArrayList<>();
    private List<Boolean> isClicks;
    private Context mContext;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private OnItemClickListener onItemClickListener;

    @NonNull
    @Override
    public ByVipAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vipselect_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ByVipAdapter.ViewHolder holder, final int position) {
        VipPrices.Prices prices = vipPrices_list.get(position);
        holder.left_text.setText(prices.getGoodsDescription());
        holder.discountDescription.setText(prices.getDiscountDescription());
        holder.text_line_1.setText(prices.getPrice() + "元");
        holder.vip_money.setText(prices.getPriceNow() + "元");
        holder.text_line_1.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        if (isClicks.get(position)) {
            holder.month_menu.setBackground(mContext.getResources().getDrawable(R.drawable.vip_bg_background));
        } else {
            holder.month_menu.setBackground(null);
        }
        if (onItemClickListener != null){
            holder.month_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(int i = 0; i <isClicks.size();i++){
                        isClicks.set(i,false);
                    }
                    isClicks.set(position,true);
                    notifyDataSetChanged();
                    onItemClickListener.onClick(position);
                }
            });
        }
    }

    public ByVipAdapter(Context context, List<VipPrices.Prices> prices) {
        mContext = context;
        vipPrices_list = prices;
        isClicks = new ArrayList<>();
        for (int i = 0; i < vipPrices_list.size(); i++) {
            if (i ==0){
                isClicks.add(true);
            }else{
                isClicks.add(false);
            }

        }
    }

    @Override
    public int getItemCount() {
        return vipPrices_list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView left_text, discountDescription, text_line_1, vip_money;
        LinearLayout month_menu;

        public ViewHolder(View view) {
            super(view);
            left_text = view.findViewById(R.id.left_text);
            discountDescription = view.findViewById(R.id.discountDescription);
            text_line_1 = view.findViewById(R.id.text_line_1);
            vip_money = view.findViewById(R.id.vip_money);
            month_menu = view.findViewById(R.id.month_menu);
        }

    }

    public interface OnItemClickListener {
        void onClick(int position);
    }
}
