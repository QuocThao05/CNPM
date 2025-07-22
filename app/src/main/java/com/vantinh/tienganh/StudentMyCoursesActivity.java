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

        // Load approved enrollments từ collection "enrollments"
        db.collection("enrollments")
                .whereEqualTo("studentId", currentStudentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    enrolledCourseList.clear();

                    android.util.Log.d("StudentMyCourses", "Found " + queryDocumentSnapshots.size() + " enrollments");

                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    // Với mỗi enrollment, load thông tin course tương ứng
                    for (QueryDocumentSnapshot enrollmentDoc : queryDocumentSnapshots) {
                        String courseId = enrollmentDoc.getString("courseId");
                        String courseName = enrollmentDoc.getString("courseName");
                        String enrollmentDate = enrollmentDoc.getTimestamp("enrollmentDate") != null ?
                            enrollmentDoc.getTimestamp("enrollmentDate").toDate().toString() : "";

                        if (courseId != null) {
                            loadCourseDetails(courseId, courseName, enrollmentDate, enrollmentDoc.getId());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentMyCourses", "Error loading enrollments", e);
                    Toast.makeText(this, "Lỗi tải khóa học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void loadCourseDetails(String courseId, String courseName, String enrollmentDate, String enrollmentId) {
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
                            enrolledCourse.setProgress(0); //

                            enrolledCourseList.add(enrolledCourse);

                            // Update UI
                            updateUI();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentMyCourses", "Error loading course details for: " + courseId, e);
                });
    }

    private void updateUI() {
        if (enrolledCourseList.isEmpty()) {
            showEmptyState();
        } else {
            layoutNoCourses.setVisibility(View.GONE);
            rvMyCourses.setVisibility(View.VISIBLE);
            courseAdapter.notifyDataSetChanged();

            android.util.Log.d("StudentMyCourses", "Showing " + enrolledCourseList.size() + " enrolled courses");
        }
    }

    private void showEmptyState() {
        layoutNoCourses.setVisibility(View.VISIBLE);
        rvMyCourses.setVisibility(View.GONE);
    }

    @Override
    public void onCourseClick(EnrolledCourse enrolledCourse) {
        // Navigate to course learning activity
        Intent intent = new Intent(this, StudentCourseDetailActivity.class);
        intent.putExtra("courseId", enrolledCourse.getCourse().getId());
        intent.putExtra("courseTitle", enrolledCourse.getCourse().getTitle());
        intent.putExtra("courseCategory", enrolledCourse.getCourse().getCategory());
        intent.putExtra("enrollmentId", enrolledCourse.getEnrollmentId());
        startActivity(intent);
    }

    @Override
    public void onContinueLearning(EnrolledCourse enrolledCourse) {
        // Navigate to lessons list
        Intent intent = new Intent(this, StudentCourseLessonsActivity.class);
        intent.putExtra("courseId", enrolledCourse.getCourse().getId());
        intent.putExtra("courseTitle", enrolledCourse.getCourse().getTitle());
        intent.putExtra("courseCategory", enrolledCourse.getCourse().getCategory());
        startActivity(intent);
    }

    @Override
    public void onViewProgress(EnrolledCourse enrolledCourse) {
        // Navigate to progress view
        Intent intent = new Intent(this, StudentProgressDetailActivity.class);
        intent.putExtra("courseId", enrolledCourse.getCourse().getId());
        intent.putExtra("courseTitle", enrolledCourse.getCourse().getTitle());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentStudentId != null) {
            loadEnrolledCourses(); // Reload when returning to this activity
        }
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
