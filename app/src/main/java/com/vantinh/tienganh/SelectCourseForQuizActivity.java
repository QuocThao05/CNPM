package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vantinh.tienganh.adapters.CourseSelectAdapter;
import com.vantinh.tienganh.models.Course;
import java.util.ArrayList;
import java.util.List;

public class SelectCourseForQuizActivity extends AppCompatActivity implements CourseSelectAdapter.OnCourseSelectListener {

    private RecyclerView rvCourses;
    private CourseSelectAdapter courseAdapter;
    private List<Course> courseList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_course_for_quiz);

        initViews();
        setupToolbar();
        initFirebase();
        setupRecyclerView();
        loadCourses();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvCourses = findViewById(R.id.rv_courses);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chọn khóa học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        courseAdapter = new CourseSelectAdapter(courseList, this);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(courseAdapter);
    }

    private void loadCourses() {
        String teacherId = mAuth.getCurrentUser().getUid();

        db.collection("courses")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    courseList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Course course = document.toObject(Course.class);
                        course.setId(document.getId());
                        courseList.add(course);
                    }
                    courseAdapter.notifyDataSetChanged();

                    if (courseList.isEmpty()) {
                        Toast.makeText(this, "Bạn chưa có khóa học nào. Vui lòng tạo khóa học trước.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải danh sách khóa học", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onCourseSelected(Course course) {
        Intent intent = new Intent(this, CreateQuizActivity.class);
        intent.putExtra("courseId", course.getId());
        intent.putExtra("courseName", course.getCourseName());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
