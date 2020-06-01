package com.smsf.recordtrancharacter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;



/**
 * @Description: 启动页
 * @Author: Mr
 * @CreateDate: 2019/12/26 15:07
 */
public class SplashActivity extends AppCompatActivity {


    // 启动图片
    private ImageView splash_img;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_layout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                intoHomePage();
            }
        },2000);
    }

    /***
     * 跳转到主页
     * */
    public void intoHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }



}
