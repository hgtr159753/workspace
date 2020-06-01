package com.smsf.recordtrancharacter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smsf.recordtrancharacter.Utils.HttpUtil;
import com.smsf.recordtrancharacter.Utils.ToastUtils;
import com.smsf.recordtrancharacter.view.LanguageDialog;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TranslateActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView fy_title_text;
    private EditText editText;
    private EditText editText1;
    private TextView feedback_btn;
    private String textResult;
    private ProgressDialog mProgressDialog;

    // 翻译相关
    private static final String WebITS_URL = "http://itrans.xfyun.cn/v2/its";
    // 应用ID（到控制台获取）
    private static final String APPID = "5ecf5008";
    // 接口APIKey（到控制台机器翻译服务页面获取）
    private static final String API_KEY = "a6346e0db5d4ffb18dd808b6a795b8f5";
    // 接口APISercet（到控制台机器翻译服务页面获取）
    private static final String API_SECRET = "418996b8c2f3c3c151fe35d904d584bd";
    // 语种列表参数值请参照接口文档：https://doc.xfyun.cn/rest_api/机器翻译.html
    // 源语种
    private static final String FROM = "cn";
    // 目标语种
    private static String TO = "en";
    // 翻译文本
    private static final String TEXT = "中华人民共和国于1949年成立";
    private ImageView iv_back;
    private ImageView copy_img;
    private ImageView fy_title_img;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        fy_title_text = findViewById(R.id.fy_title_text);
        feedback_btn = findViewById(R.id.feedback_btn);
        editText = findViewById(R.id.editText);
        editText1 = findViewById(R.id.editText1);
        copy_img = findViewById(R.id.copy_btn);
        iv_back = findViewById(R.id.iv_back);
        fy_title_img = findViewById(R.id.fy_title_img);
        editText1.setFocusable(false);//不可编辑
        fy_title_text.setOnClickListener(this);
        feedback_btn.setOnClickListener(this);
        fy_title_img.setOnClickListener(this);
        copy_img.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        textResult = getIntent().getStringExtra("textResult");
        editText.setText(textResult);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fy_title_img:
            case R.id.fy_title_text:
                // 点击标题切换目标源
                LanguageDialog languageDialog = new LanguageDialog(this);
                languageDialog.setOnClickLinter(new LanguageDialog.onClickLinter() {
                    @Override
                    public void onClick(String titleName) {
                        fy_title_text.setText(titleName);
                        languageDialog.dismiss();
                        // 判断需要转换的目标语言是什么
                        if ("彝语".equals(titleName)) {
                            TO = "ii";
                        } else if ("英文".equals(titleName)) {
                            TO = "en";
                        } else if ("日语".equals(titleName)) {
                            TO = "ja";
                        } else if ("俄语".equals(titleName)) {
                            TO = "ru";
                        } else if ("法语".equals(titleName)) {
                            TO = "fr";
                        } else if ("西班牙语".equals(titleName)) {
                            TO = "es";
                        } else if ("阿拉伯语".equals(titleName)) {
                            TO = "ar";
                        } else if ("广东话".equals(titleName)) {
                            TO = "yue";
                        }
                    }
                });
                languageDialog.show();
                break;
            case R.id.feedback_btn:
                // 翻译
                buildDialog(getResources().getString(R.string.trimming)).show();
                new Thread(runnable).start();
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.copy_btn:
                // 复制
                String text = editText1.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    ToastUtils.showToast(this, "空空如也,没有内容可以复制~");
                    return;
                }
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(text);
                Toast.makeText(this, "复制成功，可以发给朋友们了。", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private ProgressDialog buildDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "", msg);
        }
        mProgressDialog.setMessage(msg);
        return mProgressDialog;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            textResult = editText.getText().toString();
            if (!TextUtils.isEmpty(textResult)) {
                LangTran(textResult);
            } else {
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        /**重写handleMessage方法*/
        @Override
        public void handleMessage(Message msg) {
            if (msg.what ==1){
                mProgressDialog.dismiss();
                ToastUtils.showToast(TranslateActivity.this, "空空如也.快输入点内容吧~");
            }
        }
    };



    public void LangTran(String text) {
        try {
            String body = buildHttpBody(text);
            Map<String, String> header = buildHttpHeader(body);
            Map<String, Object> resultMap = HttpUtil.doPost2(WebITS_URL, header, body);
            if (resultMap != null) {
                mProgressDialog.dismiss();
                String resultStr = resultMap.get("body").toString();
                //以下仅用于调试
                Gson json = new Gson();
                ResponseData resultData = json.fromJson(resultStr, ResponseData.class);
                int code = resultData.getCode();
                if (resultData.getCode() != 0) {
                    Log.d("mrs", "-----------------code----------" + code);
                    mProgressDialog.dismiss();
                    ToastUtils.showToast(TranslateActivity.this, "翻译出错了,请重新尝试~");
                }
                if (resultData.getData() != null && resultData.getData().getResult() != null && resultData.getData().getResult().getTrans_result() != null) {
                    ResponseData.TransResult result = resultData.getData().getResult().getTrans_result();
                    editText1.setText(result.getDst());
                }
            } else {
                Toast.makeText(this, "调用失败", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 组装http请求头
     */
    public static Map<String, String> buildHttpHeader(String body) throws Exception {
        Map<String, String> header = new HashMap<String, String>();
        URL url = new URL(WebITS_URL);
        //时间戳
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateD = new Date();
        String date = format.format(dateD);
        //System.out.println("【ITS WebAPI date】\n" + date);

        //对body进行sha256签名,生成digest头部，POST请求必须对body验证
        String digestBase64 = "SHA-256=" + signBody(body);
        //System.out.println("【ITS WebAPI digestBase64】\n" + digestBase64);

        //hmacsha256加密原始字符串
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").//
                append("date: ").append(date).append("\n").//
                append("POST ").append(url.getPath()).append(" HTTP/1.1").append("\n").//
                append("digest: ").append(digestBase64);
        //System.out.println("【ITS WebAPI builder】\n" + builder);
        String sha = hmacsign(builder.toString(), API_SECRET);
        //System.out.println("【ITS WebAPI sha】\n" + sha);

        //组装authorization
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", API_KEY, "hmac-sha256", "host date request-line digest", sha);
        System.out.println("【ITS WebAPI authorization】\n" + authorization);

        header.put("Authorization", authorization);
        header.put("Content-Type", "application/json");
        header.put("Accept", "application/json,version=1.0");
        header.put("Host", url.getHost());
        header.put("Date", date);
        header.put("Digest", digestBase64);
        System.out.println("【ITS WebAPI header】\n" + header);
        return header;
    }


    /**
     * 组装http请求体
     */
    public String buildHttpBody(String text) throws Exception {
        JsonObject body = new JsonObject();
        JsonObject business = new JsonObject();
        JsonObject common = new JsonObject();
        JsonObject data = new JsonObject();
        //填充common
        common.addProperty("app_id", APPID);
        //填充business
        business.addProperty("from", FROM);
        business.addProperty("to", TO);
        //填充data
        byte[] textByte = text.getBytes("UTF-8");
        String textBase64 = android.util.Base64.encodeToString(textByte, android.util.Base64.NO_WRAP);
        //System.out.println("【OTS WebAPI textBase64编码后长度：】\n" + textBase64.length());
        data.addProperty("text", textBase64);
        //填充body
        body.add("common", common);
        body.add("business", business);
        body.add("data", data);
        return body.toString();
    }

    /**
     * 对body进行SHA-256加密
     */
    private static String signBody(String body) throws Exception {
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(body.getBytes("UTF-8"));
            encodestr = android.util.Base64.encodeToString(messageDigest.digest(), android.util.Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    /**
     * hmacsha256加密
     */
    private static String hmacsign(String signature, String apiSecret) throws Exception {
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(signature.getBytes(charset));
        return android.util.Base64.encodeToString(hexDigits, android.util.Base64.NO_WRAP);
    }

    public static class ResponseData {
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public ResultBean getData() {
            return data;
        }

        public void setData(ResultBean data) {
            this.data = data;
        }

        private int code;
        private String message;
        private String sid;
        private ResultBean data;

        public class ResultBean {
            public TransResultBean getResult() {
                return result;
            }

            public void setResult(TransResultBean result) {
                this.result = result;
            }

            private TransResultBean result;
        }


        public class TransResultBean {
            public TransResult getTrans_result() {
                return trans_result;
            }

            public void setTrans_result(TransResult trans_result) {
                this.trans_result = trans_result;
            }

            private TransResult trans_result;
        }


        public class TransResult {
            public String getDst() {
                return dst;
            }

            public void setDst(String dst) {
                this.dst = dst;
            }

            public String getSrc() {
                return src;
            }

            public void setSrc(String src) {
                this.src = src;
            }

            private String dst;
            private String src;
        }

    }
}
