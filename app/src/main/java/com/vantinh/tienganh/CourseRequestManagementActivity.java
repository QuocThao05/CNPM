package com.vantinh.tienganh;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.vantinh.tienganh.utils.RealtimeManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseRequestManagementActivity extends AppCompatActivity implements CourseRequestAdapter.OnRequestActionListener {

    private RecyclerView recyclerView;
    private CourseRequestAdapter adapter;
    private List<CourseRequest> requestList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RealtimeManager realtimeManager;
    private String currentTeacherId; // Đổi từ currentTeacherName sang currentTeacherId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_request_management);

        initViews();
        setupToolbar();
        initFirebase();
        getCurrentTeacherInfo();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_requests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new CourseRequestAdapter(requestList, this);
        recyclerView.setAdapter(adapter);

        // Bỏ các debug buttons - không cần thiết nữa
    }

    // Debug method được cải thiện để kiểm tra tất cả dữ liệu
    private void checkAllDataInFirebase() {
        Log.d("CourseRequestManagement", "=== DEBUGGING: Current teacherId: " + currentTeacherId + " ===");

        // Bước 1: Kiểm tra tất cả courseRequests trước
        db.collection("courseRequests")
                .get()
                .addOnSuccessListener(allRequestDocs -> {
                    Log.d("CourseRequestManagement", "TOTAL courseRequests in Firebase: " + allRequestDocs.size());

                    // Hiển thị tất cả requests để debug
                    StringBuilder allRequestsInfo = new StringBuilder();
                    int matchingCount = 0;

                    for (DocumentSnapshot doc : allRequestDocs.getDocuments()) {
                        String docTeacherId = doc.getString("teacherId");
                        String studentName = doc.getString("studentName");
                        String courseName = doc.getString("courseName");
                        String status = doc.getString("status");

                        Log.d("CourseRequestManagement", "Request: " + studentName +
                            " -> " + courseName + " [status: " + status + "] [teacherId: " + docTeacherId + "]");

                        allRequestsInfo.append("• ").append(studentName).append(" - ").append(courseName)
                            .append(" (Teacher: ").append(docTeacherId).append(")\n");

                        // Kiểm tra xem có match với current teacherId không
                        if (currentTeacherId != null && currentTeacherId.equals(docTeacherId)) {
                            matchingCount++;
                        }
                    }

                    // Hiển thị kết quả debug
                    final int finalMatchingCount = matchingCount; // Tạo biến final để dùng trong lambda
                    final String debugMessage = "=== DEBUG INFO ===\n" +
                        "Current Teacher ID: " + currentTeacherId + "\n" +
                        "Total Requests: " + allRequestDocs.size() + "\n" +
                        "Matching Requests: " + finalMatchingCount + "\n\n" +
                        "All Requests:\n" + allRequestsInfo.toString();

                    runOnUiThread(() -> {
                        // Hiển thị trong dialog để dễ đọc
                        new androidx.appcompat.app.AlertDialog.Builder(CourseRequestManagementActivity.this)
                            .setTitle("Firebase Debug Info")
                            .setMessage(debugMessage)
                            .setPositiveButton("OK", null)
                            .show();

                        Toast.makeText(CourseRequestManagementActivity.this,
                            "DEBUG: Found " + allRequestDocs.size() + " total, " + finalMatchingCount + " matching",
                            Toast.LENGTH_LONG).show();
                    });

                    // Bước 2: Kiểm tra courses collection để verify teacherId
                    checkTeacherCourses();
                })
                .addOnFailureListener(e -> {
                    Log.e("CourseRequestManagement", "Error checking all courseRequests", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "DEBUG ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                });
    }

    // Method mới để kiểm tra courses của teacher
    private void checkTeacherCourses() {
        Log.d("CourseRequestManagement", "=== DEBUGGING: Checking teacher's courses ===");

        db.collection("courses")
                .whereEqualTo("teacherId", currentTeacherId)
                .get()
                .addOnSuccessListener(courseDocs -> {
                    Log.d("CourseRequestManagement", "Found " + courseDocs.size() + " courses for teacherId: " + currentTeacherId);

                    StringBuilder coursesInfo = new StringBuilder();
                    for (DocumentSnapshot doc : courseDocs.getDocuments()) {
                        String title = doc.getString("title");
                        String courseId = doc.getId();
                        coursesInfo.append("• ").append(title).append(" (ID: ").append(courseId).append(")\n");

                        Log.d("CourseRequestManagement", "Teacher's course: " + title + " [ID: " + courseId + "]");
                    }

                    runOnUiThread(() -> {
                        if (courseDocs.isEmpty()) {
                            Toast.makeText(this, "DEBUG: Teacher has NO courses!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "DEBUG: Teacher has " + courseDocs.size() + " courses", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("CourseRequestManagement", "Error checking teacher courses", e);
                });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Yêu cầu tham gia khóa học");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        realtimeManager = RealtimeManager.getInstance();
    }

    private void getCurrentTeacherInfo() {
        String currentUserId = auth.getCurrentUser().getUid();
        currentTeacherId = currentUserId; // Sử dụng Firebase UID làm teacherId

        Log.d("CourseRequestManagement", "Current teacherId: " + currentTeacherId);
        setupRealtimeRequests(); // Setup ngay lập tức với teacherId
    }

    // Sử dụng RealtimeManager với teacherId
    private void setupRealtimeRequests() {
        if (currentTeacherId == null) {
            Log.e("CourseRequestManagement", "TeacherId is null - cannot setup listeners");
            Toast.makeText(this, "Không thể tải thông tin giáo viên", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("CourseRequestManagement", "Setting up real-time listener for teacherId: '" + currentTeacherId + "'");

        // Load trực tiếp với teacherId
        loadRequestsDirectly();

        // TẠM THỜI TẮT RealtimeManager để debug
        /*
        realtimeManager.listenToCourseRequests(currentTeacherId,
            new RealtimeManager.OnDataChangeListener<CourseRequest>() {
                @Override
                public void onDataChanged(List<CourseRequest> data) {
                    Log.d("CourseRequestManagement", "Real-time update received: " + data.size() + " requests");

                    requestList.clear();
                    requestList.addAll(data);

                    // Update UI với animation
                    runOnUiThread(() -> {
                        adapter.updateList(requestList);

                        Log.d("CourseRequestManagement", "Updated RecyclerView with " + requestList.size() + " items");

                        // Fade in animation cho RecyclerView
                        recyclerView.setAlpha(0f);
                        recyclerView.animate()
                                .alpha(1f)
                                .setDuration(300)
                                .start();
                    });

                    // Show notification cho yêu cầu
                    if (!data.isEmpty()) {
                        showNewRequestNotification(data.size());
                    } else {
                        Log.d("CourseRequestManagement", "No requests found for teacherId: " + currentTeacherId);
                        runOnUiThread(() -> {
                            Toast.makeText(CourseRequestManagementActivity.this, "Chưa có yêu cầu nào", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("CourseRequestManagement", "Real-time error: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(CourseRequestManagementActivity.this,
                            "Lỗi real-time: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

                    // Fallback to direct loading
                    loadRequestsDirectly();
                }
            });
        */
    }

    // Method fallback với teacherId - chỉ lấy pending requests
    private void loadRequestsDirectly() {
        Log.d("CourseRequestManagement", "Loading PENDING course requests only");

        db.collection("courseRequests")
                .whereEqualTo("status", "pending")  // Chỉ lấy pending requests
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    requestList.clear();
                    int totalDocs = queryDocumentSnapshots.size();

                    Log.d("CourseRequestManagement", "Found " + totalDocs + " pending requests");

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        CourseRequest request = doc.toObject(CourseRequest.class);
                        if (request != null && "pending".equals(request.getStatus())) {
                            requestList.add(request);
                            Log.d("CourseRequestManagement", "Added pending request from: " + request.getStudentName() +
                                " for course: " + request.getCourseName() + " with status: " + request.getStatus());
                        }
                    }

                    Log.d("CourseRequestManagement", "Loaded " + requestList.size() + " pending requests");

                    runOnUiThread(() -> {
                        // Debug: Kiểm tra adapter và RecyclerView
                        Log.d("CourseRequestManagement", "Updating adapter with " + requestList.size() + " items");
                        Log.d("CourseRequestManagement", "RecyclerView is null: " + (recyclerView == null));
                        Log.d("CourseRequestManagement", "Adapter is null: " + (adapter == null));

                        adapter.updateList(requestList);
                        adapter.notifyDataSetChanged(); // Force update

                        // Debug: Kiểm tra adapter count
                        Log.d("CourseRequestManagement", "Adapter item count after update: " + adapter.getItemCount());

                        if (requestList.isEmpty()) {
                            Toast.makeText(this, "Không có yêu cầu pending nào", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Hiển thị " + requestList.size() + " yêu cầu pending", Toast.LENGTH_SHORT).show();
                        }

                        // Cập nhật title
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Yêu cầu tham gia (" + requestList.size() + ")");
                        }

                        // Debug: Force RecyclerView to be visible
                        recyclerView.setVisibility(android.view.View.VISIBLE);
                        Log.d("CourseRequestManagement", "RecyclerView visibility set to VISIBLE");
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("CourseRequestManagement", "Error loading pending requests", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void showNewRequestNotification(int count) {
        // Hiển thị notification subtle về số lượng yêu cầu
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Yêu cầu tham gia (" + count + ")");
        }
    }

    @Override
    public void onApprove(CourseRequest request) {
        updateRequestStatus(request, "approved");
        createEnrollment(request);
    }

    @Override
    public void onReject(CourseRequest request) {
        updateRequestStatus(request, "rejected");
    }

    private void updateRequestStatus(CourseRequest request, String status) {
        // Tìm document trong courseRequests để cập nhật
        db.collection("courseRequests")
                .whereEqualTo("studentId", request.getStudentId())
                .whereEqualTo("courseId", request.getCourseId())
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        db.collection("courseRequests").document(doc.getId())
                                .update("status", status)
                                .addOnSuccessListener(aVoid -> {
                                    // Cập nhật Firebase thành công, giờ cập nhật UI ngay lập tức
                                    removeRequestFromList(request);

                                    Toast.makeText(this,
                                        status.equals("approved") ? "Đã phê duyệt yêu cầu" : "Đã từ chối yêu cầu",
                                        Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("CourseRequestManagement", "Error updating request status", e);
                                    Toast.makeText(this, "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CourseRequestManagement", "Error finding request", e);
                });
    }

    private void removeRequestFromList(CourseRequest request) {
        // Tìm và loại bỏ request khỏi danh sách
        for (int i = 0; i < requestList.size(); i++) {
            CourseRequest item = requestList.get(i);
            if (item.getStudentId().equals(request.getStudentId()) &&
                item.getCourseId().equals(request.getCourseId())) {

                // Tạo biến final để sử dụng trong lambda
                final int indexToRemove = i;

                // Loại bỏ item khỏi danh sách
                requestList.remove(i);

                // Cập nhật adapter ngay lập tức
                runOnUiThread(() -> {
                    adapter.notifyItemRemoved(indexToRemove);

                    // Cập nhật title với số lượng mới
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Yêu cầu tham gia (" + requestList.size() + ")");
                    }

                    // Hiển thị thông báo nếu không còn yêu cầu nào
                    if (requestList.isEmpty()) {
                        Toast.makeText(CourseRequestManagementActivity.this, "Đã xử lý hết tất cả yêu cầu", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.d("CourseRequestManagement", "Removed request from list - Student: " +
                      request.getStudentName() + ", Course: " + request.getCourseName());
                break;
            }
        }
    }

    private void createEnrollment(CourseRequest request) {
        Map<String, Object> enrollment = new HashMap<>();
        enrollment.put("courseID", request.getCourseId());        // Sửa từ "courseId" thành "courseID"
        enrollment.put("courseName", request.getCourseName());
        enrollment.put("enrollmentDate", new Date());
        enrollment.put("studentEmail", request.getStudentEmail());
        enrollment.put("studentID", request.getStudentId());      // Sửa từ "studentId" thành "studentID"
        enrollment.put("fullName", request.getStudentName());

        db.collection("enrollments")
                .add(enrollment)
                .addOnSuccessListener(documentReference -> {
                    Log.d("CourseRequestManagement", "Enrollment created successfully: " + documentReference.getId());
                    Log.d("CourseRequestManagement", "Created enrollment - CourseID: " + request.getCourseId() + ", StudentID: " + request.getStudentId() + ", Name: " + request.getStudentName());
                })
                .addOnFailureListener(e -> {
                    Log.e("CourseRequestManagement", "Error creating enrollment", e);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup listeners
        if (realtimeManager != null) {
            realtimeManager.removeAllListeners();
        }
    }
}
