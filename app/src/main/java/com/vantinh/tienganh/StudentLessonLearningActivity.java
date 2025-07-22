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

public class StudentLessonLearningActivity extends AppCompatActivity {

    private ScrollView scrollViewContent;
    private TextView tvLessonTitle, tvLessonType, tvEstimatedTime;
    private TextView tvLessonContent;
    private LinearLayout layoutGrammarContent;
    private TextView tvGrammarRule, tvGrammarStructure;
    private LinearLayout layoutExamples, layoutUsage, layoutNotes;
    private Button btnMarkCompleted, btnNextLesson, btnPreviousLesson;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String lessonId, lessonTitle, courseId, courseTitle, courseCategory;
    private Lesson currentLesson;

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

        if (lessonId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin bài học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
        loadLessonData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        scrollViewContent = findViewById(R.id.scroll_view_content);
        tvLessonTitle = findViewById(R.id.tv_lesson_title);
        tvLessonType = findViewById(R.id.tv_lesson_type);
        tvEstimatedTime = findViewById(R.id.tv_estimated_time);
        tvLessonContent = findViewById(R.id.tv_lesson_content);

        // Grammar-specific views
        layoutGrammarContent = findViewById(R.id.layout_grammar_content);
        tvGrammarRule = findViewById(R.id.tv_grammar_rule);
        tvGrammarStructure = findViewById(R.id.tv_grammar_structure);
        layoutExamples = findViewById(R.id.layout_examples);
        layoutUsage = findViewById(R.id.layout_usage);
        layoutNotes = findViewById(R.id.layout_notes);

        // Action buttons
        btnMarkCompleted = findViewById(R.id.btn_mark_completed);
        btnNextLesson = findViewById(R.id.btn_next_lesson);
        btnPreviousLesson = findViewById(R.id.btn_previous_lesson);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Học bài");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        btnMarkCompleted.setOnClickListener(v -> markLessonAsCompleted());
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

        // Display grammar structure
        if (currentLesson.getGrammarStructure() != null && !currentLesson.getGrammarStructure().isEmpty()) {
            tvGrammarStructure.setText(currentLesson.getGrammarStructure());
            tvGrammarStructure.setVisibility(View.VISIBLE);
        } else {
            tvGrammarStructure.setVisibility(View.GONE);
        }

        // Display examples
        displayGrammarList(layoutExamples, currentLesson.getGrammarExamples(), "📚 Ví dụ:");

        // Display usage notes
        displayGrammarList(layoutUsage, currentLesson.getGrammarUsage(), "💡 Cách sử dụng:");

        // Display notes
        displayGrammarList(layoutNotes, currentLesson.getGrammarNotes(), "📋 Ghi chú:");
    }

    private void displayGrammarList(LinearLayout container, java.util.List<String> items, String title) {
        container.removeAllViews();

        if (items != null && !items.isEmpty()) {
            // Add title
            TextView titleView = new TextView(this);
            titleView.setText(title);
            titleView.setTextSize(16);
            titleView.setTextColor(getColor(android.R.color.holo_blue_dark));
            titleView.setPadding(0, 16, 0, 8);
            container.addView(titleView);

            // Add items
            for (int i = 0; i < items.size(); i++) {
                TextView itemView = new TextView(this);
                itemView.setText((i + 1) + ". " + items.get(i));
                itemView.setTextSize(14);
                itemView.setPadding(16, 4, 0, 4);
                container.addView(itemView);
            }

            container.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.GONE);
        }
    }

    private void setupLessonNavigation() {
        // TODO: Implement lesson navigation logic
        // For now, disable navigation buttons
        btnNextLesson.setEnabled(false);
        btnPreviousLesson.setEnabled(false);
    }

    private void markLessonAsCompleted() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement lesson completion tracking
        Toast.makeText(this, "Đã đánh dấu hoàn thành bài học!", Toast.LENGTH_SHORT).show();
        btnMarkCompleted.setText("Đã hoàn thành");
        btnMarkCompleted.setEnabled(false);
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
