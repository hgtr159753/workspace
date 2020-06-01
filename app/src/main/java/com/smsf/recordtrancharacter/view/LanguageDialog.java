package com.smsf.recordtrancharacter.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.smsf.recordtrancharacter.Adapter.LanguageAdapter;
import com.smsf.recordtrancharacter.Adapter.UploadLogsAdapter;
import com.smsf.recordtrancharacter.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Mr on 2019/1/17.
 */

public class LanguageDialog extends Dialog {
    public static final int WRAP_CONTENT = WindowManager.LayoutParams.WRAP_CONTENT;
    public static final int MATCH_PARENT = WindowManager.LayoutParams.MATCH_PARENT;

    private Window window;
    private Context mContext;
    private LanguageAdapter languageAdapter;
    private List<String> titles = new ArrayList<>();
    private ListView list_view;
    public void setOnClickLinter(onClickLinter onClickLinter) {
        this.onClickLinter = onClickLinter;
    }
    private onClickLinter onClickLinter;
    /**
     * @param context
     */
    public LanguageDialog(Context context) {
        super(context, R.style.custom_dialog);
        mContext = context;
        init();
    }

    private void init() {
        window = getWindow();
        window.setContentView(R.layout.dialog_remin_layout);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) -1.0f;
        wlp.height = WRAP_CONTENT;
        window.setAttributes(wlp);
        list_view = findViewById(R.id.list_view);
        String lgu1 = "英文";
        String lgu = "广东话";
        String lgu2 = "日语";
        String lgu3 = "俄语";
        String lgu4 = "法语";
        String lgu5 = "西班牙语";
        String lgu6 = "阿拉伯语";
        String lgu7 = "彝语";
        titles.add(lgu1);
        titles.add(lgu);
        titles.add(lgu2);
        titles.add(lgu3);
        titles.add(lgu4);
        titles.add(lgu5);
        titles.add(lgu6);
        titles.add(lgu7);
        languageAdapter = new LanguageAdapter(titles, mContext);
        list_view.setAdapter(languageAdapter);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (onClickLinter !=null){
                    onClickLinter.onClick(titles.get(i));
                }
            }
        });
    }

    public interface onClickLinter{
        public void onClick(String titleName);
    }

}
