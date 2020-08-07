package com.example.hiichat.Notification;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.Model.Message;
import com.example.hiichat.R;
import com.example.hiichat.UI.ChatActivity;
import com.example.hiichat.util.TypeNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;


public class MyFirebaseService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        //FirebaseMessaging.getInstance().subscribeToTopic("Messages");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            updateToken(s);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        String sented = remoteMessage.getData().get("sender");

        String type = remoteMessage.getData().get("type");


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (type.equals("MESSAGE")){
            if (firebaseUser != null && sented.equals(firebaseUser.getUid())){
                if (!StaticConfig.UID.equals(remoteMessage.getData().get("user"))){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        sendOAndAboveNotification(remoteMessage);
                    }else {
                        sendNotification(remoteMessage);
                    }
                }
            }
        }else if (type.equals("ADDFRIEND")){
            if (firebaseUser != null && sented.equals(firebaseUser.getUid())){
                if (!StaticConfig.UID.equals(remoteMessage.getData().get("user"))){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        sendOAndAboveNotification(remoteMessage);
                    }else {
                        sendNotification(remoteMessage);
                    }
                }
            }
        }

        /*  int notificationID = new Random().nextInt(3000);*/

//        String type = remoteMessage.getData().get("tp");
//
//        String data = remoteMessage.getData().get("dt");
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        int notificationID = new Random().nextInt(3000);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            setupChannels(notificationManager);
//        }
//
//
//        if (text(type).equals(ChatActivity.Type.MESSAGE.toString())) {
//
//            Log.e("TAG", "onMessageReceived: " + data);
//
//        }
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "MESSAGE_CHANNEL_ID")
//                .setSmallIcon(R.drawable.ic_person_low)
//                .setAutoCancel(true)
//                .setContentTitle(remoteMessage.getData().get("title"))
//                .setContentText(remoteMessage.getData().get("message"));
//        assert notificationManager != null;
//        notificationManager.notify(notificationID, notificationBuilder.build());
//
    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void setupChannels(NotificationManager notificationManager) {
//        CharSequence adminChannelName = "New notification";
//        String adminChannelDescription = "Device to devie notification";
//        NotificationChannel adminChannel;
//        adminChannel = new NotificationChannel("MESSAGE_CHANNEL_ID", adminChannelName, NotificationManager.IMPORTANCE_HIGH);
//        adminChannel.setDescription(adminChannelDescription);
//        adminChannel.enableLights(true);
//        adminChannel.setLightColor(Color.RED);
//        adminChannel.enableVibration(true);
//        if (notificationManager != null) {
//            notificationManager.createNotificationChannel(adminChannel);
//        }
//    }

//    private String text(String text) {
//        return TextUtils.isEmpty(text) ? "" : text;
//    }

    private void sendOAndAboveNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String senderName = remoteMessage.getData().get("senderName");
        String idRoom = remoteMessage.getData().get("idRoom");

        ArrayList<CharSequence> arrayList = new ArrayList<>();
        arrayList.add(user);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putCharSequenceArrayList(StaticConfig.INTENT_KEY_CHAT_ID, arrayList);
        bundle.putString(StaticConfig.INTENT_KEY_CHAT_FRIEND, senderName);
        bundle.putString(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, idRoom);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultRound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotifications(title, body, pendingIntent, defaultRound, icon);


        int i = 0;
        if (j > 0) {
            i = j;
        }

        notification1.getNotificationManager().notify(i, builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String senderName = remoteMessage.getData().get("senderName");
        String idRoom = remoteMessage.getData().get("idRoom");

        ArrayList<CharSequence> arrayList = new ArrayList<>();
        arrayList.add(user);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putCharSequenceArrayList(StaticConfig.INTENT_KEY_CHAT_ID, arrayList);
        bundle.putString(StaticConfig.INTENT_KEY_CHAT_FRIEND, senderName);
        bundle.putString(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, idRoom);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultRound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultRound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManager.notify(i, builder.build());
    }

    private void updateToken(String refreshToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Token token = new Token(refreshToken);
        databaseReference.child(firebaseUser.getUid()).setValue(token);
    }

    public static Message coverJson(String s) {
        try {
            Type type = new TypeToken<Message>() {
            }.getType();
            return new Gson().fromJson(s, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
