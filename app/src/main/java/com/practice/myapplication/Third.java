package com.practice.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Third extends AppCompatActivity implements EventListener {

    final String TAG = "baidutest";
    EventManager mEventManager;//百度的监听接口
    private String mJson;
    String result;//语音识别出的声音文本
    private static final String TEMP_DIR = "/sdcard/baiduTTS";
    //    private static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";
//    private static final String MODEL_FILENAME =
//            TEMP_DIR + "/" + "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";
    protected SpeechSynthesizer mSpeechSynthesizer;
    private String mSampleDirPath;
    private static final String SAMPLE_DIR_NAME = "baiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String LICENSE_FILE_NAME = "temp_license";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";

    protected String appId = "15326178";
    protected String appKey = "TfYGHrDUcDKyv0qWEzwctU7Q";
    protected String secretKey = "Tk5tSmKaaEB7IWtANjiAPSedFGwkN8cM";

    private Robot mRobot = null;
    private Handler mHandler;
    //LinearLayout mLinearLayout;

    private List<Msg> msgList = new ArrayList<Msg>();

    private EditText inputText;

    private Button textSend;
    private Button speakSend;

    private RecyclerView msgRecyclerView;

    private MsgAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        //getSupportActionBar().hide();
        initPermission();
        //mLinearLayout = (LinearLayout) findViewById(R.id.contantLL);//对话框
        mEventManager = EventManagerFactory.create(this, "asr");
        mEventManager.registerListener(this);//开始监听



        initMsgs(); // 初始化消息数据
        inputText = (EditText) findViewById(R.id.input_text3);

        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view3);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);

        speakSend = (Button) findViewById(R.id.speechSend3);
        speakSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEventManager.send(SpeechConstant.ASR_START, mJson, null, 0, 0);
            }
        });

        textSend = (Button) findViewById(R.id.send3);
        textSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    msgList.add(msg);
                    adapter.notifyItemInserted(msgList.size() - 1); // 当有新消息时，刷新ListView中的显示
                    msgRecyclerView.scrollToPosition(msgList.size() - 1); // 将ListView定位到最后一行
                    inputText.setText(""); // 清空输入框中的内容
                    mRobot.ask(content);
                }
            }
        });



        Map<String, Object> params = new LinkedHashMap<>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);//SpeechConstant，百度的类
        mJson = new JSONObject(params).toString();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                initTTs();
//            }
//        }).start();
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                String s = (String) msg.obj;
                speak(s);//语音合成
//                TextView tv = new TextView(Third.this);
//                tv.setGravity(Gravity.LEFT);//图灵机器人返回的信息
//                tv.setText(s);
//                mLinearLayout.addView(tv);
                Msg msgReceive = new Msg(s, Msg.TYPE_RECEIVED);
                msgList.add(msgReceive);
                adapter.notifyItemInserted(msgList.size() - 1); // 当有新消息时，刷新ListView中的显示
                msgRecyclerView.scrollToPosition(msgList.size() - 1); // 将ListView定位到最后一行
                Log.d(TAG, "handleMessage: " + s);
            }
        };
        mRobot = new Robot(mHandler);

        initialEnv();
        initialTts();

        speak("准备就绪");
    }

    private void initialTts() {
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(this);
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {
            @Override
            public void onSynthesizeStart(String s) {

            }

            @Override
            public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

            }

            @Override
            public void onSynthesizeFinish(String s) {

            }

            @Override
            public void onSpeechStart(String s) {

            }

            @Override
            public void onSpeechProgressChanged(String s, int i) {

            }

            @Override
            public void onSpeechFinish(String s) {

            }

            @Override
            public void onError(String s, SpeechError speechError) {

            }
        });
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了正式离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
        // 如果合成结果出现临时授权文件将要到期的提示，说明使用了临时授权文件，请删除临时授权即可。
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/"
                + LICENSE_FILE_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId(appId/*这里只是为了让Demo运行使用的APPID,请替换成自己的id。*/);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey(appKey,
                secretKey/*这里只是为了让Demo正常运行使用APIKey,请替换成自己的APIKey*/);
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。)
        // AuthInfo接口用于测试开发者是否成功申请了在线或者离线授权，如果测试授权成功了，可以删除AuthInfo部分的代码（该接口首次验证时比较耗时），不会影响正常使用（合成使用时SDK内部会自动验证授权）
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);

        if (authInfo.isSuccess()) {
            Toast.makeText(this,"auth success",Toast.LENGTH_LONG).show();
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Toast.makeText(this,"auth failed errorMsg=" + errorMsg,Toast.LENGTH_LONG).show();
        }

        // 初始化tts
        mSpeechSynthesizer.initTts(TtsMode.MIX);
        // 加载离线英文资源（提供离线英文合成功能）
//        int result =
//                mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
//                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        //Toast.makeText(this,"loadEnglishModel result=" + result,Toast.LENGTH_LONG).show();

        //打印引擎信息和model基本信息
        //printEngineInfo();
    }

    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);
    }

    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    private void initMsgs() {
        Msg msg1 = new Msg("春眠不觉晓", Msg.TYPE_SENT);
        msgList.add(msg1);
        Msg msg2 = new Msg("处处闻啼鸟", Msg.TYPE_RECEIVED);
        msgList.add(msg2);
    }


    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void speak(String text) {
//        String text = this.mInput.getText().toString();
        //需要合成的文本text的长度不能超过1024个GBK字节。
//        if (TextUtils.isEmpty(mInput.getText())) {
//            text = "欢迎使用百度语音合成SDK,百度语音为你提供支持。";
//            mInput.setText(text);
//        }
        int speakResult = this.mSpeechSynthesizer.speak(text);
        if (speakResult < 0) {
            Toast.makeText(this,"error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ",Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public void onEvent(String s, String s1, byte[] bytes, int i, int i1) {
        if (s.equals("asr.finish")) {

            mRobot.ask(result);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    TextView tv = new TextView(Third.this);
//                    tv.setGravity(Gravity.RIGHT);
//                    tv.setText(result);
//                    mLinearLayout.addView(tv);
                    Msg msg = new Msg(result, Msg.TYPE_SENT);
                    msgList.add(msg);
                    adapter.notifyItemInserted(msgList.size() - 1); // 当有新消息时，刷新ListView中的显示
                    msgRecyclerView.scrollToPosition(msgList.size() - 1); // 将ListView定位到最后一行
                }
            });
        }
        try {
            if (s1 == null) return;
            JSONObject jsonObject = new JSONObject(s1);
            result = jsonObject.get("best_result").toString();

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    bt.setText(result);
//
//                    mTextView.setText(mTextView.getText() + "/n" + result);
//                    mTextView.setGravity(Gravity.LEFT);
//                }
//            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }
}
