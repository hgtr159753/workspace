package com.smsf.recordtrancharacter.Fragments;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.util.NlsClient;
import com.alibaba.idst.util.SpeechTranscriber;
import com.alibaba.idst.util.SpeechTranscriberCallback;
import com.mt.mtloadingmanager.LoadingManager;
import com.smsf.recordtrancharacter.R;
import com.smsf.recordtrancharacter.Utils.CreateToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import static android.media.AudioRecord.STATE_UNINITIALIZED;

/**
 * 实时语音识别
 * NlsClient: 智能语音客户端类，相当于所有语音相关处理类的factory，全局创建一个实例即可。
 * SpeechTranscriber: 代表一次实时语音流识别请求，您需要自己录制音频或从文件读取音频数据，发送给SDK。
 * SpeechTranscriberWithRecorder： 代表一次实时语音识别请求，在SpeechTranscriber的基础上内置录音功能，调用简便，推荐使用。
 * SpeechTranscriberCallback: 语音识别回调接口，在获得识别结果，发生错误等事件发生时会触发回调。您可参照demo实现此接口，在回调方法中加入自己的处理逻辑。
 * SpeechTranscriberWithRecorderCallback：语音识别回调接口，在SpeechTranscriberCallback基础上增加了录制的语音数据和音频音量的回调方法。
 */
public class SpeechTranscriberActivity extends AppCompatActivity {
    private static final String TAG = "AliSpeechDemo";

    private Button button;
    private Button play;
    private EditText mResultEdit;
    private static MyHandler handler;
    private NlsClient client;
    private SpeechTranscriber transcriber;
    //Demo录音线程
    private RecordTask recordTask;
    private static String filepath;
    private static String token;
    private static LoadingManager loadingManager;
    private SeekBar seekBar;
    private  MediaPlayer mediaPlayer;
    private Timer timer;
    private boolean isSeekbarChaning;
    //互斥变量，防止进度条和定时器冲突。
    private String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Smvoice/voice/";
    private ImageView iv_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_transcriber);
        button = findViewById(R.id.button);
        play = findViewById(R.id.button2);
        seekBar = findViewById(R.id.seekBar);
        iv_back = findViewById(R.id.iv_back);
//        mFullEdit =  findViewById(R.id.editText);
        mResultEdit =  findViewById(R.id.editText2);
        loadingManager = new LoadingManager(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTranscribe(v);
            }
        });
        filepath = getIntent().getStringExtra("filename");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(savePath + filepath);
            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // 第一步，创建client实例，client只需要创建一次，可以用它多次创建transcriber
        new Thread(gettoken).start();
        //Toast.makeText(SpeechTranscriberActivity.this, filepath, Toast.LENGTH_LONG).show();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int duration2 = mediaPlayer.getDuration() / 1000;//获取音乐总时长
                int position = mediaPlayer.getCurrentPosition();//获取当前播放的位置
//                tv_start.setText(calculateTime(position / 1000));//开始时间
//                tv_end.setText(calculateTime(duration2));//总时长
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarChaning = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekbarChaning = false;
                mediaPlayer.seekTo(seekBar.getProgress());//在当前位置播放
//                tv_start.setText(calculateTime(mediaPlayer.getCurrentPosition() / 1000));
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();//开始播放
                    int duration = mediaPlayer.getDuration();//获取音乐总时间
                    seekBar.setMax(duration);//将音乐总时间设置为Seekbar的最大值
                    timer = new Timer();//时间监听器
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (!isSeekbarChaning) {
                                if (mediaPlayer ==null)
                                    return;
                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            }
                        }
                    }, 0, 50);
                } else {
                    mediaPlayer.pause();//暂停播放
                }
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        // 等待录音线程完全停止
        if (transcriber != null) {
            transcriber.stop();
        }
        if (timer !=null){
            timer.cancel();
        }
        if (mediaPlayer !=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        // 最终，退出时释放client
        client.release();
        super.onDestroy();
    }

    /**
     * 启动录音和识别
     *
     * @param view
     */
    public void startTranscribe(View view) {
        button.setText("识别中");
        button.setEnabled(false);
//        mFullEdit.setText("");
        mResultEdit.setText("");
        loadingManager.show();
        //UI在主线程更新
        handler = new MyHandler(this);
        // 第二步，新建识别回调类
        SpeechTranscriberCallback callback = new MyCallback(handler);
        // 第三步，创建识别request
        transcriber = client.createTranscriberRequest(callback);
        // 第四步，设置相关参数
        // Token有有效期，请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
        transcriber.setToken(token);
        // 请使用阿里云语音服务管控台(https://nls-portal.console.aliyun.com/)生成您的appkey
        transcriber.setAppkey("OovNzPolkdCEii0P");
        // 设置返回中间结果，更多参数请参考官方文档
        // 返回中间结果
        transcriber.enableIntermediateResult(false);
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
        recordTask = new RecordTask(this);
        recordTask.execute();
    }


    public void stopTranscribe() {
//        button.setText("开始 录音");
//        button.setEnabled(true);
        // 停止录音
        recordTask.stop();
        transcriber.stop();
    }

    Runnable gettoken = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            token = CreateToken.test();
            client = new NlsClient();
        }
    };

    // 语音识别回调类，得到语音识别结果后在这里处理
    // 注意不要在回调方法里执行耗时操作
    private static class MyCallback implements SpeechTranscriberCallback {

        private MyHandler handler;

        public MyCallback(MyHandler handler) {
            this.handler = handler;
        }

        // 识别开始
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

    }


    // 根据识别结果更新界面的代码
    private static class MyHandler extends Handler {
        StringBuilder fullResult = new StringBuilder();
        private final WeakReference<SpeechTranscriberActivity> mActivity;

        public MyHandler(SpeechTranscriberActivity activity) {
            mActivity = new WeakReference<SpeechTranscriberActivity>(activity);
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
                    loadingManager.hide(null);
                    mActivity.get().button.setText("开始识别");
                    mActivity.get().button.setEnabled(true);
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
        WeakReference<SpeechTranscriberActivity> activityWeakReference;

        public RecordTask(SpeechTranscriberActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SpeechTranscriberActivity activity = activityWeakReference.get();
            Log.d("ji", "sending");
            File file = new File(savePath+filepath);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
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
//            button.setText("开始录音");
//            Message msg=new Message();
//            msg.obj="ok";
//           handieend.sendMessage(msg);
//            mAudioRecorder.stop();
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

    private Handler handieend = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            button.setEnabled(true);
            return true;
        }
    });
}
