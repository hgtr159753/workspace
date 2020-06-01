package com.smsf.recordtrancharacter.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smsf.recordtrancharacter.R;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class UploadLogsAdapter extends BaseAdapter {

    List<String> mList = new ArrayList<>();
    List<File> mListFile = new ArrayList<>();
    private Context mContext;
    public void setOnClickLinter(UploadLogsAdapter.onClickLinter onClickLinter) {
        this.onClickLinter = onClickLinter;
    }
    private onClickLinter onClickLinter;

    public UploadLogsAdapter(List<String> list,List<File> mListFile, Context context) {
        this.mList = list;
        this.mListFile= mListFile;
        mContext = context;
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_file, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.fileNameTextView.setText(mList.get(position));
        viewHolder.detailTextView.setText(getFileLastModifiedTime(mListFile.get(position))+" "+String.format(mContext.getResources().getString
                (R.string.text_file_size), getReadableFileSize(mListFile.get(position).length())));
        viewHolder.right_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickLinter !=null){
                    onClickLinter.onClick(position);
                }
            }
        });
        return convertView;
    }


    private static final String mformatType = "MM/dd HH:mm";
    public static String getFileLastModifiedTime(File file) {
        Calendar cal = Calendar.getInstance();
        long time = file.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat(mformatType);
        cal.setTimeInMillis(time);

        // 输出：修改时间[2] 2009-08-17 10:32:38
        return formatter.format(cal.getTime());
    }



    private String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private class ViewHolder {

        private TextView fileNameTextView;
        private TextView detailTextView;
        private ImageView right_more;

        public ViewHolder(View convertView) {
            fileNameTextView = convertView.findViewById(R.id.fileNameTextView);
            detailTextView = convertView.findViewById(R.id.fileDetailTextView);
            right_more = convertView.findViewById(R.id.right_more);

        }
    }


    public interface onClickLinter{

        public void onClick(int index);
    }

}
