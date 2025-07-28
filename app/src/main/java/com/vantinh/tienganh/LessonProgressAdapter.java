package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LessonProgressAdapter extends RecyclerView.Adapter<LessonProgressAdapter.ProgressViewHolder> {

    private List<StudentProgressDetailActivity.LessonProgressItem> progressList;

    public LessonProgressAdapter(List<StudentProgressDetailActivity.LessonProgressItem> progressList) {
        this.progressList = progressList;
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        StudentProgressDetailActivity.LessonProgressItem item = progressList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return progressList.size();
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private TextView text1, text2;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }

        public void bind(StudentProgressDetailActivity.LessonProgressItem item) {
            // Thiết lập tiêu đề bài học với status
            String title = "Bài " + item.getLessonOrder() + ": " + item.getLessonTitle();
            text1.setText(title);
            text1.setTextSize(16);

            // Thiết lập trạng thái hoàn thành
            String status;
            if (item.isCompleted()) {
                status = "✅ Đã hoàn thành";
                if (item.getCompletedAt() != null) {
                    status += " - " + item.getCompletedAt();
                }
                text1.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                text2.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else {
                status = "⏳ Chưa hoàn thành";
                text1.setTextColor(itemView.getContext().getColor(android.R.color.black));
                text2.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
            }

            text2.setText(status);
            text2.setTextSize(14);

            // Thêm padding cho item
            itemView.setPadding(16, 12, 16, 12);
        }
    }
}
