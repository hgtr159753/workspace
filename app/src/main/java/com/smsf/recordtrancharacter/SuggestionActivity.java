package com.smsf.recordtrancharacter;


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smsf.recordtrancharacter.Utils.AppUtils;
import com.smsf.recordtrancharacter.Utils.Conts;
import com.smsf.recordtrancharacter.Utils.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;


public class SuggestionActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_back;  //返回
    private TextView toolbar_title; // 标题
    private EditText feedback_content;
    private EditText feedback_contact;
    private TextView feedback_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);
        iv_back = findViewById(R.id.iv_back);
        toolbar_title = findViewById(R.id.toolbar_title);
        feedback_content = findViewById(R.id.feedback_content);
        feedback_contact = findViewById(R.id.feedback_contact);
        feedback_btn = findViewById(R.id.feedback_btn);
        toolbar_title.setText("意见反馈");
        iv_back.setVisibility(View.VISIBLE);
        iv_back.setOnClickListener(this);
        feedback_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.feedback_btn:
                // 提交反馈
                String content = feedback_content.getText().toString();
                String constact = feedback_contact.getText().toString();

                if (TextUtils.isEmpty(content)) {
                    ToastUtils.showToast(SuggestionActivity.this, "反馈意见不能为空");
                    //Toast.makeText(this, "反馈意见不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!TextUtils.isEmpty(constact) && constact.length() != 11) {
                    ToastUtils.showToast(SuggestionActivity.this, "手机号码无效，请输入正确手机号码");
                    //Toast.makeText(this, "手机号码无效，请输入正确手机号码", Toast.LENGTH_SHORT).show();
                    return;
                }
                content = content + "_" + getPackageName() + "_" + AppUtils.getVersionName(this) + "_" + AppUtils.getVersionCode(this);

                HashMap<String, String> params = new HashMap<>();
                params.put("FeedbackContent", content);
                params.put("FeedbackContact", constact);
                OkHttpUtils.post().
                        url(Conts.BASTURL)
                        .params(params)
                        .build().execute(new StringCallback() {
                    @Override
                    public void onError(okhttp3.Call call, Exception e, int id) {
                        Log.d("mrs", "============getPayQQonError===========");
                        ToastUtils.showToast(SuggestionActivity.this, "反馈成功");
                        finish();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d("mrs", "============onResponse===========" + response);
                        ToastUtils.showToast(SuggestionActivity.this, "反馈成功");
                        finish();
                    }
                });
                break;
        }
    }
}
