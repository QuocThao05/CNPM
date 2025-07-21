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

public class StudentReportAdapter extends RecyclerView.Adapter<StudentReportAdapter.ViewHolder> {

    private List<StudentReport> studentReports;
    private OnReportClickListener listener;

    public interface OnReportClickListener {
        void onViewDetail(StudentReport report);
        void onViewProgress(StudentReport report);
    }

    public StudentReportAdapter(List<StudentReport> studentReports, OnReportClickListener listener) {
        this.studentReports = studentReports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentReport report = studentReports.get(position);
        holder.bind(report, listener);
    }

    @Override
    public int getItemCount() {
        return studentReports.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStudentName, tvCourseName, tvProgress, tvScore, tvStatus, tvQuizProgress;
        private ProgressBar progressBar;
        private Button btnViewDetail, btnViewProgress;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvQuizProgress = itemView.findViewById(R.id.tv_quiz_progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
            btnViewDetail = itemView.findViewById(R.id.btn_view_detail);
            btnViewProgress = itemView.findViewById(R.id.btn_view_progress);
        }

        public void bind(StudentReport report, OnReportClickListener listener) {
            tvStudentName.setText(report.getStudentName());
            tvCourseName.setText(report.getCourseName());
            tvProgress.setText(report.getProgressText());
            tvScore.setText(report.getScoreText());
            tvStatus.setText(report.getStatusText());
            tvQuizProgress.setText(report.getQuizProgressText());

            // Set progress bar
            progressBar.setProgress((int) report.getProgress());

            // Set status color
            int statusColor = getStatusColor(report.getStatus());
            tvStatus.setTextColor(statusColor);

            // Click listeners
            btnViewDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetail(report);
                }
            });

            btnViewProgress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProgress(report);
                }
            });

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetail(report);
                }
            });
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "COMPLETED":
                    return 0xFF4CAF50; // Green
                case "IN_PROGRESS":
                    return 0xFFFF9800; // Orange
                case "NOT_STARTED":
                    return 0xFFF44336; // Red
                default:
                    return 0xFF757575; // Gray
            }
        }
    }
}
