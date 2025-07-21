package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditCourseActivity extends AppCompatActivity {

    private EditText etCourseTitle, etCourseDescription, etCourseDuration;
    private Spinner spinnerLevel, spinnerCategory;
    private Button btnUpdateCourse, btnDeleteCourse, btnManageLessons, btnViewStudents;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String courseId;
    private Course currentCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get course ID from intent
        courseId = getIntent().getStringExtra("courseId");
        if (courseId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupClickListeners();
        loadCourseData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        etCourseTitle = findViewById(R.id.et_course_title);
        etCourseDescription = findViewById(R.id.et_course_description);
        etCourseDuration = findViewById(R.id.et_course_duration);
        spinnerLevel = findViewById(R.id.spinner_level);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnUpdateCourse = findViewById(R.id.btn_update_course);
        btnDeleteCourse = findViewById(R.id.btn_delete_course);
        btnManageLessons = findViewById(R.id.btn_manage_lessons);
        btnViewStudents = findViewById(R.id.btn_view_students);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chỉnh sửa khóa học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_courses);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, TeacherDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_courses) {
                // Already on courses
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
        btnUpdateCourse.setOnClickListener(v -> updateCourse());
        btnDeleteCourse.setOnClickListener(v -> deleteCourse());
        btnManageLessons.setOnClickListener(v -> {
            Intent intent = new Intent(this, CourseLessonsActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", currentCourse != null ? currentCourse.getTitle() : "");
            startActivity(intent);
        });
        btnViewStudents.setOnClickListener(v -> {
            Intent intent = new Intent(this, CourseStudentsActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", currentCourse != null ? currentCourse.getTitle() : "");
            startActivity(intent);
        });
    }

    private void loadCourseData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("courses").document(courseId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                progressBar.setVisibility(View.GONE);
                if (documentSnapshot.exists()) {
                    currentCourse = documentSnapshot.toObject(Course.class);
                    if (currentCourse != null) {
                        currentCourse.setId(documentSnapshot.getId());
                        populateFields();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Lỗi khi tải dữ liệu khóa học", Toast.LENGTH_SHORT).show();
            });
    }

    private void populateFields() {
        etCourseTitle.setText(currentCourse.getTitle());
        etCourseDescription.setText(currentCourse.getDescription());
        etCourseDuration.setText(String.valueOf(currentCourse.getDuration()));

        // Set spinner selections based on course data
        // You'll need to implement spinner population logic here
    }

    private void updateCourse() {
        String title = etCourseTitle.getText().toString().trim();
        String description = etCourseDescription.getText().toString().trim();
        String durationStr = etCourseDuration.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời lượng phải là số", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        currentCourse.setTitle(title);
        currentCourse.setDescription(description);
        currentCourse.setDuration(duration);

        db.collection("courses").document(courseId)
            .set(currentCourse)
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Cập nhật khóa học thành công", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Lỗi khi cập nhật khóa học", Toast.LENGTH_SHORT).show();
            });
    }

    private void deleteCourse() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa khóa học này? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                progressBar.setVisibility(View.VISIBLE);
                db.collection("courses").document(courseId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Đã xóa khóa học", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Lỗi khi xóa khóa học", Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Hủy", null)
            .show();
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
