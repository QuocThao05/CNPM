package com.vantinh.tienganh;

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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EnrollmentManagementActivity extends AppCompatActivity {

    private RecyclerView rvEnrollments;
    private TabLayout tabLayout;
    private LinearLayout layoutNoEnrollments;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Enrollment> enrollmentList;
    private EnrollmentAdapter enrollmentAdapter;
    private String currentTab = "PENDING"; // PENDING, APPROVED, REJECTED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_management);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        enrollmentList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        loadEnrollments();
        addAnimations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvEnrollments = findViewById(R.id.rv_enrollments);
        tabLayout = findViewById(R.id.tab_layout);
        layoutNoEnrollments = findViewById(R.id.layout_no_enrollments);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý đăng ký");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Chờ duyệt"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã duyệt"));
        tabLayout.addTab(tabLayout.newTab().setText("Từ chối"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "PENDING";
                        break;
                    case 1:
                        currentTab = "APPROVED";
                        break;
                    case 2:
                        currentTab = "REJECTED";
                        break;
                }
                loadEnrollments();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        enrollmentAdapter = new EnrollmentAdapter(enrollmentList, new EnrollmentAdapter.OnEnrollmentActionListener() {
            @Override
            public void onApprove(Enrollment enrollment) {
                updateEnrollmentStatus(enrollment, "APPROVED", "Đăng ký đã được chấp nhận");
            }

            @Override
            public void onReject(Enrollment enrollment) {
                showRejectDialog(enrollment);
            }

            @Override
            public void onViewDetails(Enrollment enrollment) {
                showEnrollmentDetails(enrollment);
            }
        });
        rvEnrollments.setLayoutManager(new LinearLayoutManager(this));
        rvEnrollments.setAdapter(enrollmentAdapter);
    }

    private void addAnimations() {
        rvEnrollments.setAlpha(0f);
        rvEnrollments.animate()
            .alpha(1f)
            .setDuration(500)
            .start();
    }

    private void loadEnrollments() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("enrollments")
            .whereEqualTo("teacherId", currentUserId)
            .whereEqualTo("status", currentTab)
            .orderBy("enrollmentDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    enrollmentList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Enrollment enrollment = document.toObject(Enrollment.class);
                        enrollment.setId(document.getId());
                        enrollmentList.add(enrollment);
                    }

                    if (enrollmentList.isEmpty()) {
                        layoutNoEnrollments.setVisibility(View.VISIBLE);
                        rvEnrollments.setVisibility(View.GONE);
                    } else {
                        layoutNoEnrollments.setVisibility(View.GONE);
                        rvEnrollments.setVisibility(View.VISIBLE);
                    }

                    enrollmentAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Lỗi khi tải danh sách đăng ký", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showRejectDialog(Enrollment enrollment) {
        android.widget.EditText editText = new android.widget.EditText(this);
        editText.setHint("Lý do từ chối (tùy chọn)");
        editText.setLines(3);

        new android.app.AlertDialog.Builder(this)
            .setTitle("Từ chối đăng ký")
            .setMessage("Bạn có chắc chắn muốn từ chối đăng ký của " + enrollment.getStudentName() + "?")
            .setView(editText)
            .setPositiveButton("Từ chối", (dialog, which) -> {
                String reason = editText.getText().toString().trim();
                if (reason.isEmpty()) {
                    reason = "Không đủ điều kiện tham gia khóa học";
                }
                updateEnrollmentStatus(enrollment, "REJECTED", reason);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showEnrollmentDetails(Enrollment enrollment) {
        String details = "Thông tin đăng ký:\n\n" +
                        "Học viên: " + enrollment.getStudentName() + "\n" +
                        "Email: " + enrollment.getStudentEmail() + "\n" +
                        "Khóa học: " + enrollment.getCourseName() + "\n" +
                        "Ngày đăng ký: " + android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", enrollment.getEnrollmentDate()) + "\n" +
                        "Trạng thái: " + enrollment.getStatusDisplayName();

        if (enrollment.getMessage() != null && !enrollment.getMessage().isEmpty()) {
            details += "\nTin nhắn: " + enrollment.getMessage();
        }

        if (enrollment.getApprovedDate() != null) {
            details += "\nNgày duyệt: " + android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", enrollment.getApprovedDate());
        }

        new android.app.AlertDialog.Builder(this)
            .setTitle("Chi tiết đăng ký")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show();
    }

    private void updateEnrollmentStatus(Enrollment enrollment, String newStatus, String message) {
        if (enrollment.getId() == null) return;

        // Update enrollment status
        db.collection("enrollments").document(enrollment.getId())
            .update(
                "status", newStatus,
                "message", message,
                "approvedDate", new Date()
            )
            .addOnSuccessListener(aVoid -> {
                String statusMessage = newStatus.equals("APPROVED") ?
                    "Đã duyệt đăng ký của " + enrollment.getStudentName() :
                    "Đã từ chối đăng ký của " + enrollment.getStudentName();

                Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();

                // Update course enrolled students count if approved
                if (newStatus.equals("APPROVED")) {
                    updateCourseEnrollmentCount(enrollment.getCourseId(), 1);
                }

                // Reload data
                loadEnrollments();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateCourseEnrollmentCount(String courseId, int increment) {
        db.collection("courses").document(courseId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Long currentCount = documentSnapshot.getLong("enrolledStudents");
                    int newCount = (currentCount != null ? currentCount.intValue() : 0) + increment;

                    db.collection("courses").document(courseId)
                        .update("enrolledStudents", Math.max(0, newCount));
                }
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
        loadEnrollments();
    }
}
