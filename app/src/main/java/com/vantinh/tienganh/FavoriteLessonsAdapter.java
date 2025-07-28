package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FavoriteLessonsAdapter extends RecyclerView.Adapter<FavoriteLessonsAdapter.FavoriteViewHolder> {

    private List<FavoriteItem> favoriteItems;
    private OnFavoriteActionListener listener;
    private FirebaseFirestore db;

    public interface OnFavoriteActionListener {
        void onLessonClick(FavoriteItem favoriteItem);
        void onRemoveFromFavorites(FavoriteItem favoriteItem);
    }

    public FavoriteLessonsAdapter(List<FavoriteItem> favoriteItems, OnFavoriteActionListener listener) {
        this.favoriteItems = favoriteItems;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_lesson, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteItem item = favoriteItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return favoriteItems.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < favoriteItems.size()) {
            favoriteItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private CardView cardFavorite;
        private TextView tvLessonTitle;
        private TextView tvCourseTitle;
        private TextView tvLessonType;
        private TextView tvEstimatedTime;
        private TextView tvFavoriteDate;
        private ImageButton btnRemoveFavorite;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFavorite = itemView.findViewById(R.id.card_favorite);
            tvLessonTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
            tvLessonType = itemView.findViewById(R.id.tv_lesson_type);
            tvEstimatedTime = itemView.findViewById(R.id.tv_estimated_time);
            tvFavoriteDate = itemView.findViewById(R.id.tv_favorite_date);
            btnRemoveFavorite = itemView.findViewById(R.id.btn_remove_favorite);
        }

        public void bind(FavoriteItem item) {
            tvLessonTitle.setText(item.getLessonTitle());
            tvCourseTitle.setText("📚 " + item.getCourseTitle());
            tvLessonType.setText(getTypeIcon(item.getLessonType()) + " " + item.getLessonType());
            tvEstimatedTime.setText("⏱ " + item.getEstimatedTime());

            // Format favorite date
            if (item.getFavoriteDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvFavoriteDate.setText("Đã lưu: " + sdf.format(item.getFavoriteDate().toDate()));
            } else {
                tvFavoriteDate.setText("Đã lưu: --");
            }

            // Set click listeners
            cardFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLessonClick(item);
                }
            });

            btnRemoveFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveFromFavorites(item);
                }
            });
        }

        private String getTypeIcon(String lessonType) {
            if (lessonType == null) return "📖";

            switch (lessonType.toLowerCase()) {
                case "video": return "🎥";
                case "audio": return "🎵";
                case "grammar": return "📝";
                case "vocabulary": return "📚";
                case "reading": return "📖";
                case "listening": return "👂";
                case "speaking": return "🗣";
                case "writing": return "✍️";
                default: return "📖";
            }
        }
    }
}
