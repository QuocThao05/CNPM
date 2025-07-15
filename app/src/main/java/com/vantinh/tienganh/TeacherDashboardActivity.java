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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalStudents, tvActiveCourses, tvPendingRequests;
    private RecyclerView rvMyCourses, rvRecentStudents;
    private BottomNavigationView bottomNavigation;
    private CardView cardCreateContent, cardManageStudents, cardViewReports, cardSchedule;
    private Button btnCourseRequests, btnCreateCourse, btnManageCourses;
    private FloatingActionButton fabQuickAction;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupClickListeners();
        loadTeacherData();
        loadPendingRequestsCount();
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            tvWelcome = findViewById(R.id.tv_welcome);
            tvTotalStudents = findViewById(R.id.tv_total_students);
            tvActiveCourses = findViewById(R.id.tv_active_courses);
            // tvPendingRequests = findViewById(R.id.tv_pending_requests); // Comment out for now
            bottomNavigation = findViewById(R.id.bottom_navigation);

            // Cards - comment out if not exist in layout
            // cardCreateContent = findViewById(R.id.card_create_content);
            // cardManageStudents = findViewById(R.id.card_manage_students);
            // cardViewReports = findViewById(R.id.card_view_reports);
            // cardSchedule = findViewById(R.id.card_schedule);

            // Buttons - comment out if not exist in layout
            // btnCourseRequests = findViewById(R.id.btn_course_requests);
            btnCreateCourse = findViewById(R.id.btn_create_course);
            // btnManageCourses = findViewById(R.id.btn_manage_courses);

            // RecyclerViews - comment out if not exist in layout
            // rvMyCourses = findViewById(R.id.rv_my_courses);
            // rvRecentStudents = findViewById(R.id.rv_recent_students);

            // fabQuickAction = findViewById(R.id.fab_quick_action);

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bảng điều khiển giáo viên");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.teacher_toolbar_menu, menu);
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
                startActivity(new Intent(this, CourseManagementActivity.class));
                return true;
            } else if (itemId == R.id.nav_students) {
                startActivity(new Intent(this, EnrollmentManagementActivity.class));
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
        if (cardCreateContent != null) {
            cardCreateContent.setOnClickListener(v -> {
                startActivity(new Intent(this, ContentCreationActivity.class));
            });
        }

        if (cardManageStudents != null) {
            cardManageStudents.setOnClickListener(v -> {
                startActivity(new Intent(this, EnrollmentManagementActivity.class));
            });
        }

        if (cardViewReports != null) {
            cardViewReports.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng báo cáo đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (cardSchedule != null) {
            cardSchedule.setOnClickListener(v -> {
                startActivity(new Intent(this, PersonalScheduleActivity.class));
            });
        }

        // Buttons
        if (btnCourseRequests != null) {
            btnCourseRequests.setOnClickListener(v -> {
                startActivity(new Intent(this, CourseRequestManagementActivity.class));
            });
        }

        if (btnCreateCourse != null) {
            btnCreateCourse.setOnClickListener(v -> {
                startActivity(new Intent(this, CreateCourseActivity.class));
            });
        }

        if (btnManageCourses != null) {
            btnManageCourses.setOnClickListener(v -> {
                startActivity(new Intent(this, CourseManagementActivity.class));
            });
        }

        if (fabQuickAction != null) {
            fabQuickAction.setOnClickListener(v -> {
                startActivity(new Intent(this, CreateCourseActivity.class));
            });
        }
    }

    private void loadTeacherData() {
        if (mAuth.getCurrentUser() != null) {
            String teacherId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(teacherId)
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
                        Toast.makeText(this, "Lỗi tải thông tin giáo viên", Toast.LENGTH_SHORT).show();
                    });

            // Load courses count
            loadCoursesCount(teacherId);
            loadStudentsCount(teacherId);
        }
    }

    private void loadCoursesCount(String teacherId) {
        db.collection("courses")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (tvActiveCourses != null) {
                        tvActiveCourses.setText(String.valueOf(count));
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvActiveCourses != null) {
                        tvActiveCourses.setText("0");
                    }
                });
    }

    private void loadStudentsCount(String teacherId) {
        db.collection("enrollments")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (tvTotalStudents != null) {
                        tvTotalStudents.setText(String.valueOf(count));
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvTotalStudents != null) {
                        tvTotalStudents.setText("0");
                    }
                });
    }

    private void loadPendingRequestsCount() {
        if (mAuth.getCurrentUser() != null) {
            String teacherId = mAuth.getCurrentUser().getUid();
            db.collection("courseRequests")
                    .whereEqualTo("teacherId", teacherId)
                    .whereEqualTo("status", "pending")
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            if (tvPendingRequests != null) {
                                tvPendingRequests.setText("0");
                            }
                            return;
                        }

                        int count = 0;
                        if (value != null) {
                            count = value.size();
                        }

                        if (tvPendingRequests != null) {
                            tvPendingRequests.setText(String.valueOf(count));
                        }
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