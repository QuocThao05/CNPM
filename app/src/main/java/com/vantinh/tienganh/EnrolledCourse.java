package com.vantinh.tienganh;

import com.google.firebase.firestore.FirebaseFirestore;

public class EnrolledCourse {
    private Course course;
    private String enrollmentId;
    private String enrollmentDate;
    private int progress; // Phần trăm hoàn thành (0-100)
    private int completedLessons;
    private int totalLessons;
    private String lastLessonId;
    private String status; // "active", "completed", "paused"

    // Constructors
    public EnrolledCourse() {
        this.progress = 0;
        this.completedLessons = 0;
        this.totalLessons = 0;
        this.status = "active";
    }

    public EnrolledCourse(Course course, String enrollmentId) {
        this();
        this.course = course;
        this.enrollmentId = enrollmentId;
    }

    // Getters and Setters
    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress)); // Ensure 0-100 range
    }

    public int getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(int completedLessons) {
        this.completedLessons = completedLessons;
        updateProgress();
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(int totalLessons) {
        this.totalLessons = totalLessons;
        updateProgress();
    }

    public String getLastLessonId() {
        return lastLessonId;
    }

    public void setLastLessonId(String lastLessonId) {
        this.lastLessonId = lastLessonId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper methods for UI display
    public String getProgressText() {
        if (totalLessons > 0) {
            return completedLessons + "/" + totalLessons + " bài học";
        }
        return "0/0 bài học";
    }

    public String getProgressPercentageText() {
        return progress + "%";
    }

    // Auto-update progress when lessons change
    private void updateProgress() {
        if (totalLessons > 0) {
            this.progress = (int) ((float) completedLessons / totalLessons * 100);
        } else {
            this.progress = 0;
        }
    }

    // Method để tính toán tiến độ thực tế từ Firebase
    public void calculateActualProgress(String courseId, String studentId, FirebaseFirestore db) {
        // Load tổng số lessons của course
        db.collection("courses").document(courseId).collection("lessons")
                .get()
                .addOnSuccessListener(lessonsSnapshot -> {
                    int totalLessonsCount = lessonsSnapshot.size();
                    setTotalLessons(totalLessonsCount);

                    // Load số lessons đã hoàn thành của student
                    db.collection("studentProgress")
                            .whereEqualTo("studentId", studentId)
                            .whereEqualTo("courseId", courseId)
                            .whereEqualTo("completed", true)
                            .get()
                            .addOnSuccessListener(progressSnapshot -> {
                                int completedLessonsCount = progressSnapshot.size();
                                setCompletedLessons(completedLessonsCount);

                                android.util.Log.d("EnrolledCourse", "Course " + courseId +
                                    " - Completed: " + completedLessonsCount + "/" + totalLessonsCount);
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("EnrolledCourse", "Error loading progress", e);
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("EnrolledCourse", "Error loading lessons", e);
                });
    }

    // Check if course is completed
    public boolean isCompleted() {
        return progress >= 100 || "completed".equals(status);
    }

    // Check if student can continue learning
    public boolean canContinueLearning() {
        return !isCompleted() && "active".equals(status);
    }

    // Get display status for UI
    public String getDisplayStatus() {
        if (isCompleted()) {
            return "Hoàn thành";
        } else if (progress > 0) {
            return "Đang học";
        } else {
            return "Chưa bắt đầu";
        }
    }

    @Override
    public String toString() {
        return "EnrolledCourse{" +
                "course=" + (course != null ? course.getTitle() : "null") +
                ", progress=" + progress +
                ", completedLessons=" + completedLessons +
                ", totalLessons=" + totalLessons +
                ", status='" + status + '\'' +
                '}';
    }
}
