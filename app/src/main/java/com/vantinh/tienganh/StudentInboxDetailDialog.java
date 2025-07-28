package com.vantinh.tienganh;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class StudentInboxDetailDialog extends Dialog {

    private InboxMessage message;
    private OnMessageActionListener listener;

    public interface OnMessageActionListener {
        void onMarkAsRead(String messageId);
        void onViewCourse(String courseId);
    }

    public StudentInboxDetailDialog(@NonNull Context context, InboxMessage message, OnMessageActionListener listener) {
        super(context);
        this.message = message;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_inbox_message_detail);

        initViews();
        setupData();
        setupClickListeners();
    }

    private void initViews() {
        // Views are initialized in setupData()
    }

    private void setupData() {
        TextView tvMessageType = findViewById(R.id.tv_message_type);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvFullMessage = findViewById(R.id.tv_full_message);
        TextView tvFromName = findViewById(R.id.tv_from_name);
        TextView tvDate = findViewById(R.id.tv_date);
        TextView tvCourseName = findViewById(R.id.tv_course_name);
        TextView tvOriginalFeedback = findViewById(R.id.tv_original_feedback);
        View layoutOriginalFeedback = findViewById(R.id.layout_original_feedback);

        // Set message type
        tvMessageType.setText(message.getTypeDisplayName());

        // Set type color
        switch (message.getType()) {
            case "notification":
                tvMessageType.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_blue_light));
                break;
            case "feedback_response":
                tvMessageType.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_green_light));
                break;
        }

        // Set title
        tvTitle.setText(message.getTitle());

        // Set full message
        tvFullMessage.setText(message.getMessage());

        // Set from name
        tvFromName.setText("Từ: " + message.getFromName());

        // Set date
        tvDate.setText(message.getFormattedDate());

        // Set course name if available
        if (message.getCourseName() != null && !message.getCourseName().isEmpty()) {
            tvCourseName.setVisibility(View.VISIBLE);
            tvCourseName.setText("Khóa học: " + message.getCourseName());
        } else {
            tvCourseName.setVisibility(View.GONE);
        }

        // Show original feedback if this is a feedback response
        if ("feedback_response".equals(message.getType()) &&
            message.getOriginalFeedback() != null && !message.getOriginalFeedback().isEmpty()) {
            layoutOriginalFeedback.setVisibility(View.VISIBLE);
            tvOriginalFeedback.setText(message.getOriginalFeedback());
        } else {
            layoutOriginalFeedback.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        Button btnClose = findViewById(R.id.btn_close);
        Button btnViewCourse = findViewById(R.id.btn_view_course);
        Button btnMarkAsRead = findViewById(R.id.btn_mark_as_read);

        btnClose.setOnClickListener(v -> dismiss());

        btnViewCourse.setOnClickListener(v -> {
            if (listener != null && message.getCourseId() != null) {
                listener.onViewCourse(message.getCourseId());
            }
            dismiss();
        });

        btnMarkAsRead.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMarkAsRead(message.getId());
            }
            dismiss();
        });

        // Show/hide buttons based on message type and state
        if (message.getCourseId() != null && !message.getCourseId().isEmpty()) {
            btnViewCourse.setVisibility(View.VISIBLE);
        } else {
            btnViewCourse.setVisibility(View.GONE);
        }

        if (message.isRead()) {
            btnMarkAsRead.setVisibility(View.GONE);
        } else {
            btnMarkAsRead.setVisibility(View.VISIBLE);
        }
    }
}
