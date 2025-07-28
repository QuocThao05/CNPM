package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TeacherFeedbackAdapter extends RecyclerView.Adapter<TeacherFeedbackAdapter.FeedbackViewHolder> {

    private List<Feedback> feedbackList;
    private OnFeedbackClickListener listener;

    public interface OnFeedbackClickListener {
        void onFeedbackClick(Feedback feedback);
    }

    public TeacherFeedbackAdapter(List<Feedback> feedbackList, OnFeedbackClickListener listener) {
        this.feedbackList = feedbackList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.bind(feedback);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private CardView cardFeedback;
        private TextView tvStudentName;
        private TextView tvCourseName;
        private TextView tvFeedbackDate;
        private TextView tvFeedbackMessage;
        private TextView tvStatus;
        private TextView tvResponsePreview;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFeedback = itemView.findViewById(R.id.card_feedback);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvFeedbackDate = itemView.findViewById(R.id.tv_feedback_date);
            tvFeedbackMessage = itemView.findViewById(R.id.tv_feedback_message);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvResponsePreview = itemView.findViewById(R.id.tv_response_preview);

            cardFeedback.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFeedbackClick(feedbackList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Feedback feedback) {
            tvStudentName.setText(feedback.getStudentName());
            tvCourseName.setText(feedback.getCourseName());
            tvFeedbackDate.setText(feedback.getFormattedDate());

            // Hiển thị preview của feedback message (tối đa 100 ký tự)
            String message = feedback.getMessage();
            if (message != null && message.length() > 100) {
                message = message.substring(0, 100) + "...";
            }
            tvFeedbackMessage.setText(message);

            // Set status và màu sắc
            boolean hasResponse = feedback.hasResponse();
            if (hasResponse) {
                tvStatus.setText("Đã phản hồi");
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        itemView.getContext().getColor(android.R.color.holo_green_light)
                    )
                );

                // Hiển thị preview phản hồi
                String response = feedback.getTeacherResponse();
                if (response != null && response.length() > 80) {
                    response = response.substring(0, 80) + "...";
                }
                tvResponsePreview.setText("Phản hồi: " + response);
                tvResponsePreview.setVisibility(View.VISIBLE);
            } else {
                tvStatus.setText("Chưa phản hồi");
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        itemView.getContext().getColor(android.R.color.holo_orange_light)
                    )
                );
                tvResponsePreview.setVisibility(View.GONE);
            }

            // Style status TextView
            tvStatus.setPadding(16, 8, 16, 8);
            tvStatus.setTextSize(12);
        }
    }
}
