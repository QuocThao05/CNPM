package com.vantinh.tienganh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vantinh.tienganh.R;
import com.vantinh.tienganh.models.Course;
import java.util.List;

public class CourseSelectAdapter extends RecyclerView.Adapter<CourseSelectAdapter.ViewHolder> {

    private List<Course> courses;
    private OnCourseSelectListener listener;

    public interface OnCourseSelectListener {
        void onCourseSelected(Course course);
    }

    public CourseSelectAdapter(List<Course> courses, OnCourseSelectListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCourseName, tvCourseDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvCourseDescription = itemView.findViewById(R.id.tv_course_description);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseSelected(courses.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Course course) {
            tvCourseName.setText(course.getCourseName());
            tvCourseDescription.setText(course.getDescription());
        }
    }
}
