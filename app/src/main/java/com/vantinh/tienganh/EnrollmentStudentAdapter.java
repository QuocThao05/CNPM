package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EnrollmentStudentAdapter extends RecyclerView.Adapter<EnrollmentStudentAdapter.ViewHolder> {

    private List<EnrollmentStudent> enrollmentStudents;

    public EnrollmentStudentAdapter(List<EnrollmentStudent> enrollmentStudents) {
        this.enrollmentStudents = enrollmentStudents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_enrollment_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnrollmentStudent student = enrollmentStudents.get(position);

        holder.tvStudentName.setText(student.getStudentName());
        holder.tvStudentId.setText("ID: " + student.getStudentId());
        holder.tvCourseName.setText(student.getCourseName());
        holder.tvCourseId.setText("Course ID: " + student.getCourseId());
    }

    @Override
    public int getItemCount() {
        return enrollmentStudents.size();
    }

    public void updateData(List<EnrollmentStudent> newStudents) {
        this.enrollmentStudents = newStudents;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentId, tvCourseName, tvCourseId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvStudentId = itemView.findViewById(R.id.tv_student_id);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvCourseId = itemView.findViewById(R.id.tv_course_id);
        }
    }
}
