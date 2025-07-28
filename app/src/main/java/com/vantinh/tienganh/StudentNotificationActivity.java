package com.vantinh.tienganh;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class StudentNotificationActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvNotifications;
    private TextView tvNoNotifications;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<StudentNotification> notificationList;
    private StudentNotificationAdapter notificationAdapter;
    private String currentStudentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_notification);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        notificationList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadCurrentStudentInfo();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvNotifications = findViewById(R.id.rv_notifications);
        tvNoNotifications = findViewById(R.id.tv_no_notifications);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thông báo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        notificationAdapter = new StudentNotificationAdapter(notificationList, this::onNotificationClick);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notificationAdapter);
    }

    private void loadCurrentStudentInfo() {
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
                        currentStudentId = documentSnapshot.getString("id");
                        String role = documentSnapshot.getString("role");

                        if (!"student".equals(role)) {
                            Toast.makeText(this, "Chỉ học viên mới có thể xem thông báo", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        if (currentStudentId != null) {
                            loadNotifications();
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
                    android.util.Log.e("StudentNotification", "Error loading student info", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadNotifications() {
        android.util.Log.d("StudentNotification", "Loading notifications for student: " + currentStudentId);

        // Load notifications từ Firebase - sắp xếp theo thời gian mới nhất
        db.collection("notifications")
                .whereEqualTo("studentId", currentStudentId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(notificationSnapshot -> {
                    notificationList.clear();

                    android.util.Log.d("StudentNotification", "Found " + notificationSnapshot.size() + " notifications");

                    for (com.google.firebase.firestore.QueryDocumentSnapshot notificationDoc : notificationSnapshot) {
                        StudentNotification notification = new StudentNotification();
                        notification.setId(notificationDoc.getId());
                        notification.setTitle(notificationDoc.getString("title"));
                        notification.setMessage(notificationDoc.getString("message"));
                        notification.setType(notificationDoc.getString("type"));
                        notification.setStudentId(notificationDoc.getString("studentId"));
                        notification.setFeedbackId(notificationDoc.getString("feedbackId"));
                        notification.setCourseId(notificationDoc.getString("courseId"));
                        notification.setCourseName(notificationDoc.getString("courseName"));
                        notification.setTeacherResponse(notificationDoc.getString("teacherResponse"));

                        // Safely get timestamp
                        com.google.firebase.Timestamp timestamp = notificationDoc.getTimestamp("createdAt");
                        if (timestamp != null) {
                            notification.setCreatedAt(timestamp.toDate());
                        }

                        // Get read status
                        Boolean isRead = notificationDoc.getBoolean("isRead");
                        notification.setRead(isRead != null ? isRead : false);

                        notificationList.add(notification);
                    }

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentNotification", "Error loading notifications", e);
                    Toast.makeText(this, "Lỗi tải thông báo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void updateUI() {
        if (notificationList.isEmpty()) {
            showEmptyState();
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            tvNoNotifications.setVisibility(View.GONE);
            notificationAdapter.notifyDataSetChanged();

            android.util.Log.d("StudentNotification", "Displaying " + notificationList.size() + " notifications");
        }
    }

    private void showEmptyState() {
        rvNotifications.setVisibility(View.GONE);
        tvNoNotifications.setVisibility(View.VISIBLE);
    }

    private void onNotificationClick(StudentNotification notification) {
        // Đánh dấu là đã đọc
        markAsRead(notification);

        // Hiển thị chi tiết thông báo
        if ("feedback_response".equals(notification.getType())) {
            showFeedbackResponseDialog(notification);
        }
    }

    private void markAsRead(StudentNotification notification) {
        if (!notification.isRead()) {
            db.collection("notifications").document(notification.getId())
                    .update("isRead", true)
                    .addOnSuccessListener(aVoid -> {
                        notification.setRead(true);
                        notificationAdapter.notifyDataSetChanged();
                        android.util.Log.d("StudentNotification", "Marked notification as read: " + notification.getId());
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("StudentNotification", "Error marking notification as read", e);
                    });
        }
    }

    private void showFeedbackResponseDialog(StudentNotification notification) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Phản hồi từ giáo viên");

        String message = "Khóa học: " + notification.getCourseName() + "\n\n" +
                        "Phản hồi của giáo viên:\n" + notification.getTeacherResponse();

        builder.setMessage(message);
        builder.setPositiveButton("Đóng", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentStudentId != null) {
            loadNotifications(); // Refresh data when returning
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
