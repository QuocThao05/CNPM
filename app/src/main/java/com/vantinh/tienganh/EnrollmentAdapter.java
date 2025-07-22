package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EnrollmentAdapter extends RecyclerView.Adapter<EnrollmentAdapter.EnrollmentViewHolder> {

    private List<Enrollment> enrollmentList;
    private OnEnrollmentActionListener listener;

    public interface OnEnrollmentActionListener {
        void onApprove(Enrollment enrollment);
        void onReject(Enrollment enrollment);
        void onViewDetails(Enrollment enrollment);
    }

    public EnrollmentAdapter(List<Enrollment> enrollmentList, OnEnrollmentActionListener listener) {
        this.enrollmentList = enrollmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EnrollmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_enrollment, parent, false);
        return new EnrollmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnrollmentViewHolder holder, int position) {
        Enrollment enrollment = enrollmentList.get(position);
        holder.bind(enrollment);
    }

    @Override
    public int getItemCount() {
        return enrollmentList.size();
    }

    class EnrollmentViewHolder extends RecyclerView.ViewHolder {
        private CardView cardEnrollment;
        private TextView tvStudentName, tvStudentEmail, tvCourseName, tvEnrollmentDate, tvStatus, tvMessage;
        private MaterialButton btnApprove, btnReject, btnViewDetails;

        public EnrollmentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardEnrollment = itemView.findViewById(R.id.card_enrollment);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvStudentEmail = itemView.findViewById(R.id.tv_student_email);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvEnrollmentDate = itemView.findViewById(R.id.tv_enrollment_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvMessage = itemView.findViewById(R.id.tv_message);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }

        public void bind(Enrollment enrollment) {
            // Hiển thị tên học viên - sử dụng fullName thay vì getStudentName()
            String studentName = enrollment.getFullName();
            if (studentName == null || studentName.trim().isEmpty()) {
                studentName = "Đang tải...";
            }
            tvStudentName.setText("👤 " + studentName);

            // Hiển thị email với fallback
            String studentEmail = enrollment.getStudentEmail();
            if (studentEmail == null || studentEmail.trim().isEmpty()) {
                studentEmail = "Không có email";
            }
            tvStudentEmail.setText("📧 " + studentEmail);

            // Hiển thị tên khóa học
            String courseName = enrollment.getCourseName();
            if (courseName == null || courseName.trim().isEmpty()) {
                courseName = "Không có tên khóa học";
            }
            tvCourseName.setText("📚 " + courseName);

            // Hiển thị status đơn giản vì class Enrollment mới không có getStatusDisplayName()
            tvStatus.setText("Đã đăng ký");

            if (enrollment.getEnrollmentDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvEnrollmentDate.setText("📅 " + sdf.format(enrollment.getEnrollmentDate()));
            } else {
                tvEnrollmentDate.setText("📅 Không có ngày");
            }

            // Ẩn message vì class Enrollment mới không có getMessage()
            tvMessage.setVisibility(View.GONE);

            // Set status UI - đơn giản hóa vì không có status field
            setupStatusUI(enrollment);

            // Set click listeners
            btnApprove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApprove(enrollment);
                }
            });

            btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReject(enrollment);
                }
            });

            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(enrollment);
                }
            });

            // Add animation
            cardEnrollment.setOnTouchListener((v, event) -> {
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

        private void setupStatusUI(Enrollment enrollment) {
            // Vì class Enrollment mới không có status field, chúng ta sẽ ẩn các nút approve/reject
            // và chỉ hiển thị nút view details
            btnApprove.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnViewDetails.setVisibility(View.VISIBLE);
        }
    }
}
