package com.vantinh.tienganh;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateLessonActivity extends AppCompatActivity {

    private TextInputEditText etLessonTitle, etLessonContent, etEstimatedTime;
    private Spinner spinnerLessonType;
    private Button btnCreateLesson, btnAddExample, btnAddUsage, btnAddNote;
    private Toolbar toolbar;

    // Grammar-specific fields
    private LinearLayout layoutGrammarContent;
    private TextInputEditText etGrammarRule, etGrammarStructure;
    private LinearLayout layoutExamples, layoutUsage, layoutNotes;
    private List<TextInputEditText> exampleInputs, usageInputs, noteInputs;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String courseId, courseTitle, courseCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lesson);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get course info from intent
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");

        initViews();
        setupToolbar();
        setupSpinners();
        setupClickListeners();
        loadCourseCategory();
        addAnimations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etLessonTitle = findViewById(R.id.et_lesson_title);
        etLessonContent = findViewById(R.id.et_lesson_content);
        etEstimatedTime = findViewById(R.id.et_estimated_time);
        spinnerLessonType = findViewById(R.id.spinner_lesson_type);
        btnCreateLesson = findViewById(R.id.btn_create_lesson);

        // Grammar-specific views - Kích hoạt lại các view
        layoutGrammarContent = findViewById(R.id.layout_grammar_content);
        etGrammarRule = findViewById(R.id.et_grammar_rule);
        etGrammarStructure = findViewById(R.id.et_grammar_structure);
        layoutExamples = findViewById(R.id.layout_examples);
        layoutUsage = findViewById(R.id.layout_usage);
        layoutNotes = findViewById(R.id.layout_notes);
        btnAddExample = findViewById(R.id.btn_add_example);
        btnAddUsage = findViewById(R.id.btn_add_usage);
        btnAddNote = findViewById(R.id.btn_add_note);

        // Initialize lists
        exampleInputs = new ArrayList<>();
        usageInputs = new ArrayList<>();
        noteInputs = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tạo bài học mới");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinners() {
        String[] lessonTypes = {"text", "video", "audio", "quiz"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, lessonTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLessonType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnCreateLesson.setOnClickListener(v -> createLesson());
        btnAddExample.setOnClickListener(v -> addExampleField());
        btnAddUsage.setOnClickListener(v -> addUsageField());
        btnAddNote.setOnClickListener(v -> addNoteField());
    }

    private void loadCourseCategory() {
        if (courseId == null) return;

        db.collection("courses").document(courseId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    courseCategory = documentSnapshot.getString("category");
                    android.util.Log.d("CreateLesson", "Course category: " + courseCategory);

                    // Show/hide grammar-specific fields based on category
                    if ("Grammar".equalsIgnoreCase(courseCategory)) {
                        layoutGrammarContent.setVisibility(View.VISIBLE);
                        addInitialFields();
                    } else {
                        layoutGrammarContent.setVisibility(View.GONE);
                    }
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("CreateLesson", "Error loading course category", e);
                Toast.makeText(this, "Lỗi tải thông tin khóa học", Toast.LENGTH_SHORT).show();
            });
    }

    private void addInitialFields() {
        // Add initial example, usage, and note fields for Grammar lessons
        addExampleField();
        addUsageField();
        addNoteField();
    }

    private void addExampleField() {
        TextInputEditText etExample = new TextInputEditText(this);
        etExample.setHint("Ví dụ " + (exampleInputs.size() + 1));
        etExample.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        etExample.setPadding(0, 8, 0, 8);

        layoutExamples.addView(etExample);
        exampleInputs.add(etExample);
    }

    private void addUsageField() {
        TextInputEditText etUsage = new TextInputEditText(this);
        etUsage.setHint("Cách sử dụng " + (usageInputs.size() + 1));
        etUsage.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        etUsage.setPadding(0, 8, 0, 8);

        layoutUsage.addView(etUsage);
        usageInputs.add(etUsage);
    }

    private void addNoteField() {
        TextInputEditText etNote = new TextInputEditText(this);
        etNote.setHint("Ghi chú " + (noteInputs.size() + 1));
        etNote.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        etNote.setPadding(0, 8, 0, 8);

        layoutNotes.addView(etNote);
        noteInputs.add(etNote);
    }

    private void addAnimations() {
        // Add fade-in animation - Comment out vì không có main_content ID
        // findViewById(R.id.main_content).setAlpha(0f);
        // findViewById(R.id.main_content).animate()
        //     .alpha(1f)
        //     .setDuration(500)
        //     .start();
    }

    private void createLesson() {
        String title = etLessonTitle.getText().toString().trim();
        String content = etLessonContent.getText().toString().trim();
        String estimatedTimeStr = etEstimatedTime.getText().toString().trim();
        String lessonType = spinnerLessonType.getSelectedItem().toString();

        // Validation
        if (TextUtils.isEmpty(title)) {
            etLessonTitle.setError("Vui lòng nhập tiêu đề bài học");
            return;
        }

        if (TextUtils.isEmpty(content)) {
            etLessonContent.setError("Vui lòng nhập nội dung bài học");
            return;
        }

        int estimatedTime = 30; // default
        if (!TextUtils.isEmpty(estimatedTimeStr)) {
            try {
                estimatedTime = Integer.parseInt(estimatedTimeStr);
            } catch (NumberFormatException e) {
                etEstimatedTime.setError("Thời gian ước tính phải là số");
                return;
            }
        }

        // Create lesson object
        Map<String, Object> lessonData = new HashMap<>();
        lessonData.put("title", title);
        lessonData.put("content", content);
        lessonData.put("courseId", courseId);
        lessonData.put("teacherId", mAuth.getCurrentUser().getUid());
        lessonData.put("type", lessonType);
        lessonData.put("category", courseCategory);
        lessonData.put("estimatedTime", estimatedTime);
        lessonData.put("createdAt", new Date());
        lessonData.put("updatedAt", new Date());
        lessonData.put("isPublished", false);

        // Add grammar-specific data if this is a Grammar lesson
        if ("Grammar".equalsIgnoreCase(courseCategory)) {
            addGrammarData(lessonData);
        }

        // Get next order number
        getNextOrderNumber(lessonData);
    }

    private void addGrammarData(Map<String, Object> lessonData) {
        String grammarRule = etGrammarRule.getText().toString().trim();
        String grammarStructure = etGrammarStructure.getText().toString().trim();

        if (!TextUtils.isEmpty(grammarRule)) {
            lessonData.put("grammarRule", grammarRule);
        }

        if (!TextUtils.isEmpty(grammarStructure)) {
            lessonData.put("grammarStructure", grammarStructure);
        }

        // Collect examples
        List<String> examples = new ArrayList<>();
        for (TextInputEditText etExample : exampleInputs) {
            String example = etExample.getText().toString().trim();
            if (!TextUtils.isEmpty(example)) {
                examples.add(example);
            }
        }
        if (!examples.isEmpty()) {
            lessonData.put("grammarExamples", examples);
        }

        // Collect usage notes
        List<String> usage = new ArrayList<>();
        for (TextInputEditText etUsage : usageInputs) {
            String usageNote = etUsage.getText().toString().trim();
            if (!TextUtils.isEmpty(usageNote)) {
                usage.add(usageNote);
            }
        }
        if (!usage.isEmpty()) {
            lessonData.put("grammarUsage", usage);
        }

        // Collect notes
        List<String> notes = new ArrayList<>();
        for (TextInputEditText etNote : noteInputs) {
            String note = etNote.getText().toString().trim();
            if (!TextUtils.isEmpty(note)) {
                notes.add(note);
            }
        }
        if (!notes.isEmpty()) {
            lessonData.put("grammarNotes", notes);
        }
    }

    private void getNextOrderNumber(Map<String, Object> lessonData) {
        db.collection("lessons")
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int nextOrder = queryDocumentSnapshots.size() + 1;
                lessonData.put("order", nextOrder);
                saveLessonToFirebase(lessonData);
            })
            .addOnFailureListener(e -> {
                lessonData.put("order", 1);
                saveLessonToFirebase(lessonData);
            });
    }

    private void saveLessonToFirebase(Map<String, Object> lessonData) {
        btnCreateLesson.setEnabled(false);
        btnCreateLesson.setText("Đang tạo...");

        db.collection("lessons")
            .add(lessonData)
            .addOnSuccessListener(documentReference -> {
                android.util.Log.d("CreateLesson", "Lesson created with ID: " + documentReference.getId());
                Toast.makeText(this, "Tạo bài học thành công!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("CreateLesson", "Error creating lesson", e);
                Toast.makeText(this, "Lỗi tạo bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnCreateLesson.setEnabled(true);
                btnCreateLesson.setText("Tạo bài học");
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
}
