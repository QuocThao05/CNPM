package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentCourseDetailActivity extends AppCompatActivity {

    private TextView tvCourseTitle, tvCourseDescription, tvCourseCategory, tvCourseLevel;
    private TextView tvCourseDuration, tvTotalLessons, tvCompletedLessons;
    private TextView tvEnrollmentDate, tvProgressPercentage;
    private ProgressBar progressBarCompletion;
    private Button btnStartLearning, btnViewLessons, btnViewProgress;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String courseId, courseTitle, courseCategory, enrollmentId;
    private Course currentCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_course_detail);

        // Get course info from intent
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");
        courseCategory = getIntent().getStringExtra("courseCategory");
        enrollmentId = getIntent().getStringExtra("enrollmentId");

        if (courseId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
        loadCourseData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCourseTitle = findViewById(R.id.tv_course_title);
        tvCourseDescription = findViewById(R.id.tv_course_description);
        tvCourseCategory = findViewById(R.id.tv_course_category);
        tvCourseLevel = findViewById(R.id.tv_course_level);
        tvCourseDuration = findViewById(R.id.tv_course_duration);
        tvTotalLessons = findViewById(R.id.tv_total_lessons);
        tvCompletedLessons = findViewById(R.id.tv_completed_lessons);
        tvEnrollmentDate = findViewById(R.id.tv_enrollment_date);
        tvProgressPercentage = findViewById(R.id.tv_progress_percentage);
        progressBarCompletion = findViewById(R.id.progress_bar_completion);
        btnStartLearning = findViewById(R.id.btn_start_learning);
        btnViewLessons = findViewById(R.id.btn_view_lessons);
        btnViewProgress = findViewById(R.id.btn_view_progress);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết khóa học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        btnStartLearning.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentCourseLessonsActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", courseTitle);
            intent.putExtra("courseCategory", courseCategory);
            startActivity(intent);
        });

        btnViewLessons.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentCourseLessonsActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", courseTitle);
            intent.putExtra("courseCategory", courseCategory);
            startActivity(intent);
        });

        btnViewProgress.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentProgressDetailActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", courseTitle);
            startActivity(intent);
        });
    }

    private void loadCourseData() {
        db.collection("courses").document(courseId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentCourse = documentSnapshot.toObject(Course.class);
                    if (currentCourse != null) {
                        currentCourse.setId(documentSnapshot.getId());
                        displayCourseInfo();
                        loadProgressData();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseDetail", "Error loading course", e);
                Toast.makeText(this, "Lỗi tải khóa học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void displayCourseInfo() {
        tvCourseTitle.setText(currentCourse.getTitle());
        tvCourseDescription.setText(currentCourse.getDescription());
        tvCourseCategory.setText(currentCourse.getCategory());
        tvCourseLevel.setText(currentCourse.getLevel());
        tvCourseDuration.setText(currentCourse.getDuration() + " giờ");
    }

    private void loadProgressData() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String studentId = mAuth.getCurrentUser().getUid();

        // Load lesson count
        db.collection("lessons")
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("isPublished", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int totalLessons = queryDocumentSnapshots.size();
                tvTotalLessons.setText("Tổng số bài: " + totalLessons);

                // Load completed lessons count
                loadCompletedLessonsCount(studentId, totalLessons);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseDetail", "Error loading lessons", e);
                tvTotalLessons.setText("Tổng số bài: N/A");
                tvCompletedLessons.setText("Đã học: N/A");
                tvProgressPercentage.setText("0% hoàn thành");
                progressBarCompletion.setProgress(0);
            });
    }

    private void loadCompletedLessonsCount(String studentId, int totalLessons) {
        db.collection("lesson_progress")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("isCompleted", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int completedLessons = queryDocumentSnapshots.size();

                    // Update UI with detailed progress information
                    tvCompletedLessons.setText("Đã học: " + completedLessons);

                    // Calculate progress percentage
                    int progress = totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0;
                    progressBarCompletion.setProgress(progress);

                    // Enhanced progress display with fraction and percentage
                    String progressText = completedLessons + "/" + totalLessons + " bài học (" + progress + "% hoàn thành)";
                    tvProgressPercentage.setText(progressText);

                    // Log for debugging
                    android.util.Log.d("StudentCourseDetail", "Progress updated: " + completedLessons + "/" + totalLessons + " = " + progress + "%");

                    // Show completion status message
                    if (completedLessons == totalLessons && totalLessons > 0) {
                        Toast.makeText(this, "🎉 Chúc mừng! Bạn đã hoàn thành toàn bộ khóa học!", Toast.LENGTH_LONG).show();
                    } else if (completedLessons > 0) {
                        Toast.makeText(this, "Tiến độ: " + progressText, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentCourseDetail", "Error loading completed lessons", e);
                    tvCompletedLessons.setText("Đã học: 0");
                    tvProgressPercentage.setText("0/" + totalLessons + " bài học (0% hoàn thành)");
                    progressBarCompletion.setProgress(0);
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh progress data when returning from lesson to show updated progress
        if (currentCourse != null) {
            loadProgressData();
        }
    }

    // Add method to manually refresh progress (can be called from other activities)
    public void refreshProgressData() {
        loadProgressData();
    }
}
