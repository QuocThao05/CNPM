package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudentNotificationAdapter extends RecyclerView.Adapter<StudentNotificationAdapter.NotificationViewHolder> {

    private List<StudentNotification> notificationList;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(StudentNotification notification);
    }

    public StudentNotificationAdapter(List<StudentNotification> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        StudentNotification notification = notificationList.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private CardView cardNotification;
        private TextView tvTitle;
        private TextView tvMessage;
        private TextView tvTimeAgo;
        private TextView tvCourseName;
        private View viewUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotification = itemView.findViewById(R.id.card_notification);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimeAgo = itemView.findViewById(R.id.tv_time_ago);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            viewUnreadIndicator = itemView.findViewById(R.id.view_unread_indicator);

            cardNotification.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notificationList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(StudentNotification notification) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            tvTimeAgo.setText(notification.getTimeAgo());

            if (notification.getCourseName() != null) {
                tvCourseName.setText(notification.getCourseName());
                tvCourseName.setVisibility(View.VISIBLE);
            } else {
                tvCourseName.setVisibility(View.GONE);
            }

            // Hiển thị trạng thái đã đọc/chưa đọc
            if (notification.isRead()) {
                viewUnreadIndicator.setVisibility(View.GONE);
                cardNotification.setAlpha(0.7f);
                tvTitle.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
            } else {
                viewUnreadIndicator.setVisibility(View.VISIBLE);
                cardNotification.setAlpha(1.0f);
                tvTitle.setTextColor(itemView.getContext().getColor(android.R.color.black));
            }

            // Màu sắc theo loại thông báo
            if ("feedback_response".equals(notification.getType())) {
                viewUnreadIndicator.setBackgroundColor(
                    itemView.getContext().getColor(android.R.color.holo_green_dark)
                );
            } else {
                viewUnreadIndicator.setBackgroundColor(
                    itemView.getContext().getColor(android.R.color.holo_blue_dark)
                );
            }
        }
    }
}
