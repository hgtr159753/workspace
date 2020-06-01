package com.smsf.recordtrancharacter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;


public class CommonWebViewActivity extends Activity {

    private WebView webview_main;
    private ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_activity_web_view);
        iv_back = findViewById(R.id.iv_back);
        webview_main = findViewById(R.id.webview_main);
        iv_back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        String url = getIntent().getExtras().getString("url");
        webview_main.loadUrl(url);
        WebSettings settings=webview_main.getSettings();
        settings.setTextSize(WebSettings.TextSize.LARGER);
    }
}
