package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminAccountManagementActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private SearchView searchView;
    private LinearLayout layoutUsers, layoutTeachers, layoutStudents, layoutCreateUser;

    // User management
    private RecyclerView rvAllUsers, rvTeachers, rvStudents;
    private TextView tvTotalUsers, tvActiveUsers, tvInactiveUsers;
    private Button btnExportUsers, btnBulkAction, btnUserStatistics;
    private CardView cardUserStats, cardAccountActions, cardUserFilters;

    // Create user
    private EditText etNewUserName, etNewUserEmail, etNewUserPassword;
    private Spinner spinnerNewUserRole;
    private Button btnCreateUser, btnCancelCreate;

    // User actions
    private FloatingActionButton fabAddUser;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_account_management);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            adminId = mAuth.getCurrentUser().getUid();
        }

        initViews();
        setupToolbar();
        setupTabs();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        searchView = findViewById(R.id.search_view);

        // Layouts
        layoutUsers = findViewById(R.id.layout_users);
        layoutTeachers = findViewById(R.id.layout_teachers);
        layoutStudents = findViewById(R.id.layout_students);
        layoutCreateUser = findViewById(R.id.layout_create_user);

        // User management
        rvAllUsers = findViewById(R.id.rv_all_users);
        rvTeachers = findViewById(R.id.rv_teachers);
        rvStudents = findViewById(R.id.rv_students);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvActiveUsers = findViewById(R.id.tv_active_users);
        tvInactiveUsers = findViewById(R.id.tv_inactive_users);
        btnExportUsers = findViewById(R.id.btn_export_users);
        btnBulkAction = findViewById(R.id.btn_bulk_action);
        btnUserStatistics = findViewById(R.id.btn_user_statistics);
        cardUserStats = findViewById(R.id.card_user_stats);
        cardAccountActions = findViewById(R.id.card_account_actions);
        cardUserFilters = findViewById(R.id.card_user_filters);

        // Create user
        etNewUserName = findViewById(R.id.et_new_user_name);
        etNewUserEmail = findViewById(R.id.et_new_user_email);
        etNewUserPassword = findViewById(R.id.et_new_user_password);
        spinnerNewUserRole = findViewById(R.id.spinner_new_user_role);
        btnCreateUser = findViewById(R.id.btn_create_user);
        btnCancelCreate = findViewById(R.id.btn_cancel_create);

        // FAB
        fabAddUser = findViewById(R.id.fab_add_user);

        // Setup RecyclerViews
        setupRecyclerViews();

        // Setup search
        setupSearchView();
    }

    private void setupRecyclerViews() {
        if (rvAllUsers != null) {
            rvAllUsers.setLayoutManager(new LinearLayoutManager(this));
        }
        if (rvTeachers != null) {
            rvTeachers.setLayoutManager(new LinearLayoutManager(this));
        }
        if (rvStudents != null) {
            rvStudents.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void setupSearchView() {
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchUsers(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.length() > 2) {
                        searchUsers(newText);
                    }
                    return true;
                }
            });
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Quản lý tài khoản");
        }
    }

    private void setupTabs() {
        if (tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
            tabLayout.addTab(tabLayout.newTab().setText("Giáo viên"));
            tabLayout.addTab(tabLayout.newTab().setText("Học viên"));
            tabLayout.addTab(tabLayout.newTab().setText("Tạo mới"));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switchTab(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void setupClickListeners() {
        // User management buttons
        if (btnExportUsers != null) {
            btnExportUsers.setOnClickListener(v -> exportUserData());
        }
        if (btnBulkAction != null) {
            btnBulkAction.setOnClickListener(v -> showBulkActionMenu());
        }
        if (btnUserStatistics != null) {
            btnUserStatistics.setOnClickListener(v -> showUserStatistics());
        }

        // Create user buttons
        if (btnCreateUser != null) {
            btnCreateUser.setOnClickListener(v -> createNewUser());
        }
        if (btnCancelCreate != null) {
            btnCancelCreate.setOnClickListener(v -> clearCreateUserForm());
        }

        // Card clicks
        if (cardUserStats != null) {
            cardUserStats.setOnClickListener(v -> showDetailedStats());
        }
        if (cardAccountActions != null) {
            cardAccountActions.setOnClickListener(v -> showAccountActionsMenu());
        }
        if (cardUserFilters != null) {
            cardUserFilters.setOnClickListener(v -> showFilterOptions());
        }

        // FAB
        if (fabAddUser != null) {
            fabAddUser.setOnClickListener(v -> {
                if (tabLayout != null && tabLayout.getTabCount() > 3) {
                    tabLayout.getTabAt(3).select();
                    switchTab(3);
                }
            });
        }
    }

    private void switchTab(int position) {
        hideAllLayouts();

        switch (position) {
            case 0: // All users
                if (layoutUsers != null) {
                    layoutUsers.setVisibility(View.VISIBLE);
                    loadAllUsers();
                }
                break;
            case 1: // Teachers
                if (layoutTeachers != null) {
                    layoutTeachers.setVisibility(View.VISIBLE);
                    loadTeachers();
                }
                break;
            case 2: // Students
                if (layoutStudents != null) {
                    layoutStudents.setVisibility(View.VISIBLE);
                    loadStudents();
                }
                break;
            case 3: // Create new
                if (layoutCreateUser != null) {
                    layoutCreateUser.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void hideAllLayouts() {
        if (layoutUsers != null) layoutUsers.setVisibility(View.GONE);
        if (layoutTeachers != null) layoutTeachers.setVisibility(View.GONE);
        if (layoutStudents != null) layoutStudents.setVisibility(View.GONE);
        if (layoutCreateUser != null) layoutCreateUser.setVisibility(View.GONE);
    }

    private void loadUserData() {
        loadUserStatistics();
        loadAllUsers();
    }

    private void loadUserStatistics() {
        // Sample data - replace with actual Firebase queries
        if (tvTotalUsers != null) {
            tvTotalUsers.setText("1,234 người dùng");
        }
        if (tvActiveUsers != null) {
            tvActiveUsers.setText("1,156 đang hoạt động");
        }
        if (tvInactiveUsers != null) {
            tvInactiveUsers.setText("78 không hoạt động");
        }
    }

    private void loadAllUsers() {
        // Implementation to load all users from Firestore
        Toast.makeText(this, "Đang tải danh sách người dùng...", Toast.LENGTH_SHORT).show();
    }

    private void loadTeachers() {
        // Implementation to load teachers from Firestore
        Toast.makeText(this, "Đang tải danh sách giáo viên...", Toast.LENGTH_SHORT).show();
    }

    private void loadStudents() {
        // Implementation to load students from Firestore
        Toast.makeText(this, "Đang tải danh sách học viên...", Toast.LENGTH_SHORT).show();
    }

    private void searchUsers(String query) {
        Toast.makeText(this, "Tìm kiếm: " + query, Toast.LENGTH_SHORT).show();
        // Implementation for user search
    }

    private void exportUserData() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Xuất dữ liệu người dùng")
                .setMessage("Chọn định dạng và phạm vi xuất:")
                .setPositiveButton("Excel - Tất cả", (dialog, which) -> {
                    Toast.makeText(this, "Đang xuất toàn bộ dữ liệu ra Excel...", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("PDF - Thống kê", (dialog, which) -> {
                    Toast.makeText(this, "Đang xuất thống kê ra PDF...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showBulkActionMenu() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Thao tác hàng loạt")
                .setMessage("Chọn thao tác muốn thực hiện với nhiều người dùng:")
                .setPositiveButton("Gửi email", (dialog, which) -> {
                    Toast.makeText(this, "Chuẩn bị gửi email hàng loạt...", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Thay đổi trạng thái", (dialog, which) -> {
                    showStatusChangeDialog();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showStatusChangeDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Thay đổi trạng thái")
                .setMessage("Chọn trạng thái mới:")
                .setPositiveButton("Kích hoạt", (dialog, which) -> {
                    Toast.makeText(this, "Đã kích hoạt các tài khoản được chọn", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Vô hiệu hóa", (dialog, which) -> {
                    Toast.makeText(this, "Đã vô hiệu hóa các tài khoản được chọn", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showUserStatistics() {
        Intent intent = new Intent(this, SystemStatisticsActivity.class);
        intent.putExtra("mode", "user_stats");
        intent.putExtra("title", "Thống kê người dùng");
        startActivity(intent);
    }

    private void createNewUser() {
        String name = etNewUserName != null ? etNewUserName.getText().toString().trim() : "";
        String email = etNewUserEmail != null ? etNewUserEmail.getText().toString().trim() : "";
        String password = etNewUserPassword != null ? etNewUserPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable create button
        if (btnCreateUser != null) {
            btnCreateUser.setEnabled(false);
            btnCreateUser.setText("Đang tạo...");
        }

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save user info to Firestore
                        String userId = task.getResult().getUser().getUid();
                        saveUserToFirestore(userId, name, email);
                    } else {
                        Toast.makeText(this, "Lỗi tạo tài khoản: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        // Re-enable create button
                        if (btnCreateUser != null) {
                            btnCreateUser.setEnabled(true);
                            btnCreateUser.setText("Tạo tài khoản");
                        }
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String email) {
        String role = "Học viên"; // Default role
        if (spinnerNewUserRole != null) {
            role = spinnerNewUserRole.getSelectedItem().toString();
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", name);
        userData.put("email", email);
        userData.put("role", role);
        userData.put("createdBy", adminId);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("isActive", true);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    clearCreateUserForm();
                    loadUserData(); // Refresh user list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    // Re-enable create button
                    if (btnCreateUser != null) {
                        btnCreateUser.setEnabled(true);
                        btnCreateUser.setText("Tạo tài khoản");
                    }
                });
    }

    private void clearCreateUserForm() {
        if (etNewUserName != null) etNewUserName.setText("");
        if (etNewUserEmail != null) etNewUserEmail.setText("");
        if (etNewUserPassword != null) etNewUserPassword.setText("");
        if (spinnerNewUserRole != null) spinnerNewUserRole.setSelection(0);
    }

    private void showDetailedStats() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Thống kê chi tiết")
                .setMessage("Phân tích người dùng:\n\n" +
                        "📊 Tổng quan:\n" +
                        "• Tổng số: 1,234 người dùng\n" +
                        "• Hoạt động: 1,156 (93.7%)\n" +
                        "• Không hoạt động: 78 (6.3%)\n\n" +
                        "👥 Phân loại:\n" +
                        "• Học viên: 1,178 (95.5%)\n" +
                        "• Giáo viên: 56 (4.5%)\n" +
                        "• Admin: 3 (0.2%)")
                .setPositiveButton("Xem báo cáo đầy đủ", (dialog, which) -> {
                    Intent intent = new Intent(this, SystemStatisticsActivity.class);
                    intent.putExtra("mode", "detailed_user_stats");
                    startActivity(intent);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showAccountActionsMenu() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Thao tác tài khoản")
                .setMessage("Chọn thao tác muốn thực hiện:")
                .setPositiveButton("Reset mật khẩu", (dialog, which) -> {
                    showPasswordResetDialog();
                })
                .setNeutralButton("Khóa/Mở khóa", (dialog, which) -> {
                    showLockUnlockDialog();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showPasswordResetDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reset mật khẩu")
                .setMessage("Chọn cách thức reset mật khẩu:")
                .setPositiveButton("Gửi email reset", (dialog, which) -> {
                    Toast.makeText(this, "Đã gửi email reset mật khẩu", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Tạo mật khẩu tạm", (dialog, which) -> {
                    Toast.makeText(this, "Đã tạo mật khẩu tạm thời", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLockUnlockDialog() {
        Toast.makeText(this, "Chức năng khóa/mở khóa tài khoản", Toast.LENGTH_SHORT).show();
    }

    private void showFilterOptions() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Bộ lọc")
                .setMessage("Lọc người dùng theo:")
                .setPositiveButton("Theo vai trò", (dialog, which) -> {
                    filterByRole();
                })
                .setNeutralButton("Theo trạng thái", (dialog, which) -> {
                    filterByStatus();
                })
                .setNegativeButton("Đặt lại", (dialog, which) -> {
                    resetFilters();
                })
                .show();
    }

    private void filterByRole() {
        Toast.makeText(this, "Lọc theo vai trò", Toast.LENGTH_SHORT).show();
    }

    private void filterByStatus() {
        Toast.makeText(this, "Lọc theo trạng thái", Toast.LENGTH_SHORT).show();
    }

    private void resetFilters() {
        Toast.makeText(this, "Đã đặt lại bộ lọc", Toast.LENGTH_SHORT).show();
        loadAllUsers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}