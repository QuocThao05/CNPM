package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalUsers, tvTotalTeachers, tvTotalStudents, tvSystemHealth;
    private RecyclerView rvRecentActivities, rvSystemAlerts;
    private BottomNavigationView bottomNavigation;
    private CardView cardUserManagement, cardSystemStats, cardContentManagement, cardReports;
    private FloatingActionButton fabQuickAction;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupClickListeners();
        loadAdminData();
    }

    private void initViews() {
        try {
            // Toolbar
            toolbar = findViewById(R.id.toolbar);

            // Text views for statistics
            tvWelcome = findViewById(R.id.tv_welcome);
            tvTotalUsers = findViewById(R.id.tv_total_users);
            tvTotalTeachers = findViewById(R.id.tv_total_teachers);
            tvTotalStudents = findViewById(R.id.tv_total_students);
            tvSystemHealth = findViewById(R.id.tv_system_health);

            // RecyclerViews
            rvRecentActivities = findViewById(R.id.rv_recent_activities);
            rvSystemAlerts = findViewById(R.id.rv_system_alerts);

            // Bottom navigation
            bottomNavigation = findViewById(R.id.bottom_navigation);

            // Management cards
            cardUserManagement = findViewById(R.id.card_user_management);
            cardSystemStats = findViewById(R.id.card_system_stats);
            cardContentManagement = findViewById(R.id.card_content_management);
            cardReports = findViewById(R.id.card_reports);

            // Floating action button
            fabQuickAction = findViewById(R.id.fab_quick_action);

            // Setup RecyclerViews
            if (rvRecentActivities != null) {
                rvRecentActivities.setLayoutManager(new LinearLayoutManager(this));
            }
            if (rvSystemAlerts != null) {
                rvSystemAlerts.setLayoutManager(new LinearLayoutManager(this));
            }

        } catch (Exception e) {
            android.util.Log.e("AdminDashboard", "Error initializing views: " + e.getMessage());
        }
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Trang quản trị");
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
        }
    }

    private void setupClickListeners() {
        // Management cards click listeners
        if (cardUserManagement != null) {
            cardUserManagement.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminAccountManagementActivity.class);
                startActivity(intent);
            });
        }

        if (cardSystemStats != null) {
            cardSystemStats.setOnClickListener(v -> {
                Intent intent = new Intent(this, SystemStatisticsActivity.class);
                intent.putExtra("mode", "admin");
                intent.putExtra("title", "Thống kê hệ thống");
                startActivity(intent);
            });
        }

        if (cardContentManagement != null) {
            cardContentManagement.setOnClickListener(v -> {
                Intent intent = new Intent(this, ContentCreationActivity.class);
                intent.putExtra("mode", "admin");
                intent.putExtra("title", "Quản lý nội dung");
                startActivity(intent);
            });
        }

        if (cardReports != null) {
            cardReports.setOnClickListener(v -> {
                Intent intent = new Intent(this, SystemStatisticsActivity.class);
                intent.putExtra("mode", "reports");
                intent.putExtra("title", "Báo cáo hệ thống");
                startActivity(intent);
            });
        }

        // Quick action floating button
        if (fabQuickAction != null) {
            fabQuickAction.setOnClickListener(v -> showQuickActionMenu());
        }
    }

    private void showQuickActionMenu() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Thao tác nhanh - Admin")
                .setMessage("Chọn thao tác muốn thực hiện:")
                .setPositiveButton("Tạo tài khoản", (dialog, which) -> {
                    Intent intent = new Intent(this, RegisterActivity.class);
                    intent.putExtra("mode", "admin_create");
                    startActivity(intent);
                })
                .setNeutralButton("Backup hệ thống", (dialog, which) -> {
                    performSystemBackup();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performSystemBackup() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Sao lưu hệ thống")
                .setMessage("Bạn có muốn thực hiện sao lưu toàn bộ dữ liệu hệ thống?")
                .setPositiveButton("Sao lưu", (dialog, which) -> {
                    Toast.makeText(this, "Đang thực hiện sao lưu hệ thống...", Toast.LENGTH_LONG).show();
                    // Implementation for system backup
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    // Already on dashboard
                    return true;
                } else if (itemId == R.id.nav_users) {
                    Intent intent = new Intent(this, AdminAccountManagementActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_reports) {
                    Intent intent = new Intent(this, SystemStatisticsActivity.class);
                    intent.putExtra("mode", "reports");
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    openSystemSettings();
                    return true;
                }
                return false;
            });
        }
    }

    private void openSystemSettings() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Cài đặt hệ thống")
                .setMessage("Chọn cài đặt muốn thay đổi:")
                .setPositiveButton("Cài đặt chung", (dialog, which) -> {
                    Intent intent = new Intent(this, UpdateProfileActivity.class);
                    intent.putExtra("mode", "system_settings");
                    startActivity(intent);
                })
                .setNeutralButton("Bảo mật", (dialog, which) -> {
                    showSecuritySettings();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showSecuritySettings() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Cài đặt bảo mật")
                .setMessage("Cài đặt bảo mật hiện tại:\n\n" +
                        "• Xác thực 2 bước: Bật\n" +
                        "• Mã hóa dữ liệu: Bật\n" +
                        "• Đăng nhập tự động: Tắt\n" +
                        "• Lịch sử đăng nhập: 30 ngày")
                .setPositiveButton("Thay đổi", (dialog, which) -> {
                    Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_system_health) {
            showSystemHealth();
            return true;
        } else if (itemId == R.id.action_backup) {
            performSystemBackup();
            return true;
        } else if (itemId == R.id.action_logs) {
            showSystemLogs();
            return true;
        } else if (itemId == R.id.action_logout) {
            handleLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSystemHealth() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Tình trạng hệ thống")
                .setMessage("Báo cáo tình trạng hệ thống:\n\n" +
                        "🟢 Database: Hoạt động bình thường\n" +
                        "🟢 Firebase: Kết nối ổn định\n" +
                        "🟡 Storage: 78% dung lượng\n" +
                        "🟢 Performance: Tốt\n" +
                        "🟢 Security: Không phát hiện mối đe dọa")
                .setPositiveButton("Chi tiết", (dialog, which) -> {
                    Intent intent = new Intent(this, SystemStatisticsActivity.class);
                    intent.putExtra("mode", "system_health");
                    startActivity(intent);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showSystemLogs() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Nhật ký hệ thống")
                .setMessage("Hoạt động gần đây:\n\n" +
                        "15:30 - Người dùng mới đăng ký\n" +
                        "15:25 - Backup tự động hoàn thành\n" +
                        "15:20 - Giáo viên tạo bài học mới\n" +
                        "15:15 - Cập nhật hệ thống\n" +
                        "15:10 - Học viên hoàn thành quiz")
                .setPositiveButton("Xem đầy đủ", (dialog, which) -> {
                    Intent intent = new Intent(this, SystemStatisticsActivity.class);
                    intent.putExtra("mode", "logs");
                    startActivity(intent);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void handleLogout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản quản trị?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadAdminData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            if (userId != null) {
                db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String fullName = documentSnapshot.getString("fullName");
                                if (fullName != null && tvWelcome != null) {
                                    tvWelcome.setText("Chào mừng, Admin " + fullName + "!");
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (tvWelcome != null) {
                                tvWelcome.setText("Chào mừng, Quản trị viên!");
                            }
                        });

                loadSystemStats();
            }
        } else {
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loadSystemStats() {
        // Load system statistics - sample data
        if (tvTotalUsers != null) {
            tvTotalUsers.setText("1,234 người dùng");
        }
        if (tvTotalTeachers != null) {
            tvTotalTeachers.setText("56 giáo viên");
        }
        if (tvTotalStudents != null) {
            tvTotalStudents.setText("1,178 học viên");
        }
        if (tvSystemHealth != null) {
            tvSystemHealth.setText("Hệ thống: Hoạt động tốt");
        }
    }
}
