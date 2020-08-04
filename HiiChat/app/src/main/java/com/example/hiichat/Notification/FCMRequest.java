package com.example.hiichat.Notification;


import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class FCMRequest {
    @SuppressLint("StaticFieldLeak")
    private  static FCMRequest instance;
    private RequestQueue requestQueue;
    private Context ctx;

    private FCMRequest(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static FCMRequest getInstance(Context context) {
        if (instance == null) {
            instance = new FCMRequest(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
