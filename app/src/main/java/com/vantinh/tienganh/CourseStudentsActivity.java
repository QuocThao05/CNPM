package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
    private TextView tvNoStudents, tvCourseTitle;
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
        tvNoStudents = findViewById(R.id.tv_no_students);
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

        // Load all approved enrollments for this course
        db.collection("enrollments")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("status", "APPROVED")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentList.clear();

                        if (task.getResult().isEmpty()) {
                            updateUI();
                            progressBar.setVisibility(View.GONE);
                            return;
                        }

                        final int totalEnrollments = task.getResult().size();
                        final int[] processedCount = {0};

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Enrollment enrollment = doc.toObject(Enrollment.class);
                            enrollment.setId(doc.getId());

                            // Load student details
                            loadStudentDetails(enrollment, () -> {
                                processedCount[0]++;
                                if (processedCount[0] == totalEnrollments) {
                                    updateUI();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Lỗi tải danh sách học viên", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadStudentDetails(Enrollment enrollment, Runnable onComplete) {
        db.collection("users").document(enrollment.getStudentId())
                .get()
                .addOnSuccessListener(studentDoc -> {
                    if (studentDoc.exists()) {
                        CourseStudent student = new CourseStudent();
                        student.setStudentId(enrollment.getStudentId());
                        student.setStudentName(studentDoc.getString("name"));
                        student.setStudentEmail(studentDoc.getString("email"));
                        student.setEnrollmentDate(enrollment.getEnrollmentDate());
                        student.setEnrollmentId(enrollment.getId());

                        // Load progress info
                        loadStudentProgress(student, () -> {
                            studentList.add(student);
                            onComplete.run();
                        });
                    } else {
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> onComplete.run());
    }

    private void loadStudentProgress(CourseStudent student, Runnable onComplete) {
        // Load quiz results for progress calculation
        db.collection("quiz_results")
                .whereEqualTo("studentId", student.getStudentId())
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int completedQuizzes = task.getResult().size();
                        double totalScore = 0;

                        for (QueryDocumentSnapshot quizDoc : task.getResult()) {
                            Double score = quizDoc.getDouble("score");
                            if (score != null) {
                                totalScore += score;
                            }
                        }

                        student.setCompletedQuizzes(completedQuizzes);
                        student.setAverageScore(completedQuizzes > 0 ? totalScore / completedQuizzes : 0);

                        // Load total quizzes in course to calculate progress
                        db.collection("quizzes")
                                .whereEqualTo("courseId", courseId)
                                .get()
                                .addOnCompleteListener(quizTask -> {
                                    if (quizTask.isSuccessful()) {
                                        int totalQuizzes = quizTask.getResult().size();
                                        student.setTotalQuizzes(totalQuizzes);
                                        double progress = totalQuizzes > 0 ? (double) completedQuizzes / totalQuizzes * 100 : 0;
                                        student.setProgress(progress);
                                    }
                                    onComplete.run();
                                });
                    } else {
                        onComplete.run();
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
            tvNoStudents.setVisibility(View.VISIBLE);
            rvStudents.setVisibility(View.GONE);
        } else {
            tvNoStudents.setVisibility(View.GONE);
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
