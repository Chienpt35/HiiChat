package com.example.hiichat.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.airbnb.lottie.LottieAnimationView;
import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.MainActivity;
import com.example.hiichat.R;
import com.example.hiichat.util.InternetCheck;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    private LottieAnimationView animation_view_dog;
    private LottieAnimationView animation_view_process;
    private InternetCheck internetCheck;
    private DatabaseReference userDB;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        animation_view_dog = (LottieAnimationView) findViewById(R.id.animation_view_dog);
        animation_view_process = (LottieAnimationView) findViewById(R.id.animation_view_process);
        startCheckAnimation(animation_view_dog);
        userDB = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
        internetCheck = new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(Boolean internet) {
                Log.e("internet", internet + "" );
                if (internet){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }
                    },3000);
                }else {
                    showAlertDialog();
                }
            }
        });
        internetCheck.execute();

    }
    private void startCheckAnimation(final LottieAnimationView animationView) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(5000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                animationView.setProgress((Float) valueAnimator.getAnimatedValue());
            }
        });

        if (animationView.getProgress() == 0f) {
            animator.start();
        } else {
            animationView.setProgress(0f);
        }
    }

    private void showAlertDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
        dialog.setView(R.layout.alertdialog_check_internet);
        dialog.setCancelable(false);
//        dialog.setPositiveButton("Thử lại", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
        dialog.setNegativeButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }
}
