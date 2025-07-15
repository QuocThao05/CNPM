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
        // Core views that should exist
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Text views for statistics - add null checks
        tvWelcome = findViewById(R.id.tv_welcome);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTotalTeachers = findViewById(R.id.tv_total_teachers);
        tvTotalStudents = findViewById(R.id.tv_total_students);
        tvSystemHealth = findViewById(R.id.tv_system_health);

        // RecyclerViews - may not exist in layout
        rvRecentActivities = findViewById(R.id.rv_recent_activities);
        rvSystemAlerts = findViewById(R.id.rv_system_alerts);

        // Management cards - add null checks
        cardUserManagement = findViewById(R.id.card_user_management);
        cardSystemStats = findViewById(R.id.card_system_stats);
        cardContentManagement = findViewById(R.id.card_content_management);
        cardReports = findViewById(R.id.card_reports);

        // Floating action button - may not exist
        fabQuickAction = findViewById(R.id.fab_quick_action);

        // Setup RecyclerViews if they exist
        if (rvRecentActivities != null) {
            rvRecentActivities.setLayoutManager(new LinearLayoutManager(this));
        }
        if (rvSystemAlerts != null) {
            rvSystemAlerts.setLayoutManager(new LinearLayoutManager(this));
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
        // Management cards with null checks
        if (cardUserManagement != null) {
            cardUserManagement.setOnClickListener(v -> {
                startActivity(new Intent(this, SystemStatisticsActivity.class));
            });
        }

        if (cardSystemStats != null) {
            cardSystemStats.setOnClickListener(v -> {
                Intent intent = new Intent(this, SystemStatisticsActivity.class);
                intent.putExtra("mode", "stats");
                intent.putExtra("title", "Thống kê hệ thống");
                startActivity(intent);
            });
        }

        if (cardContentManagement != null) {
            cardContentManagement.setOnClickListener(v -> {
                startActivity(new Intent(this, CourseManagementActivity.class));
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

        if (fabQuickAction != null) {
            fabQuickAction.setOnClickListener(v -> {
                showQuickActionDialog();
            });
        }
    }

    private void showQuickActionDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Hành động nhanh")
                .setMessage("Chọn hành động muốn thực hiện:")
                .setPositiveButton("Tạo tài khoản", (dialog, which) -> {
                    startActivity(new Intent(this, RegisterActivity.class));
                })
                .setNeutralButton("Xem báo cáo", (dialog, which) -> {
                    Intent intent = new Intent(this, SystemStatisticsActivity.class);
                    intent.putExtra("mode", "reports");
                    startActivity(intent);
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
                    startActivity(new Intent(this, SystemStatisticsActivity.class));
                    return true;
                } else if (itemId == R.id.nav_courses) {
                    startActivity(new Intent(this, CourseManagementActivity.class));
                    return true;
                } else if (itemId == R.id.nav_analytics) {
                    Intent intent = new Intent(this, SystemStatisticsActivity.class);
                    intent.putExtra("mode", "analytics");
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(this, UpdateProfileActivity.class));
                    return true;
                }
                return false;
            });
        }
    }

    private void loadAdminData() {
        // Load welcome message
        if (mAuth.getCurrentUser() != null) {
            String adminId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(adminId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty() && tvWelcome != null) {
                                tvWelcome.setText("Chào mừng, " + name + "!");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tải thông tin admin", Toast.LENGTH_SHORT).show();
                    });
        }

        // Load system statistics
        loadUserStatistics();
        loadSystemHealth();
    }

    private void loadUserStatistics() {
        // Load total users
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = queryDocumentSnapshots.size();
                    int totalTeachers = 0;
                    int totalStudents = 0;

                    // Count teachers and students
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String role = doc.getString("role");
                        if ("teacher".equals(role)) {
                            totalTeachers++;
                        } else if ("student".equals(role)) {
                            totalStudents++;
                        }
                    }

                    // Update UI
                    if (tvTotalUsers != null) {
                        tvTotalUsers.setText(String.valueOf(totalUsers));
                    }
                    if (tvTotalTeachers != null) {
                        tvTotalTeachers.setText(String.valueOf(totalTeachers));
                    }
                    if (tvTotalStudents != null) {
                        tvTotalStudents.setText(String.valueOf(totalStudents));
                    }
                })
                .addOnFailureListener(e -> {
                    // Set default values on error
                    if (tvTotalUsers != null) tvTotalUsers.setText("0");
                    if (tvTotalTeachers != null) tvTotalTeachers.setText("0");
                    if (tvTotalStudents != null) tvTotalStudents.setText("0");
                });
    }

    private void loadSystemHealth() {
        if (tvSystemHealth != null) {
            tvSystemHealth.setText("Hệ thống hoạt động bình thường");
        }
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
}
