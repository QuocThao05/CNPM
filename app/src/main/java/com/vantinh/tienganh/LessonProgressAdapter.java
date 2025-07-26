package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LessonProgressAdapter extends RecyclerView.Adapter<LessonProgressAdapter.LessonProgressViewHolder> {

    private List<StudentProgressDetailActivity.LessonProgressItem> lessonProgressList;

    public LessonProgressAdapter(List<StudentProgressDetailActivity.LessonProgressItem> lessonProgressList) {
        this.lessonProgressList = lessonProgressList;
    }

    @NonNull
    @Override
    public LessonProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use existing Android layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new LessonProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonProgressViewHolder holder, int position) {
        StudentProgressDetailActivity.LessonProgressItem item = lessonProgressList.get(position);

        // Set completion status with emoji icons
        if (item.isCompleted()) {
            holder.tvLessonTitle.setText("✅ " + item.getLessonTitle());
            holder.tvLessonType.setText(item.getLessonType() + " - Đã hoàn thành");
            holder.tvLessonType.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvLessonTitle.setText("⭕ " + item.getLessonTitle());
            holder.tvLessonType.setText(item.getLessonType() + " - Chưa hoàn thành");
            holder.tvLessonType.setTextColor(holder.itemView.getContext().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return lessonProgressList != null ? lessonProgressList.size() : 0;
    }

    static class LessonProgressViewHolder extends RecyclerView.ViewHolder {
        TextView tvLessonTitle, tvLessonType;

        public LessonProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            // Use standard Android layout IDs
            tvLessonTitle = itemView.findViewById(android.R.id.text1);
            tvLessonType = itemView.findViewById(android.R.id.text2);
        }
    }
}
