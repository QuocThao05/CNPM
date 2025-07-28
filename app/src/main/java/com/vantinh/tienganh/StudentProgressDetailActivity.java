package com.vantinh.tienganh;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentProgressDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvStudentName, tvCourseTitle, tvStudentEmail;
    private TextView tvLessonProgress, tvEnrollmentDate, tvNoTestResults, tvNoLessons;
    private ProgressBar progressBarLessons;
    private LinearLayout layoutTestResults;
    private RecyclerView rvLessonProgress;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String studentId, studentName, courseId, courseName;
    private List<LessonProgressItem> lessonProgressList;
    private LessonProgressAdapter progressAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_progress_detail);

        // Get data from intent
        studentId = getIntent().getStringExtra("studentId");
        studentName = getIntent().getStringExtra("studentName");
        courseId = getIntent().getStringExtra("courseId");
        courseName = getIntent().getStringExtra("courseName");

        android.util.Log.d("StudentProgressDetail", "Received data:");
        android.util.Log.d("StudentProgressDetail", "StudentId: " + studentId);
        android.util.Log.d("StudentProgressDetail", "StudentName: " + studentName);
        android.util.Log.d("StudentProgressDetail", "CourseId: " + courseId);
        android.util.Log.d("StudentProgressDetail", "CourseName: " + courseName);

        if (studentId == null || courseId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin học viên hoặc khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        lessonProgressList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadStudentInfo();
        loadProgressData();
        loadTestResults();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvStudentName = findViewById(R.id.tv_student_name);
        tvCourseTitle = findViewById(R.id.tv_course_title);
        tvStudentEmail = findViewById(R.id.tv_student_email);
        tvLessonProgress = findViewById(R.id.tv_lesson_progress);
        tvEnrollmentDate = findViewById(R.id.tv_enrollment_date);
        tvNoTestResults = findViewById(R.id.tv_no_test_results);
        tvNoLessons = findViewById(R.id.tv_no_lessons);
        progressBarLessons = findViewById(R.id.progress_bar_lessons);
        layoutTestResults = findViewById(R.id.layout_test_results);
        rvLessonProgress = findViewById(R.id.rv_lesson_progress);

        // Set initial data
        if (studentName != null) {
            tvStudentName.setText(studentName);
        }
        if (courseName != null) {
            tvCourseTitle.setText(courseName);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết tiến độ học viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        progressAdapter = new LessonProgressAdapter(lessonProgressList);
        rvLessonProgress.setLayoutManager(new LinearLayoutManager(this));
        rvLessonProgress.setAdapter(progressAdapter);
    }

    private void loadStudentInfo() {
        // Load thông tin chi tiết của học viên
        db.collection("users")
                .whereEqualTo("id", studentId)
                .whereEqualTo("role", "student")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot studentDoc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String email = studentDoc.getString("email");
                        String name = studentDoc.getString("name");

                        if (email != null) {
                            tvStudentEmail.setText(email);
                        }
                        if (name != null && tvStudentName != null) {
                            tvStudentName.setText(name);
                        }

                        android.util.Log.d("StudentProgressDetail", "Student info loaded: " + name + " (" + email + ")");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentProgressDetail", "Error loading student info", e);
                });

        // Load ngày đăng ký từ enrollments
        db.collection("enrollments")
                .whereEqualTo("studentID", studentId)
                .whereEqualTo("courseID", courseId)
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot enrollmentDoc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        com.google.firebase.Timestamp enrollmentDate = enrollmentDoc.getTimestamp("enrollmentDate");

                        if (enrollmentDate != null) {
                            String dateStr = android.text.format.DateFormat.format("dd/MM/yyyy", enrollmentDate.toDate()).toString();
                            tvEnrollmentDate.setText("Ngày đăng ký: " + dateStr);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentProgressDetail", "Error loading enrollment date", e);
                });
    }

    private void loadProgressData() {
        android.util.Log.d("StudentProgressDetail", "Loading progress data for student: " + studentId + ", course: " + courseId);

        // Load tất cả lessons của khóa học
        db.collection("lessons")
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Lesson> allLessons = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Lesson lesson = document.toObject(Lesson.class);
                        lesson.setId(document.getId());
                        allLessons.add(lesson);
                    }

                    android.util.Log.d("StudentProgressDetail", "Found " + allLessons.size() + " lessons");

                    if (allLessons.isEmpty()) {
                        tvNoLessons.setVisibility(View.VISIBLE);
                        rvLessonProgress.setVisibility(View.GONE);
                        updateProgressSummary(0, 0);
                        return;
                    }

                    // Load tiến độ của học viên cho từng bài học
                    loadLessonProgress(allLessons);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentProgressDetail", "Error loading lessons", e);
                    Toast.makeText(this, "Lỗi tải danh sách bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadLessonProgress(List<Lesson> allLessons) {
        // Load lesson_progress từ Firebase
        db.collection("lesson_progress")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Boolean> progressMap = new HashMap<>();
                    Map<String, com.google.firebase.Timestamp> completionTimeMap = new HashMap<>();

                    // Tạo map tiến độ hoàn thành
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String lessonId = doc.getString("lessonId");
                        Boolean isCompleted = doc.getBoolean("isCompleted");
                        com.google.firebase.Timestamp completedAt = doc.getTimestamp("completedAt");

                        if (lessonId != null && isCompleted != null) {
                            progressMap.put(lessonId, isCompleted);
                            if (completedAt != null) {
                                completionTimeMap.put(lessonId, completedAt);
                            }
                        }
                    }

                    // Tạo danh sách LessonProgressItem
                    lessonProgressList.clear();
                    int completedCount = 0;

                    for (Lesson lesson : allLessons) {
                        LessonProgressItem progressItem = new LessonProgressItem();
                        progressItem.setLessonId(lesson.getId());
                        progressItem.setLessonTitle(lesson.getTitle());
                        progressItem.setLessonOrder(lesson.getOrder());

                        boolean isCompleted = progressMap.getOrDefault(lesson.getId(), false);
                        progressItem.setCompleted(isCompleted);

                        if (isCompleted) {
                            completedCount++;
                            com.google.firebase.Timestamp completedAt = completionTimeMap.get(lesson.getId());
                            if (completedAt != null) {
                                String dateStr = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", completedAt.toDate()).toString();
                                progressItem.setCompletedAt(dateStr);
                            }
                        }

                        lessonProgressList.add(progressItem);
                    }

                    // Sắp xếp theo thứ tự bài học
                    lessonProgressList.sort((a, b) -> Integer.compare(a.getLessonOrder(), b.getLessonOrder()));

                    // Cập nhật UI
                    updateProgressSummary(completedCount, allLessons.size());
                    progressAdapter.notifyDataSetChanged();

                    if (lessonProgressList.isEmpty()) {
                        tvNoLessons.setVisibility(View.VISIBLE);
                        rvLessonProgress.setVisibility(View.GONE);
                    } else {
                        tvNoLessons.setVisibility(View.GONE);
                        rvLessonProgress.setVisibility(View.VISIBLE);
                    }

                    android.util.Log.d("StudentProgressDetail", "Progress loaded: " + completedCount + "/" + allLessons.size() + " completed");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentProgressDetail", "Error loading lesson progress", e);
                    Toast.makeText(this, "Lỗi tải tiến độ học tập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProgressSummary(int completedLessons, int totalLessons) {
        int progressPercentage = totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0;

        tvLessonProgress.setText(completedLessons + "/" + totalLessons + " (" + progressPercentage + "%)");
        progressBarLessons.setMax(100);
        progressBarLessons.setProgress(progressPercentage);

        // Đổi màu progress bar theo tiến độ
        if (progressPercentage == 100) {
            progressBarLessons.getProgressDrawable().setColorFilter(
                getColor(android.R.color.holo_green_dark), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (progressPercentage >= 50) {
            progressBarLessons.getProgressDrawable().setColorFilter(
                getColor(android.R.color.holo_orange_light), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            progressBarLessons.getProgressDrawable().setColorFilter(
                getColor(android.R.color.holo_red_light), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void loadTestResults() {
        android.util.Log.d("StudentProgressDetail", "Loading test results for student: " + studentId + ", course: " + courseId);

        // Load kết quả bài kiểm tra từ collection "testResults"
        db.collection("testResults")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        android.util.Log.d("StudentProgressDetail", "No test results found");
                        showNoTestResults();
                        return;
                    }

                    android.util.Log.d("StudentProgressDetail", "Found " + queryDocumentSnapshots.size() + " test results");

                    // Ẩn thông báo "chưa làm bài kiểm tra"
                    tvNoTestResults.setVisibility(View.GONE);
                    layoutTestResults.setVisibility(View.VISIBLE);
                    layoutTestResults.removeAllViews();

                    // Sắp xếp kết quả theo thời gian (mới nhất trước)
                    List<QueryDocumentSnapshot> sortedResults = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        sortedResults.add(doc);
                    }
                    sortedResults.sort((a, b) -> {
                        com.google.firebase.Timestamp timeA = a.getTimestamp("completedAt");
                        com.google.firebase.Timestamp timeB = b.getTimestamp("completedAt");
                        if (timeA == null && timeB == null) return 0;
                        if (timeA == null) return 1;
                        if (timeB == null) return -1;
                        return timeB.compareTo(timeA); // Mới nhất trước
                    });

                    // Hiển thị từng kết quả bài kiểm tra
                    for (int i = 0; i < sortedResults.size(); i++) {
                        QueryDocumentSnapshot doc = sortedResults.get(i);
                        addTestResultView(doc, i + 1);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentProgressDetail", "Error loading test results", e);
                    showNoTestResults();
                    Toast.makeText(this, "Lỗi tải kết quả bài kiểm tra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addTestResultView(QueryDocumentSnapshot testResult, int attemptNumber) {
        // Tạo view cho mỗi kết quả bài kiểm tra
        View testResultView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, layoutTestResults, false);

        TextView text1 = testResultView.findViewById(android.R.id.text1);
        TextView text2 = testResultView.findViewById(android.R.id.text2);

        // Lấy dữ liệu từ document
        Object scoreObj = testResult.get("score");
        Object correctAnswersObj = testResult.get("correctAnswers");
        Object totalQuestionsObj = testResult.get("totalQuestions");
        com.google.firebase.Timestamp completedAt = testResult.getTimestamp("completedAt");

        // Xử lý score
        double score = 0;
        if (scoreObj instanceof Number) {
            score = ((Number) scoreObj).doubleValue();
        }

        // Xử lý correctAnswers và totalQuestions
        int correctAnswers = 0;
        int totalQuestions = 0;

        if (correctAnswersObj instanceof Number) {
            correctAnswers = ((Number) correctAnswersObj).intValue();
        }
        if (totalQuestionsObj instanceof Number) {
            totalQuestions = ((Number) totalQuestionsObj).intValue();
        }

        // Xử lý thời gian
        String timeStr = "Thời gian: --";
        if (completedAt != null) {
            timeStr = "Thời gian: " + android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", completedAt.toDate()).toString();
        }

        // Thiết lập nội dung
        String title = "Lần " + attemptNumber + ": " + String.format("%.1f/100 điểm", score);
        if (totalQuestions > 0) {
            title += " (" + correctAnswers + "/" + totalQuestions + " câu đúng)";
        }

        text1.setText(title);
        text1.setTextSize(16);
        text1.setTypeface(null, android.graphics.Typeface.BOLD); // Sửa từ setTextStyle thành setTypeface

        text2.setText(timeStr);
        text2.setTextSize(14);

        // Đổi màu theo điểm số
        if (score >= 80) {
            text1.setTextColor(getColor(android.R.color.holo_green_dark));
        } else if (score >= 60) {
            text1.setTextColor(getColor(android.R.color.holo_orange_dark));
        } else {
            text1.setTextColor(getColor(android.R.color.holo_red_dark));
        }

        // Thêm margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        testResultView.setLayoutParams(params);

        layoutTestResults.addView(testResultView);

        android.util.Log.d("StudentProgressDetail", "Added test result: " + score + " points");
    }

    private void showNoTestResults() {
        tvNoTestResults.setVisibility(View.VISIBLE);
        layoutTestResults.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Inner class for lesson progress item
    public static class LessonProgressItem {
        private String lessonId;
        private String lessonTitle;
        private int lessonOrder;
        private boolean isCompleted;
        private String completedAt;

        // Getters and setters
        public String getLessonId() { return lessonId; }
        public void setLessonId(String lessonId) { this.lessonId = lessonId; }
        public String getLessonTitle() { return lessonTitle; }
        public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
        public int getLessonOrder() { return lessonOrder; }
        public void setLessonOrder(int lessonOrder) { this.lessonOrder = lessonOrder; }
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
        public String getCompletedAt() { return completedAt; }
        public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    }
}
