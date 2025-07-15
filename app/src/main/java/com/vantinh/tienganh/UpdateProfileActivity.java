package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivAvatar;
    private EditText etFullName, etEmail, etPhone, etBio;
    private Spinner spinnerLevel, spinnerGoal;
    private TextView tvCurrentLevel, tvJoinDate, tvStudyStreak;
    private Button btnSave, btnCancel, btnChangePassword, btnChangeAvatar;
    private CardView cardPersonalInfo, cardStudyInfo, cardPreferences;
    private LinearLayout layoutAccountSettings;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        initViews();
        setupToolbar();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        // Avatar and basic info - add null checks
        ivAvatar = findViewById(R.id.iv_avatar);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etBio = findViewById(R.id.et_bio);

        // Study info - add null checks
        spinnerLevel = findViewById(R.id.spinner_level);
        spinnerGoal = findViewById(R.id.spinner_goal);
        tvCurrentLevel = findViewById(R.id.tv_current_level);
        tvJoinDate = findViewById(R.id.tv_join_date);
        tvStudyStreak = findViewById(R.id.tv_study_streak);

        // Buttons - add null checks
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);

        // Cards and layouts - add null checks
        cardPersonalInfo = findViewById(R.id.card_personal_info);
        cardStudyInfo = findViewById(R.id.card_study_info);
        cardPreferences = findViewById(R.id.card_preferences);
        layoutAccountSettings = findViewById(R.id.layout_account_settings);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Cập nhật hồ sơ");
        }
    }

    private void setupClickListeners() {
        // Add null checks for all click listeners
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> changePassword());
        }

        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v -> changeAvatar());
        }

        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> changeAvatar());
        }

        // Card clicks for expanding/collapsing sections
        if (cardPersonalInfo != null) {
            cardPersonalInfo.setOnClickListener(v -> toggleSection("personal"));
        }

        if (cardStudyInfo != null) {
            cardStudyInfo.setOnClickListener(v -> navigateToProgressSettings());
        }

        if (cardPreferences != null) {
            cardPreferences.setOnClickListener(v -> openPreferences());
        }
    }

    private void loadUserData() {
        if (userId != null) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Load personal information
                            String fullName = documentSnapshot.getString("fullName");
                            String email = documentSnapshot.getString("email");
                            String phone = documentSnapshot.getString("phone");
                            String bio = documentSnapshot.getString("bio");
                            String currentLevel = documentSnapshot.getString("currentLevel");

                            // Set data to views
                            if (etFullName != null && fullName != null) {
                                etFullName.setText(fullName);
                            }
                            if (etEmail != null && email != null) {
                                etEmail.setText(email);
                                etEmail.setEnabled(false); // Email usually not editable
                            }
                            if (etPhone != null && phone != null) {
                                etPhone.setText(phone);
                            }
                            if (etBio != null && bio != null) {
                                etBio.setText(bio);
                            }
                            if (tvCurrentLevel != null && currentLevel != null) {
                                tvCurrentLevel.setText("Trình độ hiện tại: " + currentLevel);
                            }

                            // Load additional study info
                            loadStudyStats();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi tải thông tin: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadStudyStats() {
        // Load study statistics
        if (tvJoinDate != null) {
            tvJoinDate.setText("Tham gia: 15/01/2025");
        }
        if (tvStudyStreak != null) {
            tvStudyStreak.setText("Chuỗi học: 15 ngày");
        }
    }

    private void saveProfile() {
        // Validate and save profile data
        String fullName = etFullName != null ? etFullName.getText().toString().trim() : "";
        String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
        String bio = etBio != null ? etBio.getText().toString().trim() : "";

        if (fullName.isEmpty()) {
            if (etFullName != null) {
                etFullName.setError("Vui lòng nhập họ tên");
                etFullName.requestFocus();
            }
            return;
        }

        // Update user data in Firestore
        if (userId != null) {
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("name", fullName);
            updates.put("phone", phone);
            updates.put("bio", bio);
            updates.put("updatedAt", com.google.firebase.Timestamp.now());

            db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void changePassword() {
        // Implementation for changing password
        Toast.makeText(this, "Chức năng đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
    }

    private void changeAvatar() {
        // Implementation for changing avatar
        Toast.makeText(this, "Chức năng đổi avatar đang phát triển", Toast.LENGTH_SHORT).show();
    }

    private void toggleSection(String section) {
        // Implementation for expanding/collapsing sections
        Toast.makeText(this, "Đang mở rộng phần " + section, Toast.LENGTH_SHORT).show();
    }

    private void navigateToProgressSettings() {
        Intent intent = new Intent(this, StudyProgressActivity.class);
        startActivity(intent);
    }

    private void openPreferences() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Tùy chọn")
                .setMessage("Chọn tùy chọn muốn thay đổi:")
                .setPositiveButton("Thông báo", (dialog, which) -> configureNotifications())
                .setNeutralButton("Ngôn ngữ", (dialog, which) -> changeLanguage())
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void configureNotifications() {
        Intent intent = new Intent(this, PersonalScheduleActivity.class);
        intent.putExtra("mode", "notifications");
        startActivity(intent);
    }

    private void changeLanguage() {
        Toast.makeText(this, "Chức năng thay đổi ngôn ngữ đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Show confirmation dialog if there are unsaved changes
            showExitConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExitConfirmation() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Thoát")
                .setMessage("Bạn có muốn lưu thay đổi trước khi thoát?")
                .setPositiveButton("Lưu", (dialog, which) -> saveProfile())
                .setNeutralButton("Không lưu", (dialog, which) -> {
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        showExitConfirmation();
    }
}