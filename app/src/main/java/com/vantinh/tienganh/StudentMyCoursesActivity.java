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

        // Đầu tiên load tổng số lessons
        loadTotalLessons(courseId, enrolledCourse, studentId);
    }

    private void loadTotalLessons(String courseId, EnrolledCourse enrolledCourse, String studentId) {
        // Thử cách 1: lessons subcollection trong course
        db.collection("courses").document(courseId).collection("lessons")
                .get()
                .addOnSuccessListener(lessonsSnapshot -> {
                    int totalLessons = lessonsSnapshot.size();
                    android.util.Log.d("StudentMyCourses", "Method 1 - Total lessons: " + totalLessons);

                    if (totalLessons > 0) {
                        loadCompletedLessons(courseId, enrolledCourse, studentId, totalLessons);
                    } else {
                        // Thử cách 2: lessons collection với filter courseId
                        db.collection("lessons")
                                .whereEqualTo("courseId", courseId)
                                .get()
                                .addOnSuccessListener(altLessons -> {
                                    int altTotal = altLessons.size();
                                    android.util.Log.d("StudentMyCourses", "Method 2 - Total lessons: " + altTotal);

                                    if (altTotal > 0) {
                                        loadCompletedLessons(courseId, enrolledCourse, studentId, altTotal);
                                    } else {
                                        // Không tìm thấy lessons, set default và update UI
                                        setDefaultProgress(enrolledCourse);
                                        updateUIForCourse(enrolledCourse);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("StudentMyCourses", "Error in method 2", e);
                                    setDefaultProgress(enrolledCourse);
                                    updateUIForCourse(enrolledCourse);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentMyCourses", "Error in method 1", e);
                    setDefaultProgress(enrolledCourse);
                    updateUIForCourse(enrolledCourse);
                });
    }

    private void loadCompletedLessons(String courseId, EnrolledCourse enrolledCourse, String studentId, int totalLessons) {
        android.util.Log.d("StudentMyCourses", "Loading completed lessons for course: " + courseId);
        android.util.Log.d("StudentMyCourses", "Student ID: " + studentId);
        android.util.Log.d("StudentMyCourses", "Total lessons: " + totalLessons);

        // Thử collection chính: lesson_progress với field isCompleted
        tryProgressCollection(courseId, enrolledCourse, studentId, totalLessons, "lesson_progress", "isCompleted");
    }

    private void tryProgressCollection(String courseId, EnrolledCourse enrolledCourse, String studentId,
                                     int totalLessons, String collectionName, String completedField) {
        android.util.Log.d("StudentMyCourses", "Trying collection: " + collectionName + " with field: " + completedField);

        db.collection(collectionName)
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", courseId)
                .whereEqualTo(completedField, true)
                .get()
                .addOnSuccessListener(progressSnapshot -> {
                    int completedLessons = progressSnapshot.size();
                    android.util.Log.d("StudentMyCourses", "Found " + completedLessons + " completed lessons in " + collectionName);

                    if (completedLessons > 0 || collectionName.equals("lesson_progress")) {
                        // Có dữ liệu hoặc đây là collection chính
                        int progressPercentage = totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0;

                        android.util.Log.d("StudentMyCourses",
                            "Final Progress: " + completedLessons + "/" + totalLessons + " = " + progressPercentage + "%");

                        // Cập nhật thông tin
                        enrolledCourse.setTotalLessons(totalLessons);
                        enrolledCourse.setCompletedLessons(completedLessons);
                        enrolledCourse.setProgress(progressPercentage);

                        // Cập nhật UI ngay lập tức
                        updateUIForCourse(enrolledCourse);
                    } else {
                        // Thử collection khác nếu lesson_progress không có dữ liệu
                        if (collectionName.equals("lesson_progress")) {
                            tryProgressCollection(courseId, enrolledCourse, studentId, totalLessons, "lessonProgress", "completed");
                        } else if (collectionName.equals("lessonProgress")) {
                            tryProgressCollection(courseId, enrolledCourse, studentId, totalLessons, "studentProgress", "completed");
                        } else {
                            // Đã thử hết, set default
                            android.util.Log.w("StudentMyCourses", "No progress data found, setting default");
                            enrolledCourse.setTotalLessons(totalLessons);
                            enrolledCourse.setCompletedLessons(0);
                            enrolledCourse.setProgress(0);
                            updateUIForCourse(enrolledCourse);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentMyCourses", "Error loading " + collectionName, e);

                    // Thử collection tiếp theo
                    if (collectionName.equals("lesson_progress")) {
                        tryProgressCollection(courseId, enrolledCourse, studentId, totalLessons, "lessonProgress", "completed");
                    } else if (collectionName.equals("lessonProgress")) {
                        tryProgressCollection(courseId, enrolledCourse, studentId, totalLessons, "studentProgress", "completed");
                    } else {
                        // Set default nếu tất cả fail
                        enrolledCourse.setTotalLessons(totalLessons);
                        enrolledCourse.setCompletedLessons(0);
                        enrolledCourse.setProgress(0);
                        updateUIForCourse(enrolledCourse);
                    }
                });
    }

    private void setDefaultProgress(EnrolledCourse enrolledCourse) {
        android.util.Log.w("StudentMyCourses", "Setting default progress for: " +
            enrolledCourse.getCourse().getTitle());

        enrolledCourse.setTotalLessons(0);
        enrolledCourse.setCompletedLessons(0);
        enrolledCourse.setProgress(0);
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
        Intent intent = new Intent(this, StudentCourseLessonsActivity.class);
        intent.putExtra("courseId", enrolledCourse.getCourse().getId()); // Sửa từ "course_id" thành "courseId"
        intent.putExtra("courseTitle", enrolledCourse.getCourse().getTitle()); // Sửa từ "course_title" thành "courseTitle"
        intent.putExtra("courseCategory", enrolledCourse.getCourse().getCategory());
        startActivity(intent);
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
        // Reload dữ liệu khi quay lại màn hình này
        if (currentStudentId != null) {
            loadEnrolledCourses();
        }
    }

    // Thêm method debug để kiểm tra dữ liệu Firebase
    private void debugFirebaseData(String studentId) {
        android.util.Log.d("StudentMyCourses", "=== DEBUG FIREBASE DATA ===");
        android.util.Log.d("StudentMyCourses", "Current studentId: " + studentId);

        // Debug: Kiểm tra enrollments
        db.collection("enrollments")
                .whereEqualTo("studentID", studentId)
                .get()
                .addOnSuccessListener(enrollments -> {
                    android.util.Log.d("StudentMyCourses", "Total enrollments: " + enrollments.size());
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : enrollments) {
                        String status = doc.getString("status");
                        String courseId = doc.getString("courseID");
                        android.util.Log.d("StudentMyCourses", "Enrollment - CourseID: " + courseId + ", Status: " + status);
                    }
                });

        // Debug: Kiểm tra các collection progress
        String[] collections = {"lessonProgress", "studentProgress", "lesson_progress"};
        for (String collection : collections) {
            db.collection(collection)
                    .whereEqualTo("studentId", studentId)
                    .get()
                    .addOnSuccessListener(progress -> {
                        android.util.Log.d("StudentMyCourses", collection + " - Total records: " + progress.size());
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : progress) {
                            String courseId = doc.getString("courseId");
                            Object completed = doc.get("completed");
                            Object isCompleted = doc.get("isCompleted");
                            android.util.Log.d("StudentMyCourses", collection + " - CourseID: " + courseId +
                                ", completed: " + completed + ", isCompleted: " + isCompleted);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("StudentMyCourses", "Error checking " + collection, e);
                    });
        }
    }
}
