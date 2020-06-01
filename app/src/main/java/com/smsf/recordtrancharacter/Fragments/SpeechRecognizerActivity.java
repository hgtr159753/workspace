package com.smsf.recordtrancharacter.Fragments;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.util.NlsClient;
import com.alibaba.idst.util.SpeechRecognizer;
import com.alibaba.idst.util.SpeechRecognizerCallback;
import com.smsf.recordtrancharacter.R;
import com.smsf.recordtrancharacter.Utils.CreateToken;
import com.smsf.recordtrancharacter.audio.WavHelper;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import jaygoo.widget.wlv.WaveLineView;

import static android.media.AudioRecord.STATE_UNINITIALIZED;

/**
 * 调用 SpeechRecognizer 的示例代码，用户需要自己采集音频
 * 本示例代码只是用来展示调用步骤，用户应该在真实使用时按需添加异常处理、防止重复点击等逻辑
 */
public class SpeechRecognizerActivity extends Fragment implements SpeechRecognizerCallback {
    private static final String TAG="AliSpeechDemo";

    private Button button;
    private EditText mFullEdit;
    private EditText mResultEdit;
    private NlsClient client;
    private SpeechRecognizer speechRecognizer;
    private RecordTask recordTask;
    public static String token;
    private static WaveLineView waveLineView;
    static String pname="";
    static String ofile="";
//    private File tmpFile = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v=inflater.inflate(R.layout.activity_speech_recognizer,null,false);
        Button button = (Button) v.findViewById(R.id.button);
        mResultEdit =  v.findViewById(R.id.editText);
        mResultEdit.setText("开始识别");
        waveLineView=v.findViewById(R.id.waveLineView);
        //第一步，创建client实例，client只需要创建一次，可以用它多次创建recognizer

       new Thread(gettoken).start();

        button.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        startRecognizer(view);
                        waveLineView.startAnim();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        stopRecognizer(view);
                        waveLineView.stopAnim();
                        break;
                    }
                }
                return false;
            }
        });


        return v;
//        });
//
//        //第一步，创建client实例，client只需要创建一次，可以用它多次创建recognizer
//        client = new NlsClient();
//        Log.i(TAG, "Simple activity loaded!");
    }
    Runnable gettoken=new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
          token= CreateToken.test();
            client = new NlsClient();
        }
    };
    @Override
    public void onDestroy(){
        if (speechRecognizer != null){
            speechRecognizer.stop();
        }

        // 最终，退出时释放client
        client.release();
        super.onDestroy();
    }

    /**
     * 启动录音和识别
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startRecognizer(View view){
//        mFullEdit.setText("");
        mResultEdit.setText("");

        // 第二步，新建识别回调类

        // 第三步，创建识别request
        speechRecognizer = client.createRecognizerRequest(this);
        // 第四步，设置相关参数
        // Token有有效期，请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
        speechRecognizer.setToken(token);
        // 请使用阿里云语音服务管控台(https://nls-portal.console.aliyun.com/)生成您的appkey
        speechRecognizer.setAppkey("GA5yZWpa4PiUcRmE");
        // 以下为设置各种识别参数，请按需选择，更多参数请见文档
        // 开启ITN
        speechRecognizer.enableInverseTextNormalization(true);
        // 开启标点
        speechRecognizer.enablePunctuationPrediction(false);
        // 不返回中间结果
        speechRecognizer.enableIntermediateResult(false);
        // 设置打开服务端VAD
        speechRecognizer.enableVoiceDetection(true);
        speechRecognizer.setMaxStartSilence(3000);
        speechRecognizer.setMaxEndSilence(600);
        // 设置定制模型和热词
        // speechRecognizer.setCustomizationId("yourCustomizationId");
        // speechRecognizer.setVocabularyId("yourVocabularyId");
        speechRecognizer.start();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        Calendar now = new GregorianCalendar();
        String fileName = simpleDate.format(now.getTime());
        pname=getActivity().getApplicationContext().getCacheDir()+"/v"+fileName+".pcm";
        ofile=getActivity().getApplicationContext().getCacheDir()+"/v"+fileName+".wav";
        //启动录音线程
        recordTask = new RecordTask(this);
        recordTask.execute();


    }

    /**
     * 停止录音和识别
     * @param view
     */
    public void stopRecognizer(View view){
        // 停止录音
        Log.i(TAG, "Stoping recognizer...");
        recordTask.stop();
        speechRecognizer.stop();
    }

    // 语音识别回调类，用户在这里得到语音识别结果，加入您自己的业务处理逻辑
    // 注意不要在回调方法里执行耗时操作
    @Override
    public void onRecognizedStarted(String msg, int code)
    {
        Log.d(TAG,"OnRecognizedStarted " + msg + ": " + String.valueOf(code));
    }

    // 请求失败
    @Override
    public void onTaskFailed(String msg, int code)
    {
        Log.d(TAG,"OnTaskFailed: " + msg + ": " + String.valueOf(code));
        recordTask.stop();
        speechRecognizer.stop();
        final String fullResult = msg;
        Runnable runnable=(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String result = null;
                if (!fullResult.equals("")){
                    JSONObject jsonObject = JSONObject.parseObject(fullResult);
                    if (jsonObject.containsKey("payload")){
                        result = jsonObject.getJSONObject("payload").getString("result");
                    }
                }
//                mFullEdit.setText(fullResult);
//                mResultEdit.setText(result);

                token=CreateToken.test();


            }
        });
        new Thread(runnable).run();
    }

    // 识别返回中间结果，只有enableIntermediateResult(true)时才会回调
    @Override
    public void onRecognizedResultChanged(final String msg, int code)
    {
        Log.d(TAG,"OnRecognizedResultChanged " + msg + ": " + String.valueOf(code));
        final String fullResult = msg;
//       Runnable runnable=(new Runnable() {
//            @Override
//            public void run() {
                String result = null;
                if (!fullResult.equals("")){
                    JSONObject jsonObject = JSONObject.parseObject(fullResult);
                    if (jsonObject.containsKey("payload")){
                        result = jsonObject.getJSONObject("payload").getString("result");
                    }
                }
//                mFullEdit.setText(fullResult);
                mResultEdit.setText(result);
//            }
//        });
    }

    // 第七步，识别结束，得到最终完整结果
    @Override
    public void onRecognizedCompleted(final String msg, int code)
    {
        Log.d(TAG,"OnRecognizedCompleted " + msg + ": " + String.valueOf(code));
        recordTask.stop();
        final String fullResult = msg;
//        Runnable runnable=(new Runnable() {
//            @Override
//            public void run() {
                String result = null;
                if (!fullResult.equals("")){
                    JSONObject jsonObject = JSONObject.parseObject(fullResult);
                    if (jsonObject.containsKey("payload")){
                        result = jsonObject.getJSONObject("payload").getString("result");
                    }
                }
//                mFullEdit.setText(fullResult);
                mResultEdit.setText(result);
//                button.setEnabled(true);
//            }
//        }.run();
    }

    // 请求结束，关闭连接
    @Override
    public void onChannelClosed(String msg, int code) {

        Log.d(TAG, "OnChannelClosed " + msg + ": " + String.valueOf(code));
    }

    static class RecordTask extends AsyncTask<Void, Integer, Void> {
        final static int SAMPLE_RATE = 16000;
        final static int SAMPLES_PER_FRAME = 640;

        private boolean sending;

        WeakReference<SpeechRecognizerActivity> activityWeakReference;

        public RecordTask(SpeechRecognizerActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        public void stop() {
            sending = false;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            FileOutputStream fos = null;

            File tmpFile=new File(pname);
            try {
                fos=new FileOutputStream(tmpFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            SpeechRecognizerActivity activity = activityWeakReference.get();
            Log.d(TAG,"Init audio recorder");
            int bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            AudioRecord mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes * 2);

            if (mAudioRecorder == null || mAudioRecorder.getState() == STATE_UNINITIALIZED) {
                throw new IllegalStateException("Failed to initialize AudioRecord!");
            }
            mAudioRecorder.startRecording();

            ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
            sending = true;
            while(sending){
                buf.clear();
                // 采集语音
                int readBytes = mAudioRecorder.read(buf, SAMPLES_PER_FRAME);
                byte[] bytes = new byte[SAMPLES_PER_FRAME];
                buf.get(bytes, 0, SAMPLES_PER_FRAME);
                try {
                    fos.write(bytes, 0,bytes.length);
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (readBytes>0 && sending){
                    // 第六步，发送语音数据到识别服务
                    int code = activity.speechRecognizer.sendAudio(bytes, bytes.length);
                    if (code < 0) {
                        Log.i(TAG, "Failed to send audio!");
                        activity.speechRecognizer.stop();
                        break;
                    }
                    Log.d(TAG, "Send audio data length: " + bytes.length);
                }
                buf.position(readBytes);
                buf.flip();
            }
            activity.speechRecognizer.stop();
            mAudioRecorder.stop();
            File file=new File(pname);
            File oifile=new File(ofile);
            try {
                WavHelper.PCMToWAV(file,oifile,1,16000,16);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
