package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CourseRequestAdapter extends RecyclerView.Adapter<CourseRequestAdapter.ViewHolder> {

    private List<CourseRequest> requestList;
    private OnRequestActionListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnRequestActionListener {
        void onApprove(CourseRequest request);
        void onReject(CourseRequest request);
    }

    public CourseRequestAdapter(List<CourseRequest> requestList, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseRequest request = requestList.get(position);

        holder.tvStudentName.setText(request.getStudentName());
        holder.tvStudentEmail.setText(request.getStudentEmail());
        holder.tvCourseName.setText(request.getCourseName());
        holder.tvMessage.setText(request.getMessage());
        holder.tvRequestDate.setText(dateFormat.format(request.getRequestDate()));
        holder.tvStatus.setText(request.getStatus().toUpperCase());

        // Set status color
        if ("pending".equals(request.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else if ("approved".equals(request.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onApprove(request);
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReject(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public void updateList(List<CourseRequest> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentEmail, tvCourseName, tvMessage, tvRequestDate, tvStatus;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvStudentEmail = itemView.findViewById(R.id.tv_student_email);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvRequestDate = itemView.findViewById(R.id.tv_request_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}
