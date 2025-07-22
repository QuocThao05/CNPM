package com.vantinh.tienganh;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vantinh.tienganh.models.Quiz;
import com.vantinh.tienganh.models.QuizQuestion;
import com.vantinh.tienganh.utils.QuestionViewHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateQuizActivity extends AppCompatActivity implements QuestionViewHelper.OnQuestionCountChangeListener {

    private Toolbar toolbar;
    private EditText etQuizTitle;
    private TextView tvCourseName, tvQuestionCount;
    private Button btnCreateQuiz, btnAddQuestion, btnRemoveQuestion;
    private LinearLayout llQuestionsContainer;
    private String courseId, courseName;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private QuestionViewHelper questionViewHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        initViews();
        setupToolbar();
        initFirebase();
        getCourseInfo();
        initQuestionHelper();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etQuizTitle = findViewById(R.id.et_quiz_title);
        tvCourseName = findViewById(R.id.tv_course_name);
        tvQuestionCount = findViewById(R.id.tv_question_count);
        btnCreateQuiz = findViewById(R.id.btn_create_quiz);
        btnAddQuestion = findViewById(R.id.btn_add_question);
        btnRemoveQuestion = findViewById(R.id.btn_remove_question);
        llQuestionsContainer = findViewById(R.id.ll_questions_container);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tạo bài kiểm tra");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void getCourseInfo() {
        courseId = getIntent().getStringExtra("courseId");
        courseName = getIntent().getStringExtra("courseName");

        if (courseName != null) {
            tvCourseName.setText("Khóa học: " + courseName);
        }
    }

    private void initQuestionHelper() {
        questionViewHelper = new QuestionViewHelper(this, llQuestionsContainer);
        questionViewHelper.setOnQuestionCountChangeListener(this);
        questionViewHelper.initializeWithDefaultQuestions();
    }

    private void setupClickListeners() {
        btnCreateQuiz.setOnClickListener(v -> createQuiz());

        btnAddQuestion.setOnClickListener(v -> {
            questionViewHelper.addQuestion();
            // Debug log
            android.util.Log.d("CreateQuiz", "Added question, total: " + questionViewHelper.getQuestionCount());
        });

        btnRemoveQuestion.setOnClickListener(v -> {
            int countBefore = questionViewHelper.getQuestionCount();
            questionViewHelper.removeLastQuestion();
            int countAfter = questionViewHelper.getQuestionCount();
            // Debug log
            android.util.Log.d("CreateQuiz", "Remove clicked - Before: " + countBefore + ", After: " + countAfter);
            Toast.makeText(this, "Đã xóa câu hỏi cuối", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onQuestionCountChanged(int count) {
        tvQuestionCount.setText("(" + count + " câu)");
        boolean canRemove = count > 2;
        btnRemoveQuestion.setEnabled(canRemove);

        // Debug log
        android.util.Log.d("CreateQuiz", "Question count changed: " + count + ", Can remove: " + canRemove);

        // Thay đổi màu nút để thể hiện trạng thái
        if (canRemove) {
            btnRemoveQuestion.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
            btnRemoveQuestion.setAlpha(1.0f);
        } else {
            btnRemoveQuestion.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
            btnRemoveQuestion.setAlpha(0.5f);
        }
    }

    private void createQuiz() {
        if (!validateInput()) {
            return;
        }

        String quizTitle = etQuizTitle.getText().toString().trim();
        List<QuizQuestion> questions = new ArrayList<>();

        // Create questions from dynamic views theo cấu trúc Firebase
        List<QuestionViewHelper.QuestionViewData> questionViews = questionViewHelper.getQuestionViews();
        for (int i = 0; i < questionViews.size(); i++) {
            QuestionViewHelper.QuestionViewData questionData = questionViews.get(i);

            // Lấy question (String)
            String questionText = questionData.etQuestion.getText().toString().trim();
            
            // Lấy options (Array)
            List<String> options = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                options.add(questionData.etOptions[j].getText().toString().trim());
            }

            // Lấy correctAnswers (Number)
            int correctAnswerIndex = getCorrectAnswerIndex(questionData);
            
            // Tạo QuizQuestion với 3 trường dữ liệu Firebase
            QuizQuestion question = new QuizQuestion();
            question.setQuestion(questionText);        // String
            question.setOptions(options);              // Array
            question.setCorrectAnswers(correctAnswerIndex); // Number
            
            questions.add(question);
            
            // Debug log để kiểm tra dữ liệu Firebase format
            android.util.Log.d("CreateQuiz", "Firebase Question " + (i+1) + ":");
            android.util.Log.d("CreateQuiz", "  question: " + questionText);
            android.util.Log.d("CreateQuiz", "  options: " + options.toString());
            android.util.Log.d("CreateQuiz", "  correctAnswers: " + correctAnswerIndex);
        }

        // Create quiz object với đầy đủ metadata
        Quiz quiz = new Quiz();
        quiz.setTitle(quizTitle);
        quiz.setCourseId(courseId);
        quiz.setCourseName(courseName);
        quiz.setTeacherId(mAuth.getCurrentUser().getUid());
        quiz.setQuestions(questions);
        quiz.setCreatedAt(new Date());
        quiz.setActive(true);

        // Log toàn bộ quiz data để debug
        android.util.Log.d("CreateQuiz", "Creating Quiz for Firebase:");
        android.util.Log.d("CreateQuiz", "  title: " + quizTitle);
        android.util.Log.d("CreateQuiz", "  courseId: " + courseId);
        android.util.Log.d("CreateQuiz", "  courseName: " + courseName);
        android.util.Log.d("CreateQuiz", "  questionsCount: " + questions.size());

        // Save to Firebase
        saveQuizToFirebase(quiz);
    }

    private boolean validateInput() {
        String quizTitle = etQuizTitle.getText().toString().trim();
        if (quizTitle.isEmpty()) {
            etQuizTitle.setError("Vui lòng nhập tên bài kiểm tra");
            return false;
        }

        List<QuestionViewHelper.QuestionViewData> questionViews = questionViewHelper.getQuestionViews();

        for (int i = 0; i < questionViews.size(); i++) {
            QuestionViewHelper.QuestionViewData questionData = questionViews.get(i);

            String questionText = questionData.etQuestion.getText().toString().trim();
            if (questionText.isEmpty()) {
                questionData.etQuestion.setError("Vui lòng nhập câu hỏi " + (i + 1));
                questionData.etQuestion.requestFocus();
                return false;
            }

            for (int j = 0; j < 4; j++) {
                String option = questionData.etOptions[j].getText().toString().trim();
                if (option.isEmpty()) {
                    questionData.etOptions[j].setError("Vui lòng nhập lựa chọn " + (char)('A' + j));
                    questionData.etOptions[j].requestFocus();
                    return false;
                }
            }

            // Check if any radio button is selected for this question
            if (questionData.rgCorrectAnswer.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Vui lòng chọn đáp án đúng cho câu hỏi " + (i + 1), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private int getCorrectAnswerIndex(QuestionViewHelper.QuestionViewData questionData) {
        RadioGroup radioGroup = questionData.rgCorrectAnswer;
        int checkedId = radioGroup.getCheckedRadioButtonId();

        android.util.Log.d("CreateQuiz", "Getting correct answer - checkedId: " + checkedId);

        if (checkedId == -1) {
            android.util.Log.d("CreateQuiz", "No option selected");
            return -1;
        }

        // Lấy trực tiếp RadioButton từ RadioGroup thay vì từ questionView
        RadioButton rbA = questionData.questionView.findViewById(R.id.rb_option_a);
        RadioButton rbB = questionData.questionView.findViewById(R.id.rb_option_b);
        RadioButton rbC = questionData.questionView.findViewById(R.id.rb_option_c);
        RadioButton rbD = questionData.questionView.findViewById(R.id.rb_option_d);

        android.util.Log.d("CreateQuiz", "RadioButton IDs - A:" + (rbA != null ? rbA.getId() : "null") +
                                        ", B:" + (rbB != null ? rbB.getId() : "null") +
                                        ", C:" + (rbC != null ? rbC.getId() : "null") +
                                        ", D:" + (rbD != null ? rbD.getId() : "null"));

        if (rbA != null && checkedId == rbA.getId()) {
            android.util.Log.d("CreateQuiz", "Selected option A (index 0)");
            return 0;
        }
        if (rbB != null && checkedId == rbB.getId()) {
            android.util.Log.d("CreateQuiz", "Selected option B (index 1)");
            return 1;
        }
        if (rbC != null && checkedId == rbC.getId()) {
            android.util.Log.d("CreateQuiz", "Selected option C (index 2)");
            return 2;
        }
        if (rbD != null && checkedId == rbD.getId()) {
            android.util.Log.d("CreateQuiz", "Selected option D (index 3)");
            return 3;
        }

        android.util.Log.e("CreateQuiz", "Could not find matching RadioButton for checkedId: " + checkedId);
        return -1;
    }

    private void saveQuizToFirebase(Quiz quiz) {
        btnCreateQuiz.setEnabled(false);
        btnCreateQuiz.setText("Đang tạo...");

        android.util.Log.d("CreateQuiz", "Starting Firebase save...");

        db.collection("quizzes")
                .add(quiz)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    android.util.Log.d("CreateQuiz", "Quiz saved successfully with ID: " + documentId);

                    // Log Firebase structure để verify
                    android.util.Log.d("CreateQuiz", "Firebase Document Structure:");
                    android.util.Log.d("CreateQuiz", "Collection: quizzes");
                    android.util.Log.d("CreateQuiz", "Document ID: " + documentId);
                    android.util.Log.d("CreateQuiz", "Fields saved:");
                    android.util.Log.d("CreateQuiz", "  - title: " + quiz.getTitle());
                    android.util.Log.d("CreateQuiz", "  - courseId: " + quiz.getCourseId());
                    android.util.Log.d("CreateQuiz", "  - courseName: " + quiz.getCourseName());
                    android.util.Log.d("CreateQuiz", "  - teacherId: " + quiz.getTeacherId());
                    android.util.Log.d("CreateQuiz", "  - questions: array with " + quiz.getQuestions().size() + " items");
                    android.util.Log.d("CreateQuiz", "  - createdAt: " + quiz.getCreatedAt());
                    android.util.Log.d("CreateQuiz", "  - active: " + quiz.isActive());

                    // Log each question structure
                    for (int i = 0; i < quiz.getQuestions().size(); i++) {
                        QuizQuestion q = quiz.getQuestions().get(i);
                        android.util.Log.d("CreateQuiz", "  Question " + (i+1) + " structure:");
                        android.util.Log.d("CreateQuiz", "    question: \"" + q.getQuestion() + "\"");
                        android.util.Log.d("CreateQuiz", "    options: " + q.getOptions().toString());
                        android.util.Log.d("CreateQuiz", "    correctAnswers: " + q.getCorrectAnswers());
                    }

                    Toast.makeText(this, "Tạo bài kiểm tra thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CreateQuiz", "Firebase save failed: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi tạo bài kiểm tra: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnCreateQuiz.setEnabled(true);
                    btnCreateQuiz.setText("Tạo bài kiểm tra");
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
