package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudentLessonAdapter extends RecyclerView.Adapter<StudentLessonAdapter.StudentLessonViewHolder> {

    private List<Lesson> lessonList;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public StudentLessonAdapter(List<Lesson> lessonList, OnLessonClickListener listener) {
        this.lessonList = lessonList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_lesson, parent, false);
        return new StudentLessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentLessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);
        holder.bind(lesson);
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    public class StudentLessonViewHolder extends RecyclerView.ViewHolder {
        private CardView cardLesson;
        private TextView tvLessonOrder;
        private TextView tvLessonTitle;
        private TextView tvLessonType;
        private TextView tvEstimatedTime;
        private TextView tvGrammarPreview;
        private ImageView ivLessonIcon;
        private ImageView ivPlayIcon;

        public StudentLessonViewHolder(@NonNull View itemView) {
            super(itemView);
            cardLesson = itemView.findViewById(R.id.card_lesson);
            tvLessonOrder = itemView.findViewById(R.id.tv_lesson_order);
            tvLessonTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvLessonType = itemView.findViewById(R.id.tv_lesson_type);
            tvEstimatedTime = itemView.findViewById(R.id.tv_estimated_time);
            tvGrammarPreview = itemView.findViewById(R.id.tv_grammar_preview);
            ivLessonIcon = itemView.findViewById(R.id.iv_lesson_icon);
            ivPlayIcon = itemView.findViewById(R.id.iv_play_icon);

            cardLesson.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLessonClick(lessonList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Lesson lesson) {
            tvLessonOrder.setText("Bài " + lesson.getOrder());
            tvLessonTitle.setText(lesson.getTitle());
            tvLessonType.setText(lesson.getTypeDisplayName());
            tvEstimatedTime.setText(lesson.getEstimatedTimeString());

            // Set lesson type icon
            setLessonTypeIcon(lesson.getType());

            // Apply lesson status styling
            applyLessonStatusStyling(lesson);

            // Show grammar preview if this is a Grammar lesson
            if ("Grammar".equalsIgnoreCase(lesson.getCategory())) {
                tvGrammarPreview.setVisibility(View.VISIBLE);
                StringBuilder preview = new StringBuilder();

                if (lesson.getGrammarRule() != null && !lesson.getGrammarRule().isEmpty()) {
                    preview.append("📝 ").append(lesson.getGrammarRule().substring(0,
                        Math.min(60, lesson.getGrammarRule().length())));
                    if (lesson.getGrammarRule().length() > 60) {
                        preview.append("...");
                    }
                }

                if (lesson.getGrammarExamples() != null && !lesson.getGrammarExamples().isEmpty()) {
                    if (preview.length() > 0) preview.append("\n");
                    preview.append("📚 ").append(lesson.getGrammarExamples().size()).append(" ví dụ");
                }

                tvGrammarPreview.setText(preview.toString());
            } else {
                tvGrammarPreview.setVisibility(View.GONE);
            }

            // Enable/disable click based on accessibility
            cardLesson.setClickable(lesson.isAccessible());
            cardLesson.setEnabled(lesson.isAccessible());

            // Add click animation only for accessible lessons
            if (lesson.isAccessible()) {
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
            } else {
                cardLesson.setOnTouchListener(null);
            }
        }

        private void setLessonTypeIcon(String lessonType) {
            int iconResId;
            switch (lessonType) {
                case "video":
                    iconResId = android.R.drawable.ic_media_play;
                    ivPlayIcon.setVisibility(View.VISIBLE);
                    break;
                case "audio":
                    iconResId = android.R.drawable.ic_btn_speak_now;
                    ivPlayIcon.setVisibility(View.VISIBLE);
                    break;
                case "quiz":
                    iconResId = android.R.drawable.ic_menu_help;
                    ivPlayIcon.setVisibility(View.GONE);
                    break;
                case "text":
                default:
                    iconResId = android.R.drawable.ic_menu_edit;
                    ivPlayIcon.setVisibility(View.GONE);
                    break;
            }

            ivLessonIcon.setImageResource(iconResId);
        }

        private void applyLessonStatusStyling(Lesson lesson) {
            if (lesson.isCompleted()) {
                // Completed lesson - green tint
                cardLesson.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.holo_green_light));
                cardLesson.getBackground().setAlpha(50);
                tvLessonOrder.setBackgroundResource(android.R.drawable.ic_menu_save); // Checkmark background
                tvLessonTitle.setAlpha(0.8f);

                // Add completed indicator
                if (tvLessonTitle.getText() != null && !tvLessonTitle.getText().toString().contains("✓")) {
                    tvLessonTitle.setText("✓ " + tvLessonTitle.getText());
                }

            } else if (lesson.isLocked()) {
                // Locked lesson - gray tint and lock icon
                cardLesson.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.darker_gray));
                cardLesson.getBackground().setAlpha(30);
                tvLessonOrder.setBackgroundResource(android.R.drawable.ic_lock_lock); // Lock background
                tvLessonTitle.setAlpha(0.5f);
                tvLessonType.setAlpha(0.5f);
                tvEstimatedTime.setAlpha(0.5f);

                // Add lock indicator
                if (tvLessonTitle.getText() != null && !tvLessonTitle.getText().toString().contains("🔒")) {
                    tvLessonTitle.setText("🔒 " + tvLessonTitle.getText());
                }

            } else if (lesson.isAccessible()) {
                // Current/next lesson - highlighted
                cardLesson.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.holo_blue_light));
                cardLesson.getBackground().setAlpha(40);
                tvLessonOrder.setBackgroundResource(android.R.drawable.ic_media_play); // Play background
                tvLessonTitle.setAlpha(1.0f);

                // Add play indicator for current lesson
                if (tvLessonTitle.getText() != null && !tvLessonTitle.getText().toString().contains("▶")) {
                    tvLessonTitle.setText("▶ " + tvLessonTitle.getText());
                }
            }
        }
    }
}
