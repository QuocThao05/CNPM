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
import java.util.List;

public class StudentEnrolledCourseAdapter extends RecyclerView.Adapter<StudentEnrolledCourseAdapter.EnrolledCourseViewHolder> {

    private List<EnrolledCourse> enrolledCourseList;
    private OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(EnrolledCourse enrolledCourse);
        void onContinueLearning(EnrolledCourse enrolledCourse);
        void onViewProgress(EnrolledCourse enrolledCourse);
    }

    public StudentEnrolledCourseAdapter(List<EnrolledCourse> enrolledCourseList, OnCourseClickListener listener) {
        this.enrolledCourseList = enrolledCourseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EnrolledCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_enrolled_course, parent, false);
        return new EnrolledCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnrolledCourseViewHolder holder, int position) {
        EnrolledCourse enrolledCourse = enrolledCourseList.get(position);
        holder.bind(enrolledCourse);
    }

    @Override
    public int getItemCount() {
        return enrolledCourseList.size();
    }

    public class EnrolledCourseViewHolder extends RecyclerView.ViewHolder {
        private CardView cardCourse;
        private TextView tvCourseTitle;
        private TextView tvCourseCategory;
        private TextView tvCourseLevel;
        private TextView tvProgressText;
        private TextView tvProgressPercentage;
        private TextView tvEnrollmentDate;
        private TextView tvStatus;
        private ProgressBar progressBarCompletion;
        private Button btnContinueLearning;
        private Button btnViewProgress;

        public EnrolledCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCourse = itemView.findViewById(R.id.card_course);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
            tvCourseCategory = itemView.findViewById(R.id.tv_course_category);
            tvCourseLevel = itemView.findViewById(R.id.tv_course_level);
            tvProgressText = itemView.findViewById(R.id.tv_progress_text);
            tvProgressPercentage = itemView.findViewById(R.id.tv_progress_percentage);
            tvEnrollmentDate = itemView.findViewById(R.id.tv_enrollment_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            progressBarCompletion = itemView.findViewById(R.id.progress_bar_completion);
            btnContinueLearning = itemView.findViewById(R.id.btn_continue_learning);
            btnViewProgress = itemView.findViewById(R.id.btn_view_progress);

            // Set up click listeners
            cardCourse.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(enrolledCourseList.get(getAdapterPosition()));
                }
            });

            btnContinueLearning.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContinueLearning(enrolledCourseList.get(getAdapterPosition()));
                }
            });

            btnViewProgress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProgress(enrolledCourseList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(EnrolledCourse enrolledCourse) {
            Course course = enrolledCourse.getCourse();

            // Set course basic info
            tvCourseTitle.setText(course.getTitle());
            tvCourseCategory.setText(course.getCategory());
            tvCourseLevel.setText(course.getLevel());

            // Set progress info
            tvProgressText.setText(enrolledCourse.getProgressText());
            tvProgressPercentage.setText(enrolledCourse.getProgressPercentageText());
            progressBarCompletion.setProgress(enrolledCourse.getProgress());

            // Set enrollment date
            if (enrolledCourse.getEnrollmentDate() != null && !enrolledCourse.getEnrollmentDate().isEmpty()) {
                tvEnrollmentDate.setText("Đăng ký: " + enrolledCourse.getEnrollmentDate().substring(0,
                    Math.min(10, enrolledCourse.getEnrollmentDate().length())));
            } else {
                tvEnrollmentDate.setText("Đăng ký: N/A");
            }

            // Set status
            String status = enrolledCourse.getStatus();
            if ("completed".equals(status)) {
                tvStatus.setText("Hoàn thành");
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                btnContinueLearning.setText("Ôn tập");
            } else if ("paused".equals(status)) {
                tvStatus.setText("Tạm dừng");
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                btnContinueLearning.setText("Tiếp tục học");
            } else {
                tvStatus.setText("Đang học");
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                btnContinueLearning.setText("Tiếp tục học");
            }

            // Enable/disable continue button based on status
            btnContinueLearning.setEnabled(enrolledCourse.canContinueLearning() || enrolledCourse.isCompleted());

            // Set category-specific styling
            setCategoryStyle(course.getCategory());
        }

        private void setCategoryStyle(String category) {
            int colorResId;
            switch (category.toLowerCase()) {
                case "grammar":
                    colorResId = android.R.color.holo_blue_light;
                    break;
                case "vocabulary":
                    colorResId = android.R.color.holo_green_light;
                    break;
                case "listening":
                    colorResId = android.R.color.holo_orange_light;
                    break;
                case "speaking":
                    colorResId = android.R.color.holo_red_light;
                    break;
                case "reading":
                    colorResId = android.R.color.holo_purple;
                    break;
                case "writing":
                    colorResId = android.R.color.darker_gray;
                    break;
                default:
                    colorResId = android.R.color.holo_blue_light;
                    break;
            }

            // Apply subtle background tint based on category
            cardCourse.setCardBackgroundColor(itemView.getContext().getColor(colorResId));
            cardCourse.getBackground().setAlpha(30); // Make it subtle
        }
    }
}
