package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Kiểm tra trạng thái đăng nhập
        if (mAuth.getCurrentUser() != null) {
            // Đã đăng nhập, chuyển đến SplashActivity để xác định role
            Intent intent = new Intent(MainActivity.this, SplashActivity.class);
            startActivity(intent);
        } else {
            // Chưa đăng nhập, chuyển đến LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        finish(); // Đóng MainActivity
    }
}