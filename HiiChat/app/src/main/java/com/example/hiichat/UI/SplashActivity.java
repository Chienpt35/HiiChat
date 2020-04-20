package com.example.hiichat.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.airbnb.lottie.LottieAnimationView;
import com.example.hiichat.R;

public class SplashActivity extends AppCompatActivity {
    private LottieAnimationView animation_view_dog;
    private LottieAnimationView animation_view_process;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        animation_view_dog = (LottieAnimationView) findViewById(R.id.animation_view_dog);
        animation_view_process = (LottieAnimationView) findViewById(R.id.animation_view_process);
        startCheckAnimation(animation_view_dog);
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            },5000);
        } else {
            animationView.setProgress(0f);
        }
    }
}
