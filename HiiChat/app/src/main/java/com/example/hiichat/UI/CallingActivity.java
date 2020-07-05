package com.example.hiichat.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.hiichat.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallingActivity extends AppCompatActivity {
    private CircleImageView imgAvatar;
    private ImageView imgMakeCall;
    private ImageView imgCancelCall;


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
    }
}