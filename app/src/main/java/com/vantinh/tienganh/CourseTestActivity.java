package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseTestActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvQuestionNumber;
    private TextView tvQuestion;
    private RadioGroup radioGroupOptions;
    private Button btnNext;
    private Button btnPrevious;
    private Button btnSubmit;
    private LinearLayout layoutTestComplete;
    private TextView tvFinalScore;
    private Button btnBackToCourse;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String courseId;
    private String courseName;
    private String studentId;
    private List<TestQuestion> testQuestions;
    private int currentQuestionIndex = 0;
    private Map<Integer, Integer> userAnswers;
    private boolean testCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_test);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        testQuestions = new ArrayList<>();
        userAnswers = new HashMap<>();

        initViews();
        getIntentData();
        setupToolbar();
        setupClickListeners();
        loadTestData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvQuestionNumber = findViewById(R.id.tv_question_number);
        tvQuestion = findViewById(R.id.tv_question);
        radioGroupOptions = findViewById(R.id.radio_group_options);
        btnNext = findViewById(R.id.btn_next);
        btnPrevious = findViewById(R.id.btn_previous);
        btnSubmit = findViewById(R.id.btn_submit);
        layoutTestComplete = findViewById(R.id.layout_test_complete);
        tvFinalScore = findViewById(R.id.tv_final_score);
        btnBackToCourse = findViewById(R.id.btn_back_to_course);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        courseId = intent.getStringExtra("courseId");
        courseName = intent.getStringExtra("courseTitle"); // Sửa từ "courseName" thành "courseTitle"

        android.util.Log.d("CourseTestActivity", "Intent data received:");
        android.util.Log.d("CourseTestActivity", "CourseId: " + courseId);
        android.util.Log.d("CourseTestActivity", "CourseName: " + courseName);

        if (courseId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy studentId từ Firebase Auth và users collection
        getCurrentStudentId();
    }

    private void getCurrentStudentId() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String firebaseUid = mAuth.getCurrentUser().getUid();
        android.util.Log.d("CourseTestActivity", "Getting student info for UID: " + firebaseUid);

        db.collection("users").document(firebaseUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        studentId = documentSnapshot.getString("id");
                        String userRole = documentSnapshot.getString("role");

                        android.util.Log.d("CourseTestActivity", "Student ID: " + studentId);
                        android.util.Log.d("CourseTestActivity", "User Role: " + userRole);

                        if (!"student".equals(userRole)) {
                            Toast.makeText(this, "Chỉ học viên mới có thể làm bài kiểm tra", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        if (studentId != null) {
                            // Bây giờ đã có đủ thông tin, load dữ liệu test
                            loadTestData();
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
                    android.util.Log.e("CourseTestActivity", "Error loading user info", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bài kiểm tra: " + (courseName != null ? courseName : ""));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (currentQuestionIndex < testQuestions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                displayQuestion();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            saveCurrentAnswer();
            submitTest();
        });

        btnBackToCourse.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadTestData() {
        if (courseId == null) return;

        android.util.Log.d("CourseTestActivity", "Loading test data for course: " + courseId);

        // Load dữ liệu test từ collection "test" riêng biệt (không phải từ trường "test" trong course)
        db.collection("test")
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        testQuestions.clear();
                        android.util.Log.d("CourseTestActivity", "Found " + queryDocumentSnapshots.size() + " test questions");

                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                TestQuestion question = new TestQuestion();

                                String questionText = doc.getString("question");
                                question.setQuestion(questionText);

                                // Xử lý options và correctAnswer với validation
                                Object optionsObj = doc.get("options");
                                Object correctAnswerObj = doc.get("correctAnswer");

                                android.util.Log.d("CourseTestActivity", "Processing question: " + questionText);
                                android.util.Log.d("CourseTestActivity", "Options type: " + (optionsObj != null ? optionsObj.getClass().getSimpleName() : "null"));
                                android.util.Log.d("CourseTestActivity", "CorrectAnswer type: " + (correctAnswerObj != null ? correctAnswerObj.getClass().getSimpleName() : "null"));

                                // Sử dụng logic để khắc phục dữ liệu bị nhầm lẫn
                                List<String> options = null;
                                Integer correctAnswer = null;

                                // Case 1: options là ArrayList và correctAnswer là Number (đúng)
                                if (optionsObj instanceof List && correctAnswerObj instanceof Number) {
                                    options = (List<String>) optionsObj;
                                    correctAnswer = ((Number) correctAnswerObj).intValue();
                                    android.util.Log.d("CourseTestActivity", "Case 1: Correct data structure");
                                }
                                // Case 2: options là Number và correctAnswer là ArrayList (bị nhầm - cần hoán đổi)
                                else if (optionsObj instanceof Number && correctAnswerObj instanceof List) {
                                    options = (List<String>) correctAnswerObj; // Hoán đổi
                                    correctAnswer = ((Number) optionsObj).intValue(); // Hoán đổi
                                    android.util.Log.d("CourseTestActivity", "Case 2: Swapped data - correcting...");
                                }
                                // Case 3: options là String và correctAnswer là Number
                                else if (optionsObj instanceof String && correctAnswerObj instanceof Number) {
                                    // Tạo options từ String (có thể là JSON)
                                    String optionsStr = (String) optionsObj;
                                    try {
                                        // Thử parse nếu là JSON array
                                        if (optionsStr.startsWith("[") && optionsStr.endsWith("]")) {
                                            optionsStr = optionsStr.substring(1, optionsStr.length() - 1);
                                            String[] optionArray = optionsStr.split(",");
                                            options = new ArrayList<>();
                                            for (String opt : optionArray) {
                                                options.add(opt.trim().replace("\"", ""));
                                            }
                                        } else {
                                            // Single option
                                            options = new ArrayList<>();
                                            options.add(optionsStr);
                                        }
                                    } catch (Exception e) {
                                        android.util.Log.e("CourseTestActivity", "Error parsing options string", e);
                                        options = new ArrayList<>();
                                        options.add(optionsStr);
                                    }
                                    correctAnswer = ((Number) correctAnswerObj).intValue();
                                    android.util.Log.d("CourseTestActivity", "Case 3: String options converted");
                                }

                                // Validation
                                if (options != null && !options.isEmpty() && correctAnswer != null) {
                                    question.setOptions(options);
                                    question.setCorrectAnswer(correctAnswer);
                                    testQuestions.add(question);

                                    android.util.Log.d("CourseTestActivity", "Added question: " + questionText);
                                    android.util.Log.d("CourseTestActivity", "Options: " + options);
                                    android.util.Log.d("CourseTestActivity", "Correct answer: " + correctAnswer);
                                } else {
                                    android.util.Log.w("CourseTestActivity", "Skipping invalid question: " + questionText);
                                }

                            } catch (Exception e) {
                                android.util.Log.e("CourseTestActivity", "Error processing question document: " + doc.getId(), e);
                            }
                        }

                        if (!testQuestions.isEmpty()) {
                            android.util.Log.d("CourseTestActivity", "Successfully loaded " + testQuestions.size() + " questions");
                            startTest();
                        } else {
                            Toast.makeText(this, "Không có câu hỏi hợp lệ trong bài kiểm tra", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Khóa học này chưa có bài kiểm tra", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CourseTestActivity", "Error loading test data", e);
                    Toast.makeText(this, "Lỗi tải bài kiểm tra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void startTest() {
        android.util.Log.d("CourseTest", "Starting test with " + testQuestions.size() + " questions");
        currentQuestionIndex = 0;
        userAnswers.clear();
        displayQuestion();
    }

    private void displayQuestion() {
        if (testQuestions.isEmpty() || currentQuestionIndex >= testQuestions.size()) {
            return;
        }

        TestQuestion currentQuestion = testQuestions.get(currentQuestionIndex);

        // Update question number and text
        tvQuestionNumber.setText("Câu " + (currentQuestionIndex + 1) + "/" + testQuestions.size());
        tvQuestion.setText(currentQuestion.getQuestion());

        // Clear and populate options
        radioGroupOptions.removeAllViews();

        List<String> options = currentQuestion.getOptions();
        if (options != null) {
            for (int i = 0; i < options.size(); i++) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setId(i);
                radioButton.setText(options.get(i));
                radioButton.setTextSize(16);
                radioButton.setPadding(8, 8, 8, 8);
                radioGroupOptions.addView(radioButton);
            }
        }

        // Restore previous answer if exists
        Integer previousAnswer = userAnswers.get(currentQuestionIndex);
        if (previousAnswer != null && previousAnswer < radioGroupOptions.getChildCount()) {
            ((RadioButton) radioGroupOptions.getChildAt(previousAnswer)).setChecked(true);
        } else {
            radioGroupOptions.clearCheck();
        }

        // Update button visibility
        btnPrevious.setVisibility(currentQuestionIndex > 0 ? View.VISIBLE : View.GONE);
        btnNext.setVisibility(currentQuestionIndex < testQuestions.size() - 1 ? View.VISIBLE : View.GONE);
        btnSubmit.setVisibility(currentQuestionIndex == testQuestions.size() - 1 ? View.VISIBLE : View.GONE);
    }

    private void saveCurrentAnswer() {
        int selectedId = radioGroupOptions.getCheckedRadioButtonId();
        if (selectedId != -1) {
            userAnswers.put(currentQuestionIndex, selectedId);
        }
    }

    private void submitTest() {
        // Check if all questions are answered
        for (int i = 0; i < testQuestions.size(); i++) {
            if (!userAnswers.containsKey(i)) {
                Toast.makeText(this, "Vui lòng trả lời tất cả câu hỏi", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Calculate score
        int correctAnswers = 0;
        for (int i = 0; i < testQuestions.size(); i++) {
            Integer userAnswer = userAnswers.get(i);
            if (userAnswer != null && userAnswer.equals(testQuestions.get(i).getCorrectAnswer())) {
                correctAnswers++;
            }
        }

        double scorePercentage = (double) correctAnswers / testQuestions.size() * 100;
        int finalScore = (int) Math.round(scorePercentage);

        // Save test result to Firebase
        saveTestResult(finalScore);

        // Show result
        showTestResult(finalScore, correctAnswers);
    }

    private void saveTestResult(int score) {
        Map<String, Object> testResult = new HashMap<>();
        testResult.put("studentId", studentId);
        testResult.put("courseId", courseId);
        testResult.put("courseName", courseName);
        testResult.put("score", score);
        testResult.put("completedAt", com.google.firebase.Timestamp.now());
        testResult.put("totalQuestions", testQuestions.size());
        testResult.put("correctAnswers", calculateCorrectAnswers());

        db.collection("testResults")
                .add(testResult)
                .addOnSuccessListener(documentReference -> {
                    android.util.Log.d("CourseTest", "Test result saved: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CourseTest", "Error saving test result", e);
                });
    }

    private int calculateCorrectAnswers() {
        int correctAnswers = 0;
        for (int i = 0; i < testQuestions.size(); i++) {
            Integer userAnswer = userAnswers.get(i);
            if (userAnswer != null && userAnswer.equals(testQuestions.get(i).getCorrectAnswer())) {
                correctAnswers++;
            }
        }
        return correctAnswers;
    }

    private void showTestResult(int score, int correctAnswers) {
        // Hide test UI, show result UI
        findViewById(R.id.layout_test_questions).setVisibility(View.GONE);
        layoutTestComplete.setVisibility(View.VISIBLE);

        String resultText = "Điểm số: " + score + "/100\n" +
                           "Số câu đúng: " + correctAnswers + "/" + testQuestions.size();

        if (score >= 80) {
            resultText += "\n🎉 Xuất sắc!";
        } else if (score >= 60) {
            resultText += "\n👍 Tốt!";
        } else {
            resultText += "\n💪 Cần cố gắng thêm!";
        }

        tvFinalScore.setText(resultText);
        testCompleted = true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (testCompleted) {
                finish();
            } else {
                // Show confirmation dialog
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Thoát bài kiểm tra")
                        .setMessage("Bạn có chắc muốn thoát? Kết quả sẽ không được lưu.")
                        .setPositiveButton("Thoát", (dialog, which) -> finish())
                        .setNegativeButton("Tiếp tục", null)
                        .show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (testCompleted) {
            super.onBackPressed();
        } else {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Thoát bài kiểm tra")
                    .setMessage("Bạn có chắc muốn thoát? Kết quả sẽ không được lưu.")
                    .setPositiveButton("Thoát", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Tiếp tục", null)
                    .show();
        }
    }

    // TestQuestion class
    public static class TestQuestion {
        private String question;
        private List<String> options;
        private int correctAnswer;

        // Getters and setters
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        public int getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
    }
}
