package com.smsf.recordtrancharacter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTabHost;
import androidx.viewpager.widget.ViewPager;

import com.smsf.recordtrancharacter.Fragments.SpeechTranscriberWithRecorderActivity;
import com.smsf.recordtrancharacter.Fragments.historyFrag;
import com.smsf.recordtrancharacter.Utils.SharedPUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import permison.PermissonUtil;
import permison.listener.PermissionListener;

public class HomeActivity extends AppCompatActivity {


    private long exitTime = 0;  //back键按下时间
    private FragmentTabHost mTabHost;
    private ViewPager mViewPager;
    private List<Fragment> mFragmentList;
    private Class mClass[] = {SpeechTranscriberWithRecorderActivity.class, historyFrag.class, UserFragment.class};
    private Fragment mFragment[] = {new SpeechTranscriberWithRecorderActivity(), new historyFrag(), new UserFragment()};
    private String mTitles[] = {"首页", "文件", "我的"};
    private int mImages[] = {
            R.drawable.tab_layout_home, R.drawable.tab_layout_course, R.drawable.tab_layout_my,
    };
    // 要申请的权限
    private String[] permissions = {Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_home_layout);
        mTabHost = findViewById(android.R.id.tabhost);
        mViewPager = findViewById(R.id.view_pager);
        mFragmentList = new ArrayList<>();
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.getTabWidget().setDividerDrawable(null);
        for (int i = 0; i < mFragment.length; i++) {
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTitles[i]).setIndicator(getTabView(i));
            mTabHost.addTab(tabSpec, mClass[i], null);
            mFragmentList.add(mFragment[i]);
            mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.WHITE);
        }
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentList.size();
            }
        });

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                mViewPager.setCurrentItem(mTabHost.getCurrentTab());
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabHost.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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
            String time=SharedPUtils.getRemainTime(HomeActivity.this);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //申请定位权限
            initLocationPermission();
        }
    }



    // 定位权限
    public void initLocationPermission() {
        PermissonUtil.checkPermission(HomeActivity.this, new PermissionListener() {
            @Override
            public void havePermission() {
                // 获取成功

            }
            @Override
            public void requestPermissionFail() {
                Toast.makeText(HomeActivity.this, "权限获取失败,请开启", Toast.LENGTH_SHORT).show();
            }
        }, permissions);
    }



    private View getTabView(int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item, null);
        ImageView image = view.findViewById(R.id.image);
        TextView title = view.findViewById(R.id.title);
        image.setImageResource(mImages[index]);
        title.setText(mTitles[index]);
        return view;
    }


    @Override
    public void onBackPressed() {
        // 返回退出APP
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(HomeActivity.this, "再按一次退出", Toast.LENGTH_LONG).show();
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
