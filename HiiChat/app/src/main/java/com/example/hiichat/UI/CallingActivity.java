package com.example.hiichat.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.R;
import com.example.hiichat.util.ImageUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallingActivity extends AppCompatActivity{
    private CircleImageView imgAvatar;
    private ImageView imgMakeCall;
    private ImageView imgCancelCall;
    private DatabaseReference usersRef;
    private TextView tvUsername;
    private TextView tvCalling;
    private String receiverUserID = "", receiverUserImage = "", receiverUseName = "";
    public String senderUserImage = "", senderUseName = "", checked = "";
    private String callingID = "", ringingID = "";
    private MediaPlayer mediaPlayerCall, mediaPlayerWaiting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        initView();
    }

    private void initView() {
        imgAvatar = (CircleImageView) findViewById(R.id.img_avatar);
        imgMakeCall = (ImageView) findViewById(R.id.img_makeCall);
        imgCancelCall = (ImageView) findViewById(R.id.img_cancelCall);
        tvUsername = (TextView) findViewById(R.id.tv_username);
        tvCalling = (TextView) findViewById(R.id.tv_calling);

        mediaPlayerCall = MediaPlayer.create(this, R.raw.ringtone_call);
        mediaPlayerWaiting = MediaPlayer.create(this, R.raw.ringtone_waiting);

        receiverUserID = getIntent().getExtras().get(StaticConfig.USER_VISIT).toString();

        usersRef = FirebaseDatabase.getInstance().getReference("user");

        getAndsetInfoFriend();

        imgCancelCall.setOnClickListener(v -> {
            mediaPlayerWaiting.stop();
            mediaPlayerCall.stop();

            checked = "checked";

            cancelCalling();
        });

        imgMakeCall.setOnClickListener(v -> {
            mediaPlayerCall.stop();

            final HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("picked", "picked");

            usersRef.child(StaticConfig.UID).child("Ringing")
                    .updateChildren(hashMap)
                    .addOnCompleteListener(task -> {
                        if (task.isComplete()){
                            startActivity(new Intent(CallingActivity.this, VideoChatActivity.class));
                        }
                    });
        });
    }

    private void cancelCalling() {

        //từ người gửi
        usersRef.child(StaticConfig.UID)
                .child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("calling")) {

                            Log.e("TAG", "từ người gửi");
                            callingID = dataSnapshot.child("calling").getValue().toString();

                            final HashMap<String, Object> callHashMap = new HashMap<>();
                            callHashMap.put("checked", "checked");
                            usersRef.child(StaticConfig.UID)
                                    .child("Calling")
                                    .updateChildren(callHashMap)
                                    .addOnCompleteListener(task -> {
                                        finish();
                                        if (task.isSuccessful()) {
                                            usersRef.child(callingID)
                                                    .child("Ringing")
                                                    .removeValue()
                                                    .addOnCompleteListener(task1 -> {
                                                       if (task1.isSuccessful()){
                                                           usersRef.child(StaticConfig.UID)
                                                                   .child("Calling")
                                                                   .child("calling")
                                                                   .removeValue();
                                                       }
                                                    });
                                        }
                                    });
                        } else {
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //từ người nhận
        usersRef.child(StaticConfig.UID)
                .child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing")) {
                            Log.e("TAG", "từ người nhận");

                            ringingID = dataSnapshot.child("ringing").getValue().toString();

                            final HashMap<String, Object> ringingHashMap = new HashMap<>();
                            ringingHashMap.put("checked", "checked");
                            usersRef.child(ringingID)
                                    .child("Calling")
                                    .updateChildren(ringingHashMap)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()){
                                            finish();
                                            usersRef.child(StaticConfig.UID)
                                                    .child("Ringing")
                                                    .removeValue()
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task.isSuccessful()) {
                                                            usersRef.child(ringingID)
                                                                    .child("Calling")
                                                                    .child("calling")
                                                                    .removeValue();
                                                        }
                                                    });
                                        }
                                    });
                        } else {
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getAndsetInfoFriend() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(receiverUserID).exists()) {
                    receiverUseName = dataSnapshot.child(receiverUserID).child("name").getValue().toString();
                    receiverUserImage = dataSnapshot.child(receiverUserID).child("avata").getValue().toString();
                    tvUsername.setText(receiverUseName);

                    if (!receiverUserImage.equals("default")) {
                        imgAvatar.setImageBitmap(ImageUtils.getBitmap(receiverUserImage));
                    }
                }
                if (dataSnapshot.child(StaticConfig.UID).exists()) {
                    senderUseName = dataSnapshot.child(StaticConfig.UID).child("name").getValue().toString();
                    senderUserImage = dataSnapshot.child(StaticConfig.UID).child("avata").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayerWaiting.start();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(StaticConfig.UID).child("Ringing").exists() && !dataSnapshot.child("Calling").exists()) {
                    imgMakeCall.setVisibility(View.VISIBLE);
                    tvCalling.setText("Ringing");
                    mediaPlayerCall.start();
                }
                if (dataSnapshot.child(receiverUserID).child("Calling").hasChild("checked") ||
                        dataSnapshot.child(StaticConfig.UID).child("Calling").hasChild("checked")){

                    finish();
                    usersRef.child(StaticConfig.UID).child("Calling").child("checked").removeValue();

                }
                if (dataSnapshot.child(receiverUserID).child("Ringing").hasChild("picked")){
                    mediaPlayerWaiting.stop();
                    startActivity(new Intent(CallingActivity.this, VideoChatActivity.class));
                    usersRef.child(receiverUserID).child("Ringing").removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}