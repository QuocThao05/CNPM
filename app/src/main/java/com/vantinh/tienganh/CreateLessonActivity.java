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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateLessonActivity extends AppCompatActivity {

    private TextInputEditText etLessonTitle, etLessonContent, etEstimatedTime;
    private Spinner spinnerLessonType;
    private Button btnCreateLesson;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String courseId, courseTitle;

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
        addAnimations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etLessonTitle = findViewById(R.id.et_lesson_title);
        etLessonContent = findViewById(R.id.et_lesson_content);
        etEstimatedTime = findViewById(R.id.et_estimated_time);
        spinnerLessonType = findViewById(R.id.spinner_lesson_type);
        btnCreateLesson = findViewById(R.id.btn_create_lesson);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tạo bài học mới");
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
        btnCreateLesson.setOnClickListener(v -> createLesson());
    }

    private void addAnimations() {
        findViewById(R.id.lesson_form_container).setAlpha(0f);
        findViewById(R.id.lesson_form_container).animate()
            .alpha(1f)
            .setDuration(500)
            .start();
    }

    private void createLesson() {
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

        final int estimatedTime = !timeStr.isEmpty() ?
            parseEstimatedTime(timeStr) : 30;

        if (estimatedTime == -1) {
            etEstimatedTime.setError("Thời gian không hợp lệ");
            return;
        }

        btnCreateLesson.setEnabled(false);
        btnCreateLesson.setText("Đang tạo...");

        // Get lesson order
        db.collection("lessons")
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnCompleteListener(task -> {
                int order = 1;
                if (task.isSuccessful()) {
                    order = task.getResult().size() + 1;
                }

                Map<String, Object> lesson = new HashMap<>();
                lesson.put("title", title);
                lesson.put("content", content);
                lesson.put("courseId", courseId);
                lesson.put("teacherId", mAuth.getCurrentUser().getUid());
                lesson.put("order", order);
                lesson.put("type", type);
                lesson.put("estimatedTime", estimatedTime);
                lesson.put("createdAt", new Date());
                lesson.put("updatedAt", new Date());
                lesson.put("isPublished", false);

                db.collection("lessons")
                    .add(lesson)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Bài học đã được tạo thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi tạo bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnCreateLesson.setEnabled(true);
                        btnCreateLesson.setText("Tạo bài học");
                    });
            });
    }

    private int parseEstimatedTime(String timeStr) {
        try {
            return Integer.parseInt(timeStr);
        } catch (NumberFormatException e) {
            return -1;
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
