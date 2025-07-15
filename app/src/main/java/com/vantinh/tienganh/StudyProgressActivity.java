package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudyProgressActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvOverallProgress, tvCompletedCourses, tvTotalStudyTime, tvQuizResults;
    private ProgressBar progressOverall, progressVocabulary, progressGrammar, progressListening, progressSpeaking;
    private TextView tvVocabularyProgress, tvGrammarProgress, tvListeningProgress, tvSpeakingProgress;
    private RecyclerView rvRecentActivities, rvAchievements;
    private CardView cardWeeklyGoal, cardMonthlyStats, cardRecentQuiz;
    private Button btnViewAllCourses, btnTakeQuiz, btnSetGoals;
    private LinearLayout layoutQuizResult;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_progress);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
        loadProgressData();
        checkQuizResult();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        // Progress TextViews
        tvOverallProgress = findViewById(R.id.tv_overall_progress);
        tvCompletedCourses = findViewById(R.id.tv_completed_courses);
        tvTotalStudyTime = findViewById(R.id.tv_total_study_time);
        tvQuizResults = findViewById(R.id.tv_quiz_results);

        // Progress bars
        progressOverall = findViewById(R.id.progress_overall);
        progressVocabulary = findViewById(R.id.progress_vocabulary);
        progressGrammar = findViewById(R.id.progress_grammar);
        progressListening = findViewById(R.id.progress_listening);
        progressSpeaking = findViewById(R.id.progress_speaking);

        // Category progress TextViews
        tvVocabularyProgress = findViewById(R.id.tv_vocabulary_progress);
        tvGrammarProgress = findViewById(R.id.tv_grammar_progress);
        tvListeningProgress = findViewById(R.id.tv_listening_progress);
        tvSpeakingProgress = findViewById(R.id.tv_speaking_progress);

        // RecyclerViews
        rvRecentActivities = findViewById(R.id.rv_recent_activities);
        rvAchievements = findViewById(R.id.rv_achievements);

        // Cards
        cardWeeklyGoal = findViewById(R.id.card_weekly_goal);
        cardMonthlyStats = findViewById(R.id.card_monthly_stats);
        cardRecentQuiz = findViewById(R.id.card_recent_quiz);

        // Buttons
        btnViewAllCourses = findViewById(R.id.btn_view_all_courses);
        btnTakeQuiz = findViewById(R.id.btn_take_quiz);
        btnSetGoals = findViewById(R.id.btn_set_goals);

        // Layouts
        layoutQuizResult = findViewById(R.id.layout_quiz_result);

        // Setup RecyclerViews
        if (rvRecentActivities != null) {
            rvRecentActivities.setLayoutManager(new LinearLayoutManager(this));
        }
        if (rvAchievements != null) {
            rvAchievements.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Tiến độ học tập");
        }
    }

    private void setupClickListeners() {
        if (btnViewAllCourses != null) {
            btnViewAllCourses.setOnClickListener(v -> {
                Intent intent = new Intent(this, CourseListActivity.class);
                intent.putExtra("title", "Tất cả khóa học");
                startActivity(intent);
            });
        }

        if (btnTakeQuiz != null) {
            btnTakeQuiz.setOnClickListener(v -> {
                Intent intent = new Intent(this, QuizActivity.class);
                intent.putExtra("mode", "practice");
                startActivity(intent);
            });
        }

        if (btnSetGoals != null) {
            btnSetGoals.setOnClickListener(v -> {
                Intent intent = new Intent(this, PersonalScheduleActivity.class);
                intent.putExtra("mode", "goals");
                startActivity(intent);
            });
        }

        // Category progress card clicks
        if (cardWeeklyGoal != null) {
            cardWeeklyGoal.setOnClickListener(v -> {
                Intent intent = new Intent(this, PersonalScheduleActivity.class);
                intent.putExtra("mode", "weekly");
                startActivity(intent);
            });
        }

        if (cardMonthlyStats != null) {
            cardMonthlyStats.setOnClickListener(v -> showDetailedStats());
        }

        if (cardRecentQuiz != null) {
            cardRecentQuiz.setOnClickListener(v -> {
                Intent intent = new Intent(this, QuizActivity.class);
                intent.putExtra("mode", "recent");
                startActivity(intent);
            });
        }

        // Category-specific navigation
        setupCategoryClicks();
    }

    private void setupCategoryClicks() {
        View vocabularySection = findViewById(R.id.vocabulary_section);
        if (vocabularySection != null) {
            vocabularySection.setOnClickListener(v -> {
                Intent intent = new Intent(this, CourseListActivity.class);
                intent.putExtra("category", "vocabulary");
                intent.putExtra("title", "Từ vựng");
                startActivity(intent);
            });
        }

        View grammarSection = findViewById(R.id.grammar_section);
        if (grammarSection != null) {
            grammarSection.setOnClickListener(v -> {
                Intent intent = new Intent(this, CourseListActivity.class);
                intent.putExtra("category", "grammar");
                intent.putExtra("title", "Ngữ pháp");
                startActivity(intent);
            });
        }

        View listeningSection = findViewById(R.id.listening_section);
        if (listeningSection != null) {
            listeningSection.setOnClickListener(v -> {
                Intent intent = new Intent(this, CourseListActivity.class);
                intent.putExtra("category", "listening");
                intent.putExtra("title", "Nghe");
                startActivity(intent);
            });
        }

        View speakingSection = findViewById(R.id.speaking_section);
        if (speakingSection != null) {
            speakingSection.setOnClickListener(v -> {
                Intent intent = new Intent(this, CourseListActivity.class);
                intent.putExtra("category", "speaking");
                intent.putExtra("title", "Nói");
                startActivity(intent);
            });
        }
    }

    private void checkQuizResult() {
        // Check if this activity was started after completing a quiz
        boolean quizCompleted = getIntent().getBooleanExtra("quizCompleted", false);
        if (quizCompleted && layoutQuizResult != null) {
            int score = getIntent().getIntExtra("score", 0);
            int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

            layoutQuizResult.setVisibility(View.VISIBLE);
            if (tvQuizResults != null) {
                tvQuizResults.setText(String.format("Kết quả Quiz: %d/%d điểm", score, totalQuestions));
            }
        }
    }

    private void loadProgressData() {
        // Load user progress data from Firebase
        loadOverallProgress();
        loadCategoryProgress();
        loadRecentActivities();
        loadAchievements();
    }

    private void loadOverallProgress() {
        // Sample data - replace with actual Firebase implementation
        if (progressOverall != null) {
            progressOverall.setProgress(75); // 75% overall progress
        }
        if (tvOverallProgress != null) {
            tvOverallProgress.setText("75% hoàn thành");
        }
        if (tvCompletedCourses != null) {
            tvCompletedCourses.setText("12/16 khóa học");
        }
        if (tvTotalStudyTime != null) {
            tvTotalStudyTime.setText("45 giờ");
        }
    }

    private void loadCategoryProgress() {
        // Sample progress data
        updateCategoryProgress(progressVocabulary, tvVocabularyProgress, 80, "Từ vựng: 80%");
        updateCategoryProgress(progressGrammar, tvGrammarProgress, 65, "Ngữ pháp: 65%");
        updateCategoryProgress(progressListening, tvListeningProgress, 70, "Nghe: 70%");
        updateCategoryProgress(progressSpeaking, tvSpeakingProgress, 55, "Nói: 55%");
    }

    private void updateCategoryProgress(ProgressBar progressBar, TextView textView, int progress, String text) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        if (textView != null) {
            textView.setText(text);
        }
    }

    private void loadRecentActivities() {
        // Implementation for loading recent activities
        // This would typically involve creating an adapter and setting it to the RecyclerView
    }

    private void loadAchievements() {
        // Implementation for loading achievements
        // This would typically involve creating an adapter and setting it to the RecyclerView
    }

    private void showDetailedStats() {
        // Show detailed statistics dialog or navigate to detailed stats activity
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Thống kê chi tiết")
                .setMessage("Tháng này:\n• 25 giờ học\n• 8 quiz hoàn thành\n• 120 từ vựng mới\n• Điểm trung bình: 8.5/10")
                .setPositiveButton("Đóng", null)
                .show();
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