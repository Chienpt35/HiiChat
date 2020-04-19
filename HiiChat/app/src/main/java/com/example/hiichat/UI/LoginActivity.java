package com.example.hiichat.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.hiichat.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void clickLogin(View view) {
    }

    public void clickResetPassword(View view) {
    }

    public void clickRegisterLayout(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
}
