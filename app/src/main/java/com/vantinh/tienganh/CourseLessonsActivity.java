package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CourseLessonsActivity extends AppCompatActivity {

    private RecyclerView rvLessons;
    private FloatingActionButton fabAddLesson;
    private TextView tvNoLessons, tvCourseTitle;
    private Toolbar toolbar;

    private FirebaseFirestore db;
    private String courseId, courseTitle;
    private List<Lesson> lessonList;
    private LessonAdapter lessonAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_lessons);

        db = FirebaseFirestore.getInstance();
        lessonList = new ArrayList<>();

        // Get course info from intent
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadLessons();
        addAnimations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvLessons = findViewById(R.id.rv_lessons);
        fabAddLesson = findViewById(R.id.fab_add_lesson);
        tvNoLessons = findViewById(R.id.tv_no_lessons);
        tvCourseTitle = findViewById(R.id.tv_course_title);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bài học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (tvCourseTitle != null) {
            tvCourseTitle.setText(courseTitle);
        }
    }

    private void setupRecyclerView() {
        lessonAdapter = new LessonAdapter(lessonList, this::onLessonClick);
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);
    }

    private void setupClickListeners() {
        fabAddLesson.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateLessonActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", courseTitle);
            startActivity(intent);
        });
    }

    private void addAnimations() {
        // Add slide-in animation
        rvLessons.setTranslationY(100f);
        rvLessons.setAlpha(0f);
        rvLessons.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .start();
    }

    private void onLessonClick(Lesson lesson) {
        Intent intent = new Intent(this, LessonDetailActivity.class);
        intent.putExtra("lessonId", lesson.getId());
        intent.putExtra("lessonTitle", lesson.getTitle());
        intent.putExtra("courseId", courseId);
        startActivity(intent);
    }

    private void loadLessons() {
        if (courseId == null) return;

        db.collection("lessons")
            .whereEqualTo("courseId", courseId)
            .orderBy("order")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    lessonList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Lesson lesson = document.toObject(Lesson.class);
                        lesson.setId(document.getId());
                        lessonList.add(lesson);
                    }

                    if (lessonList.isEmpty()) {
                        tvNoLessons.setVisibility(View.VISIBLE);
                        rvLessons.setVisibility(View.GONE);
                    } else {
                        tvNoLessons.setVisibility(View.GONE);
                        rvLessons.setVisibility(View.VISIBLE);
                    }

                    lessonAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Lỗi khi tải bài học", Toast.LENGTH_SHORT).show();
                }
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
        loadLessons();
    }
}
