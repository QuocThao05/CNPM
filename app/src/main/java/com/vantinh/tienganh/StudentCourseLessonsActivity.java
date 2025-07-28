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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        lessonAdapter = new StudentLessonAdapter(lessonList, new StudentLessonAdapter.OnLessonClickListener() {
            @Override
            public void onLessonClick(Lesson lesson) {
                // Debug logging để kiểm tra dữ liệu
                android.util.Log.d("StudentCourseLessons", "=== DEBUG LESSON CLICK ===");
                android.util.Log.d("StudentCourseLessons", "Lesson ID: " + lesson.getId());
                android.util.Log.d("StudentCourseLessons", "Lesson Title: " + lesson.getTitle());
                android.util.Log.d("StudentCourseLessons", "Course ID: " + courseId);
                android.util.Log.d("StudentCourseLessons", "Course Title: " + courseTitle);
                android.util.Log.d("StudentCourseLessons", "Course Category: " + courseCategory);

                // Kiểm tra dữ liệu trước khi chuyển
                if (lesson.getId() == null || lesson.getId().isEmpty()) {
                    Toast.makeText(StudentCourseLessonsActivity.this, "Lỗi: Bài học không có ID hợp lệ", Toast.LENGTH_LONG).show();
                    android.util.Log.e("StudentCourseLessons", "Lesson ID is null or empty!");
                    return;
                }

                if (lesson.getTitle() == null || lesson.getTitle().isEmpty()) {
                    Toast.makeText(StudentCourseLessonsActivity.this, "Lỗi: Bài học không có tiêu đề", Toast.LENGTH_LONG).show();
                    android.util.Log.e("StudentCourseLessons", "Lesson title is null or empty!");
                    return;
                }

                // Chuyển đến màn hình học bài với đầy đủ thông tin
                Intent intent = new Intent(StudentCourseLessonsActivity.this, StudentLessonLearningActivity.class);
                intent.putExtra("lessonId", lesson.getId());
                intent.putExtra("lessonTitle", lesson.getTitle());
                intent.putExtra("courseId", courseId);
                intent.putExtra("courseTitle", courseTitle);
                intent.putExtra("courseCategory", courseCategory);

                android.util.Log.d("StudentCourseLessons", "Starting StudentLessonLearningActivity with Intent");
                startActivity(intent);
            }

            @Override
            public void onFavoriteChanged(Lesson lesson, boolean isFavorite) {
                // Log favorite status change
                android.util.Log.d("StudentCourseLessons",
                    "Lesson " + lesson.getTitle() + " favorite status changed to: " + isFavorite);
            }

            @Override
            public void onLessonCompleted(Lesson lesson) {
                // Xử lý khi bài học được đánh dấu hoàn thành
                android.util.Log.d("StudentCourseLessons", "Lesson completed: " + lesson.getTitle());

                // Tính toán và hiển thị tiến độ khóa học cập nhật
                calculateAndShowCourseProgress();

                // Hiển thị thông báo khuyến khích
                showCompletionEncouragement(lesson);
            }
        }, courseId, courseTitle);

        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);
    }

    private void loadLessons() {
        layoutNoLessons.setVisibility(View.VISIBLE);
        rvLessons.setVisibility(View.GONE);

        android.util.Log.d("StudentCourseLessons", "=== DEBUG: Starting loadLessons ===");
        android.util.Log.d("StudentCourseLessons", "CourseId: " + courseId);
        android.util.Log.d("StudentCourseLessons", "CourseTitle: " + courseTitle);

        // Load lessons with proper isPublished filter (now that we fixed lesson creation)
        db.collection("lessons")
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("isPublished", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                lessonList.clear();

                android.util.Log.d("StudentCourseLessons", "=== FIREBASE SUCCESS ===");
                android.util.Log.d("StudentCourseLessons", "Total published lessons found: " + queryDocumentSnapshots.size());

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Lesson lesson = document.toObject(Lesson.class);
                    lesson.setId(document.getId());

                    // Set all lessons as accessible for now
                    lesson.setAccessible(true);
                    lesson.setLocked(false);
                    lesson.setCompleted(false);

                    lessonList.add(lesson);

                    android.util.Log.d("StudentCourseLessons", "Added lesson: " + lesson.getTitle() + " (Order: " + lesson.getOrder() + ")");
                }

                // Sort lessons by order
                lessonList.sort((l1, l2) -> Integer.compare(l1.getOrder(), l2.getOrder()));

                android.util.Log.d("StudentCourseLessons", "Sorted lessons, total: " + lessonList.size());

                // Show lessons immediately
                showLessonsDirectly();

                // Then load progress in background
                if (mAuth.getCurrentUser() != null) {
                    loadLessonProgressStatus();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseLessons", "=== FIREBASE ERROR ===", e);
                Toast.makeText(this, "Lỗi tải bài học: " + e.getMessage(), Toast.LENGTH_LONG).show();
                layoutNoLessons.setVisibility(View.VISIBLE);
                rvLessons.setVisibility(View.GONE);
            });
    }

    private void showLessonsDirectly() {
        if (lessonList.isEmpty()) {
            layoutNoLessons.setVisibility(View.VISIBLE);
            rvLessons.setVisibility(View.GONE);
        } else {
            layoutNoLessons.setVisibility(View.GONE);
            rvLessons.setVisibility(View.VISIBLE);
            lessonAdapter.notifyDataSetChanged();
        }
    }

    private void loadLessonProgressStatus() {
        if (mAuth.getCurrentUser() == null) return;

        String studentId = mAuth.getCurrentUser().getUid();

        // Load progress for all lessons at once
        db.collection("lesson_progress")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Create a map of completed lessons for quick lookup
                    Map<String, Boolean> completedLessons = new HashMap<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String lessonId = doc.getString("lessonId");
                        Boolean isCompleted = doc.getBoolean("isCompleted");
                        if (lessonId != null && isCompleted != null && isCompleted) {
                            completedLessons.put(lessonId, true);
                        }
                    }

                    // Update lesson completion status
                    for (Lesson lesson : lessonList) {
                        lesson.setCompleted(completedLessons.containsKey(lesson.getId()));
                    }

                    // Refresh adapter
                    lessonAdapter.notifyDataSetChanged();

                    android.util.Log.d("StudentCourseLessons",
                        "Loaded progress for " + completedLessons.size() + " completed lessons");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentCourseLessons", "Error loading lesson progress", e);
                });
    }

    private void calculateAndShowCourseProgress() {
        if (lessonList.isEmpty()) return;

        int totalLessons = lessonList.size();
        int completedLessons = 0;

        for (Lesson lesson : lessonList) {
            if (lesson.isCompleted()) {
                completedLessons++;
            }
        }

        int progressPercentage = totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0;

        // Hiển thị thông báo tiến độ
        String progressMessage = "📊 Tiến độ khóa học: " + completedLessons + "/" + totalLessons +
                               " (" + progressPercentage + "% hoàn thành)";

        Toast.makeText(this, progressMessage, Toast.LENGTH_LONG).show();

        // Kiểm tra nếu hoàn thành toàn bộ khóa học
        if (completedLessons == totalLessons && totalLessons > 0) {
            showCourseCompletionDialog();
        }

        android.util.Log.d("StudentCourseLessons", "Course progress: " + progressPercentage + "%");
    }

    private void showCompletionEncouragement(Lesson lesson) {
        // Tính số bài học còn lại
        int remainingLessons = 0;
        for (Lesson l : lessonList) {
            if (!l.isCompleted()) {
                remainingLessons++;
            }
        }

        String encouragement;
        if (remainingLessons == 0) {
            encouragement = "🎉 Chúc mừng! Bạn đã hoàn thành toàn bộ khóa học!";
        } else if (remainingLessons == 1) {
            encouragement = "💪 Tuyệt vời! Chỉ còn 1 bài học nữa để hoàn thành khóa học!";
        } else {
            encouragement = "👏 Tốt lắm! Còn " + remainingLessons + " bài học nữa để hoàn thành khóa học!";
        }

        Toast.makeText(this, encouragement, Toast.LENGTH_LONG).show();
    }

    private void showCourseCompletionDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🎉 Chúc mừng!")
                .setMessage("Bạn đã hoàn thành toàn bộ khóa học '" + courseTitle + "'!\n\n" +
                           "Bạn có thể tiếp tục ôn tập các bài học hoặc tham gia làm bài kiểm tra.")
                .setPositiveButton("Làm bài kiểm tra", (dialog, which) -> {
                    // Chuyển đến màn hình làm bài kiểm tra nếu có
                    Toast.makeText(this, "Chức năng làm bài kiểm tra sẽ được thêm sau", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Ôn tập", (dialog, which) -> {
                    // Giữ nguyên màn hình để ôn tập
                    dialog.dismiss();
                })
                .setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
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

        // Load student progress from lesson_progress collection
        db.collection("lesson_progress")
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
    public void onFavoriteChanged(Lesson lesson, boolean isFavorite) {
        // Handle favorite status change
        String message = isFavorite ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích";
        android.util.Log.d("StudentCourseLessons", "Favorite changed: " + lesson.getTitle() + " - " + isFavorite);
        // Could show a toast or update UI if needed
    }

    @Override
    public void onLessonCompleted(Lesson lesson) {
        // Xử lý khi bài học được đánh dấu hoàn thành
        android.util.Log.d("StudentCourseLessons", "Lesson completed: " + lesson.getTitle());

        // Tính toán và hiển thị tiến độ khóa học cập nhật
        calculateAndShowCourseProgress();

        // Hiển thị thông báo khuyến khích
        showCompletionEncouragement(lesson);
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
        // Refresh lesson progress when returning to this activity
        if (lessonAdapter != null && !lessonList.isEmpty()) {
            loadLessonProgressStatus();
        }
    }
}
