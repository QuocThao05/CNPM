package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessonList;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public LessonAdapter(List<Lesson> lessonList, OnLessonClickListener listener) {
        this.lessonList = lessonList;
        this.listener = listener;
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
        holder.bind(lesson, position + 1);
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {
        private CardView cardLesson;
        private TextView tvLessonNumber;
        private TextView tvLessonTitle;
        private TextView tvLessonType;
        private TextView tvEstimatedTime;
        private TextView tvCreatedDate;
        private ImageView ivLessonIcon;
        private View viewStatusIndicator;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            cardLesson = itemView.findViewById(R.id.card_lesson);
            tvLessonNumber = itemView.findViewById(R.id.tv_lesson_number);
            tvLessonTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvLessonType = itemView.findViewById(R.id.tv_lesson_type);
            tvEstimatedTime = itemView.findViewById(R.id.tv_estimated_time);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            ivLessonIcon = itemView.findViewById(R.id.iv_lesson_icon);
            viewStatusIndicator = itemView.findViewById(R.id.view_status_indicator);
        }

        public void bind(Lesson lesson, int lessonNumber) {
            tvLessonNumber.setText("Bài " + lessonNumber);
            tvLessonTitle.setText(lesson.getTitle());
            tvLessonType.setText(getTypeDisplayName(lesson.getType()));
            tvEstimatedTime.setText("⏱ " + lesson.getEstimatedTime() + " phút");

            if (lesson.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvCreatedDate.setText("📅 " + sdf.format(lesson.getCreatedAt()));
            }

            // Set icon based on lesson type
            setLessonIcon(lesson.getType());

            // Set status indicator color
            if (lesson.isPublished()) {
                viewStatusIndicator.setBackgroundResource(R.color.status_active);
            } else {
                viewStatusIndicator.setBackgroundResource(R.color.status_pending);
            }

            cardLesson.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLessonClick(lesson);
                }
            });

            // Add animation
            cardLesson.setOnTouchListener((v, event) -> {
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

        private String getTypeDisplayName(String type) {
            switch (type.toLowerCase()) {
                case "text": return "📝 Văn bản";
                case "video": return "🎥 Video";
                case "audio": return "🎧 Âm thanh";
                case "quiz": return "❓ Quiz";
                default: return "📄 Khác";
            }
        }

        private void setLessonIcon(String type) {
            switch (type.toLowerCase()) {
                case "video":
                    ivLessonIcon.setImageResource(android.R.drawable.ic_media_play);
                    break;
                case "audio":
                    ivLessonIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                    break;
                case "quiz":
                    ivLessonIcon.setImageResource(android.R.drawable.ic_menu_help);
                    break;
                default:
                    ivLessonIcon.setImageResource(android.R.drawable.ic_menu_edit);
                    break;
            }
        }
    }
}
