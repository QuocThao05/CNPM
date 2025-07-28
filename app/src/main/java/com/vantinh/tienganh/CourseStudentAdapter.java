package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CourseStudentAdapter extends RecyclerView.Adapter<CourseStudentAdapter.ViewHolder> {

    private List<CourseStudent> students;
    private OnStudentActionListener listener;
    private FirebaseFirestore db;
    private String courseId;

    public interface OnStudentActionListener {
        void onViewProgress(CourseStudent student);
        void onRemoveStudent(CourseStudent student);
        void onSendMessage(CourseStudent student);
    }

    public CourseStudentAdapter(List<CourseStudent> students, OnStudentActionListener listener, String courseId) {
        this.students = students;
        this.listener = listener;
        this.courseId = courseId;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseStudent student = students.get(position);
        holder.bind(student, listener, db, courseId);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStudentName, tvStudentEmail, tvEnrollmentDate;
        private TextView tvStatusBadge, tvLessonProgress, tvTestScore;
        private ProgressBar progressBarLessons;
        private Button btnViewDetails, btnSendMessage, btnRemoveStudent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvStudentEmail = itemView.findViewById(R.id.tv_student_email);
            tvEnrollmentDate = itemView.findViewById(R.id.tv_enrollment_date);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvLessonProgress = itemView.findViewById(R.id.tv_lesson_progress);
            tvTestScore = itemView.findViewById(R.id.tv_test_score);
            progressBarLessons = itemView.findViewById(R.id.progress_bar_lessons);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnSendMessage = itemView.findViewById(R.id.btn_send_message);
            btnRemoveStudent = itemView.findViewById(R.id.btn_remove_student);
        }

        public void bind(CourseStudent student, OnStudentActionListener listener, FirebaseFirestore db, String courseId) {
            // Set basic student info
            tvStudentName.setText(student.getStudentName());
            tvStudentEmail.setText(student.getStudentEmail());

            // Format enrollment date
            if (student.getEnrollmentDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvEnrollmentDate.setText("Ngày đăng ký: " + sdf.format(student.getEnrollmentDate()));
            } else {
                tvEnrollmentDate.setText("Ngày đăng ký: --");
            }

            // Set status badge
            String status = student.getStatus();
            if ("approved".equals(status)) {
                tvStatusBadge.setText("Đang học");
                tvStatusBadge.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else if ("pending".equals(status)) {
                tvStatusBadge.setText("Chờ duyệt");
                tvStatusBadge.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            } else {
                tvStatusBadge.setText("Đã từ chối");
                tvStatusBadge.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
            }

            // Set default values
            tvLessonProgress.setText("0/0 (0%)");
            progressBarLessons.setProgress(0);
            tvTestScore.setText("🎯 Chưa làm bài kiểm tra");

            // Load progress data from Firebase
            loadStudentProgress(student.getStudentId(), courseId, db);
            loadTestResults(student.getStudentId(), courseId, db);

            // Set click listeners
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProgress(student);
                }
            });

            btnSendMessage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSendMessage(student);
                }
            });

            btnRemoveStudent.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveStudent(student);
                }
            });
        }

        private void loadStudentProgress(String studentId, String courseId, FirebaseFirestore db) {
            android.util.Log.d("CourseStudentAdapter", "Loading progress for student: " + studentId + ", course: " + courseId);

            // Load total lessons first
            db.collection("lessons")
                    .whereEqualTo("courseId", courseId)
                    .get()
                    .addOnSuccessListener(lessonsSnapshot -> {
                        int totalLessons = lessonsSnapshot.size();
                        android.util.Log.d("CourseStudentAdapter", "Total lessons: " + totalLessons);

                        if (totalLessons == 0) {
                            updateProgressUI(0, 0);
                            return;
                        }

                        // Load completed lessons
                        db.collection("lesson_progress")
                                .whereEqualTo("studentId", studentId)
                                .whereEqualTo("courseId", courseId)
                                .whereEqualTo("isCompleted", true)
                                .get()
                                .addOnSuccessListener(progressSnapshot -> {
                                    int completedLessons = progressSnapshot.size();
                                    android.util.Log.d("CourseStudentAdapter", "Completed lessons: " + completedLessons);

                                    updateProgressUI(completedLessons, totalLessons);
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("CourseStudentAdapter", "Error loading lesson progress", e);
                                    updateProgressUI(0, totalLessons);
                                });
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("CourseStudentAdapter", "Error loading lessons", e);
                        updateProgressUI(0, 0);
                    });
        }

        private void updateProgressUI(int completedLessons, int totalLessons) {
            int progressPercentage = totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0;

            tvLessonProgress.setText(completedLessons + "/" + totalLessons + " (" + progressPercentage + "%)");
            progressBarLessons.setMax(100);
            progressBarLessons.setProgress(progressPercentage);

            // Set progress bar color based on progress
            if (progressPercentage == 100) {
                progressBarLessons.getProgressDrawable().setColorFilter(
                    itemView.getContext().getColor(android.R.color.holo_green_dark),
                    android.graphics.PorterDuff.Mode.SRC_IN);
            } else if (progressPercentage >= 50) {
                progressBarLessons.getProgressDrawable().setColorFilter(
                    itemView.getContext().getColor(android.R.color.holo_orange_light),
                    android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                progressBarLessons.getProgressDrawable().setColorFilter(
                    itemView.getContext().getColor(android.R.color.holo_red_light),
                    android.graphics.PorterDuff.Mode.SRC_IN);
            }

            android.util.Log.d("CourseStudentAdapter", "Progress updated: " + progressPercentage + "%");
        }

        private void loadTestResults(String studentId, String courseId, FirebaseFirestore db) {
            android.util.Log.d("CourseStudentAdapter", "Loading test results for student: " + studentId);

            db.collection("testResults")
                    .whereEqualTo("studentId", studentId)
                    .whereEqualTo("courseId", courseId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            tvTestScore.setText("🎯 Chưa làm bài kiểm tra");
                            tvTestScore.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
                            return;
                        }

                        // Find highest score
                        double highestScore = -1;
                        int totalAttempts = queryDocumentSnapshots.size();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Object scoreObj = doc.get("score");
                            if (scoreObj instanceof Number) {
                                double score = ((Number) scoreObj).doubleValue();
                                if (score > highestScore) {
                                    highestScore = score;
                                }
                            }
                        }

                        // Update UI with test results
                        if (highestScore >= 0) {
                            String scoreText = String.format("🎯 Điểm cao nhất: %.1f/100", highestScore);
                            if (totalAttempts > 1) {
                                scoreText += " (" + totalAttempts + " lần làm)";
                            }
                            tvTestScore.setText(scoreText);

                            // Set color based on score
                            if (highestScore >= 80) {
                                tvTestScore.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                            } else if (highestScore >= 60) {
                                tvTestScore.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                            } else {
                                tvTestScore.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                            }

                            android.util.Log.d("CourseStudentAdapter", "Test score updated: " + highestScore);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("CourseStudentAdapter", "Error loading test results", e);
                        tvTestScore.setText("🎯 Lỗi tải điểm số");
                        tvTestScore.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    });
        }
    }
}
