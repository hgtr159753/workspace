package com.smsf.recordtrancharacter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smsf.recordtrancharacter.Fragments.SpeechTranscriberWithRecorderActivity;
import com.smsf.recordtrancharacter.Fragments.historyFrag;
import com.smsf.recordtrancharacter.Utils.SharedPUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * @Description: app首页，按住松开识别语音
 * @Author: liys
 * @CreateDate: 2020/5/11 16:14
 */

public class BaseActivity extends FragmentActivity {
    public static ArrayList<Fragment> aList;
    BottomNavigationView bnv;
    FrameLayout mFrameLayout;
    FragmentManager mSupportFragmentManager;
    FragmentTransaction mTransaction;
    historyFrag history;
    UserFragment userFragment;
    SpeechTranscriberWithRecorderActivity trans;
    // 要申请的权限
    private String[] permissions = {Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mFrameLayout = findViewById(R.id.framepage);
        aList = new ArrayList<>();
        mSupportFragmentManager = getSupportFragmentManager();
        initview();
        bnv = findViewById(R.id.bottomNavigationView);
        bnv.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId())
            {
                case R.id.navigation_dashboard:
                    hideOthersFragment(history,true);
                    break;
                case R.id.navigation_home:
                    hideOthersFragment(trans,false);
                    break;
                case R.id.navigation_notifications:
                    hideOthersFragment(userFragment,true);
                    break;
            }
            return true;
        });
        requestPermissions();
    }
    private void initview() {
        mSupportFragmentManager = getSupportFragmentManager();
        mTransaction = mSupportFragmentManager.beginTransaction();
        history=new historyFrag();
//        SharedPUtils.setfirsttiem(this);
        trans=new SpeechTranscriberWithRecorderActivity();
        userFragment = new UserFragment();
        this.aList.add(history);
        this.aList.add(trans);
        this.aList.add(userFragment);
//        SharedPUtils.setRemainTime(BaseActivity.this,100);//测试vip计时使用
        if(!SharedPUtils.getUserSuccess(this))//判断是否用户登录
        {
            /*new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("用户尚未登录，请先登录后使用！")
                    .setPositiveButton("确定", null)
                    .show();*/
            if(SharedPUtils.getfirsttiem(this))
            {
                SharedPUtils.setRemainTime(this,60);
                SharedPUtils.setfirsttiem(this);
            }
        }
        else if (SharedPUtils.getUserSuccess(this)&&!SharedPUtils.getIsVip(this))//登录后判断是否vip，如不是，显示试用时间
        {
            if(SharedPUtils.getfirsttiem(this))
            {
                SharedPUtils.setRemainTime(this,60);
                SharedPUtils.setfirsttiem(this);
            }
            new AlertDialog.Builder(this)
                    .setTitle("标题")
                    .setMessage("试用期剩余"+SharedPUtils.getVipExpire(this)+"秒到期！")
                    .setPositiveButton("确定", null)
                    .show();
        }
        else if(SharedPUtils.getUserSuccess(this)&&SharedPUtils.getIsVip(this))//如购买vip，显示剩余时长
        {
            String time=SharedPUtils.getRemainTime(BaseActivity.this);
            if(SharedPUtils.getfirsttiem(this))
            {
                SharedPUtils.setRemainTime(this,60);
                SharedPUtils.setfirsttiem(this);
            }
            new AlertDialog.Builder(this)
                    .setTitle("标题")
                    .setMessage("VIP剩余时间:"+time+"秒！")
                    .setPositiveButton("确定", null)
                    .show();
        }
        hideOthersFragment(trans, true);
        //hideOthersFragment(userFragment,true);
        //getFilesAllName(getApplication().getCacheDir().toString());
    }

    public void hideOthersFragment(Fragment showFragment, boolean add) {
        mSupportFragmentManager = getSupportFragmentManager();
        mTransaction = mSupportFragmentManager.beginTransaction();
        if (add&&!showFragment.isAdded()) {
            mTransaction.add(R.id.framepage, showFragment);
        }
        for (Fragment fragment : aList) {
            if (showFragment.equals(fragment)) {
                mTransaction.show(fragment);
            } else{
                mTransaction.hide(fragment);
            }
        }
        mTransaction.commit();
    }

    private void requestPermissions() {
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, permissions, 321);
            }
        }
    }

    public List<String> getFilesAllName(String path) {
        File file = new File(path);
        final File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "空目录");
            return null;
        }
        List<String> s = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".pcm")) {
                s.add(files[i].getAbsolutePath());
                Log.d("ccc", files[i].getAbsolutePath());
            }

        }
        return s;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        initview();
        bnv.getMenu().getItem(0).setChecked(true);
    }

    private long exitTime = 0;  //back键按下时间
    @Override
    public void onBackPressed() {
        // 返回退出APP
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(BaseActivity.this, "再按一次退出", Toast.LENGTH_LONG).show();
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
}
