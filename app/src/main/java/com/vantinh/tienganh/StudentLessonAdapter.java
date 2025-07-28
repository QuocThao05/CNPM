package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentLessonAdapter extends RecyclerView.Adapter<StudentLessonAdapter.StudentLessonViewHolder> {

    private List<Lesson> lessonList;
    private OnLessonClickListener listener;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String courseId;
    private String courseTitle;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
        void onFavoriteChanged(Lesson lesson, boolean isFavorite);
        void onLessonCompleted(Lesson lesson); // Thêm callback cho việc hoàn thành bài học
    }

    public StudentLessonAdapter(List<Lesson> lessonList, OnLessonClickListener listener, String courseId, String courseTitle) {
        this.lessonList = lessonList;
        this.listener = listener;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public StudentLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_lesson, parent, false);
        return new StudentLessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentLessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);
        holder.bind(lesson);
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    public class StudentLessonViewHolder extends RecyclerView.ViewHolder {
        private CardView cardLesson;
        private TextView tvLessonOrder;
        private TextView tvLessonTitle;
        private TextView tvLessonType;
        private TextView tvEstimatedTime;
        private TextView tvGrammarPreview;
        private TextView tvCompletionStatus;
        private ImageView ivPlayIcon;
        private ImageView ivCompletionStatus;
        private ImageButton btnFavorite;
        private com.google.android.material.button.MaterialButton btnMarkComplete;

        public StudentLessonViewHolder(@NonNull View itemView) {
            super(itemView);
            cardLesson = itemView.findViewById(R.id.card_lesson);
            tvLessonOrder = itemView.findViewById(R.id.tv_lesson_order);
            tvLessonTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvLessonType = itemView.findViewById(R.id.tv_lesson_type);
            tvEstimatedTime = itemView.findViewById(R.id.tv_estimated_time);
            tvGrammarPreview = itemView.findViewById(R.id.tv_grammar_preview);
            tvCompletionStatus = itemView.findViewById(R.id.tv_completion_status);
            ivPlayIcon = itemView.findViewById(R.id.iv_play_icon);
            ivCompletionStatus = itemView.findViewById(R.id.iv_completion_status);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            btnMarkComplete = itemView.findViewById(R.id.btn_mark_complete);

            // Set up click listeners
            cardLesson.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLessonClick(lessonList.get(getAdapterPosition()));
                }
            });

            btnMarkComplete.setOnClickListener(v -> {
                Lesson lesson = lessonList.get(getAdapterPosition());
                markLessonAsCompleted(lesson);
            });

            btnFavorite.setOnClickListener(v -> {
                // Handle favorite functionality if needed
                Lesson lesson = lessonList.get(getAdapterPosition());
                if (listener != null) {
                    listener.onFavoriteChanged(lesson, true);
                }
            });
        }

        public void bind(Lesson lesson) {
            // Set basic lesson info
            tvLessonOrder.setText("Bài " + lesson.getOrder());
            tvLessonTitle.setText(lesson.getTitle());
            tvLessonType.setText(lesson.getTypeDisplayName());
            tvEstimatedTime.setText(lesson.getEstimatedTimeString());

            // Show grammar preview if lesson type is grammar
            if ("Grammar".equalsIgnoreCase(lesson.getCategory()) && lesson.getGrammarStructure() != null) {
                tvGrammarPreview.setText("📝 " + lesson.getGrammarStructure());
                tvGrammarPreview.setVisibility(View.VISIBLE);
            } else {
                tvGrammarPreview.setVisibility(View.GONE);
            }

            // Set completion status
            updateCompletionStatus(lesson);

            // Show play icon for video/audio lessons
            if ("video".equalsIgnoreCase(lesson.getType()) || "audio".equalsIgnoreCase(lesson.getType())) {
                ivPlayIcon.setVisibility(View.VISIBLE);
            } else {
                ivPlayIcon.setVisibility(View.GONE);
            }

            // Load completion status from Firebase
            loadLessonProgress(lesson);
        }

        private void updateCompletionStatus(Lesson lesson) {
            if (lesson.isCompleted()) {
                // Lesson is completed
                tvCompletionStatus.setText("✅ Đã hoàn thành");
                tvCompletionStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                ivCompletionStatus.setImageResource(android.R.drawable.ic_menu_my_calendar);
                btnMarkComplete.setText("Đã hoàn thành");
                btnMarkComplete.setEnabled(false);
                btnMarkComplete.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            } else {
                // Lesson is not completed
                tvCompletionStatus.setText("Chưa hoàn thành");
                tvCompletionStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                ivCompletionStatus.setImageResource(android.R.drawable.ic_menu_agenda);
                btnMarkComplete.setText("Hoàn thành");
                btnMarkComplete.setEnabled(true);
                btnMarkComplete.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_bright));
            }
        }

        private void loadLessonProgress(Lesson lesson) {
            if (auth.getCurrentUser() == null) return;

            String studentId = auth.getCurrentUser().getUid();

            db.collection("lesson_progress")
                    .whereEqualTo("studentId", studentId)
                    .whereEqualTo("courseId", courseId)
                    .whereEqualTo("lessonId", lesson.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Progress record exists, check if completed
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Boolean isCompleted = doc.getBoolean("isCompleted");
                                if (isCompleted != null && isCompleted) {
                                    lesson.setCompleted(true);
                                    updateCompletionStatus(lesson);
                                    break;
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("StudentLessonAdapter", "Error loading lesson progress", e);
                    });
        }

        private void markLessonAsCompleted(Lesson lesson) {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(itemView.getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lesson.isCompleted()) {
                Toast.makeText(itemView.getContext(), "Bài học đã được hoàn thành", Toast.LENGTH_SHORT).show();
                return;
            }

            String studentId = auth.getCurrentUser().getUid();

            // Disable button while processing
            btnMarkComplete.setEnabled(false);
            btnMarkComplete.setText("Đang lưu...");

            // Check if progress record already exists
            db.collection("lesson_progress")
                    .whereEqualTo("studentId", studentId)
                    .whereEqualTo("courseId", courseId)
                    .whereEqualTo("lessonId", lesson.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // Create new progress record
                            createLessonProgress(lesson, studentId);
                        } else {
                            // Update existing progress record
                            String progressId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            updateLessonProgress(lesson, progressId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("StudentLessonAdapter", "Error checking lesson progress", e);
                        btnMarkComplete.setEnabled(true);
                        btnMarkComplete.setText("Hoàn thành");
                        Toast.makeText(itemView.getContext(), "Lỗi kiểm tra tiến độ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        private void createLessonProgress(Lesson lesson, String studentId) {
            Map<String, Object> progressData = new HashMap<>();
            progressData.put("studentId", studentId);
            progressData.put("courseId", courseId);
            progressData.put("lessonId", lesson.getId());
            progressData.put("isCompleted", true);
            progressData.put("completedAt", new Date());
            progressData.put("createdAt", new Date());
            progressData.put("updatedAt", new Date());

            db.collection("lesson_progress")
                    .add(progressData)
                    .addOnSuccessListener(documentReference -> {
                        lesson.setCompleted(true);
                        updateCompletionStatus(lesson);

                        Toast.makeText(itemView.getContext(), "Đã đánh dấu hoàn thành bài học: " + lesson.getTitle(), Toast.LENGTH_SHORT).show();
                        android.util.Log.d("StudentLessonAdapter", "Lesson progress created successfully");

                        // Notify listener if available
                        if (listener != null) {
                            listener.onLessonCompleted(lesson);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("StudentLessonAdapter", "Error creating lesson progress", e);
                        btnMarkComplete.setEnabled(true);
                        btnMarkComplete.setText("Hoàn thành");
                        Toast.makeText(itemView.getContext(), "Lỗi lưu tiến độ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        private void updateLessonProgress(Lesson lesson, String progressId) {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("isCompleted", true);
            updateData.put("completedAt", new Date());
            updateData.put("updatedAt", new Date());

            db.collection("lesson_progress").document(progressId)
                    .update(updateData)
                    .addOnSuccessListener(aVoid -> {
                        lesson.setCompleted(true);
                        updateCompletionStatus(lesson);

                        Toast.makeText(itemView.getContext(), "Đã đánh dấu hoàn thành bài học: " + lesson.getTitle(), Toast.LENGTH_SHORT).show();
                        android.util.Log.d("StudentLessonAdapter", "Lesson progress updated successfully");

                        // Notify listener if available
                        if (listener != null) {
                            listener.onLessonCompleted(lesson);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("StudentLessonAdapter", "Error updating lesson progress", e);
                        btnMarkComplete.setEnabled(true);
                        btnMarkComplete.setText("Hoàn thành");
                        Toast.makeText(itemView.getContext(), "Lỗi cập nhật tiến độ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
