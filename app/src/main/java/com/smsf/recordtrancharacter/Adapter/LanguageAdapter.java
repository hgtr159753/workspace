package com.smsf.recordtrancharacter.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smsf.recordtrancharacter.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LanguageAdapter extends BaseAdapter {

    private List<String> titles = new ArrayList<>();
    private Context mContext;

    public LanguageAdapter(List<String> list, Context context) {
        this.titles = list;
        mContext = context;
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public Object getItem(int i) {
        return titles.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_lau_list, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.lagguage_text.setText(titles.get(i));

        return convertView;
    }


    private class ViewHolder {

        private TextView lagguage_text;


        public ViewHolder(View convertView) {
            lagguage_text = convertView.findViewById(R.id.lagguage_text);

        }
    }
}
