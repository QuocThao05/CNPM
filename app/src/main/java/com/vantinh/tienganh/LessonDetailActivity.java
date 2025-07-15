package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LessonDetailActivity extends AppCompatActivity {

    private TextView tvLessonTitle, tvLessonContent, tvLessonType, tvEstimatedTime, tvCreatedDate;
    private Toolbar toolbar;

    private FirebaseFirestore db;
    private String lessonId, lessonTitle, courseId;
    private Lesson currentLesson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);

        db = FirebaseFirestore.getInstance();

        // Get lesson info from intent
        lessonId = getIntent().getStringExtra("lessonId");
        lessonTitle = getIntent().getStringExtra("lessonTitle");
        courseId = getIntent().getStringExtra("courseId");

        initViews();
        setupToolbar();
        loadLessonDetail();
        addAnimations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvLessonTitle = findViewById(R.id.tv_lesson_title);
        tvLessonContent = findViewById(R.id.tv_lesson_content);
        tvLessonType = findViewById(R.id.tv_lesson_type);
        tvEstimatedTime = findViewById(R.id.tv_estimated_time);
        tvCreatedDate = findViewById(R.id.tv_created_date);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(lessonTitle != null ? lessonTitle : "Chi tiết bài học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addAnimations() {
        findViewById(R.id.lesson_detail_container).setAlpha(0f);
        findViewById(R.id.lesson_detail_container).animate()
            .alpha(1f)
            .setDuration(500)
            .start();
    }

    private void loadLessonDetail() {
        if (lessonId == null) return;

        db.collection("lessons").document(lessonId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentLesson = documentSnapshot.toObject(Lesson.class);
                    if (currentLesson != null) {
                        currentLesson.setId(documentSnapshot.getId());
                        displayLessonInfo();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tải bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void displayLessonInfo() {
        if (currentLesson == null) return;

        tvLessonTitle.setText(currentLesson.getTitle());
        tvLessonContent.setText(currentLesson.getContent());
        tvLessonType.setText(getTypeDisplayName(currentLesson.getType()));
        tvEstimatedTime.setText("⏱ " + currentLesson.getEstimatedTime() + " phút");

        if (currentLesson.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvCreatedDate.setText("📅 Tạo lúc: " + sdf.format(currentLesson.getCreatedAt()));
        }
    }

    private String getTypeDisplayName(String type) {
        switch (type.toLowerCase()) {
            case "text": return "📝 Văn bản";
            case "video": return "🎥 Video";
            case "audio": return "🎧 Âm thanh";
            case "quiz": return "❓ Quiz";
            default: return "📄 Khác";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lesson_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_edit) {
            editLesson();
            return true;
        } else if (itemId == R.id.action_delete) {
            deleteLesson();
            return true;
        } else if (itemId == R.id.action_publish) {
            togglePublishStatus();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void editLesson() {
        Intent intent = new Intent(this, EditLessonActivity.class);
        intent.putExtra("lessonId", lessonId);
        intent.putExtra("courseId", courseId);
        startActivity(intent);
    }

    private void deleteLesson() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa bài học này không?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                db.collection("lessons").document(lessonId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã xóa bài học", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi xóa bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void togglePublishStatus() {
        if (currentLesson == null) return;

        boolean newStatus = !currentLesson.isPublished();
        db.collection("lessons").document(lessonId)
            .update("isPublished", newStatus)
            .addOnSuccessListener(aVoid -> {
                currentLesson.setPublished(newStatus);
                String message = newStatus ? "Đã xuất bản bài học" : "Đã hủy xuất bản bài học";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi cập nhật trạng thái: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lessonId != null) {
            loadLessonDetail(); // Refresh data when returning
        }
    }
}
