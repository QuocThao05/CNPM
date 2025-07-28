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

    public void updateList(List<EnrolledCourse> newList) {
        this.enrolledCourseList.clear();
        this.enrolledCourseList.addAll(newList);
        notifyDataSetChanged();
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
        }

        public void bind(EnrolledCourse enrolledCourse) {
            Course course = enrolledCourse.getCourse();

            // Set course basic info
            tvCourseTitle.setText(course.getTitle());
            tvCourseCategory.setText(course.getCategory());
            tvCourseLevel.setText(course.getLevel());

            // Set progress info - hiển thị phần trăm bên cạnh tiêu đề
            int progress = enrolledCourse.getProgress();
            int completedLessons = enrolledCourse.getCompletedLessons();
            int totalLessons = enrolledCourse.getTotalLessons();

            // Hiển thị phần trăm ở bên ngoài
            tvProgressPercentage.setText(progress + "%");

            // Set màu sắc theo tiến độ
            if (progress == 100) {
                tvProgressPercentage.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                tvProgressPercentage.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
            } else if (progress >= 50) {
                tvProgressPercentage.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_light));
                tvProgressPercentage.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
            } else {
                tvProgressPercentage.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
                tvProgressPercentage.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
            }

            // Set detailed progress text
            String progressText = completedLessons + "/" + totalLessons + " bài học";
            tvProgressText.setText(progressText);

            // Set progress bar
            progressBarCompletion.setProgress(progress);

            // Set enrollment date
            tvEnrollmentDate.setText("Ngày đăng ký: " + enrolledCourse.getEnrollmentDate());

            // Set status
            tvStatus.setText("Trạng thái: " + enrolledCourse.getStatus());

            // Debug log
            android.util.Log.d("EnrolledCourseAdapter", "Course: " + course.getTitle() +
                " - Progress: " + progress + "%" +
                " - Completed: " + completedLessons + "/" + totalLessons);
        }
    }
}
