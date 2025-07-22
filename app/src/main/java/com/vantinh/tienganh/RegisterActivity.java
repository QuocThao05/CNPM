package com.vantinh.tienganh;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilEmail, tilAddress, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etAddress, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private Spinner spRole;
    private CardView headerCard, formCard;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRoleSpinner();
        setupClickListeners();
        startEntranceAnimation();
    }

    private void initViews() {
        tilFullName = findViewById(R.id.til_full_name);
        tilEmail = findViewById(R.id.til_email);
        tilAddress = findViewById(R.id.til_address);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etAddress = findViewById(R.id.et_address);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        btnRegister = findViewById(R.id.btn_register);
        spRole = findViewById(R.id.sp_role);
        headerCard = findViewById(R.id.header_card);
        formCard = findViewById(R.id.form_card);
    }

    private void setupRoleSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            animateButtonClick(v);
            handleRegister();
        });

        findViewById(R.id.tv_login).setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String selectedRole = spRole.getSelectedItem().toString();

        if (validateInput(fullName, email, address, password, confirmPassword)) {
            animateLoading(true);
            createUserAccount(fullName, email, address, password, selectedRole);
        }
    }

    private void createUserAccount(String fullName, String email, String address, String password, String role) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    animateLoading(false);
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserToDatabase(userId, fullName, email, address, role);
                    } else {
                        Toast.makeText(RegisterActivity.this,
                            "Đăng ký thất bại: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String fullName, String email, String address, String role) {
        // Tạo dữ liệu user với chỉ 5 trường theo yêu cầu: ID, address, email, fullName, role
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", userId);
        userData.put("email", email);
        userData.put("fullName", fullName);
        userData.put("address", address); // Địa chỉ từ input
        userData.put("role", mapRoleFromSpinner(role)); // Chuyển đổi role từ spinner

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("RegisterActivity", "User registered with 5 fields: " + userData.toString());
                    Toast.makeText(RegisterActivity.this,
                        "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    redirectToAppropriateActivity(mapRoleFromSpinner(role));
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("RegisterActivity", "Error saving user to Firestore", e);
                    Toast.makeText(RegisterActivity.this,
                        "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Phương thức chuyển đổi role từ spinner thành format chuẩn
    private String mapRoleFromSpinner(String spinnerRole) {
        switch (spinnerRole) {
            case "Giáo viên":
                return "teacher";
            case "Quản trị viên":
                return "admin";
            case "Học viên":
            default:
                return "student";
        }
    }

    private void redirectToAppropriateActivity(String role) {
        Intent intent;
        switch (role.toLowerCase()) {
            case "teacher":
                intent = new Intent(RegisterActivity.this, TeacherDashboardActivity.class);
                break;
            case "admin":
                intent = new Intent(RegisterActivity.this, AdminDashboardActivity.class);
                break;
            case "student":
            default:
                intent = new Intent(RegisterActivity.this, StudentDashboardActivity.class);
                break;
        }
        intent.putExtra("userRole", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateInput(String fullName, String email, String address, String password, String confirmPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Vui lòng nhập họ tên");
            animateError(tilFullName);
            isValid = false;
        } else {
            tilFullName.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            animateError(tilEmail);
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(address)) {
            tilAddress.setError("Vui lòng nhập địa chỉ");
            animateError(tilAddress);
            isValid = false;
        } else {
            tilAddress.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            animateError(tilPassword);
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            animateError(tilPassword);
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu không khớp");
            animateError(tilConfirmPassword);
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        return isValid;
    }

    // Animation methods (same as before)
    private void startEntranceAnimation() {
        headerCard.setAlpha(0f);
        headerCard.setTranslationY(-100f);
        formCard.setAlpha(0f);
        formCard.setTranslationY(100f);
        btnRegister.setAlpha(0f);

        ObjectAnimator headerAlpha = ObjectAnimator.ofFloat(headerCard, "alpha", 0f, 1f);
        ObjectAnimator headerTranslation = ObjectAnimator.ofFloat(headerCard, "translationY", -100f, 0f);
        ObjectAnimator formAlpha = ObjectAnimator.ofFloat(formCard, "alpha", 0f, 1f);
        ObjectAnimator formTranslation = ObjectAnimator.ofFloat(formCard, "translationY", 100f, 0f);
        ObjectAnimator btnAlpha = ObjectAnimator.ofFloat(btnRegister, "alpha", 0f, 1f);

        AnimatorSet headerSet = new AnimatorSet();
        headerSet.playTogether(headerAlpha, headerTranslation);
        headerSet.setDuration(600);
        headerSet.setInterpolator(new OvershootInterpolator());

        AnimatorSet formSet = new AnimatorSet();
        formSet.playTogether(formAlpha, formTranslation);
        formSet.setDuration(600);
        formSet.setStartDelay(200);
        formSet.setInterpolator(new OvershootInterpolator());

        btnAlpha.setDuration(400);
        btnAlpha.setStartDelay(400);

        AnimatorSet masterSet = new AnimatorSet();
        masterSet.playTogether(headerSet, formSet, btnAlpha);
        masterSet.start();
    }

    private void animateButtonClick(View view) {
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);

        AnimatorSet scaleDownSet = new AnimatorSet();
        scaleDownSet.playTogether(scaleDown, scaleDownY);
        scaleDownSet.setDuration(100);

        AnimatorSet scaleUpSet = new AnimatorSet();
        scaleUpSet.playTogether(scaleUp, scaleUpY);
        scaleUpSet.setDuration(100);

        AnimatorSet completeSet = new AnimatorSet();
        completeSet.playSequentially(scaleDownSet, scaleUpSet);
        completeSet.start();
    }

    private void animateLoading(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        btnRegister.setText(isLoading ? "Đang xử lý..." : "Tạo tài khoản");
    }

    private void animateError(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX",
            0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        shake.setDuration(600);
        shake.start();
    }
}