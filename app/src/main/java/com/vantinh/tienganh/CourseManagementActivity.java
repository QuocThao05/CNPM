package com.vantinh.tienganh;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CourseManagementActivity extends AppCompatActivity {

    private RecyclerView rvCourses;
    private ExtendedFloatingActionButton fabAddCourse;
    // private LinearLayout tvNoCourses;
    private Toolbar toolbar;
    private TextInputEditText etSearch;
    private MaterialButton btnFilter;
    private TextView tvTotalCourses, tvTotalStudents;
    // private TextView tvAvgRating, tvCourseCount;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Course> courseList;
    private List<Course> filteredCourseList;
    private CourseAdapter courseAdapter;

    // Filter variables
    private String selectedCategory = "Tất cả";
    private String selectedLevel = "Tất cả";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_management);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        courseList = new ArrayList<>();
        filteredCourseList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilter();
        setupClickListeners();
        loadCourses();
        loadStatistics();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvCourses = findViewById(R.id.recyclerView);
        fabAddCourse = findViewById(R.id.fab_add_course);
        etSearch = findViewById(R.id.et_search);
        btnFilter = findViewById(R.id.btn_filter);
        tvTotalCourses = findViewById(R.id.tv_total_courses);
        tvTotalStudents = findViewById(R.id.tv_total_students);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý khóa học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        courseAdapter = new CourseAdapter(courseList, new CourseAdapter.OnCourseClickListener() {
            @Override
            public void onCourseClick(Course course) {
                // Xem chi tiết khóa học
                Intent intent = new Intent(CourseManagementActivity.this, EditCourseActivity.class);
                intent.putExtra("courseId", course.getId());
                startActivity(intent);
            }

            @Override
            public void onManageLessons(Course course) {
                // Chuyển đến quản lý bài học
                Intent intent = new Intent(CourseManagementActivity.this, LessonManagementActivity.class);
                intent.putExtra("courseId", course.getId());
                intent.putExtra("courseTitle", course.getTitle());
                intent.putExtra("courseCategory", course.getCategory());
                startActivity(intent);
            }

            @Override
            public void onEditCourse(Course course) {
                // Chỉnh sửa khóa học
                Intent intent = new Intent(CourseManagementActivity.this, EditCourseActivity.class);
                intent.putExtra("courseId", course.getId());
                startActivity(intent);
            }
        });
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(courseAdapter);
    }

    private void setupSearchAndFilter() {
        // Setup search functionality
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString().trim();
                    filterCourses();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Setup filter button
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_course_filter, null);

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);
        Spinner spinnerLevel = dialogView.findViewById(R.id.spinner_level);
        MaterialButton btnApply = dialogView.findViewById(R.id.btn_apply_filter);
        MaterialButton btnReset = dialogView.findViewById(R.id.btn_reset_filter);

        // Setup category spinner
        String[] categories = {"Tất cả", "Ngữ pháp", "Từ vựng", "Phát âm", "Nghe", "Nói", "Đọc", "Viết"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Setup level spinner
        String[] levels = {"Tất cả", "Beginner", "Intermediate", "Advanced"};
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);

        // Set current selections
        spinnerCategory.setSelection(getCategoryPosition(selectedCategory));
        spinnerLevel.setSelection(getLevelPosition(selectedLevel));

        AlertDialog dialog = builder.setView(dialogView).create();

        btnApply.setOnClickListener(v -> {
            selectedCategory = spinnerCategory.getSelectedItem().toString();
            selectedLevel = spinnerLevel.getSelectedItem().toString();
            filterCourses();
            updateFilterButtonText();
            dialog.dismiss();
            Toast.makeText(this, "Đã áp dụng bộ lọc", Toast.LENGTH_SHORT).show();
        });

        btnReset.setOnClickListener(v -> {
            selectedCategory = "Tất cả";
            selectedLevel = "Tất cả";
            spinnerCategory.setSelection(0);
            spinnerLevel.setSelection(0);
            filterCourses();
            updateFilterButtonText();
            dialog.dismiss();
            Toast.makeText(this, "Đã reset bộ lọc", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private int getCategoryPosition(String category) {
        String[] categories = {"Tất cả", "Ngữ pháp", "Từ vựng", "Phát âm", "Nghe", "Nói", "Đọc", "Viết"};
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                return i;
            }
        }
        return 0;
    }

    private int getLevelPosition(String level) {
        String[] levels = {"Tất cả", "Beginner", "Intermediate", "Advanced"};
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].equals(level)) {
                return i;
            }
        }
        return 0;
    }

    private void updateFilterButtonText() {
        boolean hasFilters = !selectedCategory.equals("Tất cả") || !selectedLevel.equals("Tất cả");
        if (hasFilters) {
            btnFilter.setText("🔧 Lọc (" + getActiveFilterCount() + ")");
        } else {
            btnFilter.setText("🔧 Lọc");
        }
    }

    private int getActiveFilterCount() {
        int count = 0;
        if (!selectedCategory.equals("Tất cả")) count++;
        if (!selectedLevel.equals("Tất cả")) count++;
        return count;
    }

    private void filterCourses() {
        filteredCourseList.clear();

        for (Course course : courseList) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                course.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                course.getDescription().toLowerCase().contains(searchQuery.toLowerCase());

            boolean matchesCategory = selectedCategory.equals("Tất cả") ||
                course.getCategory().equals(selectedCategory);

            boolean matchesLevel = selectedLevel.equals("Tất cả") ||
                course.getLevel().equals(selectedLevel);

            if (matchesSearch && matchesCategory && matchesLevel) {
                filteredCourseList.add(course);
            }
        }

        // Update adapter with filtered list
        courseAdapter.updateCourseList(filteredCourseList);
        updateUI();
    }

    private void updateUI() {
        if (filteredCourseList.isEmpty()) {
            // tvNoCourses.setVisibility(View.VISIBLE);
            rvCourses.setVisibility(View.GONE);
        } else {
            // tvNoCourses.setVisibility(View.GONE);
            rvCourses.setVisibility(View.VISIBLE);
        }

        // Update course count
        /*if (tvCourseCount != null) {
            String countText = filteredCourseList.size() + " khóa học";
            if (!searchQuery.isEmpty() || !selectedCategory.equals("Tất cả") || !selectedLevel.equals("Tất cả")) {
                countText += " (đã lọc)";
            }
            tvCourseCount.setText(countText);
        }*/
    }

    private void loadStatistics() {
        if (mAuth.getCurrentUser() == null) return;

        String currentUserId = mAuth.getCurrentUser().getUid();

        // Load total students count từ courseRequests với status "approved"
        loadTotalStudentsFromApprovedRequests(currentUserId);

        // Set average rating (placeholder)
        /*if (tvAvgRating != null) {
            tvAvgRating.setText("4.8");
        }*/
    }

    private void loadTotalStudentsFromApprovedRequests(String teacherId) {
        Log.d("CourseManagement", "Loading total students from approved requests for teacherId: " + teacherId);

        // Lấy tất cả approved requests từ courseRequests
        db.collection("courseRequests")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(approvedRequests -> {
                    Log.d("CourseManagement", "Found " + approvedRequests.size() + " approved requests total");

                    if (approvedRequests.isEmpty()) {
                        Log.d("CourseManagement", "No approved requests found, setting students count to 0");
                        if (tvTotalStudents != null) {
                            tvTotalStudents.setText("0");
                        }
                        return;
                    }

                    // Lấy danh sách courseIds của teacher này
                    db.collection("courses")
                            .whereEqualTo("teacherId", teacherId)
                            .get()
                            .addOnSuccessListener(teacherCourses -> {
                                Log.d("CourseManagement", "Teacher has " + teacherCourses.size() + " courses");

                                // Tạo Set chứa courseIds của teacher
                                java.util.Set<String> teacherCourseIds = new java.util.HashSet<>();
                                for (com.google.firebase.firestore.QueryDocumentSnapshot courseDoc : teacherCourses) {
                                    teacherCourseIds.add(courseDoc.getId());
                                    Log.d("CourseManagement", "Teacher course ID: " + courseDoc.getId());
                                }

                                // Tạo Set để lưu unique studentId của teacher này
                                java.util.Set<String> uniqueStudentIds = new java.util.HashSet<>();

                                // Duyệt qua tất cả approved requests
                                for (com.google.firebase.firestore.QueryDocumentSnapshot requestDoc : approvedRequests) {
                                    String studentName = requestDoc.getString("studentName");
                                    String studentId = requestDoc.getString("studentId");
                                    String courseId = requestDoc.getString("courseId");
                                    String courseName = requestDoc.getString("courseName");

                                    Log.d("CourseManagement", "Processing approved request - Student: " + studentName +
                                          ", StudentId: " + studentId + ", CourseId: " + courseId + ", Course: " + courseName);

                                    // Chỉ đếm nếu courseId thuộc về teacher này
                                    if (teacherCourseIds.contains(courseId)) {
                                        if (studentId != null && !studentId.isEmpty()) {
                                            uniqueStudentIds.add(studentId);
                                            Log.d("CourseManagement", "Added studentId: " + studentId +
                                                  " for teacher's course: " + courseName + ". Total unique students: " + uniqueStudentIds.size());
                                        }
                                    } else {
                                        Log.d("CourseManagement", "Skipping request for course not belonging to this teacher: " + courseName);
                                    }
                                }

                                // Cập nhật UI
                                int finalCount = uniqueStudentIds.size();
                                Log.d("CourseManagement", "Final unique students count for teacher: " + finalCount);

                                runOnUiThread(() -> {
                                    if (tvTotalStudents != null) {
                                        tvTotalStudents.setText(String.valueOf(finalCount));
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("CourseManagement", "Error loading teacher courses", e);
                                if (tvTotalStudents != null) {
                                    tvTotalStudents.setText("0");
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("CourseManagement", "Error loading approved requests", e);
                    if (tvTotalStudents != null) {
                        tvTotalStudents.setText("0");
                    }
                });
    }

    private void loadCourses() {
        // Check if user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("courses")
            .whereEqualTo("teacherId", currentUserId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    courseList.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Course course = document.toObject(Course.class);
                        course.setId(document.getId());
                        courseList.add(course);
                    }

                    // Update total courses count
                    if (tvTotalCourses != null) {
                        tvTotalCourses.setText(String.valueOf(courseList.size()));
                    }

                    // Apply current filters
                    filterCourses();

                } else {
                    android.util.Log.e("CourseManagement", "Error loading courses", task.getException());
                    String errorMessage = "Lỗi khi tải khóa học";
                    if (task.getException() != null && task.getException().getMessage() != null) {
                        errorMessage += ": " + task.getException().getMessage();
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setupClickListeners() {
        fabAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateCourseActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_management_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_refresh) {
            loadCourses();
            return true;
        } else if (itemId == R.id.action_search) {
            // Focus on search input
            if (etSearch != null) {
                etSearch.requestFocus();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses(); // Refresh data when returning to this activity
    }
}
