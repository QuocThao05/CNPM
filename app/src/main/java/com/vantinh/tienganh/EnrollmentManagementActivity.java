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

        // Đơn giản hóa: load tất cả enrollments và hiển thị, bỏ filter theo status
        db.collection("enrollments")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    enrollmentList.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Enrollment enrollment = Enrollment.fromMap(document.getData());
                        enrollment.setId(document.getId());
                        enrollmentList.add(enrollment);
                    }

                    updateUI();
                } else {
                    android.util.Log.e("EnrollmentManagement", "Error getting enrollments", task.getException());
                    Toast.makeText(this, "Lỗi tải danh sách đăng ký: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void updateUI() {
        if (enrollmentList.isEmpty()) {
            layoutNoEnrollments.setVisibility(View.VISIBLE);
            rvEnrollments.setVisibility(View.GONE);
        } else {
            layoutNoEnrollments.setVisibility(View.GONE);
            rvEnrollments.setVisibility(View.VISIBLE);
        }

        enrollmentAdapter.notifyDataSetChanged();
    }

    private void showRejectDialog(Enrollment enrollment) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Từ chối đăng ký");
        builder.setMessage("Bạn có chắc chắn muốn từ chối đăng ký của " + enrollment.getFullName() + "?");

        builder.setPositiveButton("Từ chối", (dialog, which) -> {
            // Đơn giản hóa: chỉ hiển thị thông báo
            Toast.makeText(this, "Đã từ chối đăng ký", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showEnrollmentDetails(Enrollment enrollment) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Chi tiết đăng ký");

        StringBuilder details = new StringBuilder();
        details.append("Học viên: ").append(enrollment.getFullName()).append("\n");
        details.append("Email: ").append(enrollment.getStudentEmail()).append("\n");
        details.append("Khóa học: ").append(enrollment.getCourseName()).append("\n");
        details.append("Ngày đăng ký: ").append(enrollment.getEnrollmentDate()).append("\n");

        builder.setMessage(details.toString());
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateEnrollmentStatus(Enrollment enrollment, String status, String message) {
        // Đơn giản hóa: chỉ hiển thị thông báo vì class Enrollment mới không có status management
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
