package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessonList;
    private OnLessonClickListener onLessonClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
        void onLessonEdit(Lesson lesson);
        void onLessonDelete(Lesson lesson);
    }

    public LessonAdapter(List<Lesson> lessonList, OnLessonClickListener onLessonClickListener) {
        this.lessonList = lessonList;
        this.onLessonClickListener = onLessonClickListener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);
        holder.bind(lesson);
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    public class LessonViewHolder extends RecyclerView.ViewHolder {
        private TextView tvLessonTitle;
        private TextView tvLessonOrder;
        private TextView tvLessonType;
        private TextView tvEstimatedTime;
        private TextView tvCreatedDate;
        private TextView tvPublishStatus;
        private TextView tvGrammarInfo;
        private ImageView ivEdit;
        private ImageView ivDelete;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLessonTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvLessonOrder = itemView.findViewById(R.id.tv_lesson_order);
            tvLessonType = itemView.findViewById(R.id.tv_lesson_type);
            tvEstimatedTime = itemView.findViewById(R.id.tv_estimated_time);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            tvPublishStatus = itemView.findViewById(R.id.tv_publish_status);
            tvGrammarInfo = itemView.findViewById(R.id.tv_grammar_info);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);

            itemView.setOnClickListener(v -> {
                if (onLessonClickListener != null) {
                    onLessonClickListener.onLessonClick(lessonList.get(getAdapterPosition()));
                }
            });

            ivEdit.setOnClickListener(v -> {
                if (onLessonClickListener != null) {
                    onLessonClickListener.onLessonEdit(lessonList.get(getAdapterPosition()));
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (onLessonClickListener != null) {
                    onLessonClickListener.onLessonDelete(lessonList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Lesson lesson) {
            tvLessonTitle.setText(lesson.getTitle());
            tvLessonOrder.setText("Bài " + lesson.getOrder());
            tvLessonType.setText(lesson.getTypeDisplayName());
            tvEstimatedTime.setText(lesson.getEstimatedTimeString());

            if (lesson.getCreatedAt() != null) {
                tvCreatedDate.setText("Tạo: " + dateFormat.format(lesson.getCreatedAt()));
            }

            if (lesson.isPublished()) {
                tvPublishStatus.setText("Đã xuất bản");
                tvPublishStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else {
                tvPublishStatus.setText("Nháp");
                tvPublishStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            }

            // Show grammar-specific info if this is a Grammar lesson
            if ("Grammar".equalsIgnoreCase(lesson.getCategory())) {
                tvGrammarInfo.setVisibility(View.VISIBLE);
                StringBuilder grammarInfo = new StringBuilder();

                if (lesson.getGrammarRule() != null && !lesson.getGrammarRule().isEmpty()) {
                    grammarInfo.append("Quy tắc: ").append(lesson.getGrammarRule().substring(0,
                        Math.min(50, lesson.getGrammarRule().length()))).append("...");
                }

                if (lesson.getGrammarExamples() != null && !lesson.getGrammarExamples().isEmpty()) {
                    if (grammarInfo.length() > 0) grammarInfo.append("\n");
                    grammarInfo.append("Ví dụ: ").append(lesson.getGrammarExamples().size()).append(" mẫu");
                }

                tvGrammarInfo.setText(grammarInfo.toString());
            } else {
                tvGrammarInfo.setVisibility(View.GONE);
            }
        }
    }
}
