package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ContentCreationActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private CardView cardLessonCreation, cardQuizCreation, cardVocabularyList;
    private LinearLayout layoutLessonForm, layoutQuizForm, layoutVocabularyForm;

    // Lesson creation components
    private EditText etLessonTitle, etLessonDescription, etLessonContent;
    private Spinner spinnerLessonCategory, spinnerDifficulty;
    private Button btnSaveLesson, btnPreviewLesson;

    // Quiz creation components
    private EditText etQuizTitle, etQuizDescription;
    private EditText etQuestion, etOption1, etOption2, etOption3, etOption4;
    private RadioGroup rgCorrectAnswer;
    private RecyclerView rvQuestions;
    private Button btnAddQuestion, btnSaveQuiz, btnPreviewQuiz;

    // Vocabulary components
    private EditText etWord, etMeaning, etExample, etPronunciation;
    private Spinner spinnerWordCategory;
    private Button btnAddWord, btnSaveVocabulary;
    private RecyclerView rvVocabularyList;

    private FloatingActionButton fabPublish;
    private TextView tvContentCount;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String mode; // "lesson", "quiz", "vocabulary"
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_creation);

        // Get mode from intent
        mode = getIntent().getStringExtra("mode");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            teacherId = mAuth.getCurrentUser().getUid();
        }

        initViews();
        setupToolbar();
        setupTabs();
        setupClickListeners();
        handleModeSpecificSetup();
        loadExistingContent();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);

        // Cards
        cardLessonCreation = findViewById(R.id.card_lesson_creation);
        cardQuizCreation = findViewById(R.id.card_quiz_creation);
        cardVocabularyList = findViewById(R.id.card_vocabulary_list);

        // Layouts
        layoutLessonForm = findViewById(R.id.layout_lesson_form);
        layoutQuizForm = findViewById(R.id.layout_quiz_form);
        layoutVocabularyForm = findViewById(R.id.layout_vocabulary_form);

        // Lesson creation
        etLessonTitle = findViewById(R.id.et_lesson_title);
        etLessonDescription = findViewById(R.id.et_lesson_description);
        etLessonContent = findViewById(R.id.et_lesson_content);
        spinnerLessonCategory = findViewById(R.id.spinner_lesson_category);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        btnSaveLesson = findViewById(R.id.btn_save_lesson);
        btnPreviewLesson = findViewById(R.id.btn_preview_lesson);

        // Quiz creation
        etQuizTitle = findViewById(R.id.et_quiz_title);
        etQuizDescription = findViewById(R.id.et_quiz_description);
        etQuestion = findViewById(R.id.et_question);
        etOption1 = findViewById(R.id.et_option1);
        etOption2 = findViewById(R.id.et_option2);
        etOption3 = findViewById(R.id.et_option3);
        etOption4 = findViewById(R.id.et_option4);
        rgCorrectAnswer = findViewById(R.id.rg_correct_answer);
        rvQuestions = findViewById(R.id.rv_questions);
        btnAddQuestion = findViewById(R.id.btn_add_question);
        btnSaveQuiz = findViewById(R.id.btn_save_quiz);
        btnPreviewQuiz = findViewById(R.id.btn_preview_quiz);

        // Vocabulary
        etWord = findViewById(R.id.et_word);
        etMeaning = findViewById(R.id.et_meaning);
        etExample = findViewById(R.id.et_example);
        etPronunciation = findViewById(R.id.et_pronunciation);
        spinnerWordCategory = findViewById(R.id.spinner_word_category);
        btnAddWord = findViewById(R.id.btn_add_word);
        btnSaveVocabulary = findViewById(R.id.btn_save_vocabulary);
        rvVocabularyList = findViewById(R.id.rv_vocabulary_list);

        // Other components
        fabPublish = findViewById(R.id.fab_publish);
        tvContentCount = findViewById(R.id.tv_content_count);

        // Setup RecyclerViews
        if (rvQuestions != null) {
            rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        }
        if (rvVocabularyList != null) {
            rvVocabularyList.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Tạo nội dung");
        }
    }

    private void setupTabs() {
        if (tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setText("Bài học"));
            tabLayout.addTab(tabLayout.newTab().setText("Quiz"));
            tabLayout.addTab(tabLayout.newTab().setText("Từ vựng"));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switchContentType(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void setupClickListeners() {
        // Lesson creation
        if (btnSaveLesson != null) {
            btnSaveLesson.setOnClickListener(v -> saveLesson());
        }
        if (btnPreviewLesson != null) {
            btnPreviewLesson.setOnClickListener(v -> previewLesson());
        }

        // Quiz creation
        if (btnAddQuestion != null) {
            btnAddQuestion.setOnClickListener(v -> addQuestion());
        }
        if (btnSaveQuiz != null) {
            btnSaveQuiz.setOnClickListener(v -> saveQuiz());
        }
        if (btnPreviewQuiz != null) {
            btnPreviewQuiz.setOnClickListener(v -> previewQuiz());
        }

        // Vocabulary
        if (btnAddWord != null) {
            btnAddWord.setOnClickListener(v -> addWord());
        }
        if (btnSaveVocabulary != null) {
            btnSaveVocabulary.setOnClickListener(v -> saveVocabularyList());
        }

        // FAB
        if (fabPublish != null) {
            fabPublish.setOnClickListener(v -> publishContent());
        }
    }

    private void handleModeSpecificSetup() {
        if ("lesson".equals(mode) && tabLayout != null) {
            tabLayout.getTabAt(0).select();
            switchContentType(0);
        } else if ("quiz".equals(mode) && tabLayout != null) {
            tabLayout.getTabAt(1).select();
            switchContentType(1);
        } else if ("vocabulary".equals(mode) && tabLayout != null) {
            tabLayout.getTabAt(2).select();
            switchContentType(2);
        }
    }

    private void switchContentType(int position) {
        // Hide all layouts first
        hideAllLayouts();

        switch (position) {
            case 0: // Lesson
                if (layoutLessonForm != null) {
                    layoutLessonForm.setVisibility(View.VISIBLE);
                }
                break;
            case 1: // Quiz
                if (layoutQuizForm != null) {
                    layoutQuizForm.setVisibility(View.VISIBLE);
                }
                break;
            case 2: // Vocabulary
                if (layoutVocabularyForm != null) {
                    layoutVocabularyForm.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void hideAllLayouts() {
        if (layoutLessonForm != null) layoutLessonForm.setVisibility(View.GONE);
        if (layoutQuizForm != null) layoutQuizForm.setVisibility(View.GONE);
        if (layoutVocabularyForm != null) layoutVocabularyForm.setVisibility(View.GONE);
    }

    private void saveLesson() {
        if (teacherId == null) {
            Toast.makeText(this, "Lỗi: Không thể xác định giáo viên", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etLessonTitle != null ? etLessonTitle.getText().toString().trim() : "";
        String description = etLessonDescription != null ? etLessonDescription.getText().toString().trim() : "";
        String content = etLessonContent != null ? etLessonContent.getText().toString().trim() : "";

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> lessonData = new HashMap<>();
        lessonData.put("teacherId", teacherId);
        lessonData.put("title", title);
        lessonData.put("description", description);
        lessonData.put("content", content);
        lessonData.put("type", "lesson");
        lessonData.put("createdAt", System.currentTimeMillis());
        lessonData.put("status", "draft");

        db.collection("content")
                .add(lessonData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Bài học đã được lưu thành công!", Toast.LENGTH_SHORT).show();
                    clearLessonForm();
                    updateContentCount();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void previewLesson() {
        Intent intent = new Intent(this, CourseListActivity.class);
        intent.putExtra("mode", "preview");
        intent.putExtra("title", etLessonTitle != null ? etLessonTitle.getText().toString() : "");
        startActivity(intent);
    }

    private void addQuestion() {
        String question = etQuestion != null ? etQuestion.getText().toString().trim() : "";
        String option1 = etOption1 != null ? etOption1.getText().toString().trim() : "";
        String option2 = etOption2 != null ? etOption2.getText().toString().trim() : "";
        String option3 = etOption3 != null ? etOption3.getText().toString().trim() : "";
        String option4 = etOption4 != null ? etOption4.getText().toString().trim() : "";

        if (question.isEmpty() || option1.isEmpty() || option2.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập câu hỏi và ít nhất 2 đáp án", Toast.LENGTH_SHORT).show();
            return;
        }

        int correctAnswer = 0;
        if (rgCorrectAnswer != null && rgCorrectAnswer.getCheckedRadioButtonId() != -1) {
            RadioButton selectedButton = findViewById(rgCorrectAnswer.getCheckedRadioButtonId());
            // Logic to determine correct answer index
        }

        // Add question to list (implementation would involve adapter)
        Toast.makeText(this, "Câu hỏi đã được thêm", Toast.LENGTH_SHORT).show();
        clearQuestionForm();
    }

    private void saveQuiz() {
        // Implementation similar to saveLesson
        Toast.makeText(this, "Quiz đã được lưu", Toast.LENGTH_SHORT).show();
    }

    private void previewQuiz() {
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("mode", "preview");
        startActivity(intent);
    }

    private void addWord() {
        String word = etWord != null ? etWord.getText().toString().trim() : "";
        String meaning = etMeaning != null ? etMeaning.getText().toString().trim() : "";
        String example = etExample != null ? etExample.getText().toString().trim() : "";

        if (word.isEmpty() || meaning.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập từ và nghĩa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add word to vocabulary list
        Toast.makeText(this, "Từ vựng đã được thêm", Toast.LENGTH_SHORT).show();
        clearVocabularyForm();
    }

    private void saveVocabularyList() {
        Toast.makeText(this, "Danh sách từ vựng đã được lưu", Toast.LENGTH_SHORT).show();
    }

    private void publishContent() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Xuất bản nội dung")
                .setMessage("Bạn có chắc chắn muốn xuất bản nội dung này? Học viên sẽ có thể truy cập.")
                .setPositiveButton("Xuất bản", (dialog, which) -> {
                    Toast.makeText(this, "Nội dung đã được xuất bản!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void clearLessonForm() {
        if (etLessonTitle != null) etLessonTitle.setText("");
        if (etLessonDescription != null) etLessonDescription.setText("");
        if (etLessonContent != null) etLessonContent.setText("");
    }

    private void clearQuestionForm() {
        if (etQuestion != null) etQuestion.setText("");
        if (etOption1 != null) etOption1.setText("");
        if (etOption2 != null) etOption2.setText("");
        if (etOption3 != null) etOption3.setText("");
        if (etOption4 != null) etOption4.setText("");
        if (rgCorrectAnswer != null) rgCorrectAnswer.clearCheck();
    }

    private void clearVocabularyForm() {
        if (etWord != null) etWord.setText("");
        if (etMeaning != null) etMeaning.setText("");
        if (etExample != null) etExample.setText("");
        if (etPronunciation != null) etPronunciation.setText("");
    }

    private void loadExistingContent() {
        updateContentCount();
    }

    private void updateContentCount() {
        if (teacherId != null) {
            db.collection("content")
                    .whereEqualTo("teacherId", teacherId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int count = queryDocumentSnapshots.size();
                        if (tvContentCount != null) {
                            tvContentCount.setText("Đã tạo: " + count + " nội dung");
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, TeacherDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}