package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class StudentCourseAdapter extends RecyclerView.Adapter<StudentCourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public StudentCourseAdapter(List<Course> courseList, OnCourseClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }

    public void updateCourses(List<Course> newCourses) {
        this.courseList = newCourses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        private CardView cardCourse;
        private ImageView ivCourseImage;
        private TextView tvCourseTitle;
        private TextView tvCourseDescription;
        private TextView tvTeacherName;
        private TextView tvCourseLevel;
        private TextView tvCourseCategory;
        private TextView tvCourseDuration;
        private TextView tvEnrolledStudents;
        private TextView tvCourseRating;
        private TextView tvCreatedDate;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCourse = itemView.findViewById(R.id.card_course);
            ivCourseImage = itemView.findViewById(R.id.iv_course_image);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
            tvCourseDescription = itemView.findViewById(R.id.tv_course_description);
            tvTeacherName = itemView.findViewById(R.id.tv_teacher_name);
            tvCourseLevel = itemView.findViewById(R.id.tv_course_level);
            tvCourseCategory = itemView.findViewById(R.id.tv_course_category);
            tvCourseDuration = itemView.findViewById(R.id.tv_course_duration);
            tvEnrolledStudents = itemView.findViewById(R.id.tv_enrolled_students);
            tvCourseRating = itemView.findViewById(R.id.tv_course_rating);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
        }

        public void bind(Course course) {
            tvCourseTitle.setText(course.getTitle());
            tvCourseDescription.setText(course.getDescription());
            tvTeacherName.setText("📚 " + course.getCategory()); // Thay thế teacherName bằng category
            tvCourseLevel.setText(course.getLevel());
            tvCourseCategory.setText(course.getCategory());
            tvCourseDuration.setText(course.getDuration() + " giờ");
            tvEnrolledStudents.setText(course.getEnrolledStudents() + " học viên");
            tvCourseRating.setText(String.format(Locale.getDefault(), "%.1f★", course.getRating()));

            if (course.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvCreatedDate.setText("📅 " + sdf.format(course.getCreatedAt()));
            }

            // Set course image placeholder
            ivCourseImage.setImageResource(R.drawable.ic_course_default);

            // Set level badge color
            setLevelBadgeColor(course.getLevel());

            cardCourse.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });

            // Add ripple animation
            cardCourse.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        break;
                }
                return false;
            });
        }

        private void setLevelBadgeColor(String level) {
            int colorRes;
            switch (level.toLowerCase()) {
                case "beginner":
                    colorRes = R.color.status_active; // Green
                    break;
                case "intermediate":
                    colorRes = R.color.status_pending; // Orange
                    break;
                case "advanced":
                    colorRes = R.color.status_inactive; // Red
                    break;
                default:
                    colorRes = R.color.text_secondary;
                    break;
            }
            tvCourseLevel.setBackgroundColor(itemView.getContext().getColor(colorRes));
        }
    }
}
