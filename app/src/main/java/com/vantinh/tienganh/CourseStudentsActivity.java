package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CourseStudentsActivity extends AppCompatActivity {

    private RecyclerView rvStudents;
    private LinearLayout layoutNoStudents;  // Đổi từ TextView thành LinearLayout
    private TextView tvCourseTitle;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<CourseStudent> studentList;
    private CourseStudentAdapter studentAdapter;
    private String courseId, courseTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_students);

        // Get course info from intent
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");

        if (courseId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        studentList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupRecyclerView();
        loadStudents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        rvStudents = findViewById(R.id.rv_students);
        layoutNoStudents = findViewById(R.id.tv_no_students); // Sử dụng ID đúng từ layout XML
        tvCourseTitle = findViewById(R.id.tv_course_title);
        progressBar = findViewById(R.id.progress_bar);

        if (courseTitle != null) {
            tvCourseTitle.setText(courseTitle);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh sách học viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_students);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, TeacherDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_courses) {
                startActivity(new Intent(this, CourseManagementActivity.class));
                return true;
            } else if (itemId == R.id.nav_students) {
                // Already on students
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, UpdateProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        studentAdapter = new CourseStudentAdapter(studentList, new CourseStudentAdapter.OnStudentActionListener() {
            @Override
            public void onViewProgress(CourseStudent student) {
                Intent intent = new Intent(CourseStudentsActivity.this, StudentProgressDetailActivity.class);
                intent.putExtra("studentId", student.getStudentId());
                intent.putExtra("studentName", student.getStudentName());
                intent.putExtra("courseId", courseId);
                intent.putExtra("courseName", courseTitle);
                startActivity(intent);
            }

            @Override
            public void onRemoveStudent(CourseStudent student) {
                showRemoveStudentDialog(student);
            }

            @Override
            public void onSendMessage(CourseStudent student) {
                // TODO: Implement messaging functionality
                Toast.makeText(CourseStudentsActivity.this, "Chức năng nhắn tin đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(studentAdapter);
    }

    private void loadStudents() {
        progressBar.setVisibility(View.VISIBLE);

        // Load students từ courseRequests với status "approved" cho courseId này
        db.collection("courseRequests")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("status", "approved")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentList.clear();

                        if (task.getResult().isEmpty()) {
                            android.util.Log.d("CourseStudents", "No approved requests found for courseId: " + courseId);
                            updateUI();
                            progressBar.setVisibility(View.GONE);
                            return;
                        }

                        android.util.Log.d("CourseStudents", "Found " + task.getResult().size() + " approved requests for courseId: " + courseId);

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            // Lấy dữ liệu trực tiếp từ courseRequests
                            String studentName = doc.getString("studentName");
                            String studentId = doc.getString("studentId");
                            String studentEmail = doc.getString("studentEmail");
                            String courseName = doc.getString("courseName");

                            android.util.Log.d("CourseStudents", "Processing approved request - Student: " + studentName +
                                  ", StudentId: " + studentId + ", Course: " + courseName);

                            // Tạo CourseStudent object
                            CourseStudent student = new CourseStudent();
                            student.setStudentId(studentId);
                            student.setStudentName(studentName);
                            student.setStudentEmail(studentEmail);
                            student.setEnrollmentDate(new java.util.Date()); // Có thể lấy từ timestamp nếu có

                            studentList.add(student);
                        }

                        android.util.Log.d("CourseStudents", "Added " + studentList.size() + " students to list");
                        updateUI();
                        progressBar.setVisibility(View.GONE);

                    } else {
                        android.util.Log.e("CourseStudents", "Error loading approved requests", task.getException());
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Lỗi tải danh sách học viên: " +
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRemoveStudentDialog(CourseStudent student) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa học viên")
                .setMessage("Bạn có chắc chắn muốn xóa " + student.getStudentName() + " khỏi khóa học này?")
                .setPositiveButton("Xóa", (dialog, which) -> removeStudent(student))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void removeStudent(CourseStudent student) {
        if (student.getEnrollmentId() == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin đăng ký", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Update enrollment status to REMOVED instead of deleting
        db.collection("enrollments").document(student.getEnrollmentId())
                .update("status", "REMOVED")
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đã xóa học viên khỏi khóa học", Toast.LENGTH_SHORT).show();
                    loadStudents(); // Reload the list
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi khi xóa học viên", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (studentList.isEmpty()) {
            layoutNoStudents.setVisibility(View.VISIBLE);
            rvStudents.setVisibility(View.GONE);
        } else {
            layoutNoStudents.setVisibility(View.GONE);
            rvStudents.setVisibility(View.VISIBLE);
        }
        studentAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
