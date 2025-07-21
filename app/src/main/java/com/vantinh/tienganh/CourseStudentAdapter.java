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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CourseStudentAdapter extends RecyclerView.Adapter<CourseStudentAdapter.ViewHolder> {

    private List<CourseStudent> students;
    private OnStudentActionListener listener;

    public interface OnStudentActionListener {
        void onViewProgress(CourseStudent student);
        void onRemoveStudent(CourseStudent student);
        void onSendMessage(CourseStudent student);
    }

    public CourseStudentAdapter(List<CourseStudent> students, OnStudentActionListener listener) {
        this.students = students;
        this.listener = listener;
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
        holder.bind(student, listener);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStudentName, tvStudentEmail, tvEnrollmentDate, tvProgress, tvScore, tvStatus, tvQuizProgress;
        private ProgressBar progressBar;
        private Button btnViewProgress, btnRemove, btnMessage;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvStudentEmail = itemView.findViewById(R.id.tv_student_email);
            tvEnrollmentDate = itemView.findViewById(R.id.tv_enrollment_date);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvQuizProgress = itemView.findViewById(R.id.tv_quiz_progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
            btnViewProgress = itemView.findViewById(R.id.btn_view_progress);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            btnMessage = itemView.findViewById(R.id.btn_message);
        }

        public void bind(CourseStudent student, OnStudentActionListener listener) {
            tvStudentName.setText(student.getStudentName());
            tvStudentEmail.setText(student.getStudentEmail());
            tvProgress.setText(student.getProgressText());
            tvScore.setText(student.getScoreText());
            tvStatus.setText(student.getProgressStatus());
            tvQuizProgress.setText(student.getQuizProgressText());

            // Format enrollment date
            if (student.getEnrollmentDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvEnrollmentDate.setText("Đăng ký: " + dateFormat.format(student.getEnrollmentDate()));
            }

            // Set progress bar
            progressBar.setProgress((int) student.getProgress());

            // Set status color
            int statusColor = getStatusColor(student.getProgress());
            tvStatus.setTextColor(statusColor);

            // Click listeners
            btnViewProgress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProgress(student);
                }
            });

            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveStudent(student);
                }
            });

            btnMessage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSendMessage(student);
                }
            });

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProgress(student);
                }
            });
        }

        private int getStatusColor(double progress) {
            if (progress >= 100) {
                return 0xFF4CAF50; // Green
            } else if (progress >= 50) {
                return 0xFF2196F3; // Blue
            } else if (progress > 0) {
                return 0xFFFF9800; // Orange
            } else {
                return 0xFFF44336; // Red
            }
        }
    }
}
