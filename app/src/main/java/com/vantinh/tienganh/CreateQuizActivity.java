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
import com.vantinh.tienganh.models.TestQuestion;
import com.vantinh.tienganh.models.SimpleTestQuestion;
import com.vantinh.tienganh.utils.QuestionViewHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateQuizActivity extends AppCompatActivity implements QuestionViewHelper.OnQuestionCountChangeListener {

    private Toolbar toolbar;
    private EditText etQuizTitle;
    private TextView tvCourseName, tvQuestionCount;
    private Button btnCreateQuiz, btnAddQuestion, btnRemoveQuestion, btnDebugCreate;
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
        btnDebugCreate = findViewById(R.id.btn_debug_create);
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

        btnDebugCreate.setOnClickListener(v -> createDebugSampleData());
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

        // Debug: Bắt đầu quá trình tạo bài kiểm tra
        android.util.Log.d("CreateQuiz", "=== BẮT ĐẦU TẠO BÁI KIỂM TRA ===");
        android.util.Log.d("CreateQuiz", "Course ID: " + courseId);
        android.util.Log.d("CreateQuiz", "Course Name: " + courseName);

        List<SimpleTestQuestion> simpleTestQuestions = new ArrayList<>();

        // Tạo từng câu hỏi theo cấu trúc mới chỉ với 3 trường
        List<QuestionViewHelper.QuestionViewData> questionViews = questionViewHelper.getQuestionViews();
        android.util.Log.d("CreateQuiz", "Số câu hỏi cần tạo: " + questionViews.size());

        for (int i = 0; i < questionViews.size(); i++) {
            QuestionViewHelper.QuestionViewData questionData = questionViews.get(i);

            // 1. Lấy question (String) - nội dung câu hỏi
            String questionText = questionData.etQuestion.getText().toString().trim();
            
            // 2. Lấy correctAnswer (Array) - 4 đáp án A,B,C,D
            List<String> correctAnswerArray = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                String optionText = questionData.etOptions[j].getText().toString().trim();
                correctAnswerArray.add(optionText);
                android.util.Log.d("CreateQuiz", "  Option " + (char)('A' + j) + ": " + optionText);
            }

            // 3. Lấy options (Number) - index của đáp án đúng (0-3)
            int correctAnswerIndex = getCorrectAnswerIndex(questionData);
            
            // Tạo SimpleTestQuestion với cấu trúc ĐÚNG
            SimpleTestQuestion simpleQuestion = new SimpleTestQuestion();
            simpleQuestion.setCourseId(courseId);                    // String - ID khóa học
            simpleQuestion.setOptions(correctAnswerArray);          // SỬA: options là array - 4 đáp án
            simpleQuestion.setCorrectAnswer(correctAnswerIndex);    // SỬA: correctAnswer là number - index đáp án đúng
            simpleQuestion.setQuestion(questionText);              // String - câu hỏi

            simpleTestQuestions.add(simpleQuestion);

            // Debug log chi tiết cho từng câu hỏi với courseId
            android.util.Log.d("CreateQuiz", "--- QUESTION " + (i+1) + " DEBUG ---");
            android.util.Log.d("CreateQuiz", "courseId (String): \"" + courseId + "\"");
            android.util.Log.d("CreateQuiz", "question (String): \"" + questionText + "\"");
            android.util.Log.d("CreateQuiz", "correctAnswer (Array): " + correctAnswerArray.toString());
            android.util.Log.d("CreateQuiz", "options (Number): " + correctAnswerIndex);
            android.util.Log.d("CreateQuiz", "Đáp án đúng là: " +
                (correctAnswerIndex >= 0 && correctAnswerIndex < correctAnswerArray.size() ?
                 correctAnswerArray.get(correctAnswerIndex) : "KHÔNG XÁC ĐỊNH"));
            android.util.Log.d("CreateQuiz", "SimpleTestQuestion Object: " + simpleQuestion.toString());
        }

        // Debug tổng kết trước khi lưu
        android.util.Log.d("CreateQuiz", "=== TỔNG KẾT TRƯỚC KHI LƯU ===");
        android.util.Log.d("CreateQuiz", "Tổng số câu hỏi: " + simpleTestQuestions.size());
        android.util.Log.d("CreateQuiz", "Collection đích: test");
        android.util.Log.d("CreateQuiz", "Cấu trúc mỗi document:");
        android.util.Log.d("CreateQuiz", "  - correctAnswer: Array[4] (4 đáp án)");
        android.util.Log.d("CreateQuiz", "  - options: Number (index đáp án đúng)");
        android.util.Log.d("CreateQuiz", "  - question: String (câu hỏi)");

        // Lưu từng câu hỏi như một document riêng trong collection "test"
        saveSimpleTestQuestionsToFirebase(simpleTestQuestions);
    }

    private void createDebugSampleData() {
        // Tạo dữ liệu mẫu để kiểm tra
        android.util.Log.d("CreateQuiz", "=== TẠO DỮ LIỆU MẪU ===");
        android.util.Log.d("CreateQuiz", "CourseId for sample data: " + courseId);

        // Tạo một danh sách câu hỏi mẫu
        List<SimpleTestQuestion> sampleQuestions = new ArrayList<>();

        // Tạo 5 câu hỏi mẫu với đáp án ngẫu nhiên
        for (int i = 1; i <= 5; i++) {
            SimpleTestQuestion question = new SimpleTestQuestion();
            question.setCourseId(courseId);  // Thêm courseId cho dữ liệu mẫu
            question.setQuestion("Câu hỏi mẫu " + i + " - Khóa học: " + courseName);

            List<String> answers = new ArrayList<>();
            answers.add("Đáp án A - Câu hỏi " + i);
            answers.add("Đáp án B - Câu hỏi " + i);
            answers.add("Đáp án C - Câu hỏi " + i);
            answers.add("Đáp án D - Câu hỏi " + i);
            question.setOptions(answers);          // SỬA: options là array

            // Chọn ngẫu nhiên một đáp án đúng
            int correctIndex = (int) (Math.random() * 4);
            question.setCorrectAnswer(correctIndex);  // SỬA: correctAnswer là number

            sampleQuestions.add(question);

            android.util.Log.d("CreateQuiz", "Câu hỏi mẫu " + i + ": " + question.toString());
        }

        // Lưu vào Firebase
        saveSimpleTestQuestionsToFirebase(sampleQuestions);
    }

    private boolean validateInput() {
        // Bỏ validation tên bài kiểm tra vì không cần thiết với cấu trúc mới
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

        // Phương pháp đơn giản: Kiểm tra trực tiếp từng RadioButton theo ID gốc
        RadioButton rbA = questionData.questionView.findViewById(R.id.rb_option_a);
        RadioButton rbB = questionData.questionView.findViewById(R.id.rb_option_b);
        RadioButton rbC = questionData.questionView.findViewById(R.id.rb_option_c);
        RadioButton rbD = questionData.questionView.findViewById(R.id.rb_option_d);

        android.util.Log.d("CreateQuiz", "RadioButton states - A:" + (rbA != null ? rbA.isChecked() : "null") +
                                        ", B:" + (rbB != null ? rbB.isChecked() : "null") +
                                        ", C:" + (rbC != null ? rbC.isChecked() : "null") +
                                        ", D:" + (rbD != null ? rbD.isChecked() : "null"));

        if (rbA != null && rbA.isChecked()) {
            android.util.Log.d("CreateQuiz", "Option A selected (index 0)");
            return 0;
        }
        if (rbB != null && rbB.isChecked()) {
            android.util.Log.d("CreateQuiz", "Option B selected (index 1)");
            return 1;
        }
        if (rbC != null && rbC.isChecked()) {
            android.util.Log.d("CreateQuiz", "Option C selected (index 2)");
            return 2;
        }
        if (rbD != null && rbD.isChecked()) {
            android.util.Log.d("CreateQuiz", "Option D selected (index 3)");
            return 3;
        }

        // Nếu vẫn không tìm được, thử kiểm tra bằng ID được sinh ra động
        android.util.Log.d("CreateQuiz", "Fallback: Checking by generated IDs");
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View child = radioGroup.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                if (rb.isChecked()) {
                    android.util.Log.d("CreateQuiz", "Found checked RadioButton at child position " + i);
                    return i;
                }
            }
        }

        android.util.Log.e("CreateQuiz", "Could not determine correct answer index for checkedId: " + checkedId);
        return -1;
    }

    private void saveTestQuestionsToFirebase(List<TestQuestion> testQuestions) {
        btnCreateQuiz.setEnabled(false);
        btnCreateQuiz.setText("Đang tạo...");

        android.util.Log.d("CreateQuiz", "Starting Firebase save for test questions...");

        // Lưu từng câu hỏi trong danh sách như một document riêng biệt
        for (TestQuestion question : testQuestions) {
            db.collection("test")  // Thay đổi từ "quizzes" sang "test"
                    .add(question)
                    .addOnSuccessListener(documentReference -> {
                        String documentId = documentReference.getId();
                        android.util.Log.d("CreateQuiz", "Test question saved successfully with ID: " + documentId);

                        // Log từng câu hỏi đã lưu
                        android.util.Log.d("CreateQuiz", "Saved Test Question:");
                        android.util.Log.d("CreateQuiz", "  ID: " + documentId);
                        android.util.Log.d("CreateQuiz", "  courseId: " + question.getCourseId());
                        android.util.Log.d("CreateQuiz", "  teacherId: " + question.getTeacherId());
                        android.util.Log.d("CreateQuiz", "  correctAnswer: " + question.getCorrectAnswer());
                        android.util.Log.d("CreateQuiz", "  options: " + question.getOptions());
                        android.util.Log.d("CreateQuiz", "  question: " + question.getQuestion());

                        // Hiển thị thông báo thành công cho lần lưu cuối cùng
                        if (question == testQuestions.get(testQuestions.size() - 1)) {
                            Toast.makeText(this, "Tạo bài kiểm tra thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("CreateQuiz", "Firebase save failed: " + e.getMessage(), e);
                        Toast.makeText(this, "Lỗi khi tạo bài kiểm tra: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnCreateQuiz.setEnabled(true);
                        btnCreateQuiz.setText("Tạo bài kiểm tra");
                    });
        }
    }

    private void saveSimpleTestQuestionsToFirebase(List<SimpleTestQuestion> simpleTestQuestions) {
        btnCreateQuiz.setEnabled(false);
        btnCreateQuiz.setText("Đang tạo...");

        android.util.Log.d("CreateQuiz", "Starting Firebase save for simple test questions...");

        // Lưu từng câu hỏi trong danh sách như một document riêng biệt
        for (SimpleTestQuestion question : simpleTestQuestions) {
            db.collection("test")  // Thay đổi từ "quizzes" sang "test"
                    .add(question)
                    .addOnSuccessListener(documentReference -> {
                        String documentId = documentReference.getId();
                        android.util.Log.d("CreateQuiz", "Simple test question saved successfully with ID: " + documentId);

                        // Log từng câu hỏi đã lưu
                        android.util.Log.d("CreateQuiz", "Saved Simple Test Question:");
                        android.util.Log.d("CreateQuiz", "  ID: " + documentId);
                        android.util.Log.d("CreateQuiz", "  correctAnswer: " + question.getCorrectAnswer());
                        android.util.Log.d("CreateQuiz", "  options: " + question.getOptions());
                        android.util.Log.d("CreateQuiz", "  question: " + question.getQuestion());

                        // Hiển thị thông báo thành công cho lần lưu cuối cùng
                        if (question == simpleTestQuestions.get(simpleTestQuestions.size() - 1)) {
                            Toast.makeText(this, "Tạo bài kiểm tra thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("CreateQuiz", "Firebase save failed: " + e.getMessage(), e);
                        Toast.makeText(this, "Lỗi khi tạo bài kiểm tra: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnCreateQuiz.setEnabled(true);
                        btnCreateQuiz.setText("Tạo bài kiểm tra");
                    });
        }
    }

    private void saveQuizQuestionsToFirebase(List<QuizQuestion> quizQuestions) {
        btnCreateQuiz.setEnabled(false);
        btnCreateQuiz.setText("Đang tạo...");

        android.util.Log.d("CreateQuiz", "Starting Firebase save for quiz questions (old structure)...");

        // Lưu từng câu hỏi trong danh sách như một document riêng biệt
        for (QuizQuestion question : quizQuestions) {
            db.collection("quizzes")  // Lưu vào collection "quizzes" với cấu trúc cũ
                    .add(question)
                    .addOnSuccessListener(documentReference -> {
                        String documentId = documentReference.getId();
                        android.util.Log.d("CreateQuiz", "Quiz question (old structure) saved successfully with ID: " + documentId);

                        // Hiển thị thông báo thành công cho lần lưu cuối cùng
                        if (question == quizQuestions.get(quizQuestions.size() - 1)) {
                            Toast.makeText(this, "Tạo bài kiểm tra thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("CreateQuiz", "Firebase save failed: " + e.getMessage(), e);
                        Toast.makeText(this, "Lỗi khi tạo bài kiểm tra: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnCreateQuiz.setEnabled(true);
                        btnCreateQuiz.setText("Tạo bài kiểm tra");
                    });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
