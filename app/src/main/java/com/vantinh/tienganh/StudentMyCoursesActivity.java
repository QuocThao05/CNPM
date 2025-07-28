package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class StudentMyCoursesActivity extends AppCompatActivity implements StudentEnrolledCourseAdapter.OnCourseClickListener {

    private RecyclerView rvMyCourses;
    private LinearLayout layoutNoCourses;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<EnrolledCourse> enrolledCourseList;
    private StudentEnrolledCourseAdapter courseAdapter;
    private String currentStudentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_my_courses);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        enrolledCourseList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        getCurrentStudentId();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMyCourses = findViewById(R.id.rv_my_courses);
        layoutNoCourses = findViewById(R.id.layout_no_courses);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Khóa học của tôi");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        courseAdapter = new StudentEnrolledCourseAdapter(enrolledCourseList, this);
        rvMyCourses.setLayoutManager(new LinearLayoutManager(this));
        rvMyCourses.setAdapter(courseAdapter);
    }

    private void getCurrentStudentId() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String firebaseUid = mAuth.getCurrentUser().getUid();
        
        // Get student info từ users collection
        db.collection("users").document(firebaseUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userRole = documentSnapshot.getString("role");
                        currentStudentId = documentSnapshot.getString("id");

                        if (!"student".equals(userRole)) {
                            Toast.makeText(this, "Chỉ học viên mới có thể xem khóa học của mình", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        if (currentStudentId != null) {
                            // Thêm debug để kiểm tra dữ liệu Firebase
                            debugFirebaseData(currentStudentId);
                            loadEnrolledCourses();
                        } else {
                            Toast.makeText(this, "Không tìm thấy thông tin học viên", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentMyCourses", "Error loading user info", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadEnrolledCourses() {
        layoutNoCourses.setVisibility(View.VISIBLE);
        rvMyCourses.setVisibility(View.GONE);

        android.util.Log.d("StudentMyCourses", "Loading enrolled courses for student: " + currentStudentId);

        // Load ONLY approved enrollments từ collection "enrollments" (đã có trường status)
        db.collection("enrollments")
                .whereEqualTo("studentID", currentStudentId)
                .whereEqualTo("status", "approved")  // Thêm lại filter theo status approved
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    enrolledCourseList.clear();

                    android.util.Log.d("StudentMyCourses", "Found " + queryDocumentSnapshots.size() + " approved enrollments");

                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    final int totalEnrollments = queryDocumentSnapshots.size();
                    final int[] loadedCount = {0}; // Counter để track số lượng course đã load xong

                    // Với mỗi approved enrollment, load thông tin course tương ứng
                    for (QueryDocumentSnapshot enrollmentDoc : queryDocumentSnapshots) {
                        String courseId = enrollmentDoc.getString("courseID");
                        String courseName = enrollmentDoc.getString("courseName");
                        String enrollmentDate = enrollmentDoc.getTimestamp("enrollmentDate") != null ?
                            enrollmentDoc.getTimestamp("enrollmentDate").toDate().toString() : "";

                        if (courseId != null) {
                            loadCourseDetails(courseId, courseName, enrollmentDate, enrollmentDoc.getId(),
                                totalEnrollments, loadedCount);
                        } else {
                            loadedCount[0]++;
                            if (loadedCount[0] == totalEnrollments) {
                                updateUI();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentMyCourses", "Error loading approved enrollments", e);
                    Toast.makeText(this, "Lỗi tải khóa học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void loadCourseDetails(String courseId, String courseName, String enrollmentDate,
                                 String enrollmentId, int totalEnrollments, int[] loadedCount) {
        db.collection("courses").document(courseId)
                .get()
                .addOnSuccessListener(courseDoc -> {
                    if (courseDoc.exists()) {
                        Course course = courseDoc.toObject(Course.class);
                        if (course != null) {
                            course.setId(courseDoc.getId());

                            // Tạo EnrolledCourse object
                            EnrolledCourse enrolledCourse = new EnrolledCourse();
                            enrolledCourse.setCourse(course);
                            enrolledCourse.setEnrollmentId(enrollmentId);
                            enrolledCourse.setEnrollmentDate(enrollmentDate);
                            enrolledCourse.setStatus("active");

                            // Set dữ liệu mặc định
                            enrolledCourse.setTotalLessons(0);
                            enrolledCourse.setCompletedLessons(0);
                            enrolledCourse.setProgress(0);

                            enrolledCourseList.add(enrolledCourse);

                            // Tính toán tiến độ thực tế từ Firebase
                            calculateRealProgress(enrolledCourse, courseId, currentStudentId);

                            android.util.Log.d("StudentMyCourses", "Added course: " + course.getTitle());
                        }
                    } else {
                        android.util.Log.w("StudentMyCourses", "Course not found: " + courseId);
                    }

                    // Increment counter và check if all courses loaded
                    loadedCount[0]++;
                    android.util.Log.d("StudentMyCourses", "Loaded " + loadedCount[0] + "/" + totalEnrollments + " courses");

                    if (loadedCount[0] == totalEnrollments) {
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentMyCourses", "Error loading course details for: " + courseId, e);

                    // Vẫn increment counter ngay cả khi có lỗi
                    loadedCount[0]++;
                    if (loadedCount[0] == totalEnrollments) {
                        updateUI();
                    }
                });
    }

    private void calculateRealProgress(EnrolledCourse enrolledCourse, String courseId, String studentId) {
        android.util.Log.d("StudentMyCourses", "=== CALCULATING REAL PROGRESS ===");
        android.util.Log.d("StudentMyCourses", "CourseId: " + courseId + ", StudentId: " + studentId);

        // Đầu tiên load tổng số lessons - sử dụng cách đơn giản hơn
        db.collection("lessons")
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(lessonsSnapshot -> {
                    int totalLessons = lessonsSnapshot.size();
                    android.util.Log.d("StudentMyCourses", "Total lessons found: " + totalLessons);

                    if (totalLessons > 0) {
                        // Load completed lessons từ lesson_progress
                        db.collection("lesson_progress")
                                .whereEqualTo("studentId", studentId)
                                .whereEqualTo("courseId", courseId)
                                .whereEqualTo("isCompleted", true)
                                .get()
                                .addOnSuccessListener(progressSnapshot -> {
                                    int completedLessons = progressSnapshot.size();
                                    int progressPercentage = (completedLessons * 100) / totalLessons;

                                    android.util.Log.d("StudentMyCourses",
                                        "Progress calculated: " + completedLessons + "/" + totalLessons + " = " + progressPercentage + "%");

                                    // Cập nhật thông tin EnrolledCourse
                                    enrolledCourse.setTotalLessons(totalLessons);
                                    enrolledCourse.setCompletedLessons(completedLessons);
                                    enrolledCourse.setProgress(progressPercentage);

                                    // Cập nhật UI ngay lập tức
                                    runOnUiThread(() -> {
                                        if (courseAdapter != null) {
                                            courseAdapter.notifyDataSetChanged();
                                            android.util.Log.d("StudentMyCourses", "UI updated for course: " +
                                                enrolledCourse.getCourse().getTitle() + " - " + progressPercentage + "%");
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("StudentMyCourses", "Error loading progress", e);
                                    // Set default progress nếu có lỗi
                                    enrolledCourse.setTotalLessons(totalLessons);
                                    enrolledCourse.setCompletedLessons(0);
                                    enrolledCourse.setProgress(0);
                                    runOnUiThread(() -> {
                                        if (courseAdapter != null) {
                                            courseAdapter.notifyDataSetChanged();
                                        }
                                    });
                                });
                    } else {
                        android.util.Log.w("StudentMyCourses", "No lessons found for course: " + courseId);
                        // Không có lessons, set default
                        enrolledCourse.setTotalLessons(0);
                        enrolledCourse.setCompletedLessons(0);
                        enrolledCourse.setProgress(0);
                        runOnUiThread(() -> {
                            if (courseAdapter != null) {
                                courseAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentMyCourses", "Error loading lessons", e);
                    // Set default nếu có lỗi
                    enrolledCourse.setTotalLessons(0);
                    enrolledCourse.setCompletedLessons(0);
                    enrolledCourse.setProgress(0);
                    runOnUiThread(() -> {
                        if (courseAdapter != null) {
                            courseAdapter.notifyDataSetChanged();
                        }
                    });
                });
    }

    private void updateUIForCourse(EnrolledCourse enrolledCourse) {
        runOnUiThread(() -> {
            if (courseAdapter != null) {
                courseAdapter.notifyDataSetChanged();
                android.util.Log.d("StudentMyCourses", "UI updated - " +
                    enrolledCourse.getCourse().getTitle() + ": " + enrolledCourse.getProgress() + "%");
            }
        });
    }

    private void updateUI() {
        runOnUiThread(() -> {
            if (enrolledCourseList.isEmpty()) {
                showEmptyState();
            } else {
                layoutNoCourses.setVisibility(View.GONE);
                rvMyCourses.setVisibility(View.VISIBLE);
                courseAdapter.notifyDataSetChanged();
                android.util.Log.d("StudentMyCourses", "Updated UI with " + enrolledCourseList.size() + " courses");
            }
        });
    }

    private void showEmptyState() {
        runOnUiThread(() -> {
            layoutNoCourses.setVisibility(View.VISIBLE);
            rvMyCourses.setVisibility(View.GONE);
            android.util.Log.d("StudentMyCourses", "Showing empty state");
        });
    }

    @Override
    public void onCourseClick(EnrolledCourse enrolledCourse) {
        Intent intent = new Intent(this, StudentCourseDetailActivity.class);
        intent.putExtra("course_id", enrolledCourse.getCourse().getId());
        intent.putExtra("course_title", enrolledCourse.getCourse().getTitle());
        startActivity(intent);
    }

    @Override
    public void onContinueLearning(EnrolledCourse enrolledCourse) {
        // Chuyển đến màn hình học bài đầu tiên chưa hoàn thành
        Intent intent = new Intent(this, StudentCourseDetailActivity.class);
        intent.putExtra("course_id", enrolledCourse.getCourse().getId());
        intent.putExtra("course_title", enrolledCourse.getCourse().getTitle());
        startActivity(intent);
    }

    private void debugFirebaseData(String studentId) {
        android.util.Log.d("StudentMyCourses", "=== DEBUG FIREBASE DATA ===");
        android.util.Log.d("StudentMyCourses", "Student ID: " + studentId);

        // Debug enrollments
        db.collection("enrollments")
                .whereEqualTo("studentID", studentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    android.util.Log.d("StudentMyCourses", "Total enrollments: " + querySnapshot.size());
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        String status = doc.getString("status");
                        String courseId = doc.getString("courseID");
                        android.util.Log.d("StudentMyCourses", "Enrollment - CourseID: " + courseId + ", Status: " + status);
                    }
                })
                .addOnFailureListener(e -> android.util.Log.e("StudentMyCourses", "Error debugging enrollments", e));

        // Debug lesson_progress
        db.collection("lesson_progress")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    android.util.Log.d("StudentMyCourses", "Total lesson_progress records: " + querySnapshot.size());
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        String courseId = doc.getString("courseId");
                        Boolean isCompleted = doc.getBoolean("isCompleted");
                        android.util.Log.d("StudentMyCourses", "Progress - CourseID: " + courseId + ", Completed: " + isCompleted);
                    }
                })
                .addOnFailureListener(e -> android.util.Log.e("StudentMyCourses", "Error debugging lesson_progress", e));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload dữ liệu khi quay lại activity để cập nhật tiến độ mới nhất
        if (currentStudentId != null) {
            loadEnrolledCourses();
        }
    }
}
