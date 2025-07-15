package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonalScheduleActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvCurrentWeek, tvStudyHours, tvUpcomingClass, tvGoalProgress;
    private RecyclerView rvSchedule, rvGoals;
    private CardView cardTodaySchedule, cardWeeklyGoal, cardStudyReminder;
    private Button btnAddSchedule, btnViewCalendar, btnSetReminder;
    private FloatingActionButton fabAddGoal;
    private LinearLayout layoutGoalSetting;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String mode; // "goals", "weekly", "daily"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_schedule);

        // Get mode from intent
        mode = getIntent().getStringExtra("mode");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
        loadScheduleData();
        handleModeSpecificSetup();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        // TextViews
        tvCurrentWeek = findViewById(R.id.tv_current_week);
        tvStudyHours = findViewById(R.id.tv_study_hours);
        tvUpcomingClass = findViewById(R.id.tv_upcoming_class);
        tvGoalProgress = findViewById(R.id.tv_goal_progress);

        // RecyclerViews
        rvSchedule = findViewById(R.id.rv_schedule);
        rvGoals = findViewById(R.id.rv_goals);

        // Cards
        cardTodaySchedule = findViewById(R.id.card_today_schedule);
        cardWeeklyGoal = findViewById(R.id.card_weekly_goal);
        cardStudyReminder = findViewById(R.id.card_study_reminder);

        // Buttons
        btnAddSchedule = findViewById(R.id.btn_add_schedule);
        btnViewCalendar = findViewById(R.id.btn_view_calendar);
        btnSetReminder = findViewById(R.id.btn_set_reminder);

        // FAB
        fabAddGoal = findViewById(R.id.fab_add_goal);

        // Layouts
        layoutGoalSetting = findViewById(R.id.layout_goal_setting);

        // Setup RecyclerViews
        if (rvSchedule != null) {
            rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        }
        if (rvGoals != null) {
            rvGoals.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            String title = "Lịch học cá nhân";
            if ("goals".equals(mode)) {
                title = "Mục tiêu học tập";
            } else if ("weekly".equals(mode)) {
                title = "Lịch tuần";
            }
            getSupportActionBar().setTitle(title);
        }
    }

    private void setupClickListeners() {
        if (btnAddSchedule != null) {
            btnAddSchedule.setOnClickListener(v -> showAddScheduleDialog());
        }

        if (btnViewCalendar != null) {
            btnViewCalendar.setOnClickListener(v -> openCalendarView());
        }

        if (btnSetReminder != null) {
            btnSetReminder.setOnClickListener(v -> setupStudyReminder());
        }

        if (fabAddGoal != null) {
            fabAddGoal.setOnClickListener(v -> showAddGoalDialog());
        }

        if (cardTodaySchedule != null) {
            cardTodaySchedule.setOnClickListener(v -> {
                Intent intent = new Intent(this, StudyProgressActivity.class);
                startActivity(intent);
            });
        }

        if (cardWeeklyGoal != null) {
            cardWeeklyGoal.setOnClickListener(v -> {
                if (layoutGoalSetting != null) {
                    layoutGoalSetting.setVisibility(
                        layoutGoalSetting.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
                    );
                }
            });
        }

        if (cardStudyReminder != null) {
            cardStudyReminder.setOnClickListener(v -> setupStudyReminder());
        }
    }

    private void handleModeSpecificSetup() {
        if ("goals".equals(mode)) {
            // Show goal-related sections
            if (layoutGoalSetting != null) {
                layoutGoalSetting.setVisibility(View.VISIBLE);
            }
            if (fabAddGoal != null) {
                fabAddGoal.setVisibility(View.VISIBLE);
            }
        } else if ("weekly".equals(mode)) {
            // Focus on weekly schedule
            loadWeeklySchedule();
        }
    }

    private void loadScheduleData() {
        // Load user's schedule data from Firebase
        loadTodaySchedule();
        loadWeeklyGoals();
        loadUpcomingClasses();
    }

    private void loadTodaySchedule() {
        // Sample data - replace with actual Firebase implementation
        if (tvUpcomingClass != null) {
            tvUpcomingClass.setText("Tiếp theo: Ngữ pháp - 14:00");
        }
        if (tvStudyHours != null) {
            tvStudyHours.setText("Hôm nay: 2/4 giờ");
        }
    }

    private void loadWeeklyGoals() {
        // Sample data
        if (tvGoalProgress != null) {
            tvGoalProgress.setText("Tuần này: 12/20 giờ học");
        }
    }

    private void loadWeeklySchedule() {
        // Load weekly schedule data
        android.widget.Toast.makeText(this, "Đang tải lịch tuần...", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void loadUpcomingClasses() {
        // Load upcoming classes
        if (tvCurrentWeek != null) {
            tvCurrentWeek.setText("Tuần 15/07 - 21/07/2025");
        }
    }

    private void showAddScheduleDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Thêm lịch học")
                .setMessage("Chọn loại lịch học muốn thêm:")
                .setPositiveButton("Từ vựng", (dialog, which) -> addScheduleItem("vocabulary"))
                .setNeutralButton("Ngữ pháp", (dialog, which) -> addScheduleItem("grammar"))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addScheduleItem(String type) {
        android.widget.Toast.makeText(this, "Đã thêm lịch học " + type, android.widget.Toast.LENGTH_SHORT).show();
        // Implementation to add schedule item to Firebase
    }

    private void showAddGoalDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Đặt mục tiêu mới")
                .setMessage("Bạn muốn đặt mục tiêu gì?")
                .setPositiveButton("Giờ học/tuần", (dialog, which) -> setWeeklyHourGoal())
                .setNeutralButton("Số từ vựng", (dialog, which) -> setVocabularyGoal())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setWeeklyHourGoal() {
        android.widget.Toast.makeText(this, "Mục tiêu: 20 giờ/tuần đã được đặt", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void setVocabularyGoal() {
        android.widget.Toast.makeText(this, "Mục tiêu: 50 từ vựng mới/tuần đã được đặt", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void openCalendarView() {
        android.widget.Toast.makeText(this, "Đang mở lịch...", android.widget.Toast.LENGTH_SHORT).show();
        // Could implement a calendar view or navigate to calendar activity
    }

    private void setupStudyReminder() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Đặt nhắc nhở")
                .setMessage("Chọn thời gian nhắc nhở học tập:")
                .setPositiveButton("Hàng ngày 19:00", (dialog, which) -> setReminder("daily_7pm"))
                .setNeutralButton("Thứ 2,4,6 - 18:00", (dialog, which) -> setReminder("mwf_6pm"))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setReminder(String reminderType) {
        android.widget.Toast.makeText(this, "Đã đặt nhắc nhở thành công", android.widget.Toast.LENGTH_SHORT).show();
        // Implementation to set reminder
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}