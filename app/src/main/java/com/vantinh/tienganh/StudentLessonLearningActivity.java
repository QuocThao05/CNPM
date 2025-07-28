package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vantinh.tienganh.models.LessonProgress;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class StudentLessonLearningActivity extends AppCompatActivity {

    private TextView tvLessonTitle, tvLessonType, tvEstimatedTime, tvLessonOrder;
    private TextView tvLessonContent;
    private LinearLayout layoutGrammarContent;
    private TextView tvGrammarRule, tvGrammarExplanation, tvGrammarExamples;
    private androidx.cardview.widget.CardView cardVocabularyContent;
    private TextView tvVocabularyWord, tvVocabularyPronunciation, tvVocabularyMeaning, tvVocabularyExample;
    private Button btnMarkComplete, btnNextLesson, btnPreviousLesson;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String lessonId, lessonTitle, courseId, courseTitle, courseCategory;
    private Lesson currentLesson;
    private boolean isLessonCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_lesson_learning);

        // Get lesson info from intent
        lessonId = getIntent().getStringExtra("lessonId");
        lessonTitle = getIntent().getStringExtra("lessonTitle");
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");
        courseCategory = getIntent().getStringExtra("courseCategory");

        // Debug logging để kiểm tra dữ liệu nhận được
        android.util.Log.d("StudentLessonLearning", "=== DEBUG RECEIVED DATA ===");
        android.util.Log.d("StudentLessonLearning", "Received lessonId: " + lessonId);
        android.util.Log.d("StudentLessonLearning", "Received lessonTitle: " + lessonTitle);
        android.util.Log.d("StudentLessonLearning", "Received courseId: " + courseId);
        android.util.Log.d("StudentLessonLearning", "Received courseTitle: " + courseTitle);
        android.util.Log.d("StudentLessonLearning", "Received courseCategory: " + courseCategory);

        if (lessonId == null) {
            android.util.Log.e("StudentLessonLearning", "ERROR: lessonId is null! Finishing activity.");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin bài học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (lessonId.isEmpty()) {
            android.util.Log.e("StudentLessonLearning", "ERROR: lessonId is empty! Finishing activity.");
            Toast.makeText(this, "Lỗi: ID bài học trống", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.util.Log.d("StudentLessonLearning", "✅ All data validation passed. Continuing with initialization.");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
        loadLessonData();
        checkLessonProgress();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvLessonTitle = findViewById(R.id.tv_lesson_title);
        tvLessonType = findViewById(R.id.tv_lesson_type);
        tvEstimatedTime = findViewById(R.id.tv_estimated_time);
        tvLessonContent = findViewById(R.id.tv_lesson_content);

        // Grammar-specific views
        layoutGrammarContent = findViewById(R.id.card_grammar_content);
        tvGrammarRule = findViewById(R.id.tv_grammar_rule);
        tvGrammarExplanation = findViewById(R.id.tv_grammar_explanation);
        tvGrammarExamples = findViewById(R.id.tv_grammar_examples);

        // Vocabulary-specific views
        cardVocabularyContent = findViewById(R.id.card_vocabulary_content);
        tvVocabularyWord = findViewById(R.id.tv_vocabulary_word);
        tvVocabularyPronunciation = findViewById(R.id.tv_vocabulary_pronunciation);
        tvVocabularyMeaning = findViewById(R.id.tv_vocabulary_meaning);
        tvVocabularyExample = findViewById(R.id.tv_vocabulary_example);

        // Action buttons
        btnMarkComplete = findViewById(R.id.btn_mark_complete);
        btnNextLesson = findViewById(R.id.btn_next_lesson);
        btnPreviousLesson = findViewById(R.id.btn_previous_lesson);
        tvLessonOrder = findViewById(R.id.tv_lesson_order);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Học bài");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        btnMarkComplete.setOnClickListener(v -> markLessonAsCompleted());
        btnNextLesson.setOnClickListener(v -> navigateToNextLesson());
        btnPreviousLesson.setOnClickListener(v -> navigateToPreviousLesson());
    }

    private void loadLessonData() {
        db.collection("lessons").document(lessonId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentLesson = documentSnapshot.toObject(Lesson.class);
                    if (currentLesson != null) {
                        currentLesson.setId(documentSnapshot.getId());
                        displayLessonContent();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentLessonLearning", "Error loading lesson", e);
                Toast.makeText(this, "Lỗi tải bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void displayLessonContent() {
        // Display basic lesson info
        tvLessonTitle.setText(currentLesson.getTitle());
        tvLessonType.setText(currentLesson.getTypeDisplayName());
        tvEstimatedTime.setText(currentLesson.getEstimatedTimeString());
        tvLessonContent.setText(currentLesson.getContent());

        // Display Grammar-specific content if applicable
        if ("Grammar".equalsIgnoreCase(currentLesson.getCategory())) {
            layoutGrammarContent.setVisibility(View.VISIBLE);
            displayGrammarContent();
        } else {
            layoutGrammarContent.setVisibility(View.GONE);
        }

        // Set up lesson navigation
        setupLessonNavigation();
    }

    private void displayGrammarContent() {
        // Display grammar rule
        if (currentLesson.getGrammarRule() != null && !currentLesson.getGrammarRule().isEmpty()) {
            tvGrammarRule.setText(currentLesson.getGrammarRule());
            tvGrammarRule.setVisibility(View.VISIBLE);
        } else {
            tvGrammarRule.setVisibility(View.GONE);
        }

        // Display grammar structure (using grammarStructure instead of grammarExplanation)
        if (currentLesson.getGrammarStructure() != null && !currentLesson.getGrammarStructure().isEmpty()) {
            tvGrammarExplanation.setText(currentLesson.getGrammarStructure());
            tvGrammarExplanation.setVisibility(View.VISIBLE);
        } else {
            tvGrammarExplanation.setVisibility(View.GONE);
        }

        // Display grammar examples
        if (currentLesson.getGrammarExamples() != null && !currentLesson.getGrammarExamples().isEmpty()) {
            tvGrammarExamples.setText(android.text.TextUtils.join("\n", currentLesson.getGrammarExamples()));
            tvGrammarExamples.setVisibility(View.VISIBLE);
        } else {
            tvGrammarExamples.setVisibility(View.GONE);
        }
    }

    private void setupLessonNavigation() {
        // TODO: Implement lesson navigation logic
        // For now, disable navigation buttons
        btnNextLesson.setEnabled(false);
        btnPreviousLesson.setEnabled(false);
    }

    private void checkLessonProgress() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String studentId = mAuth.getCurrentUser().getUid();

        db.collection("lesson_progress")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("lessonId", lessonId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        LessonProgress progress = queryDocumentSnapshots.getDocuments().get(0).toObject(LessonProgress.class);
                        if (progress != null && progress.isCompleted()) {
                            isLessonCompleted = true;
                            btnMarkComplete.setText("✅ Đã hoàn thành");
                            btnMarkComplete.setEnabled(false);
                            btnMarkComplete.setBackgroundColor(getColor(android.R.color.holo_green_light));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentLessonLearning", "Error checking lesson progress", e);
                });
    }

    private void markLessonAsCompleted() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLessonCompleted) {
            Toast.makeText(this, "Bài học đã được đánh dấu hoàn thành trước đó", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentId = mAuth.getCurrentUser().getUid();

        // Disable button while processing
        btnMarkComplete.setEnabled(false);
        btnMarkComplete.setText("Đang lưu...");

        // Check if progress record already exists
        db.collection("lesson_progress")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("lessonId", lessonId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Create new progress record
                        createLessonProgress(studentId);
                    } else {
                        // Update existing progress record
                        String progressId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        updateLessonProgress(progressId);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentLessonLearning", "Error checking lesson progress", e);
                    btnMarkComplete.setEnabled(true);
                    btnMarkComplete.setText("Đánh dấu hoàn thành");
                    Toast.makeText(this, "Lỗi kiểm tra tiến độ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createLessonProgress(String studentId) {
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("studentId", studentId);
        progressData.put("courseId", courseId);
        progressData.put("lessonId", lessonId);
        progressData.put("isCompleted", true);
        progressData.put("completedAt", new Date());
        progressData.put("createdAt", new Date());
        progressData.put("updatedAt", new Date());

        db.collection("lesson_progress")
                .add(progressData)
                .addOnSuccessListener(documentReference -> {
                    isLessonCompleted = true;
                    btnMarkComplete.setText("✅ Đã hoàn thành");
                    btnMarkComplete.setEnabled(false);
                    btnMarkComplete.setBackgroundColor(getColor(android.R.color.holo_green_light));

                    Toast.makeText(this, "Đã đánh dấu hoàn thành bài học!", Toast.LENGTH_SHORT).show();
                    android.util.Log.d("StudentLessonLearning", "Lesson progress created successfully");

                    // Calculate and show updated course progress
                    calculateAndShowCourseProgress();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentLessonLearning", "Error creating lesson progress", e);
                    btnMarkComplete.setEnabled(true);
                    btnMarkComplete.setText("Đánh dấu hoàn thành");
                    Toast.makeText(this, "Lỗi lưu tiến độ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLessonProgress(String progressId) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("isCompleted", true);
        updateData.put("completedAt", new Date());
        updateData.put("updatedAt", new Date());

        db.collection("lesson_progress").document(progressId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    isLessonCompleted = true;
                    btnMarkComplete.setText("✅ Đã hoàn thành");
                    btnMarkComplete.setEnabled(false);
                    btnMarkComplete.setBackgroundColor(getColor(android.R.color.holo_green_light));

                    Toast.makeText(this, "Đã đánh dấu hoàn thành bài học!", Toast.LENGTH_SHORT).show();
                    android.util.Log.d("StudentLessonLearning", "Lesson progress updated successfully");

                    // Calculate and show updated course progress
                    calculateAndShowCourseProgress();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentLessonLearning", "Error updating lesson progress", e);
                    btnMarkComplete.setEnabled(true);
                    btnMarkComplete.setText("Đánh dấu hoàn thành");
                    Toast.makeText(this, "Lỗi cập nhật tiến độ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // New method to calculate and display course progress after completing a lesson
    private void calculateAndShowCourseProgress() {
        if (mAuth.getCurrentUser() == null) return;

        String studentId = mAuth.getCurrentUser().getUid();

        // Get total lessons count
        db.collection("lessons")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("isPublished", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalLessons = queryDocumentSnapshots.size();

                    // Get completed lessons count
                    db.collection("lesson_progress")
                            .whereEqualTo("studentId", studentId)
                            .whereEqualTo("courseId", courseId)
                            .whereEqualTo("isCompleted", true)
                            .get()
                            .addOnSuccessListener(progressSnapshots -> {
                                int completedLessons = progressSnapshots.size();
                                int progressPercentage = totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0;

                                // Show progress update
                                String progressMessage = "📊 Tiến độ khóa học: " + completedLessons + "/" + totalLessons +
                                                       " (" + progressPercentage + "% hoàn thành)";

                                Toast.makeText(this, progressMessage, Toast.LENGTH_LONG).show();

                                // Check if course is completed
                                if (completedLessons == totalLessons && totalLessons > 0) {
                                    Toast.makeText(this, "🎉 Chúc mừng! Bạn đã hoàn thành toàn bộ khóa học: " + courseTitle + "!", Toast.LENGTH_LONG).show();
                                }

                                android.util.Log.d("StudentLessonLearning", "Course progress: " + completedLessons + "/" + totalLessons + " = " + progressPercentage + "%");
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("StudentLessonLearning", "Error calculating course progress", e);
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentLessonLearning", "Error getting total lessons", e);
                });
    }

    private void navigateToNextLesson() {
        Toast.makeText(this, "Chuyển đến bài học tiếp theo", Toast.LENGTH_SHORT).show();
    }

    private void navigateToPreviousLesson() {
        Toast.makeText(this, "Quay lại bài học trước", Toast.LENGTH_SHORT).show();
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
