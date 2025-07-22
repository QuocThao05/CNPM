package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private BottomNavigationView bottomNavigation;
    private CardView cardVocabulary, cardGrammar, cardListening, cardSpeaking;
    private Button btnSearchCourses, btnMyCourses, btnStudyProgress, btnQuickQuiz;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupClickListeners();
        loadUserInfo();
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            tvWelcome = findViewById(R.id.tv_welcome);
            bottomNavigation = findViewById(R.id.bottom_navigation);

            // Cards
            cardVocabulary = findViewById(R.id.card_vocabulary);
            cardGrammar = findViewById(R.id.card_grammar);
            cardListening = findViewById(R.id.card_listening);
            cardSpeaking = findViewById(R.id.card_speaking);

            // Buttons
            btnSearchCourses = findViewById(R.id.btn_search_courses);
            btnMyCourses = findViewById(R.id.btn_my_courses);
            btnStudyProgress = findViewById(R.id.btn_study_progress);
            btnQuickQuiz = findViewById(R.id.btn_quick_quiz);

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bảng điều khiển học viên");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_courses) {
                startActivity(new Intent(this, StudentCourseSearchActivity.class));
                return true;
            } else if (itemId == R.id.nav_progress) {
                startActivity(new Intent(this, StudyProgressActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, UpdateProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        // Quick action cards
        cardVocabulary.setOnClickListener(v -> {
            // Navigate to vocabulary practice
            Toast.makeText(this, "Chức năng luyện từ vựng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        cardGrammar.setOnClickListener(v -> {
            // Navigate to grammar practice
            Toast.makeText(this, "Chức năng luyện ngữ pháp đang phát triển", Toast.LENGTH_SHORT).show();
        });

        cardListening.setOnClickListener(v -> {
            // Navigate to listening practice
            Toast.makeText(this, "Chức năng luyện nghe đang phát triển", Toast.LENGTH_SHORT).show();
        });

        cardSpeaking.setOnClickListener(v -> {
            // Navigate to speaking practice
            Toast.makeText(this, "Chức năng luyện nói đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Course buttons
        btnSearchCourses.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentCourseSearchActivity.class));
        });

        btnMyCourses.setOnClickListener(v -> {
            // Navigate to enrolled courses
            startActivity(new Intent(this, StudentMyCoursesActivity.class));
        });

        // Progress buttons
        btnStudyProgress.setOnClickListener(v -> {
            startActivity(new Intent(this, StudyProgressActivity.class));
        });

        btnQuickQuiz.setOnClickListener(v -> {
            startActivity(new Intent(this, QuizActivity.class));
        });
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                tvWelcome.setText("Chào mừng, " + name + "!");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
    }
}
