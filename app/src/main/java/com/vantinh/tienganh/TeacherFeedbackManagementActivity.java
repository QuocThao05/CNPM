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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class TeacherFeedbackManagementActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvFeedbacks;
    private LinearLayout tvNoFeedbacks; // Đổi từ TextView thành LinearLayout

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Feedback> feedbackList;
    private TeacherFeedbackAdapter feedbackAdapter;
    private String currentTeacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_feedback_management);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        feedbackList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadCurrentTeacherInfo();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvFeedbacks = findViewById(R.id.rv_feedbacks);
        tvNoFeedbacks = findViewById(R.id.tv_no_feedbacks);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý phản hồi");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        feedbackAdapter = new TeacherFeedbackAdapter(feedbackList, this::onFeedbackClick);
        rvFeedbacks.setLayoutManager(new LinearLayoutManager(this));
        rvFeedbacks.setAdapter(feedbackAdapter);
    }

    private void loadCurrentTeacherInfo() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String firebaseUid = mAuth.getCurrentUser().getUid();

        // Get teacher info từ users collection
        db.collection("users").document(firebaseUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentTeacherId = documentSnapshot.getString("id");
                        String role = documentSnapshot.getString("role");

                        if (!"teacher".equals(role)) {
                            Toast.makeText(this, "Chỉ giáo viên mới có thể truy cập", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        if (currentTeacherId != null) {
                            loadFeedbacks();
                        } else {
                            Toast.makeText(this, "Không tìm thấy thông tin giáo viên", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TeacherFeedback", "Error loading teacher info", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadFeedbacks() {
        android.util.Log.d("TeacherFeedback", "Loading feedbacks for teacher: " + currentTeacherId);

        // Load tất cả feedback cho các khóa học của giáo viên này
        db.collection("courses")
                .whereEqualTo("teacherId", currentTeacherId)
                .get()
                .addOnSuccessListener(coursesSnapshot -> {
                    if (coursesSnapshot.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    List<String> teacherCourseIds = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot courseDoc : coursesSnapshot) {
                        teacherCourseIds.add(courseDoc.getId());
                    }

                    android.util.Log.d("TeacherFeedback", "Found " + teacherCourseIds.size() + " courses for teacher");

                    // Load feedbacks cho các khóa học này
                    loadFeedbacksForCourses(teacherCourseIds);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TeacherFeedback", "Error loading teacher courses", e);
                    Toast.makeText(this, "Lỗi tải khóa học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void loadFeedbacksForCourses(List<String> courseIds) {
        if (courseIds.isEmpty()) {
            showEmptyState();
            return;
        }

        // Load feedbacks từ Firebase - không sắp xếp để tránh composite index
        db.collection("feedbacks")
                .whereIn("courseId", courseIds)
                .get()
                .addOnSuccessListener(feedbackSnapshot -> {
                    feedbackList.clear();

                    android.util.Log.d("TeacherFeedback", "Found " + feedbackSnapshot.size() + " feedbacks");

                    for (com.google.firebase.firestore.QueryDocumentSnapshot feedbackDoc : feedbackSnapshot) {
                        Feedback feedback = new Feedback();
                        feedback.setId(feedbackDoc.getId());
                        feedback.setCourseId(feedbackDoc.getString("courseId"));
                        feedback.setCourseName(feedbackDoc.getString("courseName"));
                        feedback.setMessage(feedbackDoc.getString("message"));
                        feedback.setStudentId(feedbackDoc.getString("studentId"));
                        feedback.setStudentName(feedbackDoc.getString("studentName"));
                        feedback.setStudentEmail(feedbackDoc.getString("studentEmail"));
                        feedback.setStatus(feedbackDoc.getString("status"));

                        // Safely get timestamp
                        com.google.firebase.Timestamp timestamp = feedbackDoc.getTimestamp("feedbackRequest");
                        if (timestamp != null) {
                            feedback.setFeedbackRequest(timestamp.toDate());
                        }

                        // Get teacher response if exists
                        feedback.setTeacherResponse(feedbackDoc.getString("teacherResponse"));
                        com.google.firebase.Timestamp responseTimestamp = feedbackDoc.getTimestamp("responseDate");
                        if (responseTimestamp != null) {
                            feedback.setResponseDate(responseTimestamp.toDate());
                        }

                        feedbackList.add(feedback);
                    }

                    // Sắp xếp trong code thay vì Firebase query
                    feedbackList.sort((f1, f2) -> {
                        if (f1.getFeedbackRequest() == null && f2.getFeedbackRequest() == null) return 0;
                        if (f1.getFeedbackRequest() == null) return 1;
                        if (f2.getFeedbackRequest() == null) return -1;
                        return f2.getFeedbackRequest().compareTo(f1.getFeedbackRequest()); // Mới nhất trước
                    });

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TeacherFeedback", "Error loading feedbacks", e);
                    Toast.makeText(this, "Lỗi tải phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void updateUI() {
        if (feedbackList.isEmpty()) {
            showEmptyState();
        } else {
            rvFeedbacks.setVisibility(View.VISIBLE);
            tvNoFeedbacks.setVisibility(View.GONE);
            feedbackAdapter.notifyDataSetChanged();

            android.util.Log.d("TeacherFeedback", "Displaying " + feedbackList.size() + " feedbacks");
        }
    }

    private void showEmptyState() {
        rvFeedbacks.setVisibility(View.GONE);
        tvNoFeedbacks.setVisibility(View.VISIBLE);
    }

    private void onFeedbackClick(Feedback feedback) {
        // Mở dialog hoặc activity để phản hồi
        FeedbackResponseDialog dialog = new FeedbackResponseDialog(this, feedback, new FeedbackResponseDialog.OnResponseListener() {
            @Override
            public void onResponseSent(String response) {
                updateFeedbackResponse(feedback, response);
            }
        });
        dialog.show();
    }

    private void updateFeedbackResponse(Feedback feedback, String response) {
        android.util.Log.d("TeacherFeedback", "Updating feedback response for: " + feedback.getId());

        // Update feedback in Firebase
        db.collection("feedbacks").document(feedback.getId())
                .update(
                    "teacherResponse", response,
                    "responseDate", com.google.firebase.Timestamp.now(),
                    "status", "responded"
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã gửi phản hồi thành công!", Toast.LENGTH_SHORT).show();

                    // Update local data
                    feedback.setTeacherResponse(response);
                    feedback.setResponseDate(new java.util.Date());
                    feedback.setStatus("responded");
                    feedbackAdapter.notifyDataSetChanged();

                    // Tạo notification cho học viên
                    createNotificationForStudent(feedback, response);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TeacherFeedback", "Error updating feedback response", e);
                    Toast.makeText(this, "Lỗi gửi phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createNotificationForStudent(Feedback feedback, String response) {
        // Tạo notification cho học viên về phản hồi từ giáo viên
        java.util.Map<String, Object> notification = new java.util.HashMap<>();
        notification.put("studentId", feedback.getStudentId());
        notification.put("title", "Phản hồi từ giáo viên");
        notification.put("message", "Giáo viên đã phản hồi đánh giá của bạn về khóa học: " + feedback.getCourseName());
        notification.put("type", "feedback_response");
        notification.put("feedbackId", feedback.getId());
        notification.put("courseId", feedback.getCourseId());
        notification.put("courseName", feedback.getCourseName());
        notification.put("teacherResponse", response);
        notification.put("createdAt", com.google.firebase.Timestamp.now());
        notification.put("isRead", false);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    android.util.Log.d("TeacherFeedback", "Notification created for student: " + feedback.getStudentId());
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TeacherFeedback", "Error creating notification", e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTeacherId != null) {
            loadFeedbacks(); // Refresh data when returning
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
