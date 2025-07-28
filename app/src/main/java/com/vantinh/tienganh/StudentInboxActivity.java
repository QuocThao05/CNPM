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

public class StudentInboxActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvInbox;
    private LinearLayout layoutNoMessages;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<InboxMessage> messageList;
    private StudentInboxAdapter inboxAdapter;
    private String currentStudentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_inbox);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        messageList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadCurrentStudentInfo();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvInbox = findViewById(R.id.rv_inbox);
        layoutNoMessages = findViewById(R.id.layout_no_messages);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hộp thư");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        inboxAdapter = new StudentInboxAdapter(messageList, this::onMessageClick);
        rvInbox.setLayoutManager(new LinearLayoutManager(this));
        rvInbox.setAdapter(inboxAdapter);
    }

    private void loadCurrentStudentInfo() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String firebaseUid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(firebaseUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentStudentId = documentSnapshot.getString("id");
                        String role = documentSnapshot.getString("role");

                        if (!"student".equals(role)) {
                            Toast.makeText(this, "Chỉ học viên mới có thể xem hộp thư", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        if (currentStudentId != null) {
                            loadInboxMessages();
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
                    android.util.Log.e("StudentInbox", "Error loading user info", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadInboxMessages() {
        android.util.Log.d("StudentInbox", "Loading inbox messages for student: " + currentStudentId);

        // Load notifications và feedback responses cho học viên
        loadNotifications();
        loadFeedbackResponses();
    }

    private void loadNotifications() {
        db.collection("notifications")
                .whereEqualTo("studentId", currentStudentId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(notificationsSnapshot -> {
                    android.util.Log.d("StudentInbox", "Found " + notificationsSnapshot.size() + " notifications");

                    for (com.google.firebase.firestore.QueryDocumentSnapshot notifDoc : notificationsSnapshot) {
                        InboxMessage message = new InboxMessage();
                        message.setId(notifDoc.getId());
                        message.setType("notification");
                        message.setTitle(notifDoc.getString("title"));
                        message.setMessage(notifDoc.getString("message"));
                        message.setFromType("system");
                        message.setFromName("Hệ thống");

                        com.google.firebase.Timestamp timestamp = notifDoc.getTimestamp("createdAt");
                        if (timestamp != null) {
                            message.setCreatedAt(timestamp.toDate());
                        }

                        Boolean isRead = notifDoc.getBoolean("isRead");
                        message.setRead(isRead != null ? isRead : false);

                        // Additional data
                        message.setCourseId(notifDoc.getString("courseId"));
                        message.setCourseName(notifDoc.getString("courseName"));

                        messageList.add(message);
                    }

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentInbox", "Error loading notifications", e);
                    updateUI();
                });
    }

    private void loadFeedbackResponses() {
        android.util.Log.d("StudentInbox", "Loading feedback responses for student: " + currentStudentId);

        // Load feedback responses từ giáo viên - bỏ orderBy để tránh composite index
        db.collection("feedbacks")
                .whereEqualTo("studentId", currentStudentId)
                .whereEqualTo("status", "responded")
                .get()
                .addOnSuccessListener(feedbacksSnapshot -> {
                    android.util.Log.d("StudentInbox", "Found " + feedbacksSnapshot.size() + " feedback responses");

                    for (com.google.firebase.firestore.QueryDocumentSnapshot feedbackDoc : feedbacksSnapshot) {
                        String teacherResponse = feedbackDoc.getString("teacherResponse");

                        // Chỉ thêm nếu có teacherResponse
                        if (teacherResponse != null && !teacherResponse.trim().isEmpty()) {
                            InboxMessage message = new InboxMessage();
                            message.setId(feedbackDoc.getId());
                            message.setType("feedback_response");
                            message.setTitle("Phản hồi từ giáo viên về khóa học: " + feedbackDoc.getString("courseName"));
                            message.setMessage(teacherResponse);
                            message.setFromType("teacher");
                            message.setFromName("Giáo viên");

                            com.google.firebase.Timestamp timestamp = feedbackDoc.getTimestamp("responseDate");
                            if (timestamp != null) {
                                message.setCreatedAt(timestamp.toDate());
                            } else {
                                // Fallback to current time if no responseDate
                                message.setCreatedAt(new java.util.Date());
                            }

                            // Check if already read by student
                            Boolean readByStudent = feedbackDoc.getBoolean("readByStudent");
                            message.setRead(readByStudent != null ? readByStudent : false);

                            // Additional feedback data
                            message.setCourseId(feedbackDoc.getString("courseId"));
                            message.setCourseName(feedbackDoc.getString("courseName"));
                            message.setOriginalFeedback(feedbackDoc.getString("message"));

                            messageList.add(message);

                            android.util.Log.d("StudentInbox", "Added feedback response: " + message.getTitle());
                        }
                    }

                    // Sắp xếp lại theo thời gian trong code
                    messageList.sort((m1, m2) -> {
                        if (m1.getCreatedAt() == null && m2.getCreatedAt() == null) return 0;
                        if (m1.getCreatedAt() == null) return 1;
                        if (m2.getCreatedAt() == null) return -1;
                        return m2.getCreatedAt().compareTo(m1.getCreatedAt()); // Mới nhất trước
                    });

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentInbox", "Error loading feedback responses", e);

                    // Thử load với cách khác nếu query trên thất bại
                    loadFeedbackResponsesAlternative();
                });
    }

    private void loadFeedbackResponsesAlternative() {
        android.util.Log.d("StudentInbox", "Trying alternative method to load feedback responses");

        // Thử load tất cả feedbacks của student và filter trong code
        db.collection("feedbacks")
                .whereEqualTo("studentId", currentStudentId)
                .get()
                .addOnSuccessListener(feedbacksSnapshot -> {
                    android.util.Log.d("StudentInbox", "Alternative method found " + feedbacksSnapshot.size() + " feedbacks");

                    int responseCount = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot feedbackDoc : feedbacksSnapshot) {
                        String status = feedbackDoc.getString("status");
                        String teacherResponse = feedbackDoc.getString("teacherResponse");

                        // Filter trong code: có teacherResponse và status = "responded"
                        if ("responded".equals(status) && teacherResponse != null && !teacherResponse.trim().isEmpty()) {
                            InboxMessage message = new InboxMessage();
                            message.setId(feedbackDoc.getId());
                            message.setType("feedback_response");
                            message.setTitle("Phản hồi từ giáo viên về khóa học: " + feedbackDoc.getString("courseName"));
                            message.setMessage(teacherResponse);
                            message.setFromType("teacher");
                            message.setFromName("Giáo viên");

                            com.google.firebase.Timestamp timestamp = feedbackDoc.getTimestamp("responseDate");
                            if (timestamp != null) {
                                message.setCreatedAt(timestamp.toDate());
                            } else {
                                message.setCreatedAt(new java.util.Date());
                            }

                            Boolean readByStudent = feedbackDoc.getBoolean("readByStudent");
                            message.setRead(readByStudent != null ? readByStudent : false);

                            message.setCourseId(feedbackDoc.getString("courseId"));
                            message.setCourseName(feedbackDoc.getString("courseName"));
                            message.setOriginalFeedback(feedbackDoc.getString("message"));

                            messageList.add(message);
                            responseCount++;

                            android.util.Log.d("StudentInbox", "Added feedback response (alt): " + message.getTitle());
                        }
                    }

                    android.util.Log.d("StudentInbox", "Total responses added: " + responseCount);

                    // Sắp xếp lại theo thời gian
                    messageList.sort((m1, m2) -> {
                        if (m1.getCreatedAt() == null && m2.getCreatedAt() == null) return 0;
                        if (m1.getCreatedAt() == null) return 1;
                        if (m2.getCreatedAt() == null) return -1;
                        return m2.getCreatedAt().compareTo(m1.getCreatedAt());
                    });

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentInbox", "Alternative method also failed", e);
                    updateUI(); // Vẫn hiển thị notifications nếu có
                });
    }

    private void updateUI() {
        if (messageList.isEmpty()) {
            showEmptyState();
        } else {
            rvInbox.setVisibility(View.VISIBLE);
            layoutNoMessages.setVisibility(View.GONE);
            inboxAdapter.notifyDataSetChanged();

            android.util.Log.d("StudentInbox", "Displaying " + messageList.size() + " messages");
        }
    }

    private void showEmptyState() {
        rvInbox.setVisibility(View.GONE);
        layoutNoMessages.setVisibility(View.VISIBLE);
    }

    private void onMessageClick(InboxMessage message) {
        // Hiển thị dialog chi tiết tin nhắn
        StudentInboxDetailDialog dialog = new StudentInboxDetailDialog(this, message, new StudentInboxDetailDialog.OnMessageActionListener() {
            @Override
            public void onMarkAsRead(String messageId) {
                markMessageAsRead(messageId, message.getType());
            }

            @Override
            public void onViewCourse(String courseId) {
                android.util.Log.d("StudentInbox", "onViewCourse called with courseId: " + courseId);

                if (courseId == null || courseId.trim().isEmpty()) {
                    Toast.makeText(StudentInboxActivity.this, "Không có thông tin khóa học", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Chuyển đến StudentCourseDetailActivity với đúng tên extra parameters
                android.content.Intent intent = new android.content.Intent(StudentInboxActivity.this, StudentCourseDetailActivity.class);
                intent.putExtra("course_id", courseId);
                intent.putExtra("course_title", message.getCourseName() != null ? message.getCourseName() : "Chi tiết khóa học");

                android.util.Log.d("StudentInbox", "Starting StudentCourseDetailActivity with courseId: " + courseId + ", courseName: " + message.getCourseName());

                startActivity(intent);
            }
        });

        dialog.show();
    }

    private void markMessageAsRead(String messageId, String messageType) {
        android.util.Log.d("StudentInbox", "Marking message as read: " + messageId + " (type: " + messageType + ")");

        if ("notification".equals(messageType)) {
            // Mark notification as read
            db.collection("notifications").document(messageId)
                    .update("isRead", true)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("StudentInbox", "Notification marked as read");
                        // Update local data
                        for (InboxMessage msg : messageList) {
                            if (msg.getId().equals(messageId)) {
                                msg.setRead(true);
                                break;
                            }
                        }
                        inboxAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("StudentInbox", "Error marking notification as read", e);
                        Toast.makeText(this, "Lỗi đánh dấu đã đọc", Toast.LENGTH_SHORT).show();
                    });
        } else if ("feedback_response".equals(messageType)) {
            // Mark feedback response as read by student
            db.collection("feedbacks").document(messageId)
                    .update("readByStudent", true)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("StudentInbox", "Feedback response marked as read");
                        // Update local data
                        for (InboxMessage msg : messageList) {
                            if (msg.getId().equals(messageId)) {
                                msg.setRead(true);
                                break;
                            }
                        }
                        inboxAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("StudentInbox", "Error marking feedback response as read", e);
                        Toast.makeText(this, "Lỗi đánh dấu đã đọc", Toast.LENGTH_SHORT).show();
                    });
        }
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
        // Reload messages when returning to this activity
        if (currentStudentId != null) {
            messageList.clear();
            loadInboxMessages();
        }
    }
}
