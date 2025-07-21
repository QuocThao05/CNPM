package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CourseManagementActivity extends AppCompatActivity {

    private RecyclerView rvCourses;
    private FloatingActionButton fabAddCourse;
    private LinearLayout tvNoCourses; // Changed from TextView to LinearLayout
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Course> courseList;
    private CourseAdapter courseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_management);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        courseList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadCourses();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvCourses = findViewById(R.id.rv_courses);
        fabAddCourse = findViewById(R.id.fab_add_course);
        tvNoCourses = findViewById(R.id.tv_no_courses);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý khóa học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        courseAdapter = new CourseAdapter(courseList, this::onCourseClick);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(courseAdapter);
    }

    private void setupClickListeners() {
        fabAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateCourseActivity.class);
            startActivity(intent);
        });
    }

    private void onCourseClick(Course course) {
        // Navigate to EditCourseActivity instead of CourseLessonsActivity
        Intent intent = new Intent(this, EditCourseActivity.class);
        intent.putExtra("courseId", course.getId());
        intent.putExtra("courseTitle", course.getTitle());
        startActivity(intent);
    }

    private void loadCourses() {
        // Check if user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        // Add debug logging
        android.util.Log.d("CourseManagement", "Loading courses for user: " + currentUserId);

        db.collection("courses")
            .whereEqualTo("teacherId", currentUserId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    courseList.clear();
                    android.util.Log.d("CourseManagement", "Found " + task.getResult().size() + " courses");

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Course course = document.toObject(Course.class);
                        course.setId(document.getId());
                        courseList.add(course);
                    }

                    if (courseList.isEmpty()) {
                        tvNoCourses.setVisibility(View.VISIBLE);
                        rvCourses.setVisibility(View.GONE);
                    } else {
                        tvNoCourses.setVisibility(View.GONE);
                        rvCourses.setVisibility(View.VISIBLE);
                    }

                    courseAdapter.notifyDataSetChanged();
                } else {
                    android.util.Log.e("CourseManagement", "Error loading courses", task.getException());
                    Toast.makeText(this, "Lỗi khi tải khóa học: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
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
            // Implement search functionality
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
