package com.vantinh.tienganh;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseRequestManagementActivity extends AppCompatActivity implements CourseRequestAdapter.OnRequestActionListener {

    private RecyclerView recyclerView;
    private CourseRequestAdapter adapter;
    private List<CourseRequest> requestList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_request_management);

        initViews();
        setupToolbar();
        initFirebase();
        loadCourseRequests();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_requests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new CourseRequestAdapter(requestList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Yêu cầu tham gia khóa học");
        }
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            teacherId = auth.getCurrentUser().getUid();
        }
    }

    private void loadCourseRequests() {
        if (teacherId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin giáo viên", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("courseRequests")
                .whereEqualTo("teacherId", teacherId)
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải yêu cầu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    requestList.clear();
                    if (value != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            CourseRequest request = doc.toObject(CourseRequest.class);
                            if (request != null) {
                                request.setRequestId(doc.getId());
                                requestList.add(request);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onApprove(CourseRequest request) {
        // Update request status to approved
        updateRequestStatus(request, "approved");

        // Create enrollment record
        createEnrollment(request);
    }

    @Override
    public void onReject(CourseRequest request) {
        updateRequestStatus(request, "rejected");
    }

    private void updateRequestStatus(CourseRequest request, String status) {
        request.setStatus(status);
        request.setResponseDate(new Date());

        db.collection("courseRequests").document(request.getRequestId())
                .set(request)
                .addOnSuccessListener(aVoid -> {
                    String message = status.equals("approved") ? "Đã duyệt yêu cầu" : "Đã từ chối yêu cầu";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createEnrollment(CourseRequest request) {
        Enrollment enrollment = new Enrollment(
            request.getStudentId(),
            request.getCourseId(),
            new Date(),
            0.0 // Initial progress
        );

        db.collection("enrollments")
                .add(enrollment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Học viên đã được ghi danh vào khóa học", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tạo ghi danh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
