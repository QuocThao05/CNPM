package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CourseListActivity extends AppCompatActivity {

    private RecyclerView rvCourses;
    private TextView tvTitle, tvNoData;
    private FloatingActionButton fabFilter;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String category;
    private String pageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        // Get data from intent
        category = getIntent().getStringExtra("category");
        pageTitle = getIntent().getStringExtra("title");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
        loadCourses();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title);
        tvNoData = findViewById(R.id.tv_no_data);
        rvCourses = findViewById(R.id.rv_courses);
        fabFilter = findViewById(R.id.fab_filter);

        // Setup RecyclerView
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(pageTitle != null ? pageTitle : "Danh sách khóa học");
        }
    }

    private void setupClickListeners() {
        if (fabFilter != null) {
            fabFilter.setOnClickListener(v -> {
                // Show filter dialog or navigate to filter activity
                showFilterOptions();
            });
        }
    }

    private void showFilterOptions() {
        // Implementation for filter options
        Toast.makeText(this, "Chức năng lọc đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCourses() {
        // Implementation for loading courses from Firebase
        // For now, show a sample message
        if (tvTitle != null) {
            tvTitle.setText("Khóa học " + (pageTitle != null ? pageTitle : ""));
        }

        // Sample implementation - replace with actual Firebase query
        loadSampleCourses();
    }

    private void loadSampleCourses() {
        // This is sample data - replace with actual Firebase implementation
        Toast.makeText(this, "Đang tải khóa học cho: " + category, Toast.LENGTH_SHORT).show();
    }

    // Method to handle course item click
    private void onCourseClick(String courseId) {
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("courseId", courseId);
        intent.putExtra("category", category);
        startActivity(intent);
    }
}