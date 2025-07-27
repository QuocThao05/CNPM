package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private OnCourseClickListener listener;
    private FirebaseFirestore db;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
        void onManageLessons(Course course);
        void onEditCourse(Course course);
    }

    public CourseAdapter(List<Course> courseList, OnCourseClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    // Method to update course list for filtering
    public void updateCourseList(List<Course> newCourseList) {
        this.courseList = newCourseList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
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
        private TextView tvCourseLevel;
        private TextView tvCourseCategory;
        private TextView tvCourseDuration;
        private TextView tvEnrolledStudents;
        private TextView tvCourseRating;
        private TextView tvCreatedDate;
        private Button btnManageLessons;
        private Button btnEditCourse;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCourse = itemView.findViewById(R.id.card_course);
            ivCourseImage = itemView.findViewById(R.id.iv_course_image);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
            tvCourseDescription = itemView.findViewById(R.id.tv_course_description);
            tvCourseLevel = itemView.findViewById(R.id.tv_course_level);
            tvCourseCategory = itemView.findViewById(R.id.tv_course_category);
            tvCourseDuration = itemView.findViewById(R.id.tv_course_duration);
            tvEnrolledStudents = itemView.findViewById(R.id.tv_enrolled_students);
            tvCourseRating = itemView.findViewById(R.id.tv_course_rating);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            btnManageLessons = itemView.findViewById(R.id.btn_manage_lessons);
            btnEditCourse = itemView.findViewById(R.id.btn_edit_course);
        }

        public void bind(Course course) {
            tvCourseTitle.setText(course.getTitle());
            tvCourseDescription.setText(course.getDescription());
            tvCourseLevel.setText(course.getLevel());
            tvCourseCategory.setText(course.getCategory());
            tvCourseDuration.setText(course.getDuration() + " giờ");

            // Load số học viên thực tế từ courseRequests với status "approved"
            loadEnrolledStudentsCount(course.getId(), tvEnrolledStudents);

            tvCourseRating.setText(String.format(Locale.getDefault(), "%.1f★", course.getRating()));

            if (course.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvCreatedDate.setText("📅 " + sdf.format(course.getCreatedAt()));
            }

            // Set course image (placeholder for now)
            ivCourseImage.setImageResource(R.drawable.ic_course_default);

            // Set up click listeners
            cardCourse.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });

            btnManageLessons.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onManageLessons(course);
                }
            });

            btnEditCourse.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditCourse(course);
                }
            });

            // Add animation
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

        private void loadEnrolledStudentsCount(String courseId, TextView tvEnrolledStudents) {
            // Hiển thị loading state
            tvEnrolledStudents.setText("👥 ⏳...");

            // Query courseRequests với status "approved" cho courseId này
            db.collection("courseRequests")
                    .whereEqualTo("courseId", courseId)
                    .whereEqualTo("status", "approved")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // Đếm unique studentId để tránh trùng lặp
                        java.util.Set<String> uniqueStudentIds = new java.util.HashSet<>();

                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String studentId = doc.getString("studentId");
                            String studentName = doc.getString("studentName");
                            String courseName = doc.getString("courseName");

                            android.util.Log.d("CourseAdapter", "Found approved request - Student: " + studentName +
                                  ", StudentId: " + studentId + ", Course: " + courseName + ", CourseId: " + courseId);

                            if (studentId != null && !studentId.isEmpty()) {
                                uniqueStudentIds.add(studentId);
                            }
                        }

                        int enrolledCount = uniqueStudentIds.size();
                        android.util.Log.d("CourseAdapter", "Course " + courseId + " has " + enrolledCount + " unique enrolled students");

                        // Cập nhật UI với số học viên thực tế
                        tvEnrolledStudents.setText("👥 " + enrolledCount + " học viên");
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("CourseAdapter", "Error loading enrolled students for course: " + courseId, e);
                        // Hiển thị fallback nếu có lỗi
                        tvEnrolledStudents.setText("👥 0 học viên");
                    });
        }
    }
}
