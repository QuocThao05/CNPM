package com.vantinh.tienganh;

import android.os.Bundle;
import android.view.MenuItem;
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

    private TextView tvCourseTitle, tvProgressSummary;
    private RecyclerView rvLessonProgress;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String courseId, courseTitle;
    private List<LessonProgressItem> lessonProgressList;
    private LessonProgressAdapter progressAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use simple layout since we don't have custom layout
        setContentView(android.R.layout.activity_list_item);

        // Get course info from intent
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");

        if (courseId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadProgressData();
    }

    private void initViews() {
        // Set title directly since we may not have custom toolbar
        setTitle("Chi tiết tiến độ học tập");

        // Don't try to find custom views that don't exist
        // We'll display progress information via Toast messages instead
        tvCourseTitle = null;
        tvProgressSummary = null;
        rvLessonProgress = null;

        android.util.Log.d("StudentProgressDetail", "Using simplified UI without custom layouts");
    }

    private void setupRecyclerView() {
        if (rvLessonProgress != null) {
            lessonProgressList = new ArrayList<>();
            progressAdapter = new LessonProgressAdapter(lessonProgressList);
            rvLessonProgress.setLayoutManager(new LinearLayoutManager(this));
            rvLessonProgress.setAdapter(progressAdapter);
        }
    }

    private void loadProgressData() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String studentId = mAuth.getCurrentUser().getUid();

        if (tvCourseTitle != null) {
            tvCourseTitle.setText(courseTitle);
        }

        // Load all lessons for this course
        db.collection("lessons")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("isPublished", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Lesson> allLessons = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Lesson lesson = document.toObject(Lesson.class);
                        lesson.setId(document.getId());
                        allLessons.add(lesson);
                    }

                    if (allLessons.isEmpty()) {
                        String message = "Khóa học này chưa có bài học nào";
                        if (tvProgressSummary != null) {
                            tvProgressSummary.setText(message);
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    // Load progress for each lesson
                    loadLessonProgress(studentId, allLessons);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentProgressDetail", "Error loading lessons", e);
                    Toast.makeText(this, "Lỗi tải danh sách bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadLessonProgress(String studentId, List<Lesson> allLessons) {
        // Load all progress records for this student and course
        db.collection("lesson_progress")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("isCompleted", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Create a map of completed lesson IDs
                    Map<String, Boolean> completedLessons = new HashMap<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String lessonId = document.getString("lessonId");
                        if (lessonId != null) {
                            completedLessons.put(lessonId, true);
                        }
                    }

                    // Create progress items for all lessons
                    if (lessonProgressList != null) {
                        lessonProgressList.clear();
                    } else {
                        lessonProgressList = new ArrayList<>();
                    }

                    int completedCount = 0;

                    for (Lesson lesson : allLessons) {
                        boolean isCompleted = completedLessons.containsKey(lesson.getId());
                        if (isCompleted) {
                            completedCount++;
                        }

                        LessonProgressItem progressItem = new LessonProgressItem(
                                lesson.getId(),
                                lesson.getTitle(),
                                lesson.getTypeDisplayName(),
                                isCompleted
                        );
                        lessonProgressList.add(progressItem);
                    }

                    // Update UI
                    int totalLessons = allLessons.size();
                    int progressPercentage = totalLessons > 0 ? (completedCount * 100) / totalLessons : 0;

                    String progressText = "Bạn đã hoàn thành " + completedCount + " trong tổng số " + totalLessons + " bài học";
                    String completionText = progressPercentage + "% hoàn thành (" + completedCount + "/" + totalLessons + ")";

                    if (tvProgressSummary != null) {
                        tvProgressSummary.setText(progressText + "\n" + completionText);
                    }

                    // Show progress as Toast if no UI elements available
                    Toast.makeText(this, completionText, Toast.LENGTH_LONG).show();

                    if (progressAdapter != null) {
                        progressAdapter.notifyDataSetChanged();
                    }

                    android.util.Log.d("StudentProgressDetail", "Progress loaded: " + completedCount + "/" + totalLessons);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentProgressDetail", "Error loading lesson progress", e);
                    Toast.makeText(this, "Lỗi tải tiến độ học tập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    // Inner class for lesson progress item
    public static class LessonProgressItem {
        private String lessonId;
        private String lessonTitle;
        private String lessonType;
        private boolean isCompleted;

        public LessonProgressItem(String lessonId, String lessonTitle, String lessonType, boolean isCompleted) {
            this.lessonId = lessonId;
            this.lessonTitle = lessonTitle;
            this.lessonType = lessonType;
            this.isCompleted = isCompleted;
        }

        // Getters
        public String getLessonId() { return lessonId; }
        public String getLessonTitle() { return lessonTitle; }
        public String getLessonType() { return lessonType; }
        public boolean isCompleted() { return isCompleted; }
    }
}
