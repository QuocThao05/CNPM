package com.vantinh.tienganh;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class StudentProgressDetailActivity extends AppCompatActivity {

    private TextView tvStudentName, tvCourseName;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_progress_detail);

        initViews();
        setupToolbar();
        loadStudentData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvStudentName = findViewById(R.id.tv_student_name);
        tvCourseName = findViewById(R.id.tv_course_name);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết tiến độ học viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadStudentData() {
        String studentName = getIntent().getStringExtra("studentName");
        String courseName = getIntent().getStringExtra("courseName");

        if (studentName != null && tvStudentName != null) {
            tvStudentName.setText(studentName);
        }

        if (courseName != null && tvCourseName != null) {
            tvCourseName.setText(courseName);
        }
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
