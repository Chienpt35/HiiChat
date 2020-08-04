package com.example.hiichat.Notification;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.hiichat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class FCMSubSend implements FCMKey {

    private static final String TAG = "SubscribeTopic";

    public static void subscribe(String topic) {
        if (TextUtils.isEmpty(topic)) {
            return;
        }
        FCMInit.get().getFirebaseMessaging().subscribeToTopic(topic).addOnCompleteListener(task -> {
            String msg = "Đăng kí thành công topic" + topic;
            if (!task.isSuccessful()) {
                msg = "Đăng kí thất bại topic" + topic;
            }
            Log.d(TAG, msg);
        });
    }


    public static void unsubscribe(String topic) {
        if (TextUtils.isEmpty(topic)) {
            return;
        }
        FCMInit.get().getFirebaseMessaging().unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
            String msg = "Hủy Đăng kí thành công topic" + topic;
            if (!task.isSuccessful()) {
                msg = "Hủy Đăng kí thất bại topic" + topic;
            }
            Log.d(TAG, msg);
        });
    }

    public static void send(FCMModel fcmModel, Context context) {
        if (fcmModel == null) {
            return;
        }
        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", fcmModel.title);
            notifcationBody.put("tp", fcmModel.type);
            notifcationBody.put("message", fcmModel.message);
            notifcationBody.put("dt", fcmModel.data);
            notification.put("to", "/topics/" + fcmModel.to);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification, response -> {

        }, error -> {
            Log.e("TAG", "error: " + error);
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", SERVER_KEY);
                params.put("Content-Type", CONTENT_TYPE);
                return params;
            }
        };
        FCMRequest.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

}
