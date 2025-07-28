package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FavoriteLessonsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvFavoriteLessons;
    private LinearLayout layoutNoFavorites;
    private ProgressBar progressBar;
    private TextView tvFavoriteCount;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<FavoriteItem> favoriteList;
    private FavoriteLessonsAdapter favoritesAdapter;
    private String currentStudentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_lessons);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        favoriteList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        getCurrentStudentId();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvFavoriteLessons = findViewById(R.id.rv_favorite_lessons);
        layoutNoFavorites = findViewById(R.id.layout_no_favorites);
        progressBar = findViewById(R.id.progress_bar);
        tvFavoriteCount = findViewById(R.id.tv_favorite_count);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bài học yêu thích");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        favoritesAdapter = new FavoriteLessonsAdapter(favoriteList, new FavoriteLessonsAdapter.OnFavoriteActionListener() {
            @Override
            public void onLessonClick(FavoriteItem favoriteItem) {
                // Navigate to lesson detail
                Intent intent = new Intent(FavoriteLessonsActivity.this, LessonDetailActivity.class);
                intent.putExtra("lessonId", favoriteItem.getLessonId());
                intent.putExtra("lessonTitle", favoriteItem.getLessonTitle());
                intent.putExtra("courseId", favoriteItem.getCourseId());
                intent.putExtra("courseTitle", favoriteItem.getCourseTitle());
                startActivity(intent);
            }

            @Override
            public void onRemoveFromFavorites(FavoriteItem favoriteItem) {
                showRemoveConfirmDialog(favoriteItem);
            }
        });

        rvFavoriteLessons.setLayoutManager(new LinearLayoutManager(this));
        rvFavoriteLessons.setAdapter(favoritesAdapter);
    }

    private void getCurrentStudentId() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentStudentId = documentSnapshot.getString("id");
                        
                        if (currentStudentId != null) {
                            loadFavoriteLessons();
                        } else {
                            Toast.makeText(this, "Không tìm thấy thông tin học viên", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FavoriteLessons", "Error loading user info", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void loadFavoriteLessons() {
        android.util.Log.d("FavoriteLessons", "Loading favorite lessons for student: " + currentStudentId);

        db.collection("favoriteItems")
                .whereEqualTo("studentId", currentStudentId)
                .orderBy("favoriteDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteList.clear();

                    android.util.Log.d("FavoriteLessons", "Found " + queryDocumentSnapshots.size() + " favorite items");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            FavoriteItem item = doc.toObject(FavoriteItem.class);
                            item.setId(doc.getId());
                            favoriteList.add(item);

                            android.util.Log.d("FavoriteLessons", "Added favorite: " + item.getLessonTitle());
                        } catch (Exception e) {
                            android.util.Log.e("FavoriteLessons", "Error parsing favorite item: " + doc.getId(), e);
                        }
                    }

                    updateUI();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FavoriteLessons", "Error loading favorite lessons", e);
                    Toast.makeText(this, "Lỗi tải bài học yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUI();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void updateUI() {
        int favoriteCount = favoriteList.size();
        
        if (favoriteCount == 0) {
            tvFavoriteCount.setText("Bạn chưa có bài học yêu thích nào");
            layoutNoFavorites.setVisibility(View.VISIBLE);
            rvFavoriteLessons.setVisibility(View.GONE);
        } else {
            tvFavoriteCount.setText("Bạn đã lưu " + favoriteCount + " bài học yêu thích");
            layoutNoFavorites.setVisibility(View.GONE);
            rvFavoriteLessons.setVisibility(View.VISIBLE);
            favoritesAdapter.notifyDataSetChanged();
        }

        android.util.Log.d("FavoriteLessons", "UI updated with " + favoriteCount + " favorites");
    }

    private void showRemoveConfirmDialog(FavoriteItem favoriteItem) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa khỏi yêu thích")
                .setMessage("Bạn có chắc muốn xóa \"" + favoriteItem.getLessonTitle() + "\" khỏi danh sách yêu thích?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    removeFromFavorites(favoriteItem);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void removeFromFavorites(FavoriteItem favoriteItem) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("favoriteItems").document(favoriteItem.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from local list
                    int position = favoriteList.indexOf(favoriteItem);
                    if (position != -1) {
                        favoriteList.remove(position);
                        favoritesAdapter.removeItem(position);
                    }

                    Toast.makeText(this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    updateUI();
                    progressBar.setVisibility(View.GONE);

                    android.util.Log.d("FavoriteLessons", "Removed favorite: " + favoriteItem.getLessonTitle());
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FavoriteLessons", "Error removing favorite", e);
                    Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorites when returning from lesson detail
        if (currentStudentId != null) {
            loadFavoriteLessons();
        }
    }
}
