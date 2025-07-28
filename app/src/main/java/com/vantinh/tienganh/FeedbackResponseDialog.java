package com.vantinh.tienganh;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class FeedbackResponseDialog extends Dialog {

    private TextView tvStudentName, tvCourseName, tvFeedbackDate, tvFeedbackMessage, tvCurrentResponse;
    private EditText etResponse;
    private Button btnSendResponse, btnCancel;

    private Feedback feedback;
    private OnResponseListener listener;

    public interface OnResponseListener {
        void onResponseSent(String response);
    }

    public FeedbackResponseDialog(@NonNull Context context, Feedback feedback, OnResponseListener listener) {
        super(context);
        this.feedback = feedback;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_feedback_response);

        initViews();
        setupData();
        setupClickListeners();
    }

    private void initViews() {
        tvStudentName = findViewById(R.id.tv_student_name);
        tvCourseName = findViewById(R.id.tv_course_name);
        tvFeedbackDate = findViewById(R.id.tv_feedback_date);
        tvFeedbackMessage = findViewById(R.id.tv_feedback_message);
        tvCurrentResponse = findViewById(R.id.tv_current_response);
        etResponse = findViewById(R.id.et_response);
        btnSendResponse = findViewById(R.id.btn_send_response);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupData() {
        tvStudentName.setText("Học viên: " + feedback.getStudentName());
        tvCourseName.setText("Khóa học: " + feedback.getCourseName());
        tvFeedbackDate.setText("Ngày gửi: " + feedback.getFormattedDate());
        tvFeedbackMessage.setText(feedback.getMessage());

        // Hiển thị phản hồi hiện tại nếu có
        if (feedback.hasResponse()) {
            tvCurrentResponse.setVisibility(View.VISIBLE);
            tvCurrentResponse.setText("Phản hồi hiện tại (" + feedback.getFormattedResponseDate() + "):\n" + feedback.getTeacherResponse());
            etResponse.setHint("Nhập phản hồi mới để cập nhật...");
            btnSendResponse.setText("Cập nhật phản hồi");
        } else {
            tvCurrentResponse.setVisibility(View.GONE);
            etResponse.setHint("Nhập phản hồi của bạn...");
            btnSendResponse.setText("Gửi phản hồi");
        }
    }

    private void setupClickListeners() {
        btnSendResponse.setOnClickListener(v -> {
            String response = etResponse.getText().toString().trim();

            if (response.isEmpty()) {
                etResponse.setError("Vui lòng nhập nội dung phản hồi");
                return;
            }

            if (response.length() < 10) {
                etResponse.setError("Phản hồi quá ngắn (ít nhất 10 ký tự)");
                return;
            }

            if (listener != null) {
                listener.onResponseSent(response);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}
