package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vantinh.tienganh.adapters.EditTestQuestionAdapter;
import com.vantinh.tienganh.models.SimpleTestQuestion;
import java.util.ArrayList;
import java.util.List;

public class EditTestQuestionsActivity extends AppCompatActivity {

    private static final String TAG = "EditTestQuestions";
    
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoQuestions;
    private EditTestQuestionAdapter adapter;
    
    private FirebaseFirestore db;
    private String courseId;
    private String courseName;
    private List<SimpleTestQuestion> testQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_test_questions);

        initViews();
        setupToolbar();
        initFirebase();
        getCourseInfo();
        setupRecyclerView();
        loadTestQuestions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.rv_test_questions);
        progressBar = findViewById(R.id.progress_bar);
        tvNoQuestions = findViewById(R.id.tv_no_questions);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chỉnh sửa bài kiểm tra");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void getCourseInfo() {
        courseId = getIntent().getStringExtra("courseId");
        courseName = getIntent().getStringExtra("courseName");
        
        if (courseName != null) {
            getSupportActionBar().setSubtitle(courseName);
        }
    }

    private void setupRecyclerView() {
        testQuestions = new ArrayList<>();
        adapter = new EditTestQuestionAdapter(testQuestions, this::editQuestion, this::deleteQuestion);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadTestQuestions() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoQuestions.setVisibility(View.GONE);

        Log.d(TAG, "Loading test questions for courseId: " + courseId);

        db.collection("test")
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    testQuestions.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SimpleTestQuestion question = document.toObject(SimpleTestQuestion.class);
                        question.setDocumentId(document.getId()); // Lưu document ID để có thể edit/delete
                        testQuestions.add(question);
                        
                        Log.d(TAG, "Loaded question: " + question.getQuestion());
                    }

                    progressBar.setVisibility(View.GONE);
                    
                    if (testQuestions.isEmpty()) {
                        tvNoQuestions.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvNoQuestions.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }

                    Log.d(TAG, "Total questions loaded: " + testQuestions.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading test questions", e);
                    progressBar.setVisibility(View.GONE);
                    tvNoQuestions.setVisibility(View.VISIBLE);
                    tvNoQuestions.setText("Lỗi khi tải danh sách câu hỏi");
                    Toast.makeText(this, "Lỗi khi tải danh sách câu hỏi: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }

    private void editQuestion(SimpleTestQuestion question) {
        // Chuyển đến activity chỉnh sửa câu hỏi
        Intent intent = new Intent(this, EditSingleTestQuestionActivity.class);
        intent.putExtra("courseId", courseId);
        intent.putExtra("courseName", courseName);
        intent.putExtra("questionId", question.getDocumentId());
        intent.putExtra("question", question.getQuestion());
        // Truyền thêm dữ liệu khác nếu cần
        startActivity(intent);
    }

    private void deleteQuestion(SimpleTestQuestion question) {
        // Xóa câu hỏi
        if (question.getDocumentId() != null) {
            db.collection("test")
                    .document(question.getDocumentId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Question deleted successfully");
                        Toast.makeText(this, "Đã xóa câu hỏi", Toast.LENGTH_SHORT).show();
                        loadTestQuestions(); // Reload danh sách
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting question", e);
                        Toast.makeText(this, "Lỗi khi xóa câu hỏi: " + e.getMessage(), 
                                     Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload danh sách khi quay lại từ activity chỉnh sửa
        loadTestQuestions();
    }
}
