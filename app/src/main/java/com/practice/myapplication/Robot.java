package com.practice.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2018/4/1.
 */
public class Robot {

    private final String APIKEY = "0512dec465c748eaa2a586b9a87d437c";
//    private final String USERID = "b13956031698f664";
    private final String URL = "http://www.tuling123.com/openapi/api";


    private OkHttpClient mClient;
    private Handler mHandler;

    private String TAG = "ttttt";

    public Robot(Handler handler) {
        mClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
        mHandler = handler;
    }

    public void ask(String question){
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String s = getJson(question).toString();
        RequestBody body = RequestBody.create(JSON, s);
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {//从图灵机器人那里返回的信息
                String ss = response.body().string();
                Log.d(TAG, "onResponse: " + ss);
                try {
                    JSONObject jsonObject = new JSONObject(ss);
                    Message message = Message.obtain();
                    message.obj = jsonObject.getString("text");
                    mHandler.sendMessage(message);//发还给FirstTryActivity
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private JSONObject getJson(String question){
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("key",APIKEY);
            requestJson.put("info",question);
            requestJson.put("userid","100");

            Log.d(TAG, "getJson: "+requestJson.toString());
            return requestJson;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public class requestBean{
//
//        private final String APIKEY = "886c51e867ca4920b95c9e5cd8c94f2a";
//        private final String USERID = "b13956031698f664";
//
//        private int reqType;
//
//        public requestBean(String question) {
//            reqType = 0;
//            Map<String,Map<String,String>> perception = new LinkedHashMap<>();
//            Map<String, String> text = new LinkedHashMap<>();
//            text.put("text",question);
//            perception.put("inputText",text);
//            Map<String, String> userInfo = new LinkedHashMap<>();
//            userInfo.put("apiKey",APIKEY);
//            userInfo.put("userId",USERID);
//        }
//    }
}
