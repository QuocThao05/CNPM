package com.vantinh.tienganh;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentFeedbackActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner spinnerCourses;
    private EditText etFeedbackMessage;
    private Button btnSubmitFeedback;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Course> enrolledCourses;
    private ArrayAdapter<String> courseAdapter;
    private String currentStudentId;
    private String currentStudentName;
    private String currentStudentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_feedback);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        enrolledCourses = new ArrayList<>();

        initViews();
        setupToolbar();
        setupSpinner();
        setupClickListeners();
        loadCurrentStudentInfo();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerCourses = findViewById(R.id.spinner_courses);
        etFeedbackMessage = findViewById(R.id.et_feedback_message);
        btnSubmitFeedback = findViewById(R.id.btn_submit_feedback);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Đánh giá khóa học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinner() {
        List<String> courseNames = new ArrayList<>();
        courseNames.add("Chọn khóa học để đánh giá...");

        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseNames);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(courseAdapter);
    }

    private void setupClickListeners() {
        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void loadCurrentStudentInfo() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String firebaseUid = mAuth.getCurrentUser().getUid();
        currentStudentEmail = mAuth.getCurrentUser().getEmail();

        // Get student info từ users collection
        db.collection("users").document(firebaseUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentStudentId = documentSnapshot.getString("id");
                        currentStudentName = documentSnapshot.getString("name");

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
                    android.util.Log.e("StudentFeedback", "Error loading user info", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadEnrolledCourses() {
        android.util.Log.d("StudentFeedback", "=== LOADING ENROLLED COURSES ===");
        android.util.Log.d("StudentFeedback", "Student ID: " + currentStudentId);
        android.util.Log.d("StudentFeedback", "Student Email: " + currentStudentEmail);

        if (currentStudentId == null) {
            Toast.makeText(this, "Không tìm thấy ID học viên", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset spinner data
        enrolledCourses.clear();
        List<String> courseNames = new ArrayList<>();
        courseNames.add("Chọn khóa học để đánh giá...");
        courseAdapter.clear();
        courseAdapter.addAll(courseNames);
        courseAdapter.notifyDataSetChanged();

        // Thử nhiều collection name và field name có thể có
        tryLoadFromEnrollments();
    }

    private void tryLoadFromEnrollments() {
        android.util.Log.d("StudentFeedback", "Trying collection 'enrollments' with field 'studentID'");

        // Thử với "enrollments" collection và "studentID" field
        db.collection("enrollments")
                .whereEqualTo("studentID", currentStudentId)
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("StudentFeedback", "enrollments/studentID query returned " + queryDocumentSnapshots.size() + " results");

                    if (!queryDocumentSnapshots.isEmpty()) {
                        processEnrollmentResults(queryDocumentSnapshots);
                    } else {
                        // Thử với field name khác
                        tryAlternativeFieldNames();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentFeedback", "Error with enrollments/studentID", e);
                    tryAlternativeFieldNames();
                });
    }

    private void tryAlternativeFieldNames() {
        android.util.Log.d("StudentFeedback", "Trying collection 'enrollments' with field 'studentId'");

        // Thử với "studentId" (chữ d thường)
        db.collection("enrollments")
                .whereEqualTo("studentId", currentStudentId)
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("StudentFeedback", "enrollments/studentId query returned " + queryDocumentSnapshots.size() + " results");

                    if (!queryDocumentSnapshots.isEmpty()) {
                        processEnrollmentResults(queryDocumentSnapshots);
                    } else {
                        // Thử collection name khác
                        tryAlternativeCollectionNames();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentFeedback", "Error with enrollments/studentId", e);
                    tryAlternativeCollectionNames();
                });
    }

    private void tryAlternativeCollectionNames() {
        android.util.Log.d("StudentFeedback", "Trying collection 'enrollment' with field 'studentID'");

        // Thử với "enrollment" collection (số ít)
        db.collection("enrollment")
                .whereEqualTo("studentID", currentStudentId)
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("StudentFeedback", "enrollment/studentID query returned " + queryDocumentSnapshots.size() + " results");

                    if (!queryDocumentSnapshots.isEmpty()) {
                        processEnrollmentResults(queryDocumentSnapshots);
                    } else {
                        // Thử load ALL enrollments để debug
                        debugLoadAllEnrollments();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentFeedback", "Error with enrollment/studentID", e);
                    debugLoadAllEnrollments();
                });
    }

    private void debugLoadAllEnrollments() {
        android.util.Log.d("StudentFeedback", "=== DEBUG: Loading ALL enrollments to check data structure ===");

        db.collection("enrollments")
                .limit(5) // Chỉ lấy 5 record đầu để debug
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("StudentFeedback", "Total enrollments in database: " + queryDocumentSnapshots.size());

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        android.util.Log.d("StudentFeedback", "Enrollment doc: " + doc.getId());
                        android.util.Log.d("StudentFeedback", "Data: " + doc.getData().toString());
                    }

                    // Vẫn không có dữ liệu phù hợp
                    showNoCoursesMessage();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentFeedback", "Error loading debug enrollments", e);
                    showNoCoursesMessage();
                });
    }

    private void processEnrollmentResults(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
        android.util.Log.d("StudentFeedback", "Processing " + queryDocumentSnapshots.size() + " enrollment results");

        List<String> courseNames = new ArrayList<>();
        courseNames.add("Chọn khóa học để đánh giá...");

        final int totalEnrollments = queryDocumentSnapshots.size();
        final int[] processedCount = {0};

        // Load course details for each enrollment
        for (com.google.firebase.firestore.QueryDocumentSnapshot enrollmentDoc : queryDocumentSnapshots) {
            // Thử nhiều field name có thể có cho courseId
            String courseId = enrollmentDoc.getString("courseID");
            if (courseId == null) {
                courseId = enrollmentDoc.getString("courseId");
            }
            if (courseId == null) {
                courseId = enrollmentDoc.getString("course_id");
            }

            android.util.Log.d("StudentFeedback", "Processing enrollment: " + enrollmentDoc.getId() + ", courseId: " + courseId);

            if (courseId != null) {
                loadCourseDetailsImproved(courseId, courseNames, totalEnrollments, processedCount);
            } else {
                android.util.Log.w("StudentFeedback", "No courseId found in enrollment: " + enrollmentDoc.getData());
                processedCount[0]++;
                checkIfAllProcessed(courseNames, totalEnrollments, processedCount[0]);
            }
        }
    }

    private void showNoCoursesMessage() {
        Toast.makeText(this, "Bạn chưa có khóa học nào được duyệt để đánh giá", Toast.LENGTH_LONG).show();

        // Vẫn hiển thị spinner với message
        List<String> courseNames = new ArrayList<>();
        courseNames.add("Không có khóa học nào để đánh giá");
        courseAdapter.clear();
        courseAdapter.addAll(courseNames);
        courseAdapter.notifyDataSetChanged();
    }

    private void submitFeedback() {
        int selectedPosition = spinnerCourses.getSelectedItemPosition();
        String feedbackMessage = etFeedbackMessage.getText().toString().trim();

        // Validation
        if (selectedPosition <= 0) {
            Toast.makeText(this, "Vui lòng chọn khóa học", Toast.LENGTH_SHORT).show();
            return;
        }

        if (feedbackMessage.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (feedbackMessage.length() < 10) {
            Toast.makeText(this, "Nội dung đánh giá quá ngắn (ít nhất 10 ký tự)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected course
        Course selectedCourse = enrolledCourses.get(selectedPosition - 1); // -1 vì có item đầu là "Chọn khóa học..."

        // Create feedback data
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("courseId", selectedCourse.getId());
        feedbackData.put("courseName", selectedCourse.getTitle());
        feedbackData.put("message", feedbackMessage);
        feedbackData.put("feedbackRequest", com.google.firebase.Timestamp.now());
        feedbackData.put("studentEmail", currentStudentEmail);
        feedbackData.put("studentId", currentStudentId);
        feedbackData.put("studentName", currentStudentName);
        feedbackData.put("status", "pending"); // Trạng thái feedback: pending, reviewed

        // Disable button while submitting
        btnSubmitFeedback.setEnabled(false);
        btnSubmitFeedback.setText("Đang gửi...");

        // Save to Firebase
        db.collection("feedbacks")
                .add(feedbackData)
                .addOnSuccessListener(documentReference -> {
                    android.util.Log.d("StudentFeedback", "Feedback submitted successfully with ID: " + documentReference.getId());

                    Toast.makeText(this, "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();

                    // Clear form
                    spinnerCourses.setSelection(0);
                    etFeedbackMessage.setText("");

                    // Re-enable button
                    btnSubmitFeedback.setEnabled(true);
                    btnSubmitFeedback.setText("Gửi đánh giá");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentFeedback", "Error submitting feedback", e);
                    Toast.makeText(this, "Lỗi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Re-enable button
                    btnSubmitFeedback.setEnabled(true);
                    btnSubmitFeedback.setText("Gửi đánh giá");
                });
    }

    private void loadCourseDetailsImproved(String courseId, List<String> courseNames, int totalEnrollments, int[] processedCount) {
        android.util.Log.d("StudentFeedback", "Loading course details for courseId: " + courseId);

        db.collection("courses").document(courseId)
                .get()
                .addOnSuccessListener(courseDoc -> {
                    processedCount[0]++;

                    if (courseDoc.exists()) {
                        Course course = courseDoc.toObject(Course.class);
                        if (course != null) {
                            course.setId(courseDoc.getId());
                            enrolledCourses.add(course);
                            courseNames.add(course.getTitle());

                            android.util.Log.d("StudentFeedback", "Added course: " + course.getTitle());
                        }
                    } else {
                        android.util.Log.w("StudentFeedback", "Course not found: " + courseId);
                    }

                    checkIfAllProcessed(courseNames, totalEnrollments, processedCount[0]);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentFeedback", "Error loading course details for: " + courseId, e);
                    processedCount[0]++;
                    checkIfAllProcessed(courseNames, totalEnrollments, processedCount[0]);
                });
    }

    private void checkIfAllProcessed(List<String> courseNames, int totalEnrollments, int processedCount) {
        android.util.Log.d("StudentFeedback", "Processed " + processedCount + "/" + totalEnrollments + " enrollments");

        if (processedCount >= totalEnrollments) {
            // Tất cả enrollment đã được xử lý
            if (enrolledCourses.isEmpty()) {
                showNoCoursesMessage();
            } else {
                // Update spinner with courses
                courseAdapter.clear();
                courseAdapter.addAll(courseNames);
                courseAdapter.notifyDataSetChanged();

                android.util.Log.d("StudentFeedback", "Updated spinner with " + enrolledCourses.size() + " courses");
                Toast.makeText(this, "Đã tải " + enrolledCourses.size() + " khóa học", Toast.LENGTH_SHORT).show();
            }
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
