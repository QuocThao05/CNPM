package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditCourseActivity extends AppCompatActivity {

    private EditText etCourseTitle, etCourseDescription, etCourseDuration;
    private Spinner spinnerLevel, spinnerCategory;
    private Button btnUpdateCourse, btnDeleteCourse, btnManageLessons, btnViewStudents, btnEditTestQuestions;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String courseId;
    private Course currentCourse;

    // Arrays cho spinners
    private String[] levels = {"Beginner", "Intermediate", "Advanced"};
    private String[] categories = {"Grammar", "Vocabulary", "Listening", "Speaking", "Reading", "Writing"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get course ID from intent
        courseId = getIntent().getStringExtra("courseId");
        if (courseId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupSpinners();
        setupBottomNavigation();
        setupClickListeners();
        loadCourseData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        etCourseTitle = findViewById(R.id.et_course_title);
        etCourseDescription = findViewById(R.id.et_course_description);
        etCourseDuration = findViewById(R.id.et_course_duration);
        spinnerLevel = findViewById(R.id.spinner_level);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnUpdateCourse = findViewById(R.id.btn_update_course);
        btnDeleteCourse = findViewById(R.id.btn_delete_course);
        btnManageLessons = findViewById(R.id.btn_manage_lessons);
        btnViewStudents = findViewById(R.id.btn_view_students);
        btnEditTestQuestions = findViewById(R.id.btn_edit_test_questions); // Thêm nút mới
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chỉnh sửa khóa học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinners() {
        // Setup Level spinner
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);

        // Setup Category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_courses);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, TeacherDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_courses) {
                // Already on courses
                return true;
            } else if (itemId == R.id.nav_students) {
                startActivity(new Intent(this, EnrollmentManagementActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, UpdateProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        btnUpdateCourse.setOnClickListener(v -> updateCourse());
        btnDeleteCourse.setOnClickListener(v -> deleteCourse());
        btnManageLessons.setOnClickListener(v -> {
            Intent intent = new Intent(this, LessonManagementActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", currentCourse != null ? currentCourse.getTitle() : "");
            intent.putExtra("courseCategory", currentCourse != null ? currentCourse.getCategory() : "");
            startActivity(intent);
        });
        btnViewStudents.setOnClickListener(v -> {
            Intent intent = new Intent(this, CourseStudentsActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", currentCourse != null ? currentCourse.getTitle() : "");
            startActivity(intent);
        });
        btnEditTestQuestions.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditTestQuestionsActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", currentCourse != null ? currentCourse.getTitle() : "");
            startActivity(intent);
        });
    }

    private void loadCourseData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("courses").document(courseId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                progressBar.setVisibility(View.GONE);
                if (documentSnapshot.exists()) {
                    currentCourse = documentSnapshot.toObject(Course.class);
                    if (currentCourse != null) {
                        currentCourse.setId(documentSnapshot.getId());
                        populateFields();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Lỗi khi tải dữ liệu khóa học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                android.util.Log.e("EditCourse", "Error loading course", e);
            });
    }

    private void populateFields() {
        // Điền thông tin khóa học vào các trường
        etCourseTitle.setText(currentCourse.getTitle());
        etCourseDescription.setText(currentCourse.getDescription());
        etCourseDuration.setText(String.valueOf(currentCourse.getDuration()));

        // Set spinner selections
        setSpinnerSelection(spinnerLevel, currentCourse.getLevel());
        setSpinnerSelection(spinnerCategory, currentCourse.getCategory());

        android.util.Log.d("EditCourse", "Populated fields for course: " + currentCourse.getTitle());
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private void updateCourse() {
        String title = etCourseTitle.getText().toString().trim();
        String description = etCourseDescription.getText().toString().trim();
        String durationStr = etCourseDuration.getText().toString().trim();
        String level = spinnerLevel.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        // Validation
        if (title.isEmpty()) {
            etCourseTitle.setError("Vui lòng nhập tên khóa học");
            etCourseTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etCourseDescription.setError("Vui lòng nhập mô tả khóa học");
            etCourseDescription.requestFocus();
            return;
        }

        if (durationStr.isEmpty()) {
            etCourseDuration.setError("Vui lòng nhập thời lượng");
            etCourseDuration.requestFocus();
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                etCourseDuration.setError("Thời lượng phải lớn hơn 0");
                etCourseDuration.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etCourseDuration.setError("Thời lượng phải là số");
            etCourseDuration.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdateCourse.setEnabled(false);

        // Tạo map để update
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("description", description);
        updates.put("duration", duration);
        updates.put("level", level);
        updates.put("category", category);
        updates.put("updatedAt", new Date());

        android.util.Log.d("EditCourse", "Updating course with: " + updates);

        db.collection("courses").document(courseId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                btnUpdateCourse.setEnabled(true);
                Toast.makeText(this, "Cập nhật khóa học thành công!", Toast.LENGTH_SHORT).show();

                // Cập nhật currentCourse object
                currentCourse.setTitle(title);
                currentCourse.setDescription(description);
                currentCourse.setDuration(duration);
                currentCourse.setLevel(level);
                currentCourse.setCategory(category);

                android.util.Log.d("EditCourse", "Course updated successfully");
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnUpdateCourse.setEnabled(true);
                Toast.makeText(this, "Lỗi khi cập nhật khóa học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                android.util.Log.e("EditCourse", "Error updating course", e);
            });
    }

    private void deleteCourse() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa khóa học")
            .setMessage("Bạn có chắc chắn muốn xóa khóa học \"" + currentCourse.getTitle() + "\"?\n\nHành động này sẽ:\n- Xóa khóa học vĩnh viễn\n- Xóa tất cả bài học trong khóa\n- Không thể hoàn tác")
            .setPositiveButton("Xóa", (dialog, which) -> performDeleteCourse())
            .setNegativeButton("Hủy", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void performDeleteCourse() {
        progressBar.setVisibility(View.VISIBLE);
        btnDeleteCourse.setEnabled(false);

        db.collection("courses").document(courseId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Đã xóa khóa học thành công", Toast.LENGTH_SHORT).show();
                android.util.Log.d("EditCourse", "Course deleted successfully");

                // Quay về CourseManagementActivity
                Intent intent = new Intent(this, CourseManagementActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnDeleteCourse.setEnabled(true);
                Toast.makeText(this, "Lỗi khi xóa khóa học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                android.util.Log.e("EditCourse", "Error deleting course", e);
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

    @Override
    protected void onResume() {
        super.onResume();
        // Reload course data when returning to this activity
        if (courseId != null) {
            loadCourseData();
        }
    }
}
