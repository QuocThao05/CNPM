package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();

        // Removed pre-fill test account for production use
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        tvRegister.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Long click to create test account
        btnLogin.setOnLongClickListener(v -> {
            showCreateTestAccountDialog();
            return true;
        });
    }

    private void handleLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (validateInput(email, password)) {
            btnLogin.setEnabled(false);
            btnLogin.setText("Đang đăng nhập...");

            android.util.Log.d("LoginActivity", "Attempting to login with email: " + email);

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    android.util.Log.d("LoginActivity", "Login task completed. Success: " + task.isSuccessful());

                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String userId = mAuth.getCurrentUser().getUid();
                        android.util.Log.d("LoginActivity", "Login successful for user: " + userId);
                        getUserRoleAndRedirect(userId);
                    } else {
                        handleLoginError(task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("LoginActivity", "Login failure", e);
                    handleLoginError(e);
                });
        }
    }

    private void handleLoginError(Exception exception) {
        btnLogin.setEnabled(true);
        btnLogin.setText("Đăng nhập");

        String errorMessage = "Đăng nhập thất bại";

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "Tài khoản không tồn tại. Bạn có muốn tạo tài khoản này không?";
            showCreateAccountDialog();
            return;
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Email hoặc mật khẩu không đúng";
        } else if (exception instanceof com.google.firebase.FirebaseNetworkException) {
            // Xử lý lỗi mạng cụ thể
            errorMessage = "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối Internet và thử lại.";
            showNetworkErrorDialog();
            return;
        } else if (exception instanceof com.google.firebase.auth.FirebaseAuthException) {
            // Xử lý các lỗi Firebase khác
            errorMessage = "Lỗi xác thực: " + exception.getMessage();
        } else if (exception != null) {
            errorMessage = "Lỗi đăng nhập: " + exception.getMessage();
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showCreateAccountDialog() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        new AlertDialog.Builder(this)
                .setTitle("Tạo tài khoản")
                .setMessage("Tài khoản " + email + " chưa tồn tại. Bạn có muốn tạo tài khoản này không?")
                .setPositiveButton("Tạo tài khoản", (dialog, which) -> {
                    createAccount(email, password, "student"); // Default to student
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showCreateTestAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tạo tài khoản test")
                .setMessage("Chọn loại tài khoản test muốn tạo:")
                .setPositiveButton("Học viên", (dialog, which) -> {
                    etEmail.setText("student@test.com");
                    etPassword.setText("123456");
                    createAccount("student@test.com", "123456", "student");
                })
                .setNeutralButton("Giáo viên", (dialog, which) -> {
                    etEmail.setText("teacher@test.com");
                    etPassword.setText("123456");
                    createAccount("teacher@test.com", "123456", "teacher");
                })
                .setNegativeButton("Admin", (dialog, which) -> {
                    etEmail.setText("admin@test.com");
                    etPassword.setText("123456");
                    createAccount("admin@test.com", "123456", "admin");
                })
                .show();
    }

    private void createAccount(String email, String password, String role) {
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang tạo tài khoản...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(userId, email, role);
                    } else {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Đăng nhập");
                        String error = task.getException() != null ? task.getException().getMessage() : "Lỗi tạo tài khoản";
                        Toast.makeText(this, "Tạo tài khoản thất bại: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String email, String role) {
        // Tạo đối tượng User với chỉ 5 trường dữ liệu theo yêu cầu: ID, address, email, fullName, role
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", userId);
        userData.put("email", email);
        userData.put("fullName", getFullNameFromRole(role)); // Sử dụng fullName thay vì name
        userData.put("address", ""); // Địa chỉ mặc định trống
        userData.put("role", role);

        // Không lưu createdAt, updatedAt để chỉ có đúng 5 trường theo yêu cầu

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                    Toast.makeText(this, "Tạo tài khoản thành công! Đang đăng nhập...", Toast.LENGTH_SHORT).show();
                    android.util.Log.d("LoginActivity", "User saved with 5 fields: " + userData.toString());
                    redirectToRoleActivity(role);
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                    android.util.Log.e("LoginActivity", "Error saving user to Firestore", e);
                    Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String getFullNameFromRole(String role) {
        switch (role.toLowerCase()) {
            case "admin": return "Quản trị viên";
            case "teacher": return "Giáo viên";
            case "student": return "Học viên";
            default: return "Người dùng";
        }
    }

    private void getUserRoleAndRedirect(String userId) {
        android.util.Log.d("LoginActivity", "Getting user role for: " + userId);

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");

                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        android.util.Log.d("LoginActivity", "User role found: " + role);

                        if (role != null) {
                            redirectToRoleActivity(role);
                        } else {
                            android.util.Log.w("LoginActivity", "User role is null, defaulting to student");
                            redirectToRoleActivity("student");
                        }
                    } else {
                        android.util.Log.w("LoginActivity", "User document does not exist, creating default student profile");
                        // Create user document if it doesn't exist
                        saveUserToFirestore(userId, mAuth.getCurrentUser().getEmail(), "student");
                    }
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                    android.util.Log.e("LoginActivity", "Error getting user role", e);
                    Toast.makeText(this, "Lỗi lấy thông tin người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void redirectToRoleActivity(String role) {
        android.util.Log.d("LoginActivity", "Redirecting to activity for role: " + role);

        Intent intent;
        switch (role.toLowerCase()) {
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                android.util.Log.d("LoginActivity", "Redirecting to AdminDashboardActivity");
                break;
            case "teacher":
                intent = new Intent(this, TeacherDashboardActivity.class);
                android.util.Log.d("LoginActivity", "Redirecting to TeacherDashboardActivity");
                break;
            case "student":
            default:
                intent = new Intent(this, StudentDashboardActivity.class);
                android.util.Log.d("LoginActivity", "Redirecting to StudentDashboardActivity");
                break;
        }

        // Clear the activity stack so user can't go back to login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showNetworkErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi kết nối")
                .setMessage("Không thể kết nối đến server. Vui lòng kiểm tra:\n\n" +
                           "• Kết nối Internet\n" +
                           "• Tắt VPN nếu đang sử dụng\n" +
                           "• Thử chuyển từ WiFi sang 4G hoặc ngược lại")
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    // Retry login with current credentials
                    handleLogin();
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Cài đặt mạng", (dialog, which) -> {
                    // Mở cài đặt WiFi
                    try {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                    } catch (Exception e) {
                        Toast.makeText(this, "Không thể mở cài đặt", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void retryFirebaseConnection() {
        // Reset Firebase Auth instance
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Clear any cached auth state
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }

        Toast.makeText(this, "Đã reset kết nối Firebase, vui lòng thử lại", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset button state khi quay lại activity
        btnLogin.setEnabled(true);
        btnLogin.setText("Đăng nhập");
    }
}
