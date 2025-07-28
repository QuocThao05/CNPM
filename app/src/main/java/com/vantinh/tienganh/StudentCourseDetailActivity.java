package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentCourseDetailActivity extends AppCompatActivity {

    private TextView tvCourseTitle, tvCourseDescription, tvCourseCategory, tvCourseLevel;
    private TextView tvCourseDuration, tvTotalLessons, tvCompletedLessons;
    private TextView tvEnrollmentDate, tvProgressPercentage, tvTestScore; // Thêm tvTestScore
    private ProgressBar progressBarCompletion;
    private Button btnStartLearning, btnTakeQuiz; // Xóa btnViewLessons và btnViewProgress, thêm btnTakeQuiz
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String courseId, courseTitle, courseCategory, enrollmentId;
    private Course currentCourse;
    private int currentProgress = 0; // Thêm biến để track progress

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_course_detail);

        // Get course info from intent - sửa lại tên extra cho đúng
        courseId = getIntent().getStringExtra("course_id"); // Thay đổi từ "courseId" thành "course_id"
        courseTitle = getIntent().getStringExtra("course_title"); // Thay đổi từ "courseTitle" thành "course_title"
        courseCategory = getIntent().getStringExtra("courseCategory");
        enrollmentId = getIntent().getStringExtra("enrollmentId");

        android.util.Log.d("StudentCourseDetail", "Intent data - courseId: " + courseId + ", courseTitle: " + courseTitle);

        if (courseId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
        loadCourseData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCourseTitle = findViewById(R.id.tv_course_title);
        tvCourseDescription = findViewById(R.id.tv_course_description);
        tvCourseCategory = findViewById(R.id.tv_course_category);
        tvCourseLevel = findViewById(R.id.tv_course_level);
        tvCourseDuration = findViewById(R.id.tv_course_duration);
        tvTotalLessons = findViewById(R.id.tv_total_lessons);
        tvCompletedLessons = findViewById(R.id.tv_completed_lessons);
        tvEnrollmentDate = findViewById(R.id.tv_enrollment_date);
        tvProgressPercentage = findViewById(R.id.tv_progress_percentage);
        tvTestScore = findViewById(R.id.tv_test_score); // Khai báo TextView cho điểm số bài kiểm tra
        progressBarCompletion = findViewById(R.id.progress_bar_completion);
        btnStartLearning = findViewById(R.id.btn_start_learning);
        btnTakeQuiz = findViewById(R.id.btn_take_quiz); // Thay thế btnViewLessons và btnViewProgress
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết khóa học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        btnStartLearning.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentCourseLessonsActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseTitle", courseTitle);
            intent.putExtra("courseCategory", courseCategory);
            startActivity(intent);
        });

        btnTakeQuiz.setOnClickListener(v -> {
            // Kiểm tra xem học viên đã hoàn thành 100% bài học chưa
            if (currentProgress < 100) {
                Toast.makeText(this,
                    "Bạn cần hoàn thành tất cả bài học (" + currentProgress + "% hoàn thành) trước khi làm bài kiểm tra",
                    Toast.LENGTH_LONG).show();
                return;
            }

            // Load dữ liệu test từ Firebase và tạo form bài kiểm tra
            loadTestDataAndStartQuiz();
        });
    }

    private void loadCourseData() {
        db.collection("courses").document(courseId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentCourse = documentSnapshot.toObject(Course.class);
                    if (currentCourse != null) {
                        currentCourse.setId(documentSnapshot.getId());
                        displayCourseInfo();
                        loadProgressData();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseDetail", "Error loading course", e);
                Toast.makeText(this, "Lỗi tải khóa học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void displayCourseInfo() {
        tvCourseTitle.setText(currentCourse.getTitle());
        tvCourseDescription.setText(currentCourse.getDescription());
        tvCourseCategory.setText(currentCourse.getCategory());
        tvCourseLevel.setText(currentCourse.getLevel());
        tvCourseDuration.setText(currentCourse.getDuration() + " giờ");
    }

    private void loadProgressData() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        // Get student info từ users collection
        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String studentId = documentSnapshot.getString("id");
                        if (studentId != null) {
                            calculateProgress(studentId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentCourseDetail", "Error loading student info", e);
                });
    }

    private void calculateProgress(String studentId) {
        android.util.Log.d("StudentCourseDetail", "=== CALCULATING PROGRESS FOR COURSE DETAIL ===");
        android.util.Log.d("StudentCourseDetail", "CourseId: " + courseId);
        android.util.Log.d("StudentCourseDetail", "StudentId: " + studentId);

        // Sử dụng cùng logic với StudentMyCoursesActivity để đảm bảo đồng bộ
        // Thử load lessons từ subcollection trước
        db.collection("courses").document(courseId).collection("lessons")
            .get()
            .addOnSuccessListener(lessonsSnapshot -> {
                int totalLessons = lessonsSnapshot.size();
                android.util.Log.d("StudentCourseDetail", "Total lessons from courses/{courseId}/lessons: " + totalLessons);

                if (totalLessons == 0) {
                    // Thử cách khác: lessons collection với courseId filter
                    db.collection("lessons")
                        .whereEqualTo("courseId", courseId)
                        .get()
                        .addOnSuccessListener(alternativeLessons -> {
                            int altTotalLessons = alternativeLessons.size();
                            android.util.Log.d("StudentCourseDetail", "Total lessons from lessons collection: " + altTotalLessons);

                            if (altTotalLessons > 0) {
                                calculateProgressWithLessons(studentId, altTotalLessons);
                            } else {
                                // Fallback: không có bài học nào
                                updateProgressUI(0, 0, 0);
                            }
                        })
                        .addOnFailureListener(e -> {
                            android.util.Log.e("StudentCourseDetail", "Error loading alternative lessons", e);
                            updateProgressUI(0, 0, 0);
                        });
                } else {
                    calculateProgressWithLessons(studentId, totalLessons);
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseDetail", "Error loading lessons", e);
                // Fallback to old method with published filter
                loadLessonsWithPublishedFilter(studentId);
            });
    }

    private void loadLessonsWithPublishedFilter(String studentId) {
        android.util.Log.d("StudentCourseDetail", "Fallback: Loading lessons with isPublished filter");

        db.collection("lessons")
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("isPublished", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int totalLessons = queryDocumentSnapshots.size();
                calculateProgressWithLessons(studentId, totalLessons);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseDetail", "Error loading lessons with published filter", e);
                updateProgressUI(0, 0, 0);
            });
    }

    private void calculateProgressWithLessons(String studentId, int totalLessons) {
        android.util.Log.d("StudentCourseDetail", "Calculating progress with " + totalLessons + " total lessons");

        // Sử dụng collection lesson_progress với field isCompleted (đồng bộ với StudentMyCoursesActivity)
        db.collection("lesson_progress")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("isCompleted", true)
            .get()
            .addOnSuccessListener(progressSnapshots -> {
                int completedLessons = progressSnapshots.size();
                android.util.Log.d("StudentCourseDetail", "Completed lessons from lesson_progress: " + completedLessons);

                updateProgressUI(totalLessons, completedLessons, totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseDetail", "Error loading lesson_progress", e);
                // Thử collection khác nếu lesson_progress không có dữ liệu
                tryAlternativeProgressCollection(studentId, totalLessons);
            });
    }

    private void tryAlternativeProgressCollection(String studentId, int totalLessons) {
        android.util.Log.d("StudentCourseDetail", "Trying alternative progress collection: lessonProgress");

        db.collection("lessonProgress")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("completed", true)
            .get()
            .addOnSuccessListener(progressSnapshot -> {
                int completedLessons = progressSnapshot.size();
                android.util.Log.d("StudentCourseDetail", "Completed lessons from lessonProgress: " + completedLessons);

                updateProgressUI(totalLessons, completedLessons, totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseDetail", "Error loading lessonProgress", e);
                // Thử collection cuối cùng
                tryLastProgressCollection(studentId, totalLessons);
            });
    }

    private void tryLastProgressCollection(String studentId, int totalLessons) {
        android.util.Log.d("StudentCourseDetail", "Trying last progress collection: studentProgress");

        db.collection("studentProgress")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("completed", true)
            .get()
            .addOnSuccessListener(progressSnapshot -> {
                int completedLessons = progressSnapshot.size();
                android.util.Log.d("StudentCourseDetail", "Completed lessons from studentProgress: " + completedLessons);

                updateProgressUI(totalLessons, completedLessons, totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseDetail", "Error loading studentProgress", e);
                // Set default values
                updateProgressUI(totalLessons, 0, 0);
            });
    }

    private void updateProgressUI(int totalLessons, int completedLessons, int progress) {
        tvTotalLessons.setText("Tổng số bài: " + totalLessons);
        tvCompletedLessons.setText("Đã hoàn thành: " + completedLessons);
        currentProgress = progress;
        tvProgressPercentage.setText(currentProgress + "%");
        progressBarCompletion.setProgress(currentProgress);

        android.util.Log.d("StudentCourseDetail", "=== FINAL PROGRESS IN DETAIL ===");
        android.util.Log.d("StudentCourseDetail", "Total lessons: " + totalLessons);
        android.util.Log.d("StudentCourseDetail", "Completed lessons: " + completedLessons);
        android.util.Log.d("StudentCourseDetail", "Progress percentage: " + currentProgress + "%");

        // Update button state based on progress
        updateButtonStates();

        // Load test score after updating progress
        loadTestScore();
    }

    private void updateButtonStates() {
        // Cập nhật trạng thái nút dựa trên tiến độ
        if (currentProgress >= 100) {
            btnTakeQuiz.setEnabled(true);
            btnTakeQuiz.setText("Làm bài kiểm tra");
            btnTakeQuiz.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_green_dark))
            );
        } else {
            btnTakeQuiz.setEnabled(false);
            btnTakeQuiz.setText("Làm bài kiểm tra (Hoàn thành hết bài học trước)");
            btnTakeQuiz.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(getColor(android.R.color.darker_gray))
            );
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh progress data when returning from lesson to show updated progress
        if (currentCourse != null) {
            loadProgressData();
        }
    }

    // Add method to manually refresh progress (can be called from other activities)
    public void refreshProgressData() {
        loadProgressData();
    }

    // Method để load dữ liệu test và bắt đầu làm bài kiểm tra
    private void loadTestDataAndStartQuiz() {
        android.util.Log.d("StudentCourseDetail", "=== STARTING QUIZ DEBUG ===");
        android.util.Log.d("StudentCourseDetail", "Course ID: " + courseId);
        android.util.Log.d("StudentCourseDetail", "Course Title: " + courseTitle);

        if (courseId == null || courseId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID khóa học", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem có bài kiểm tra nào cho khóa học này không bằng cách query collection "test"
        android.util.Log.d("StudentCourseDetail", "Querying collection 'test' with courseId: " + courseId);

        db.collection("test")
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                android.util.Log.d("StudentCourseDetail", "Query successful! Found " + queryDocumentSnapshots.size() + " test documents");

                if (!queryDocumentSnapshots.isEmpty()) {
                    // Debug: In ra thông tin của các documents tìm được
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        android.util.Log.d("StudentCourseDetail", "Test document ID: " + doc.getId());
                        android.util.Log.d("StudentCourseDetail", "Test document data: " + doc.getData().toString());

                        // Kiểm tra cấu trúc dữ liệu
                        String question = doc.getString("question");
                        Object options = doc.get("options"); // Có thể là Array hoặc List
                        Object correctAnswer = doc.get("correctAnswer"); // Có thể là Number

                        android.util.Log.d("StudentCourseDetail", "Question: " + question);
                        android.util.Log.d("StudentCourseDetail", "Options type: " + (options != null ? options.getClass().getSimpleName() : "null"));
                        android.util.Log.d("StudentCourseDetail", "Options: " + options);
                        android.util.Log.d("StudentCourseDetail", "CorrectAnswer type: " + (correctAnswer != null ? correctAnswer.getClass().getSimpleName() : "null"));
                        android.util.Log.d("StudentCourseDetail", "CorrectAnswer: " + correctAnswer);
                    }

                    // Có bài kiểm tra, chuyển đến activity làm bài
                    android.util.Log.d("StudentCourseDetail", "Starting CourseTestActivity...");
                    Intent intent = new Intent(this, CourseTestActivity.class);
                    intent.putExtra("courseId", courseId);
                    intent.putExtra("courseTitle", courseTitle);

                    try {
                        startActivity(intent);
                        android.util.Log.d("StudentCourseDetail", "CourseTestActivity started successfully");
                    } catch (Exception e) {
                        android.util.Log.e("StudentCourseDetail", "Error starting CourseTestActivity", e);
                        Toast.makeText(this, "Lỗi khởi chạy bài kiểm tra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    android.util.Log.w("StudentCourseDetail", "No test documents found for courseId: " + courseId);
                    Toast.makeText(this, "Khóa học này chưa có bài kiểm tra", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StudentCourseDetail", "Error querying test collection", e);
                android.util.Log.e("StudentCourseDetail", "Error details: " + e.getMessage());
                Toast.makeText(this, "Lỗi kiểm tra bài kiểm tra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    // Method để load điểm số bài kiểm tra từ Firebase
    private void loadTestScore() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        // Get student info từ users collection
        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String studentId = documentSnapshot.getString("id");
                        if (studentId != null) {
                            loadTestScoreFromResults(studentId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentCourseDetail", "Error loading student info for test score", e);
                });
    }

    private void loadTestScoreFromResults(String studentId) {
        android.util.Log.d("StudentCourseDetail", "Loading test score for student: " + studentId + ", course: " + courseId);

        // Query testResults collection - bỏ orderBy để tránh lỗi index
        db.collection("testResults")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Tìm điểm cao nhất trong code thay vì dùng orderBy
                        double highestScore = -1;
                        com.google.firebase.firestore.QueryDocumentSnapshot bestDoc = null;

                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Object scoreObj = doc.get("score");
                            double currentScore = 0;

                            // Xử lý nhiều kiểu dữ liệu cho score
                            if (scoreObj instanceof Double) {
                                currentScore = (Double) scoreObj;
                            } else if (scoreObj instanceof Long) {
                                currentScore = ((Long) scoreObj).doubleValue();
                            } else if (scoreObj instanceof String) {
                                try {
                                    currentScore = Double.parseDouble((String) scoreObj);
                                } catch (NumberFormatException e) {
                                    android.util.Log.w("StudentCourseDetail", "Invalid score format: " + scoreObj);
                                    continue;
                                }
                            }

                            if (currentScore > highestScore) {
                                highestScore = currentScore;
                                bestDoc = doc;
                            }
                        }

                        if (bestDoc != null && highestScore >= 0) {
                            displayTestScore(bestDoc, highestScore);
                        } else {
                            showNoTestScore();
                        }
                    } else {
                        // Chưa có kết quả bài kiểm tra
                        showNoTestScore();
                        android.util.Log.d("StudentCourseDetail", "No test results found");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StudentCourseDetail", "Error loading test score", e);
                    tvTestScore.setText("Lỗi tải điểm số: " + e.getMessage());
                    tvTestScore.setTextColor(getColor(android.R.color.holo_red_dark));
                    tvTestScore.setVisibility(View.VISIBLE);
                });
    }

    private void displayTestScore(com.google.firebase.firestore.QueryDocumentSnapshot doc, double score) {
        try {
            String scoreText = String.format("Điểm bài kiểm tra: %.1f/100", score);

            // Safely get correctAnswers và totalQuestions
            Object correctAnswersObj = doc.get("correctAnswers");
            Object totalQuestionsObj = doc.get("totalQuestions");

            if (correctAnswersObj != null && totalQuestionsObj != null) {
                int correctAnswers = 0;
                int totalQuestions = 0;

                if (correctAnswersObj instanceof Long) {
                    correctAnswers = ((Long) correctAnswersObj).intValue();
                } else if (correctAnswersObj instanceof Double) {
                    correctAnswers = ((Double) correctAnswersObj).intValue();
                }

                if (totalQuestionsObj instanceof Long) {
                    totalQuestions = ((Long) totalQuestionsObj).intValue();
                } else if (totalQuestionsObj instanceof Double) {
                    totalQuestions = ((Double) totalQuestionsObj).intValue();
                }

                if (totalQuestions > 0) {
                    scoreText += String.format(" (%d/%d câu đúng)", correctAnswers, totalQuestions);
                }
            }

            // Safely get completedAt
            com.google.firebase.Timestamp completedAt = doc.getTimestamp("completedAt");
            if (completedAt != null) {
                try {
                    String dateText = android.text.format.DateFormat.format("dd/MM/yyyy", completedAt.toDate()).toString();
                    scoreText += "\nNgày làm bài: " + dateText;
                } catch (Exception e) {
                    android.util.Log.w("StudentCourseDetail", "Error formatting date", e);
                }
            }

            tvTestScore.setText(scoreText);
            tvTestScore.setVisibility(View.VISIBLE);

            // Set màu sắc dựa trên điểm số
            if (score >= 80) {
                tvTestScore.setTextColor(getColor(android.R.color.holo_green_dark));
            } else if (score >= 60) {
                tvTestScore.setTextColor(getColor(android.R.color.holo_orange_dark));
            } else {
                tvTestScore.setTextColor(getColor(android.R.color.holo_red_dark));
            }

            android.util.Log.d("StudentCourseDetail", "Test score loaded successfully: " + score);

        } catch (Exception e) {
            android.util.Log.e("StudentCourseDetail", "Error displaying test score", e);
            showNoTestScore();
        }
    }

    private void showNoTestScore() {
        tvTestScore.setText("Chưa làm bài kiểm tra");
        tvTestScore.setTextColor(getColor(android.R.color.darker_gray));
        tvTestScore.setVisibility(View.VISIBLE);
    }
}
