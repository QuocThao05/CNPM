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
        private TextView tvStudentName;
        private TextView tvStudentEmail;
        private TextView tvCourseName;
        private TextView tvEnrollmentDate;
        private TextView tvStatus;
        private TextView tvMessage;
        private MaterialButton btnApprove;
        private MaterialButton btnReject;
        private MaterialButton btnViewDetails;
        private View statusIndicator;

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
            statusIndicator = itemView.findViewById(R.id.status_indicator);
        }

        public void bind(Enrollment enrollment) {
            tvStudentName.setText("👤 " + enrollment.getStudentName());
            tvStudentEmail.setText("📧 " + enrollment.getStudentEmail());
            tvCourseName.setText("📚 " + enrollment.getCourseName());
            tvStatus.setText(enrollment.getStatusDisplayName());

            if (enrollment.getEnrollmentDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvEnrollmentDate.setText("📅 " + sdf.format(enrollment.getEnrollmentDate()));
            }

            // Show/hide message
            if (enrollment.getMessage() != null && !enrollment.getMessage().isEmpty()) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText("💬 " + enrollment.getMessage());
            } else {
                tvMessage.setVisibility(View.GONE);
            }

            // Set status indicator color and show/hide action buttons
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
            String status = enrollment.getStatus();

            switch (status) {
                case "PENDING":
                    statusIndicator.setBackgroundResource(R.color.status_pending);
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.status_pending));
                    btnApprove.setVisibility(View.VISIBLE);
                    btnReject.setVisibility(View.VISIBLE);
                    btnViewDetails.setVisibility(View.VISIBLE);
                    break;

                case "APPROVED":
                    statusIndicator.setBackgroundResource(R.color.status_active);
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.status_active));
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnViewDetails.setVisibility(View.VISIBLE);
                    break;

                case "REJECTED":
                    statusIndicator.setBackgroundResource(R.color.status_inactive);
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.status_inactive));
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnViewDetails.setVisibility(View.VISIBLE);
                    break;

                default:
                    statusIndicator.setBackgroundResource(R.color.text_secondary);
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnViewDetails.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
