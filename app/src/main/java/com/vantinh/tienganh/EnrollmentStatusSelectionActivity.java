package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;

public class EnrollmentStatusSelectionActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private View layoutApproved, layoutRejected;
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_status_selection);

        initViews();
        setupToolbar();
        getTeacherId();
        setupClickListeners();
        addAnimations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        layoutApproved = findViewById(R.id.layout_approved);
        layoutRejected = findViewById(R.id.layout_rejected);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý đăng ký");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getTeacherId() {
        // Lấy teacherId từ Intent hoặc từ FirebaseAuth
        teacherId = getIntent().getStringExtra("teacherId");
        if (teacherId == null) {
            teacherId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    private void setupClickListeners() {
        layoutApproved.setOnClickListener(v -> {
            animateCardClick(layoutApproved);
            Intent intent = new Intent(this, EnrollmentListActivity.class);
            intent.putExtra("teacherId", teacherId);
            intent.putExtra("status", "approved");
            intent.putExtra("title", "Đã duyệt");
            startActivity(intent);
        });

        layoutRejected.setOnClickListener(v -> {
            animateCardClick(layoutRejected);
            Intent intent = new Intent(this, EnrollmentListActivity.class);
            intent.putExtra("teacherId", teacherId);
            intent.putExtra("status", "rejected");
            intent.putExtra("title", "Đã từ chối");
            startActivity(intent);
        });
    }

    private void addAnimations() {
        // Fade in animation cho các cards
        layoutApproved.setAlpha(0f);
        layoutRejected.setAlpha(0f);

        layoutApproved.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(200)
                .start();

        layoutRejected.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(400)
                .start();
    }

    private void animateCardClick(View card) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 0.95f, 1.0f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(100);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        card.startAnimation(scaleAnimation);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
