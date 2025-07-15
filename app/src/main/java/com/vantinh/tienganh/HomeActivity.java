package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.annotation.Nullable;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvUserRole;
    private Button btnCourses, btnSchedule, btnProgress, btnQuiz;
    private Button btnProfile, btnFeedback;
    
    // Admin/Teacher specific buttons
    private Button btnManageUsers, btnCreateContent, btnStatistics;

    private String userRole = "Student"; // Default role, should be loaded from preferences/database
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Activity Result Launcher để thay thế startActivityForResult
    private ActivityResultLauncher<Intent> updateProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Khởi tạo ActivityResultLauncher
        updateProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Kiểm tra kết quả trả về từ UpdateProfileActivity
                    if (result.getResultCode() == RESULT_OK) {
                        // Nếu thành công, tải lại thông tin người dùng
                        Toast.makeText(this, "Refreshing profile...", Toast.LENGTH_SHORT).show();
                        loadUserInfo();
                    }
                });

        initViews();
        setupClickListeners();
        loadUserInfo();
        setupClickListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        tvUserRole = findViewById(R.id.tv_user_role);
        btnCourses = findViewById(R.id.btn_courses);
        btnSchedule = findViewById(R.id.btn_schedule);
        btnProgress = findViewById(R.id.btn_progress);
        btnQuiz = findViewById(R.id.btn_quiz);
        btnProfile = findViewById(R.id.btn_profile);
        btnFeedback = findViewById(R.id.btn_feedback);
        
        // Admin/Teacher buttons
        btnManageUsers = findViewById(R.id.btn_manage_users);
        btnCreateContent = findViewById(R.id.btn_create_content);
        btnStatistics = findViewById(R.id.btn_statistics);
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Lấy dữ liệu từ document
                        String fullName = document.getString("fullName");
                        userRole = document.getString("role"); // Cập nhật biến userRole của class

                        // Cập nhật giao diện
                        tvWelcome.setText("Welcome, " + fullName + "!");
                        tvUserRole.setText("Role: " + userRole);

                        // Gọi lại hàm để cập nhật các nút bấm dựa trên vai trò thật
                        setupRoleBasedUI();
                        // Gọi lại hàm để đảm bảo các nút bấm hoạt động
                        setupClickListeners();

                    } else {
                        // Document không tồn tại, có thể là lỗi
                        Toast.makeText(HomeActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                        handleLogout(); // Đăng xuất người dùng
                    }
                } else {
                    // Lỗi khi truy cập Firestore
                    Toast.makeText(HomeActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Không có người dùng nào đăng nhập, quay về màn hình Login
            handleLogout();
        }
    }

    private void setupClickListeners() {
        btnCourses.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CourseListActivity.class);
            startActivity(intent);
        });

        btnSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PersonalScheduleActivity.class);
            startActivity(intent);
        });

        btnProgress.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StudyProgressActivity.class);
            startActivity(intent);
        });

        btnQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, QuizActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UpdateProfileActivity.class);
            // Sử dụng ActivityResultLauncher thay vì startActivityForResult
            updateProfileLauncher.launch(intent);
        });

        btnFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });

        // Admin/Teacher specific buttons
        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, AdminAccountManagementActivity.class);
                startActivity(intent);
            });
        }

        if (btnCreateContent != null) {
            btnCreateContent.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ContentCreationActivity.class);
                startActivity(intent);
            });
        }

        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SystemStatisticsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupRoleBasedUI() {
        // Hide/show buttons based on user role
        switch (userRole) {
            case "Student":
                // Hide admin/teacher buttons
                if (btnManageUsers != null) btnManageUsers.setVisibility(android.view.View.GONE);
                if (btnCreateContent != null) btnCreateContent.setVisibility(android.view.View.GONE);
                if (btnStatistics != null) btnStatistics.setVisibility(android.view.View.GONE);
                break;
                
            case "Teacher":
                // Show content creation, hide admin buttons
                if (btnManageUsers != null) btnManageUsers.setVisibility(android.view.View.GONE);
                if (btnCreateContent != null) btnCreateContent.setVisibility(android.view.View.VISIBLE);
                if (btnStatistics != null) btnStatistics.setVisibility(android.view.View.GONE);
                break;
                
            case "Admin":
                // Show all buttons
                if (btnManageUsers != null) btnManageUsers.setVisibility(android.view.View.VISIBLE);
                if (btnCreateContent != null) btnCreateContent.setVisibility(android.view.View.VISIBLE);
                if (btnStatistics != null) btnStatistics.setVisibility(android.view.View.VISIBLE);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            handleLogout();
            return true;
        } else if (id == R.id.action_settings) {
            // TODO: Open settings activity
            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_about) {
            // TODO: Show about dialog
            Toast.makeText(this, "English Learning App v1.0", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void handleLogout() {
        // 1. Đăng xuất khỏi tài khoản Firebase
        mAuth.signOut();

        // 2. Hiển thị thông báo (tùy chọn)
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // 3. Tạo một Intent để mở lại LoginActivity
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);

        // 4. Xóa hết các màn hình cũ, đảm bảo người dùng không thể "Back" lại
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 5. Bắt đầu màn hình Login và đóng màn hình Home
        startActivity(intent);
        finish();
    }

}