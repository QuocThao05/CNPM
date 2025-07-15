package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class QuizActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvQuestion, tvQuestionNumber, tvTimer, tvScore;
    private RadioGroup rgOptions;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnNext, btnPrevious, btnSubmit, btnHint;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CountDownTimer quizTimer;

    private int currentQuestionIndex = 0;
    private int totalQuestions = 10;
    private int score = 0;
    private long timeLeftInMillis = 600000; // 10 minutes
    private String courseId, category, mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Get data from intent
        courseId = getIntent().getStringExtra("courseId");
        category = getIntent().getStringExtra("category");
        mode = getIntent().getStringExtra("mode");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
        startQuiz();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvQuestion = findViewById(R.id.tv_question);
        tvQuestionNumber = findViewById(R.id.tv_question_number);
        tvTimer = findViewById(R.id.tv_timer);
        tvScore = findViewById(R.id.tv_score);
        rgOptions = findViewById(R.id.rg_options);
        rbOption1 = findViewById(R.id.rb_option1);
        rbOption2 = findViewById(R.id.rb_option2);
        rbOption3 = findViewById(R.id.rb_option3);
        rbOption4 = findViewById(R.id.rb_option4);
        btnNext = findViewById(R.id.btn_next);
        btnPrevious = findViewById(R.id.btn_previous);
        btnSubmit = findViewById(R.id.btn_submit);
        btnHint = findViewById(R.id.btn_hint);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            String title = "Quiz";
            if ("quick".equals(mode)) {
                title = "Quiz nhanh";
            } else if (category != null) {
                title = "Quiz " + category;
            }
            getSupportActionBar().setTitle(title);
        }
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> nextQuestion());
        btnPrevious.setOnClickListener(v -> previousQuestion());
        btnSubmit.setOnClickListener(v -> submitQuiz());
        btnHint.setOnClickListener(v -> showHint());
    }

    private void startQuiz() {
        updateQuestionDisplay();
        startTimer();
        loadQuestion(currentQuestionIndex);
    }

    private void updateQuestionDisplay() {
        tvQuestionNumber.setText(String.format("Câu %d/%d", currentQuestionIndex + 1, totalQuestions));
        progressBar.setProgress((currentQuestionIndex + 1) * 100 / totalQuestions);

        // Update button visibility
        btnPrevious.setVisibility(currentQuestionIndex > 0 ? View.VISIBLE : View.GONE);
        btnNext.setVisibility(currentQuestionIndex < totalQuestions - 1 ? View.VISIBLE : View.GONE);
        btnSubmit.setVisibility(currentQuestionIndex == totalQuestions - 1 ? View.VISIBLE : View.GONE);
    }

    private void loadQuestion(int questionIndex) {
        // Sample question - replace with actual data from Firebase
        tvQuestion.setText("Đây là câu hỏi số " + (questionIndex + 1) + ". What is the correct answer?");
        rbOption1.setText("Option A");
        rbOption2.setText("Option B");
        rbOption3.setText("Option C");
        rbOption4.setText("Option D");

        // Clear previous selection
        rgOptions.clearCheck();
    }

    private void nextQuestion() {
        saveCurrentAnswer();
        if (currentQuestionIndex < totalQuestions - 1) {
            currentQuestionIndex++;
            updateQuestionDisplay();
            loadQuestion(currentQuestionIndex);
        }
    }

    private void previousQuestion() {
        saveCurrentAnswer();
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            updateQuestionDisplay();
            loadQuestion(currentQuestionIndex);
        }
    }

    private void saveCurrentAnswer() {
        int selectedId = rgOptions.getCheckedRadioButtonId();
        if (selectedId != -1) {
            // Save answer logic here
            RadioButton selectedOption = findViewById(selectedId);
            String answer = selectedOption.getText().toString();
            // Save to local storage or prepare for submission
        }
    }

    private void submitQuiz() {
        new AlertDialog.Builder(this)
                .setTitle("Nộp bài thi")
                .setMessage("Bạn có chắc chắn muốn nộp bài không?")
                .setPositiveButton("Nộp bài", (dialog, which) -> {
                    saveCurrentAnswer();
                    finishQuiz();
                })
                .setNegativeButton("Tiếp tục", null)
                .show();
    }

    private void finishQuiz() {
        if (quizTimer != null) {
            quizTimer.cancel();
        }

        // Calculate final score and navigate to results
        Intent intent = new Intent(this, StudyProgressActivity.class);
        intent.putExtra("quizCompleted", true);
        intent.putExtra("score", score);
        intent.putExtra("totalQuestions", totalQuestions);
        startActivity(intent);
        finish();
    }

    private void showHint() {
        Toast.makeText(this, "Gợi ý: Đọc kỹ câu hỏi và loại trừ các đáp án sai", Toast.LENGTH_LONG).show();
    }

    private void startTimer() {
        quizTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                Toast.makeText(QuizActivity.this, "Hết thời gian!", Toast.LENGTH_SHORT).show();
                finishQuiz();
            }
        }.start();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            new AlertDialog.Builder(this)
                    .setTitle("Thoát quiz")
                    .setMessage("Bạn có chắc chắn muốn thoát? Tiến độ sẽ không được lưu.")
                    .setPositiveButton("Thoát", (dialog, which) -> {
                        if (quizTimer != null) {
                            quizTimer.cancel();
                        }
                        finish();
                    })
                    .setNegativeButton("Ở lại", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (quizTimer != null) {
            quizTimer.cancel();
        }
    }
}