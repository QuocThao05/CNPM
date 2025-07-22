package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Delay 2 giây rồi kiểm tra user role
        new Handler().postDelayed(this::checkUserRoleAndNavigate, 2000);
    }

    private void checkUserRoleAndNavigate() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            Intent intent;

                            switch (role != null ? role : "student") {
                                case "teacher":
                                    intent = new Intent(this, TeacherDashboardActivity.class);
                                    break;
                                case "admin":
                                    intent = new Intent(this, AdminDashboardActivity.class);
                                    break;
                                default:
                                    intent = new Intent(this, StudentDashboardActivity.class);
                                    break;
                            }

                            startActivity(intent);
                            finish();
                        } else {
                            // User document không tồn tại, chuyển về login
                            navigateToLogin();
                        }
                    })
                    .addOnFailureListener(e -> navigateToLogin());
        } else {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
