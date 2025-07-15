package com.vantinh.tienganh;

        import android.content.Intent;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Looper;

        import androidx.appcompat.app.AppCompatActivity;

        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;

        public class SplashActivity extends AppCompatActivity {

            private static final int SPLASH_DELAY = 2000; // 2 seconds
            private FirebaseAuth mAuth;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_splash);

                mAuth = FirebaseAuth.getInstance();

                // Delay for splash screen
                new Handler(Looper.getMainLooper()).postDelayed(this::checkUserAuthentication, SPLASH_DELAY);
            }

            private void checkUserAuthentication() {
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser != null) {
                    // User is signed in, redirect to main activity
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    // No user is signed in, redirect to login
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
        }