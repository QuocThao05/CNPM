package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SystemStatisticsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private LinearLayout layoutStudents, layoutReports, layoutNotifications;

    // Student management
    private RecyclerView rvStudents, rvStudentProgress;
    private TextView tvTotalStudents, tvActiveStudents, tvAverageProgress;
    private Button btnViewAllStudents, btnExportStudentData;
    private CardView cardTopPerformers, cardNeedHelp;

    // Reports
    private RecyclerView rvReports;
    private TextView tvTotalLessons, tvCompletedQuizzes, tvAverageScore;
    private Button btnGenerateReport, btnExportReport;
    private CardView cardWeeklyReport, cardMonthlyReport, cardCustomReport;

    // Notifications
    private RecyclerView rvNotifications;
    private TextView tvUnreadCount, tvRecentActivity;
    private Button btnMarkAllRead, btnNotificationSettings;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String mode; // "students", "reports", "notifications"
    private String pageTitle;
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_statistics);

        // Get data from intent
        mode = getIntent().getStringExtra("mode");
        pageTitle = getIntent().getStringExtra("title");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            teacherId = mAuth.getCurrentUser().getUid();
        }

        initViews();
        setupToolbar();
        setupTabs();
        setupClickListeners();
        handleModeSpecificSetup();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);

        // Layouts - only reference if they exist
        layoutStudents = findViewById(R.id.layout_students);
        layoutReports = findViewById(R.id.layout_reports);
        layoutNotifications = findViewById(R.id.layout_notifications);

        // Student management - add null checks
        rvStudents = findViewById(R.id.rv_students);
        rvStudentProgress = findViewById(R.id.rv_student_progress);
        tvTotalStudents = findViewById(R.id.tv_total_students);
        tvActiveStudents = findViewById(R.id.tv_active_students);
        tvAverageProgress = findViewById(R.id.tv_average_progress);
        btnViewAllStudents = findViewById(R.id.btn_view_all_students);
        btnExportStudentData = findViewById(R.id.btn_export_student_data);
        cardTopPerformers = findViewById(R.id.card_top_performers);
        cardNeedHelp = findViewById(R.id.card_need_help);

        // Reports - add null checks
        rvReports = findViewById(R.id.rv_reports);
        tvTotalLessons = findViewById(R.id.tv_total_lessons);
        tvCompletedQuizzes = findViewById(R.id.tv_completed_quizzes);
        tvAverageScore = findViewById(R.id.tv_average_score);
        btnGenerateReport = findViewById(R.id.btn_generate_report);
        btnExportReport = findViewById(R.id.btn_export_report);
        cardWeeklyReport = findViewById(R.id.card_weekly_report);
        cardMonthlyReport = findViewById(R.id.card_monthly_report);
        cardCustomReport = findViewById(R.id.card_custom_report);

        // Notifications - add null checks
        rvNotifications = findViewById(R.id.rv_notifications);
        tvUnreadCount = findViewById(R.id.tv_unread_count);
        tvRecentActivity = findViewById(R.id.tv_recent_activity);
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read);
        btnNotificationSettings = findViewById(R.id.btn_notification_settings);

        // Setup RecyclerViews
        setupRecyclerViews();
    }

    private void setupRecyclerViews() {
        if (rvStudents != null) {
            rvStudents.setLayoutManager(new LinearLayoutManager(this));
        }
        if (rvStudentProgress != null) {
            rvStudentProgress.setLayoutManager(new LinearLayoutManager(this));
        }
        if (rvReports != null) {
            rvReports.setLayoutManager(new LinearLayoutManager(this));
        }
        if (rvNotifications != null) {
            rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(pageTitle != null ? pageTitle : "Thống kê hệ thống");
        }
    }

    private void setupTabs() {
        if (tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setText("Học viên"));
            tabLayout.addTab(tabLayout.newTab().setText("Báo cáo"));
            tabLayout.addTab(tabLayout.newTab().setText("Thông báo"));

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
        // Add null checks for all click listeners
        if (btnViewAllStudents != null) {
            btnViewAllStudents.setOnClickListener(v -> {
                // Implementation for viewing all students
                android.util.Log.d("SystemStats", "View all students clicked");
            });
        }

        if (btnExportStudentData != null) {
            btnExportStudentData.setOnClickListener(v -> {
                // Implementation for exporting student data
                android.util.Log.d("SystemStats", "Export student data clicked");
            });
        }

        if (btnGenerateReport != null) {
            btnGenerateReport.setOnClickListener(v -> {
                // Implementation for generating report
                android.util.Log.d("SystemStats", "Generate report clicked");
            });
        }

        if (btnExportReport != null) {
            btnExportReport.setOnClickListener(v -> {
                // Implementation for exporting report
                android.util.Log.d("SystemStats", "Export report clicked");
            });
        }

        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> {
                // Implementation for marking all as read
                android.util.Log.d("SystemStats", "Mark all read clicked");
            });
        }

        if (btnNotificationSettings != null) {
            btnNotificationSettings.setOnClickListener(v -> {
                // Implementation for notification settings
                android.util.Log.d("SystemStats", "Notification settings clicked");
            });
        }
    }

    private void handleModeSpecificSetup() {
        if ("students".equals(mode) && tabLayout != null) {
            tabLayout.getTabAt(0).select();
            switchTab(0);
        } else if ("reports".equals(mode) && tabLayout != null) {
            tabLayout.getTabAt(1).select();
            switchTab(1);
        } else if ("notifications".equals(mode) && tabLayout != null) {
            tabLayout.getTabAt(2).select();
            switchTab(2);
        }
    }

    private void switchTab(int position) {
        hideAllLayouts();

        switch (position) {
            case 0: // Students
                if (layoutStudents != null) {
                    layoutStudents.setVisibility(View.VISIBLE);
                    loadStudentData();
                }
                break;
            case 1: // Reports
                if (layoutReports != null) {
                    layoutReports.setVisibility(View.VISIBLE);
                    loadReportData();
                }
                break;
            case 2: // Notifications
                if (layoutNotifications != null) {
                    layoutNotifications.setVisibility(View.VISIBLE);
                    loadNotificationData();
                }
                break;
        }
    }

    private void hideAllLayouts() {
        if (layoutStudents != null) layoutStudents.setVisibility(View.GONE);
        if (layoutReports != null) layoutReports.setVisibility(View.GONE);
        if (layoutNotifications != null) layoutNotifications.setVisibility(View.GONE);
    }

    private void loadData() {
        // Load initial data based on mode
        if ("students".equals(mode)) {
            loadStudentData();
        } else if ("reports".equals(mode)) {
            loadReportData();
        } else if ("notifications".equals(mode)) {
            loadNotificationData();
        } else {
            // Load all data
            loadStudentData();
            loadReportData();
            loadNotificationData();
        }
    }

    private void loadStudentData() {
        // Load student statistics
        if (tvTotalStudents != null) {
            tvTotalStudents.setText("45 học viên");
        }
        if (tvActiveStudents != null) {
            tvActiveStudents.setText("38 đang hoạt động");
        }
        if (tvAverageProgress != null) {
            tvAverageProgress.setText("Tiến độ TB: 72%");
        }
    }

    private void loadReportData() {
        // Load report statistics
        if (tvTotalLessons != null) {
            tvTotalLessons.setText("24 bài học");
        }
        if (tvCompletedQuizzes != null) {
            tvCompletedQuizzes.setText("156 lượt quiz");
        }
        if (tvAverageScore != null) {
            tvAverageScore.setText("Điểm TB: 8.2/10");
        }
    }

    private void loadNotificationData() {
        // Load notification data
        if (tvUnreadCount != null) {
            tvUnreadCount.setText("5 thông báo chưa đọc");
        }
        if (tvRecentActivity != null) {
            tvRecentActivity.setText("Hoạt động gần đây: 12 sự kiện");
        }
    }

    // Student management methods
    private void viewAllStudents() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Danh sách học viên")
                .setMessage("Hiển thị tất cả 45 học viên trong khóa học của bạn:")
                .setPositiveButton("Xem chi tiết", (dialog, which) -> {
                    // Navigate to detailed student list
                    android.widget.Toast.makeText(this, "Đang tải danh sách chi tiết...", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void exportStudentData() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Xuất dữ liệu học viên")
                .setMessage("Chọn định dạng xuất:")
                .setPositiveButton("Excel", (dialog, which) -> {
                    android.widget.Toast.makeText(this, "Đang xuất file Excel...", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("PDF", (dialog, which) -> {
                    android.widget.Toast.makeText(this, "Đang xuất file PDF...", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showTopPerformers() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Học viên xuất sắc")
                .setMessage("Top 5 học viên có kết quả tốt nhất:\n\n" +
                        "1. Nguyễn Văn A - 95%\n" +
                        "2. Trần Thị B - 92%\n" +
                        "3. Lê Văn C - 89%\n" +
                        "4. Phạm Thị D - 87%\n" +
                        "5. Hoàng Văn E - 85%")
                .setPositiveButton("Xem chi tiết", (dialog, which) -> {
                    // Navigate to detailed view
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showStudentsNeedHelp() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Học viên cần hỗ trợ")
                .setMessage("Học viên có tiến độ chậm:\n\n" +
                        "• Nguyễn Văn X - 45% (không hoạt động 5 ngày)\n" +
                        "• Trần Thị Y - 38% (điểm quiz thấp)\n" +
                        "• Lê Văn Z - 42% (chưa hoàn thành bài tập)")
                .setPositiveButton("Gửi nhắc nhở", (dialog, which) -> {
                    android.widget.Toast.makeText(this, "Đã gửi nhắc nhở đến học viên", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    // Report methods
    private void generateReport() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Tạo báo cáo")
                .setMessage("Chọn loại báo cáo muốn tạo:")
                .setPositiveButton("Báo cáo tuần", (dialog, which) -> showWeeklyReport())
                .setNeutralButton("Báo cáo tháng", (dialog, which) -> showMonthlyReport())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void exportReport() {
        android.widget.Toast.makeText(this, "Đang xuất báo cáo...", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void showWeeklyReport() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Báo cáo tuần (09/07 - 15/07)")
                .setMessage("• Số học viên hoạt động: 38/45\n" +
                        "• Bài học hoàn thành: 89\n" +
                        "• Quiz hoàn thành: 234\n" +
                        "• Điểm trung bình: 8.2/10\n" +
                        "• Thời gian học TB: 3.5h/người")
                .setPositiveButton("Xuất báo cáo", (dialog, which) -> exportReport())
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showMonthlyReport() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Báo cáo tháng (Tháng 7/2025)")
                .setMessage("• Tổng số học viên: 45\n" +
                        "• Học viên mới: 8\n" +
                        "• Tỷ lệ hoàn thành: 78%\n" +
                        "• Nội dung mới: 12 bài học\n" +
                        "• Đánh giá TB: 4.6/5 sao")
                .setPositiveButton("Xuất báo cáo", (dialog, which) -> exportReport())
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void createCustomReport() {
        android.widget.Toast.makeText(this, "Chức năng báo cáo tùy chỉnh đang phát triển", android.widget.Toast.LENGTH_SHORT).show();
    }

    // Notification methods
    private void markAllNotificationsRead() {
        android.widget.Toast.makeText(this, "Đã đánh dấu tất cả thông báo là đã đọc", android.widget.Toast.LENGTH_SHORT).show();
        if (tvUnreadCount != null) {
            tvUnreadCount.setText("0 thông báo chưa đọc");
        }
    }

    private void openNotificationSettings() {
        Intent intent = new Intent(this, UpdateProfileActivity.class);
        intent.putExtra("mode", "notifications");
        startActivity(intent);
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
        Intent intent = new Intent(this, TeacherDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}