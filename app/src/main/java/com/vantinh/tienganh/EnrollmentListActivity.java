package com.vantinh.tienganh;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnrollmentListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvEnrollments;
    private LinearLayout layoutEmpty;
    private TextView tvTotalCount, tvEmptyMessage;
    private Button fabRefresh;

    private FirebaseFirestore db;
    private List<EnrollmentStudent> enrollmentStudents;
    private EnrollmentStudentAdapter adapter;

    private String teacherId;
    private String status;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_list);

        initViews();
        getIntentData();
        setupToolbar();
        setupRecyclerView();
        initFirebase();
        setupFabRefresh();
        loadEnrollmentData();
        addEntranceAnimations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvEnrollments = findViewById(R.id.rv_enrollments);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvTotalCount = findViewById(R.id.tv_total_count);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        fabRefresh = findViewById(R.id.fab_refresh);
    }

    private void getIntentData() {
        teacherId = getIntent().getStringExtra("teacherId");
        status = getIntent().getStringExtra("status");
        title = getIntent().getStringExtra("title");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        enrollmentStudents = new ArrayList<>();
        adapter = new EnrollmentStudentAdapter(enrollmentStudents);
        rvEnrollments.setLayoutManager(new LinearLayoutManager(this));
        rvEnrollments.setAdapter(adapter);

        // Add item animation
        rvEnrollments.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupFabRefresh() {
        fabRefresh.setOnClickListener(v -> {
            // Animate FAB
            fabRefresh.animate()
                    .rotation(360f)
                    .setDuration(500)
                    .withEndAction(() -> {
                        fabRefresh.setRotation(0f);
                        loadEnrollmentData();
                    })
                    .start();
        });
    }

    private void addEntranceAnimations() {
        // Fade in animations
        View headerCard = findViewById(R.id.header_card);
        headerCard.setAlpha(0f);
        rvEnrollments.setAlpha(0f);
        fabRefresh.setAlpha(0f);

        headerCard.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(100)
                .start();

        rvEnrollments.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(200)
                .start();

        fabRefresh.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(300)
                .start();
    }

    private void loadEnrollmentData() {
        Log.d("EnrollmentList", "Loading enrollment data for teacherId: " + teacherId + ", status: " + status);

        // Lấy danh sách khóa học của giáo viên trước
        db.collection("courses")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> courseIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        courseIds.add(document.getId());
                    }

                    Log.d("EnrollmentList", "Found " + courseIds.size() + " courses for teacher");

                    if (courseIds.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    loadStudentsByStatus(courseIds);
                })
                .addOnFailureListener(e -> {
                    Log.e("EnrollmentList", "Error loading courses", e);
                    showEmptyState();
                });
    }

    private void loadStudentsByStatus(List<String> courseIds) {
        final List<EnrollmentStudent> allStudents = new ArrayList<>();
        final Set<String> uniqueStudents = new HashSet<>(); // Để tránh trùng lặp

        for (String courseId : courseIds) {
            db.collection("courseRequests")
                    .whereEqualTo("courseId", courseId)
                    .whereEqualTo("status", status)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("EnrollmentList", "Found " + queryDocumentSnapshots.size() + " requests for course: " + courseId);

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String studentId = document.getString("studentId");
                            String studentName = document.getString("studentName");
                            String courseName = document.getString("courseName");

                            // Tạo key unique dựa trên studentId và courseId để tránh trùng lặp
                            String uniqueKey = studentId + "_" + courseId;

                            if (!uniqueStudents.contains(uniqueKey)) {
                                uniqueStudents.add(uniqueKey);
                                EnrollmentStudent student = new EnrollmentStudent(
                                        studentId, studentName, courseId, courseName, status
                                );
                                allStudents.add(student);
                            }
                        }

                        // Cập nhật UI sau khi xử lý tất cả courses
                        updateUI(allStudents);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EnrollmentList", "Error loading requests for course: " + courseId, e);
                    });
        }
    }

    private void updateUI(List<EnrollmentStudent> students) {
        runOnUiThread(() -> {
            enrollmentStudents.clear();
            enrollmentStudents.addAll(students);
            adapter.updateData(enrollmentStudents);

            // Animate count update
            animateCountUpdate(students.size());

            if (students.isEmpty()) {
                showEmptyState();
            } else {
                showDataState();
            }

            Log.d("EnrollmentList", "Updated UI with " + students.size() + " students");
        });
    }

    private void animateCountUpdate(int newCount) {
        // Scale animation for count
        tvTotalCount.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(200)
                .withEndAction(() -> {
                    tvTotalCount.setText(String.valueOf(newCount));
                    tvTotalCount.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    private void showEmptyState() {
        rvEnrollments.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);

        String message = "Không có học viên nào ";
        if ("approved".equals(status)) {
            message += "đã được duyệt";
        } else if ("rejected".equals(status)) {
            message += "đã bị từ chối";
        }

        tvEmptyMessage.setText(message);

        // Animate empty state
        layoutEmpty.setAlpha(0f);
        layoutEmpty.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
    }

    private void showDataState() {
        rvEnrollments.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
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
