package com.vantinh.tienganh;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditLessonActivity extends AppCompatActivity {

    private TextInputEditText etLessonTitle, etLessonContent, etEstimatedTime;
    private Spinner spinnerLessonType;
    private Button btnUpdateLesson;
    private Toolbar toolbar;

    private FirebaseFirestore db;
    private String lessonId, courseId;
    private Lesson currentLesson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lesson);

        db = FirebaseFirestore.getInstance();

        // Get lesson info from intent
        lessonId = getIntent().getStringExtra("lessonId");
        courseId = getIntent().getStringExtra("courseId");

        initViews();
        setupToolbar();
        setupSpinners();
        setupClickListeners();
        loadLessonData();
        addAnimations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etLessonTitle = findViewById(R.id.et_lesson_title);
        etLessonContent = findViewById(R.id.et_lesson_content);
        etEstimatedTime = findViewById(R.id.et_estimated_time);
        spinnerLessonType = findViewById(R.id.spinner_lesson_type);
        btnUpdateLesson = findViewById(R.id.btn_update_lesson);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sửa bài học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinners() {
        String[] types = {"text", "video", "audio", "quiz"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLessonType.setAdapter(typeAdapter);
    }

    private void setupClickListeners() {
        btnUpdateLesson.setOnClickListener(v -> updateLesson());
    }

    private void addAnimations() {
        findViewById(R.id.lesson_edit_container).setAlpha(0f);
        findViewById(R.id.lesson_edit_container).animate()
            .alpha(1f)
            .setDuration(500)
            .start();
    }

    private void loadLessonData() {
        if (lessonId == null) return;

        db.collection("lessons").document(lessonId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentLesson = documentSnapshot.toObject(Lesson.class);
                    if (currentLesson != null) {
                        currentLesson.setId(documentSnapshot.getId());
                        populateFields();
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tải dữ liệu bài học", Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void populateFields() {
        if (currentLesson == null) return;

        etLessonTitle.setText(currentLesson.getTitle());
        etLessonContent.setText(currentLesson.getContent());
        etEstimatedTime.setText(String.valueOf(currentLesson.getEstimatedTime()));

        // Set spinner selection
        String[] types = {"text", "video", "audio", "quiz"};
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(currentLesson.getType())) {
                spinnerLessonType.setSelection(i);
                break;
            }
        }
    }

    private void updateLesson() {
        String title = etLessonTitle.getText().toString().trim();
        String content = etLessonContent.getText().toString().trim();
        String timeStr = etEstimatedTime.getText().toString().trim();
        String type = spinnerLessonType.getSelectedItem().toString();

        if (title.isEmpty()) {
            etLessonTitle.setError("Vui lòng nhập tiêu đề bài học");
            return;
        }

        if (content.isEmpty()) {
            etLessonContent.setError("Vui lòng nhập nội dung bài học");
            return;
        }

        int estimatedTime = 30;
        if (!timeStr.isEmpty()) {
            try {
                estimatedTime = Integer.parseInt(timeStr);
            } catch (NumberFormatException e) {
                etEstimatedTime.setError("Thời gian không hợp lệ");
                return;
            }
        }

        btnUpdateLesson.setEnabled(false);
        btnUpdateLesson.setText("Đang cập nhật...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("content", content);
        updates.put("type", type);
        updates.put("estimatedTime", estimatedTime);
        updates.put("updatedAt", new Date());

        db.collection("lessons").document(lessonId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Bài học đã được cập nhật!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi cập nhật bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnUpdateLesson.setEnabled(true);
                btnUpdateLesson.setText("Cập nhật bài học");
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
