package com.vantinh.tienganh;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentCourseSearchActivity extends AppCompatActivity {

    private RecyclerView rvCourses;
    private SearchView searchView;
    private ChipGroup chipGroupCategories, chipGroupLevels;
    private LinearLayout layoutNoCourses;
    private Toolbar toolbar;
    private TextView tvCourseCount;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Course> courseList;
    private StudentCourseAdapter courseAdapter;
    private String selectedCategory = "";
    private String selectedLevel = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_course_search);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        courseList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilters();
        loadAvailableCourses();
        addAnimations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvCourses = findViewById(R.id.rv_courses);
        searchView = findViewById(R.id.search_view);
        chipGroupCategories = findViewById(R.id.chip_group_categories);
        chipGroupLevels = findViewById(R.id.chip_group_levels);
        layoutNoCourses = findViewById(R.id.layout_no_courses);
        tvCourseCount = findViewById(R.id.tv_course_count);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tìm kiếm khóa học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        courseAdapter = new StudentCourseAdapter(courseList, this::onCourseClick);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(courseAdapter);
    }

    private void setupSearchAndFilters() {
        // Setup search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCourses();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {
                    filterCourses();
                }
                return true;
            }
        });

        // Setup category chips
        String[] categories = {"Tất cả", "Grammar", "Vocabulary", "Listening", "Speaking", "Reading", "Writing"};
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck other chips
                    for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                        Chip otherChip = (Chip) chipGroupCategories.getChildAt(i);
                        if (otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                    selectedCategory = category.equals("Tất cả") ? "" : category;
                    filterCourses();
                }
            });
            chipGroupCategories.addView(chip);

            // Set "Tất cả" as default
            if (category.equals("Tất cả")) {
                chip.setChecked(true);
            }
        }

        // Setup level chips
        String[] levels = {"Tất cả", "Beginner", "Intermediate", "Advanced"};
        for (String level : levels) {
            Chip chip = new Chip(this);
            chip.setText(level);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck other chips
                    for (int i = 0; i < chipGroupLevels.getChildCount(); i++) {
                        Chip otherChip = (Chip) chipGroupLevels.getChildAt(i);
                        if (otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                    selectedLevel = level.equals("Tất cả") ? "" : level;
                    filterCourses();
                }
            });
            chipGroupLevels.addView(chip);

            // Set "Tất cả" as default
            if (level.equals("Tất cả")) {
                chip.setChecked(true);
            }
        }
    }

    private void addAnimations() {
        rvCourses.setAlpha(0f);
        rvCourses.animate()
            .alpha(1f)
            .setDuration(500)
            .start();
    }

    private void onCourseClick(Course course) {
        // Show dialog to request enrollment
        showEnrollmentRequestDialog(course);
    }

    private void showEnrollmentRequestDialog(Course course) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_enrollment_request, null);

        TextView tvCourseName = dialogView.findViewById(R.id.tv_course_name);
        TextView tvTeacherName = dialogView.findViewById(R.id.tv_teacher_name);
        android.widget.EditText etMessage = dialogView.findViewById(R.id.et_message);

        tvCourseName.setText(course.getTitle());
        tvTeacherName.setText("Giáo viên: " + course.getTeacherName());

        builder.setView(dialogView)
                .setTitle("Yêu cầu tham gia khóa học")
                .setPositiveButton("Gửi yêu cầu", (dialog, which) -> {
                    String message = etMessage.getText().toString().trim();
                    if (message.isEmpty()) {
                        message = "Tôi muốn tham gia khóa học này.";
                    }
                    sendEnrollmentRequest(course, message);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void sendEnrollmentRequest(Course course, String message) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentId = mAuth.getCurrentUser().getUid();

        // Get student info first
        db.collection("users").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String studentName = documentSnapshot.getString("name");
                        String studentEmail = documentSnapshot.getString("email");

                        // Check if request already exists
                        checkExistingRequest(course, studentId, studentName, studentEmail, message);
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin học viên", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkExistingRequest(Course course, String studentId, String studentName, String studentEmail, String message) {
        db.collection("courseRequests")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", course.getId())
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Bạn đã gửi yêu cầu cho khóa học này rồi", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if already enrolled
                    checkExistingEnrollment(course, studentId, studentName, studentEmail, message);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kiểm tra yêu cầu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkExistingEnrollment(Course course, String studentId, String studentName, String studentEmail, String message) {
        db.collection("enrollments")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", course.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Bạn đã tham gia khóa học này rồi", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create new request
                    createEnrollmentRequest(course, studentId, studentName, studentEmail, message);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kiểm tra ghi danh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createEnrollmentRequest(Course course, String studentId, String studentName, String studentEmail, String message) {
        CourseRequest request = new CourseRequest(
                studentId,
                studentName,
                studentEmail,
                course.getId(),
                course.getTitle(),
                course.getTeacherId(),
                message
        );

        db.collection("courseRequests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Yêu cầu đã được gửi thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi gửi yêu cầu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAvailableCourses() {
        // Show loading indicator
        layoutNoCourses.setVisibility(View.VISIBLE);
        rvCourses.setVisibility(View.GONE);

        // Load ALL active courses from teachers - simplified query to avoid index requirement
        db.collection("courses")
            .whereEqualTo("isActive", true)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    courseList.clear();
                    android.util.Log.d("StudentCourseSearch", "Found " + task.getResult().size() + " total courses");

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Course course = document.toObject(Course.class);
                        course.setId(document.getId());
                        courseList.add(course);

                        android.util.Log.d("StudentCourseSearch", "Added course: " + course.getTitle() +
                                           " by teacher: " + course.getTeacherName());
                    }

                    // Sort by creation date in code instead of query
                    courseList.sort((c1, c2) -> {
                        if (c1.getCreatedAt() == null && c2.getCreatedAt() == null) return 0;
                        if (c1.getCreatedAt() == null) return 1;
                        if (c2.getCreatedAt() == null) return -1;
                        return c2.getCreatedAt().compareTo(c1.getCreatedAt()); // Newest first
                    });

                    // Apply initial filter based on intent extra (if any)
                    applyInitialFilter();

                    // Display results
                    filterCourses();

                    Toast.makeText(this, "Đã tải " + courseList.size() + " khóa học", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.e("StudentCourseSearch", "Error loading courses", task.getException());
                    Toast.makeText(this, "Lỗi khi tải khóa học: " + task.getException().getMessage(),
                                 Toast.LENGTH_SHORT).show();
                    layoutNoCourses.setVisibility(View.VISIBLE);
                    rvCourses.setVisibility(View.GONE);
                }
            });
    }

    private void applyInitialFilter() {
        // Check if we came from a specific category button
        String categoryFromIntent = getIntent().getStringExtra("category");
        if (categoryFromIntent != null && !categoryFromIntent.isEmpty()) {
            selectedCategory = categoryFromIntent;

            // Update the corresponding chip to be selected
            for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupCategories.getChildAt(i);
                if (chip.getText().toString().equals(categoryFromIntent)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    private void filterCourses() {
        List<Course> filteredList = new ArrayList<>();
        String searchQuery = searchView.getQuery().toString().toLowerCase().trim();

        for (Course course : courseList) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                                  course.getTitle().toLowerCase().contains(searchQuery) ||
                                  course.getDescription().toLowerCase().contains(searchQuery) ||
                                  course.getTeacherName().toLowerCase().contains(searchQuery);

            boolean matchesCategory = selectedCategory.isEmpty() ||
                                    course.getCategory().equals(selectedCategory);

            boolean matchesLevel = selectedLevel.isEmpty() ||
                                 course.getLevel().equals(selectedLevel);

            if (matchesSearch && matchesCategory && matchesLevel) {
                filteredList.add(course);
            }
        }

        if (filteredList.isEmpty()) {
            layoutNoCourses.setVisibility(View.VISIBLE);
            rvCourses.setVisibility(View.GONE);
        } else {
            layoutNoCourses.setVisibility(View.GONE);
            rvCourses.setVisibility(View.VISIBLE);
        }

        courseAdapter.updateCourses(filteredList);
        tvCourseCount.setText("Tìm thấy " + filteredList.size() + " khóa học");
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
