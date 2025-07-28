package com.vantinh.tienganh;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vantinh.tienganh.models.SimpleTestQuestion;
import java.util.ArrayList;
import java.util.List;

public class EditSingleTestQuestionActivity extends AppCompatActivity {

    private static final String TAG = "EditSingleTestQuestion";
    
    private Toolbar toolbar;
    private TextInputEditText etQuestion, etOptionA, etOptionB, etOptionC, etOptionD;
    private RadioGroup rgCorrectAnswer;
    private RadioButton rbOptionA, rbOptionB, rbOptionC, rbOptionD;
    private Button btnSaveQuestion, btnDeleteQuestion;
    
    private FirebaseFirestore db;
    private String courseId, courseName, questionId;
    private SimpleTestQuestion currentQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_single_test_question);

        initViews();
        setupToolbar();
        initFirebase();
        getQuestionInfo();
        setupClickListeners();
        loadQuestionData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etQuestion = findViewById(R.id.et_question);
        etOptionA = findViewById(R.id.et_option_a);
        etOptionB = findViewById(R.id.et_option_b);
        etOptionC = findViewById(R.id.et_option_c);
        etOptionD = findViewById(R.id.et_option_d);
        rgCorrectAnswer = findViewById(R.id.rg_correct_answer);
        rbOptionA = findViewById(R.id.rb_option_a);
        rbOptionB = findViewById(R.id.rb_option_b);
        rbOptionC = findViewById(R.id.rb_option_c);
        rbOptionD = findViewById(R.id.rb_option_d);
        btnSaveQuestion = findViewById(R.id.btn_save_question);
        btnDeleteQuestion = findViewById(R.id.btn_delete_question);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chỉnh sửa câu hỏi");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void getQuestionInfo() {
        courseId = getIntent().getStringExtra("courseId");
        courseName = getIntent().getStringExtra("courseName");
        questionId = getIntent().getStringExtra("questionId");
        
        if (courseName != null) {
            getSupportActionBar().setSubtitle(courseName);
        }
    }

    private void setupClickListeners() {
        btnSaveQuestion.setOnClickListener(v -> saveQuestion());
        btnDeleteQuestion.setOnClickListener(v -> deleteQuestion());
    }

    private void loadQuestionData() {
        if (questionId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID câu hỏi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading question data for ID: " + questionId);

        db.collection("test")
                .document(questionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentQuestion = documentSnapshot.toObject(SimpleTestQuestion.class);
                        if (currentQuestion != null) {
                            currentQuestion.setDocumentId(documentSnapshot.getId());
                            populateFields();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy câu hỏi", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading question", e);
                    Toast.makeText(this, "Lỗi khi tải câu hỏi: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateFields() {
        if (currentQuestion == null) return;

        // Điền nội dung câu hỏi
        etQuestion.setText(currentQuestion.getQuestion());

        // Điền các lựa chọn - options là array chứa 4 đáp án
        List<String> options = currentQuestion.getOptions();
        if (options != null && options.size() >= 4) {
            etOptionA.setText(options.get(0));
            etOptionB.setText(options.get(1));
            etOptionC.setText(options.get(2));
            etOptionD.setText(options.get(3));
        }

        // Chọn đáp án đúng - correctAnswer là number (index)
        int correctIndex = currentQuestion.getCorrectAnswer();
        switch (correctIndex) {
            case 0:
                rbOptionA.setChecked(true);
                break;
            case 1:
                rbOptionB.setChecked(true);
                break;
            case 2:
                rbOptionC.setChecked(true);
                break;
            case 3:
                rbOptionD.setChecked(true);
                break;
        }

        Log.d(TAG, "Populated fields for question: " + currentQuestion.getQuestion());
    }

    private void saveQuestion() {
        if (!validateInput()) {
            return;
        }

        String questionText = etQuestion.getText().toString().trim();
        List<String> options = new ArrayList<>();
        options.add(etOptionA.getText().toString().trim());
        options.add(etOptionB.getText().toString().trim());
        options.add(etOptionC.getText().toString().trim());
        options.add(etOptionD.getText().toString().trim());

        int correctAnswerIndex = getCorrectAnswerIndex();

        // Cập nhật currentQuestion với cấu trúc đúng
        currentQuestion.setQuestion(questionText);
        currentQuestion.setOptions(options);                    // options là array
        currentQuestion.setCorrectAnswer(correctAnswerIndex);   // correctAnswer là number

        btnSaveQuestion.setEnabled(false);
        btnSaveQuestion.setText("Đang lưu...");

        Log.d(TAG, "Saving question: " + currentQuestion.toString());

        db.collection("test")
                .document(questionId)
                .set(currentQuestion)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Question saved successfully");
                    Toast.makeText(this, "Đã lưu câu hỏi", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving question", e);
                    Toast.makeText(this, "Lỗi khi lưu câu hỏi: " + e.getMessage(),
                                 Toast.LENGTH_SHORT).show();
                    btnSaveQuestion.setEnabled(true);
                    btnSaveQuestion.setText("Lưu thay đổi");
                });
    }

    private void deleteQuestion() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa câu hỏi")
                .setMessage("Bạn có chắc chắn muốn xóa câu hỏi này?\n\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> performDeleteQuestion())
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performDeleteQuestion() {
        btnDeleteQuestion.setEnabled(false);
        btnDeleteQuestion.setText("Đang xóa...");

        db.collection("test")
                .document(questionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Question deleted successfully");
                    Toast.makeText(this, "Đã xóa câu hỏi", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting question", e);
                    Toast.makeText(this, "Lỗi khi xóa câu hỏi: " + e.getMessage(),
                                 Toast.LENGTH_SHORT).show();
                    btnDeleteQuestion.setEnabled(true);
                    btnDeleteQuestion.setText("Xóa câu hỏi");
                });
    }

    private boolean validateInput() {
        String questionText = etQuestion.getText().toString().trim();
        if (questionText.isEmpty()) {
            etQuestion.setError("Vui lòng nhập nội dung câu hỏi");
            etQuestion.requestFocus();
            return false;
        }

        String[] options = {
            etOptionA.getText().toString().trim(),
            etOptionB.getText().toString().trim(),
            etOptionC.getText().toString().trim(),
            etOptionD.getText().toString().trim()
        };

        for (int i = 0; i < options.length; i++) {
            if (options[i].isEmpty()) {
                switch (i) {
                    case 0:
                        etOptionA.setError("Vui lòng nhập lựa chọn A");
                        etOptionA.requestFocus();
                        break;
                    case 1:
                        etOptionB.setError("Vui lòng nhập lựa chọn B");
                        etOptionB.requestFocus();
                        break;
                    case 2:
                        etOptionC.setError("Vui lòng nhập lựa chọn C");
                        etOptionC.requestFocus();
                        break;
                    case 3:
                        etOptionD.setError("Vui lòng nhập lựa chọn D");
                        etOptionD.requestFocus();
                        break;
                }
                return false;
            }
        }

        if (rgCorrectAnswer.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Vui lòng chọn đáp án đúng", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private int getCorrectAnswerIndex() {
        int checkedId = rgCorrectAnswer.getCheckedRadioButtonId();

        if (checkedId == rbOptionA.getId()) {
            return 0;
        } else if (checkedId == rbOptionB.getId()) {
            return 1;
        } else if (checkedId == rbOptionC.getId()) {
            return 2;
        } else if (checkedId == rbOptionD.getId()) {
            return 3;
        }

        return -1;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
