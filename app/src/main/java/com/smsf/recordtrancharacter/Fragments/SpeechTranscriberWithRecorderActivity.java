package com.smsf.recordtrancharacter.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.token.AccessToken;
import com.alibaba.idst.util.NlsClient;
import com.alibaba.idst.util.SpeechTranscriberWithRecorder;
import com.alibaba.idst.util.SpeechTranscriberWithRecorderCallback;
import com.smsf.recordtrancharacter.MainActivity;
import com.smsf.recordtrancharacter.R;
import com.smsf.recordtrancharacter.TranslateActivity;
import com.smsf.recordtrancharacter.UserLoginActivity;
import com.smsf.recordtrancharacter.Utils.CreateToken;
import com.smsf.recordtrancharacter.Utils.SharedPUtils;
import com.smsf.recordtrancharacter.VipActivity;
import com.smsf.recordtrancharacter.audio.WavHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import jaygoo.widget.wlv.WaveLineView;

import static com.smsf.recordtrancharacter.Utils.CreateToken.token;

/**
 * 调用 SpeechTranscriberWithRecorder 的示例代码，此类内置了录音功能，调用简便，推荐使用
 * 本示例代码只是用来展示调用步骤，用户应该在真实使用时按需添加异常处理、防止重复点击等逻辑
 */
public class SpeechTranscriberWithRecorderActivity extends Fragment {
    private static final String TAG = "AliSpeechDemo";

    private Button button;
    //    private EditText mFullEdit;
    private  EditText mResultEdit;
    private  MyHandler handler;
    private NlsClient client;
    private WaveLineView waveLineView;
    private SpeechTranscriberWithRecorder speechTranscriber;
    private long startrectime = 0;
    private long endrectime = 0;
    private static long lastClickTime;
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private String filename;
    private String ofile;
    private File f, fo;
    private FileOutputStream fos;
    private ImageView delect_img;
    private RelativeLayout title_menu;
    private RelativeLayout save_btn;
    private String filetime;
    private String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Smvoice/voice/";
    private Button buttonfy;
    private boolean isPaues;  // 标识暂停
    private long userTime;  //获取当前用户剩余秒数

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_speech_recognizer, container, false);
        button = v.findViewById(R.id.button);
//        mFullEdit =  findViewById(R.id.editText);
        mResultEdit = v.findViewById(R.id.editText);
        mResultEdit.setFocusable(false);
        waveLineView = v.findViewById(R.id.waveLineView);
        delect_img = v.findViewById(R.id.delect_img);
        title_menu = v.findViewById(R.id.title_menu);
        save_btn = v.findViewById(R.id.save_btn);
        buttonfy = v.findViewById(R.id.buttonfy);
        boolean isShow = SharedPUtils.getMenuShow(getActivity());
        if (!isShow) {
            boolean isVip = SharedPUtils.getIsVip(getActivity());
            if (isVip) {
                title_menu.setVisibility(View.GONE);
            } else {
                title_menu.setVisibility(View.VISIBLE);
            }
        }
        waveLineView.startAnim();
        new Thread(gettoken).start();



        //UI在主线程更新
        handler = new MyHandler(this);
        delect_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title_menu.setVisibility(View.GONE);
                SharedPUtils.setMenuShow(getActivity(), true);
            }
        });
//        return super.onCreateView(inflater, container, savedInstanceState);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFastClick()) {
                    if (button.getText().equals("按下说话")) {
                        if (isViptimeEnough()) {
                            isPaues = false;
                            waveLineView.startAnim();
                            startTranscribe(v);
                            startrectime = System.currentTimeMillis();
                            Timer t = new Timer();
                            TestTimer task = new TestTimer();
                            t.schedule(task, 1000, 1000);
                        } else {
                            AlertDialog.Builder customizeDialog = new AlertDialog.Builder(getActivity());
                            customizeDialog.create();
                            AlertDialog dia = customizeDialog.show();
                            customizeDialog.setTitle("提示")
                                    .setMessage("剩余时间已用完，请去充值！")
                                    .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dia.dismiss();
                                        }
                                    })
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (!SharedPUtils.getUserSuccess(getActivity())) {
                                                Intent intentlogin = new Intent(getActivity(), UserLoginActivity.class);
                                                startActivity(intentlogin);
                                                return;
                                            }
                                            Intent intent = new Intent(getActivity(), VipActivity.class);
                                            startActivity(intent);
                                        }
                                    })
                                    .show();
                        }
                    } else {
                        isPaues = true;
                        stopTranscribe();
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        waveLineView.stopAnim();
                        endrectime = System.currentTimeMillis();
                        Log.d("reclength", String.valueOf((endrectime - startrectime) / 1000));
                        long time = Long.parseLong(SharedPUtils.getRemainTime(getActivity()));
                        time -= (endrectime - startrectime) / 1000;
                        Log.d("mrs", "=========time=========" + time);
                        if (time >= 0) {
                            SharedPUtils.setRemainTime(getActivity(), time);
                        } else {
                            AlertDialog.Builder customizeDialog = new AlertDialog.Builder(getActivity());
                            customizeDialog.create();
                            AlertDialog dia = customizeDialog.show();
                            customizeDialog.setTitle("提示")
                                    .setMessage("剩余时间已用完，请去充值！")
                                    .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dia.dismiss();
                                        }
                                    })
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (!SharedPUtils.getUserSuccess(getActivity())) {
                                                Intent intentlogin = new Intent(getActivity(), UserLoginActivity.class);
                                                startActivity(intentlogin);
                                                return;
                                            }
                                            Intent intent = new Intent(getActivity(), VipActivity.class);
                                            startActivity(intent);
                                        }
                                    })
                                    .show();
                        }
                    }
                }
            }
        });
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText inputServer = new EditText(getActivity());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                inputServer.setText("转文字" + filetime);
                builder.setTitle("编辑新文件名").setView(inputServer)
                        .setNegativeButton("取消", (dialogInterface, i) -> {
                            //取消重新选择

                        });
                builder.setPositiveButton("保存", (dialog, which) -> {
                    String textName = inputServer.getText().toString();
                    if (!TextUtils.isEmpty(textName)) {
                        try {
                            fo = new File(savePath + textName + ".wav");
                            WavHelper.PCMToWAV(f, fo, 1, 16000, 16);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        stopTranscribe();
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        stopTranscribe(v);
                        waveLineView.stopAnim();
                        save_btn.setVisibility(View.GONE);
                        mResultEdit.setText("");
                        Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "文件名称不能为空", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });
        buttonfy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 翻译
                String textResult = mResultEdit.getText().toString();
                if (!TextUtils.isEmpty(textResult)) {
                    Intent intent = new Intent(getActivity(), TranslateActivity.class);
                    intent.putExtra("textResult", textResult);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "没有内容需要翻译呀", Toast.LENGTH_SHORT).show();
                }

            }
        });
        return v;
    }


    @Override
    public void onDestroy() {
        if (speechTranscriber != null) {
            speechTranscriber.stop();
            speechTranscriber = null;
        }
        // 最终，退出时释放client
        if (client !=null){
            client.release();
            super.onDestroy();
        }

    }

    /**
     * 启动录音和识别
     */
    public void startTranscribe(View view) {
        save_btn.setVisibility(View.GONE);
        buttonfy.setVisibility(View.GONE);
        if (speechTranscriber != null) {
            button.setText("录音中");
            speechTranscriber.start();
            return;
        }
        button.setText("录音中");
        mResultEdit.setText("");
        Calendar now = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        filetime = simpleDate.format(now.getTime());
        filename = "radio" + filetime + ".pcm";
        //ofile = "radio" + filetime + ".wav";
        File foder = new File(savePath);
        if (!foder.exists()) {
            foder.mkdirs();
        }
        String fPath = savePath + filename;
        Log.d("mrs", "===========fPath======" + fPath);
        f = new File(fPath);
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(token)) {
            return;
        }
        client = new NlsClient();
        // 第二步，新建识别回调类
        SpeechTranscriberWithRecorderCallback callback = new MyCallback();
        // 第三步，创建识别request
        speechTranscriber = client.createTranscriberWithRecorder(callback);
        // 第四步，设置相关参数
        // Token有有效期，请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
        speechTranscriber.setToken(token);
        // 请使用阿里云语音服务管控台(https://nls-portal.console.aliyun.com/)生成您的appkey
        speechTranscriber.setAppkey("OovNzPolkdCEii0P");
        // 设置返回中间结果，更多参数请参考官方文档
        // 返回中间结果
        speechTranscriber.enableIntermediateResult(false);
        // 开启标点
        speechTranscriber.enablePunctuationPrediction(true);
        // 开启ITN
        speechTranscriber.enableInverseTextNormalization(true);
        // 设置静音断句长度
//        speechTranscriber.setMaxSentenceSilence(500);
        // 设置定制模型和热词
        // speechTranscriber.setCustomizationId("yourCustomizationId");
        // speechTranscriber.setVocabularyId("yourVocabularyId");
        speechTranscriber.start();
    }

    public void creatFile(String path, String fileName) {
        File file = new File(path, fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void stopTranscribe() {
        button.setText("按下说话");
//        button.setEnabled(true);
        // 第八步，停止本次识别
        speechTranscriber.stop();
        save_btn.setVisibility(View.VISIBLE);
        buttonfy.setVisibility(View.VISIBLE);
    }

    // 语音识别回调类，得到语音识别结果后在这里处理
    //    // 注意不要在回调方法里调用transcriber.stop()方法
    //    // 注意不要在回调方法里执行耗时操作
    private class MyCallback implements SpeechTranscriberWithRecorderCallback {

        // 识别开始
        @Override
        public void onTranscriptionStarted(String msg, int code) {
            Log.d(TAG, "OnTranscriptionStarted " + msg + ": " + String.valueOf(code));
        }

        // 请求失败
        @Override
        public void onTaskFailed(String msg, int code) {
            Log.d(TAG, "OnTaskFailed " + msg + ": " + String.valueOf(code));
            handler.sendEmptyMessage(0);
        }

        // 识别返回中间结果，只有开启相关选项时才会回调
        @Override
        public void onTranscriptionResultChanged(final String msg, int code) {
            Log.d(TAG, "OnTranscriptionResultChanged " + msg + ": " + String.valueOf(code));
            Message message = new Message();
            message.obj = msg;
            handler.sendMessage(message);
        }

        // 开始识别一个新的句子
        @Override
        public void onSentenceBegin(String msg, int code) {
            Log.i(TAG, "Sentence begin");
        }

        // 第七步，当前句子识别结束，得到完整的句子文本
        @Override
        public void onSentenceEnd(final String msg, int code) {
            Log.d(TAG, "OnSentenceEnd " + msg + ": " + String.valueOf(code));
            Message message = new Message();
            message.obj = msg;
            handler.sendMessage(message);
        }

        // 识别结束
        @Override
        public void onTranscriptionCompleted(final String msg, int code) {
            Log.d(TAG, "OnTranscriptionCompleted " + msg + ": " + String.valueOf(code));
            Message message = new Message();
            message.obj = msg;
            handler.sendMessage(message);
            handler.clearResult();
        }

        // 请求结束，关闭连接
        @Override
        public void onChannelClosed(String msg, int code) {
            Log.d(TAG, "OnChannelClosed " + msg + ": " + String.valueOf(code));
        }

        // 手机采集的语音数据的回调
        @Override
        public void onVoiceData(byte[] bytes, int i) {
            try {
                fos.write(bytes);
//                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("voicedata", String.valueOf(i));
        }

        // 手机采集的语音音量大小的回调
        @Override
        public void onVoiceVolume(int i) {
            double myVolume = (i - 10) * 4;
//            waveLineView.setVolume((int) myVolume);
            Log.d("MainActivity", "current volume is " + myVolume);
        }
    }

    ;

    // 根据识别结果更新界面的代码
    private  class MyHandler extends Handler {
        StringBuilder fullResult = new StringBuilder();
        private final WeakReference<SpeechTranscriberWithRecorderActivity> mActivity;

        public MyHandler(SpeechTranscriberWithRecorderActivity activity) {
            mActivity = new WeakReference<SpeechTranscriberWithRecorderActivity>(activity);
        }

        public void clearResult() {
            this.fullResult = new StringBuilder();
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj == null) {
                Log.i(TAG, "Empty message received.");
                return;
            }
            String rawResult = (String) msg.obj;
            String result = null;
            String displayResult = null;
            if (rawResult != null && !rawResult.equals("")) {
                JSONObject jsonObject = JSONObject.parseObject(rawResult);
                if (jsonObject.containsKey("payload")) {
                    result = jsonObject.getJSONObject("payload").getString("result");
                    int time = jsonObject.getJSONObject("payload").getIntValue("time");
                    if (time != -1) {
                        fullResult.append(result);
                        displayResult = fullResult.toString();
                        fullResult.append("\n");
                    } else {
                        displayResult = fullResult.toString() + result;
                    }
                    Log.d("mrs", "=========displayResult=========" + displayResult);
                    mResultEdit.setText(displayResult);
//                    mActivity.get().mFullEdit.setText(displayResult);
                }
            }
//            mActivity.get().mResultEdit.setText(result);
        }
    }
//判断按钮是否快速点击

    public boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    //vip剩余
    private boolean isViptimeEnough() {
        userTime = Long.parseLong(SharedPUtils.getRemainTime(getActivity()));
        if (userTime > 1)
            return true;
        else
            return false;
    }

    Runnable gettoken = new Runnable() {
        @Override
        public void run() {
            token = CreateToken.test();
            Log.d("mrs","===========token=========="+token);
        }
    };

    //实现定时器接口的类
    class TestTimer extends TimerTask {
        handleTimeout handler = new handleTimeout();

        @Override
        public void run() {
            if (isPaues)
                return;
            long dur = System.currentTimeMillis();
            long time=  (dur - startrectime) / 1000;
            Log.d("mrs", "=========timerun==========" + time);
            // 获取当前用户剩余秒数
            long viptime = Long.parseLong(SharedPUtils.getRemainTime(getActivity()));
            Log.d("mrs", "=========viptime==========" + viptime);
            if (viptime <=time){
                SharedPUtils.setRemainTime(getActivity(), 0);
                Message msg = new Message();
                msg.obj = "timeout";
                handler.sendMessage(msg);
                this.cancel();
            }
           /* long dur = System.currentTimeMillis();
            long viptime = Long.parseLong(SharedPUtils.getRemainTime(getActivity()));
            Log.d("mrs", "=========startrectime==========" + (dur - startrectime) / 1000);
            Log.d("mrs", "=========viptime==========" + viptime);
            if ((dur - startrectime) / 1000 > viptime) {
                SharedPUtils.setRemainTime(getActivity(), 0);
//                stopTranscribe(button);
                Message msg = new Message();
                msg.obj = "timeout";
                handler.sendMessage(msg);
                this.cancel();

            }*/
        }
    }

    private class handleTimeout extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            AlertDialog.Builder customizeDialog = new AlertDialog.Builder(getActivity());
            customizeDialog.create();
            AlertDialog dia = customizeDialog.show();
            customizeDialog.setTitle("提示")
                    .setMessage("剩余时间已用完，请去充值！")
                    .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dia.dismiss();
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!SharedPUtils.getUserSuccess(getActivity())) {
                                Intent intentlogin = new Intent(getActivity(), UserLoginActivity.class);
                                startActivity(intentlogin);
                                return;
                            }
                            Intent intent = new Intent(getActivity(), VipActivity.class);
                            startActivity(intent);
                        }
                    })
                    .show();
            button.setEnabled(true);
            stopTranscribe();
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            // 说明隐藏
            waveLineView.stopAnim();
        } else {
            waveLineView.startAnim();
        }
    }

}
