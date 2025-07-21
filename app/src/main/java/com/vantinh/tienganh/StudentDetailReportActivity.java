package com.vantinh.tienganh;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class StudentDetailReportActivity extends AppCompatActivity {

    private TextView tvStudentName, tvCourseName, tvStudentEmail;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail_report);

        initViews();
        setupToolbar();
        loadStudentData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvStudentName = findViewById(R.id.tv_student_name);
        tvCourseName = findViewById(R.id.tv_course_name);
        tvStudentEmail = findViewById(R.id.tv_student_email);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Báo cáo chi tiết học viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadStudentData() {
        String studentName = getIntent().getStringExtra("studentName");
        String courseName = getIntent().getStringExtra("courseName");
        String studentEmail = getIntent().getStringExtra("studentEmail");

        if (studentName != null && tvStudentName != null) {
            tvStudentName.setText(studentName);
        }

        if (courseName != null && tvCourseName != null) {
            tvCourseName.setText(courseName);
        }

        if (studentEmail != null && tvStudentEmail != null) {
            tvStudentEmail.setText(studentEmail);
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
