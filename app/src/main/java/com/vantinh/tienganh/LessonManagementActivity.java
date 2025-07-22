package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class LessonManagementActivity extends AppCompatActivity implements LessonAdapter.OnLessonClickListener {

    private RecyclerView rvLessons;
    private FloatingActionButton fabAddLesson;
    private LinearLayout layoutNoLessons;
    private TextView tvCourseTitle;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Lesson> lessonList;
    private LessonAdapter lessonAdapter;

    private String courseId;
    private String courseTitle;
    private String courseCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_management);

        // Get course info from intent
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");
        courseCategory = getIntent().getStringExtra("courseCategory");

        if (courseId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        lessonList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadLessons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvLessons = findViewById(R.id.rv_lessons);
        fabAddLesson = findViewById(R.id.fab_add_lesson);
        layoutNoLessons = findViewById(R.id.layout_no_lessons);
        tvCourseTitle = findViewById(R.id.tv_course_title);

        tvCourseTitle.setText(courseTitle);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý bài học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        lessonAdapter = new LessonAdapter(lessonList, this);
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);
    }

    private void setupClickListeners() {
        fabAddLesson.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateLessonActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", courseTitle);
            intent.putExtra("courseCategory", courseCategory);
            startActivity(intent);
        });
    }

    private void loadLessons() {
        layoutNoLessons.setVisibility(View.VISIBLE);
        rvLessons.setVisibility(View.GONE);

        // Debug logging
        android.util.Log.d("LessonManagement", "Loading lessons for courseId: " + courseId);

        // Simplified query without orderBy to avoid index issues
        db.collection("lessons")
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                lessonList.clear();

                android.util.Log.d("LessonManagement", "Firebase query successful. Found " +
                    queryDocumentSnapshots.size() + " documents");

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Lesson lesson = document.toObject(Lesson.class);
                    lesson.setId(document.getId());
                    lessonList.add(lesson);

                    android.util.Log.d("LessonManagement", "Loaded lesson: " + lesson.getTitle() +
                        " (Order: " + lesson.getOrder() + ")");
                }

                // Sort in memory instead of using Firestore orderBy
                lessonList.sort((l1, l2) -> Integer.compare(l1.getOrder(), l2.getOrder()));

                if (lessonList.isEmpty()) {
                    layoutNoLessons.setVisibility(View.VISIBLE);
                    rvLessons.setVisibility(View.GONE);
                    android.util.Log.d("LessonManagement", "No lessons found, showing empty state");
                } else {
                    layoutNoLessons.setVisibility(View.GONE);
                    rvLessons.setVisibility(View.VISIBLE);
                    android.util.Log.d("LessonManagement", "Showing " + lessonList.size() + " lessons");
                }

                lessonAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Đã tải " + lessonList.size() + " bài học", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("LessonManagement", "Error loading lessons", e);
                Toast.makeText(this, "Lỗi tải bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                layoutNoLessons.setVisibility(View.VISIBLE);
                rvLessons.setVisibility(View.GONE);
            });
    }

    @Override
    public void onLessonClick(Lesson lesson) {
        // Navigate to lesson detail/edit
        Intent intent = new Intent(this, EditLessonActivity.class);
        intent.putExtra("lessonId", lesson.getId());
        intent.putExtra("courseId", courseId);
        intent.putExtra("courseCategory", courseCategory);
        startActivity(intent);
    }

    @Override
    public void onLessonEdit(Lesson lesson) {
        Intent intent = new Intent(this, EditLessonActivity.class);
        intent.putExtra("lessonId", lesson.getId());
        intent.putExtra("courseId", courseId);
        intent.putExtra("courseCategory", courseCategory);
        startActivity(intent);
    }

    @Override
    public void onLessonDelete(Lesson lesson) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xóa bài học")
            .setMessage("Bạn có chắc chắn muốn xóa bài học \"" + lesson.getTitle() + "\"?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                deleteLesson(lesson);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteLesson(Lesson lesson) {
        db.collection("lessons").document(lesson.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã xóa bài học", Toast.LENGTH_SHORT).show();
                loadLessons(); // Reload the list
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("LessonManagement", "Error deleting lesson", e);
                Toast.makeText(this, "Lỗi xóa bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLessons(); // Reload when returning from other activities
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lesson_management_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_refresh) {
            loadLessons();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
