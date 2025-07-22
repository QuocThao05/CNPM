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

public class CreateCourseActivity extends AppCompatActivity {

    private TextInputEditText etCourseTitle, etCourseDescription, etDuration;
    private Spinner spinnerLevel, spinnerCategory;
    private Button btnCreateCourse;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupSpinners();
        setupClickListeners();
        addAnimations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etCourseTitle = findViewById(R.id.et_course_title);
        etCourseDescription = findViewById(R.id.et_course_description);
        etDuration = findViewById(R.id.et_duration);
        spinnerLevel = findViewById(R.id.spinner_level);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnCreateCourse = findViewById(R.id.btn_create_course);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tạo khóa học mới");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinners() {
        // Level spinner
        String[] levels = {"Beginner", "Intermediate", "Advanced"};
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);

        // Category spinner
        String[] categories = {"Grammar", "Vocabulary", "Listening", "Speaking", "Reading", "Writing"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupClickListeners() {
        btnCreateCourse.setOnClickListener(v -> createCourse());
    }

    private void addAnimations() {
        // Add fade-in animation for the form
        findViewById(R.id.course_form_container).setAlpha(0f);
        findViewById(R.id.course_form_container).animate()
            .alpha(1f)
            .setDuration(500)
            .start();
    }

    private void createCourse() {
        // Check if user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để tạo khóa học", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etCourseTitle.getText().toString().trim();
        String description = etCourseDescription.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();
        String level = spinnerLevel.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (title.isEmpty()) {
            etCourseTitle.setError("Vui lòng nhập tên khóa học");
            return;
        }

        if (description.isEmpty()) {
            etCourseDescription.setError("Vui lòng nhập mô tả khóa học");
            return;
        }

        int duration = 0;
        if (!durationStr.isEmpty()) {
            try {
                duration = Integer.parseInt(durationStr);
            } catch (NumberFormatException e) {
                etDuration.setError("Thời lượng không hợp lệ");
                return;
            }
        }

        // Show loading animation
        btnCreateCourse.setEnabled(false);
        btnCreateCourse.setText("Đang tạo...");

        // Create course object with proper user info
        Map<String, Object> course = new HashMap<>();
        course.put("title", title);
        course.put("description", description);
        course.put("teacherId", mAuth.getCurrentUser().getUid());
        course.put("level", level);
        course.put("category", category);
        course.put("duration", duration);
        course.put("createdAt", new Date());
        course.put("updatedAt", new Date());
        course.put("isActive", true);
        course.put("enrolledStudents", 0);
        course.put("rating", 0.0);

        // Add debug logging
        android.util.Log.d("CreateCourse", "User ID: " + mAuth.getCurrentUser().getUid());
        android.util.Log.d("CreateCourse", "Creating course: " + title);

        // Save to Firestore
        db.collection("courses")
            .add(course)
            .addOnSuccessListener(documentReference -> {
                android.util.Log.d("CreateCourse", "Course created successfully: " + documentReference.getId());
                Toast.makeText(this, "Khóa học đã được tạo thành công!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("CreateCourse", "Error creating course", e);
                Toast.makeText(this, "Lỗi khi tạo khóa học: " + e.getMessage(), Toast.LENGTH_LONG).show();
                btnCreateCourse.setEnabled(true);
                btnCreateCourse.setText("Tạo khóa học");
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
