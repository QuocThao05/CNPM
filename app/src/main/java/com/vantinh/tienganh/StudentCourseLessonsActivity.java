package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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
import java.util.List;

public class StudentCourseLessonsActivity extends AppCompatActivity implements StudentLessonAdapter.OnLessonClickListener {

    private RecyclerView rvLessons;
    private LinearLayout layoutNoLessons;
    private TextView tvCourseTitle, tvCourseInfo;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Lesson> lessonList;
    private StudentLessonAdapter lessonAdapter;

    private String courseId;
    private String courseTitle;
    private String courseCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_course_lessons);

        // Get course info from intent
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");
        courseCategory = getIntent().getStringExtra("courseCategory");

        if (courseId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        lessonList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadLessons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvLessons = findViewById(R.id.rv_lessons);
        layoutNoLessons = findViewById(R.id.layout_no_lessons);
        tvCourseTitle = findViewById(R.id.tv_course_title);
        tvCourseInfo = findViewById(R.id.tv_course_info);

        tvCourseTitle.setText(courseTitle);
        tvCourseInfo.setText("Danh mục: " + courseCategory);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bài học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        lessonAdapter = new StudentLessonAdapter(lessonList, this);
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);
    }

    private void loadLessons() {
        layoutNoLessons.setVisibility(View.VISIBLE);
        rvLessons.setVisibility(View.GONE);

        android.util.Log.d("StudentCourseLessons", "=== DEBUG: Starting loadLessons ===");
        android.util.Log.d("StudentCourseLessons", "CourseId: " + courseId);
        android.util.Log.d("StudentCourseLessons", "CourseTitle: " + courseTitle);

        // Simplified approach - load lessons directly first
        db.collection("lessons")
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                lessonList.clear();

                android.util.Log.d("StudentCourseLessons", "=== FIREBASE SUCCESS ===");
                android.util.Log.d("StudentCourseLessons", "Total documents found: " + queryDocumentSnapshots.size());

                int publishedCount = 0;
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    android.util.Log.d("StudentCourseLessons", "Document ID: " + document.getId());

                    // Check all fields in document
                    android.util.Log.d("StudentCourseLessons", "Document data: " + document.getData());

                    Lesson lesson = document.toObject(Lesson.class);
                    lesson.setId(document.getId());

                    android.util.Log.d("StudentCourseLessons", "Lesson: " + lesson.getTitle() +
                        ", Published: " + lesson.isPublished() +
                        ", Order: " + lesson.getOrder());

                    // Add all lessons for now (ignore published status for debugging)
                    lessonList.add(lesson);

                    if (lesson.isPublished()) {
                        publishedCount++;
                    }
                }

                android.util.Log.d("StudentCourseLessons", "Published lessons: " + publishedCount);
                android.util.Log.d("StudentCourseLessons", "Total lessons added: " + lessonList.size());

                // Sort lessons by order
                lessonList.sort((l1, l2) -> Integer.compare(l1.getOrder(), l2.getOrder()));

                // Show lessons immediately for debugging
                showLessonsDirectly();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseLessons", "=== FIREBASE ERROR ===", e);
                Toast.makeText(this, "Lỗi tải bài học: " + e.getMessage(), Toast.LENGTH_LONG).show();
                layoutNoLessons.setVisibility(View.VISIBLE);
                rvLessons.setVisibility(View.GONE);
            });
    }

    private void showLessonsDirectly() {
        android.util.Log.d("StudentCourseLessons", "=== SHOWING LESSONS ===");

        if (lessonList.isEmpty()) {
            android.util.Log.d("StudentCourseLessons", "Lesson list is empty - showing no lessons layout");
            layoutNoLessons.setVisibility(View.VISIBLE);
            rvLessons.setVisibility(View.GONE);
        } else {
            android.util.Log.d("StudentCourseLessons", "Showing " + lessonList.size() + " lessons in RecyclerView");

            // Set all lessons as accessible for debugging
            for (Lesson lesson : lessonList) {
                lesson.setAccessible(true);
                lesson.setLocked(false);
                lesson.setCompleted(false);
            }

            layoutNoLessons.setVisibility(View.GONE);
            rvLessons.setVisibility(View.VISIBLE);
            lessonAdapter.notifyDataSetChanged();

            Toast.makeText(this, "DEBUG: Hiển thị " + lessonList.size() + " bài học", Toast.LENGTH_LONG).show();
        }
    }

    private String currentStudentId;

    private void getCurrentStudentId(Runnable onComplete) {
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
                        onComplete.run();
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin học viên", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentCourseLessons", "Error loading student info", e);
                    Toast.makeText(this, "Lỗi tải thông tin học viên", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadStudentProgress() {
        if (currentStudentId == null) {
            updateUI();
            return;
        }

        // Load student progress from lessonProgress collection
        db.collection("lessonProgress")
            .whereEqualTo("studentId", currentStudentId)
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Mark completed lessons and determine next available lesson
                java.util.Set<String> completedLessons = new java.util.HashSet<>();

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String lessonId = doc.getString("lessonId");
                    Boolean isCompleted = doc.getBoolean("isCompleted");
                    if (lessonId != null && Boolean.TRUE.equals(isCompleted)) {
                        completedLessons.add(lessonId);
                    }
                }

                // Update lesson accessibility based on progress
                updateLessonAccessibility(completedLessons);
                updateUI();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseLessons", "Error loading progress", e);
                // If no progress data, allow access to first lesson only
                updateLessonAccessibility(new java.util.HashSet<>());
                updateUI();
            });
    }

    private void updateLessonAccessibility(java.util.Set<String> completedLessons) {
        for (int i = 0; i < lessonList.size(); i++) {
            Lesson lesson = lessonList.get(i);

            if (i == 0) {
                // First lesson is always accessible
                lesson.setAccessible(true);
                lesson.setLocked(false);
            } else {
                // Other lessons are only accessible if previous lesson is completed
                Lesson previousLesson = lessonList.get(i - 1);
                boolean previousCompleted = completedLessons.contains(previousLesson.getId());

                lesson.setAccessible(previousCompleted);
                lesson.setLocked(!previousCompleted);
            }

            // Mark if lesson is completed
            lesson.setCompleted(completedLessons.contains(lesson.getId()));

            android.util.Log.d("StudentCourseLessons",
                "Lesson " + lesson.getOrder() + ": " + lesson.getTitle() +
                " - Accessible: " + lesson.isAccessible() +
                " - Completed: " + lesson.isCompleted());
        }
    }

    private void updateUI() {
        if (lessonList.isEmpty()) {
            layoutNoLessons.setVisibility(View.VISIBLE);
            rvLessons.setVisibility(View.GONE);
        } else {
            layoutNoLessons.setVisibility(View.GONE);
            rvLessons.setVisibility(View.VISIBLE);
            lessonAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Đã tải " + lessonList.size() + " bài học", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLessonClick(Lesson lesson) {
        // Check if lesson is accessible
        if (!lesson.isAccessible()) {
            if (lesson.isLocked()) {
                Toast.makeText(this, "🔒 Bài học này chưa mở khóa!\nHãy hoàn thành bài học trước đó.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Bài học này chưa sẵn sàng.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Navigate to lesson learning activity
        Intent intent = new Intent(this, StudentLessonLearningActivity.class);
        intent.putExtra("lessonId", lesson.getId());
        intent.putExtra("lessonTitle", lesson.getTitle());
        intent.putExtra("courseId", courseId);
        intent.putExtra("courseTitle", courseTitle);
        intent.putExtra("courseCategory", courseCategory);
        startActivity(intent);
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
        loadLessons(); // Reload when returning from lesson
    }
}
