package com.smsf.recordtrancharacter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.util.NlsClient;
import com.alibaba.idst.util.SpeechTranscriber;
import com.alibaba.idst.util.SpeechTranscriberCallback;
import com.mt.mtloadingmanager.LoadingManager;
import com.smsf.recordtrancharacter.Fragments.SpeechTranscriberActivity;
import com.smsf.recordtrancharacter.Utils.CreateToken;
import com.smsf.recordtrancharacter.Utils.SharedPUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import jaygoo.widget.wlv.WaveLineView;
import tech.oom.idealrecorder.IdealRecorder;
import tech.oom.idealrecorder.StatusListener;

import static com.smsf.recordtrancharacter.Utils.CreateToken.token;

public class MainActivity extends Fragment {

    private Button button;
    private static MyHandler handler;
    private WaveLineView waveLineView;
    private TextView tips;
    private SpeechTranscriber transcriber;
    private EditText mResultEdit;
    private IdealRecorder idealRecorder;
    private NlsClient client;
    private IdealRecorder.RecordConfig recordConfig;
    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;
    private RecordTask recordTask;
    private String filepath;
    private long startrectime=0;
    private long endrectime=0;
    private static LoadingManager loadingManager;

    static Context context;
    private StatusListener statusListener = new StatusListener() {
        @Override
        public void onStartRecording() {
            waveLineView.startAnim();
            button.setText("开始录音");
            startrectime=System.currentTimeMillis();
        }


        @Override
        public void onVoiceVolume(int volume) {
            double myVolume = (volume - 40) * 4;
            waveLineView.setVolume((int) myVolume);
            Log.d("MainActivity", "current volume is " + volume);
        }

        @Override
        public void onRecordError(int code, String errorMsg) {
            tips.setText("录音错误" + errorMsg);
        }
        @Override
        public void onFileSaveFailed(String error) {
//            Toast.makeText(MainActivity.this, "文件保存失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFileSaveSuccess(String fileUri) {
//            Toast.makeText(MainActivity.this, "文件保存成功,路径是" + fileUri, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopRecording() {
//            recordBtn.setText("录音结束");
            waveLineView.stopAnim();
            idealRecorder.stop();
        }
    };


    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            if (requestCode == 100) {
                record();
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // 权限申请失败回调。
            if (requestCode == 100) {
//                Toast.makeText(MainActivity.this, "没有录音和文件读取权限，你自己看着办", Toast.LENGTH_SHORT).show();
            }
            if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, deniedPermissions)) {
                AndPermission.defaultSettingDialog(MainActivity.this, 300).show();
            }
        }

    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.activity_speech_recognizer, container, false);
        button =v.findViewById(R.id.button);
        new Thread(gettoken).start();
        waveLineView = v.findViewById(R.id.waveLineView);
        mResultEdit = v.findViewById(R.id.editText);
//        client = new NlsClient();
        IdealRecorder.getInstance().init(getActivity());
        context=getActivity();
        loadingManager=new LoadingManager(context);
        idealRecorder = IdealRecorder.getInstance();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFastClick()) {
                    if (button.getText().equals("按下说话")) {
                        if(isViptimeEnough()) {
                            readyRecord();
                        }
                        else
                        {
                         new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("剩余时间不足，请先充值！")
                        .setPositiveButton("确定", null)
                        .show();
                        }
                    } else {
                        stopRecord();
//                        button.setText("按下说话");
                        endrectime=System.currentTimeMillis();
                        Log.d("reclength", String.valueOf((endrectime-startrectime)/1000));
                        long time=Long.parseLong(SharedPUtils.getRemainTime(getActivity()));
                        time-=(endrectime-startrectime)/1000;
                        if(time>=0) {
                            startTranscribe(v);
                            SharedPUtils.setRemainTime(getActivity(),time);
                        }
                        else
                        {
                            new AlertDialog.Builder(context)
                                    .setTitle("提示")
                                    .setMessage("剩余时间不足，请先充值！")
                                    .setPositiveButton("确定", null)
                                    .show();
                        }
                    }
                }
            }
        });
        recordConfig = new IdealRecorder.RecordConfig(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        return v;
    }


    /**
     * 开始录音
     */
    private void record() {
        //如果需要保存录音文件  设置好保存路径就会自动保存  也可以通过onRecordData 回调自己保存  不设置 不会保存录音
        Log.d("mrs","============getSaveFilePath()=========="+getSaveFilePath());
        idealRecorder.setRecordFilePath(getSaveFilePath());
//        idealRecorder.setWavFormat(false);
        //设置录音配置 最长录音时长 以及音量回调的时间间隔
        idealRecorder.setRecordConfig(recordConfig).setMaxRecordTime(Long.parseLong(SharedPUtils.getRemainTime(getActivity()))*1000).setVolumeInterval(200);
        //设置录音时各种状态的监听
        idealRecorder.setStatusListener(statusListener);
        idealRecorder.start(); //开始录音
    }

    /**
     * 获取文件保存路径
     *
     * @return
     */
    private String getSaveFilePath() {
        File file = new File(String.valueOf(getActivity().getCacheDir()));
        if (!file.exists()) {
            file.mkdirs();
        }
        File wavFile = new File(file, "ideal.wav");
        return wavFile.getAbsolutePath();
    }


    /**
     * 停止录音
     */
    private void stopRecord() {
        //停止录音
        idealRecorder.stop();


    }


    /**
     * 准备录音 录音之前 先判断是否有相关权限
     */
    private void readyRecord() {
        AndPermission.with(this)
                .requestCode(100)
                .permission(Permission.MICROPHONE, Permission.STORAGE)
                .rationale(rationaleListener).callback(listener).start();

    }

    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            com.yanzhenjie.alertdialog.AlertDialog.newBuilder(getActivity())
                    .setTitle("提示")
                    .setMessage("录制声音保存录音需要录音和读取文件相关权限")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rationale.resume();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    rationale.cancel();
                }
            }).create().show();
        }
    };

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


    /**
     * 启动录音和识别
     *
     * @param view
     */
    public void startTranscribe(View view) {
        button.setText("识别中");
        loadingManager.show();
        button.setEnabled(false);
//        mFullEdit.setText("");
        mResultEdit.setText("");
//        loadingManager.show();
        //UI在主线程更新
        handler = new MyHandler(MainActivity.this);
        // 第二步，新建识别回调类
        SpeechTranscriberCallback callback = new MainActivity.MyCallback(handler);
        // 第三步，创建识别request
        transcriber = client.createTranscriberRequest(callback);
        // 第四步，设置相关参数
        // Token有有效期，请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
        transcriber.setToken(token);
        // 请使用阿里云语音服务管控台(https://nls-portal.console.aliyun.com/)生成您的appkey
        transcriber.setAppkey("GA5yZWpa4PiUcRmE");
        // 设置返回中间结果，更多参数请参考官方文档
        // 返回中间结果
        transcriber.enableIntermediateResult(true);
        // 开启标点
        transcriber.enablePunctuationPrediction(true);
        // 开启ITN
        transcriber.enableInverseTextNormalization(true);
        // 设置静音断句长度
//        transcriber.setMaxSentenceSilence(500);
        // 设置定制模型和热词
        // transcriber.setCustomizationId("yourCustomizationId");
        // transcriber.setVocabularyId("yourVocabularyId");
        transcriber.start();
        //启动录音线程
        recordTask = new RecordTask(MainActivity.this);
        recordTask.execute();
    }


    public void stopTranscribe() {
//        button.setText("开始 录音");
//        button.setEnabled(true);
        // 停止录音
        recordTask.stop();
        transcriber.stop();
    }

    // 语音识别回调类，得到语音识别结果后在这里处理
    // 注意不要在回调方法里执行耗时操作
    private static class MyCallback implements SpeechTranscriberCallback {

        private MainActivity.MyHandler handler;

        public MyCallback(MainActivity.MyHandler handler) {
            this.handler = handler;
        }

        // 识别开始
        // 识别开始
        @Override
        public void onTranscriptionStarted(String msg, int code) {
            Log.d("TAG", "OnTranscriptionStarted " + msg + ": " + String.valueOf(code));
        }

        // 请求失败
        @Override
        public void onTaskFailed(String msg, int code) {
            Log.d("TAG", "OnTaskFailed " + msg + ": " + String.valueOf(code));
            handler.sendEmptyMessage(0);
        }

        // 识别返回中间结果，只有开启相关选项时才会回调
        @Override
        public void onTranscriptionResultChanged(final String msg, int code) {
            Log.d("TAG", "OnTranscriptionResultChanged " + msg + ": " + String.valueOf(code));
            Message message = new Message();
            message.obj = msg;
            handler.sendMessage(message);
        }

        // 开始识别一个新的句子
        @Override
        public void onSentenceBegin(String msg, int code) {
            Log.i("TAG", "Sentence begin");
        }

        // 第七步，当前句子识别结束，得到完整的句子文本
        @Override
        public void onSentenceEnd(final String msg, int code) {
            Log.d("TAG", "OnSentenceEnd " + msg + ": " + String.valueOf(code));
            Message message = new Message();
            message.obj = msg;
            handler.sendMessage(message);
        }

        // 识别结束
        @Override
        public void onTranscriptionCompleted(final String msg, int code) {
            Log.d("TAG", "OnTranscriptionCompleted " + msg + ": " + String.valueOf(code));
            Message message = new Message();
            message.obj = "finish";
            handler.sendMessage(message);
            handler.clearResult();

//            new AlertDialog.Builder(context)
//                        .setTitle("提示")
//                        .setMessage("识别完成，未发现语音内容！")
//                        .setPositiveButton("确定", null)
//                        .show();
//


        }

        // 请求结束，关闭连接
        @Override
        public void onChannelClosed(String msg, int code) {
            Log.d("TAG", "OnChannelClosed " + msg + ": " + String.valueOf(code));
        }

    }

    // 根据识别结果更新界面的代码
    private static class MyHandler extends Handler {
        StringBuilder fullResult = new StringBuilder();
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        public void clearResult() {
            this.fullResult = new StringBuilder();
        }

        @Override
        public void handleMessage(Message msg) {
            String rawResult = (String) msg.obj;
            String result = null;
            String displayResult = null;
            if (msg.obj == null) {
                Log.i("TAG", "Empty message received.");
                return;
            }
            if(msg.obj.equals("finish"))
            {
                loadingManager.hide(null);
                if(result==null)
                {
                    new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("识别完成")
                        .setPositiveButton("确定", null)
                        .show();
//
                }
             mActivity.get().button.setEnabled(true);
                mActivity.get().button.setText("按下说话");
                return;
            }
            if (!rawResult.equals("")) {
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
                    System.out.println(displayResult);
                    mActivity.get().mResultEdit.setText(displayResult);
                    mActivity.get().button.setText("按下说话");
                    mActivity.get().button.setEnabled(true);
//                  return;
                }
            }


//            mActivity.get().mResultEdit.setText(displayResult);
        }
    }

    // 录音并发送给识别的代码，客户可以直接使用
    class RecordTask extends AsyncTask<Void, Integer, Void> {
        final static int SAMPLE_RATE = 16000;
        final static int SAMPLES_PER_FRAME = 640;
        private boolean sending;
        WeakReference<MainActivity> activityWeakReference;

        public RecordTask(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity activity = activityWeakReference.get();
            Log.d("ji", "sending");
            File file = new File(getSaveFilePath());
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(getSaveFilePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            byte[] b = new byte[3200];
            int len = 0;
            sending = true;
            while (true && sending) {
                try {
                    if (!((len = fis.read(b)) > 0)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("ji", "send data pack length: " + len);
                transcriber.sendAudio(b, len);

                int deltaSleep = getSleepDelta(len, 16000);
                try {
                    Thread.sleep(deltaSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stopTranscribe();

            return null;
        }

        public int getSleepDelta(int dataSize, int sampleRate) {
            // 仅支持16位采样
            int sampleBytes = 16;
            // 仅支持单通道
            int soundChannel = 1;
            return (dataSize * 10 * 8000) / (160 * sampleRate);
        }
        public void stop() {
            sending = false;
        }

    }
    Runnable gettoken=new Runnable() {
        @Override
        public void run() {
            token= CreateToken.test();
            client = new NlsClient();
        }
    };
    //vip剩余
    private boolean isViptimeEnough()
    {
        long time=Long.parseLong(SharedPUtils.getRemainTime(getActivity()));
        if(time>0)
            return true;
        else
            return false;
    }
}