package com.vantinh.tienganh;

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
        this.progress = Math.max(0, Math.min(100, progress));
    }

    public int getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(int completedLessons) {
        this.completedLessons = completedLessons;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(int totalLessons) {
        this.totalLessons = totalLessons;
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

    // Helper methods
    public String getProgressText() {
        if (totalLessons == 0) {
            return "Chưa có bài học";
        }
        return completedLessons + "/" + totalLessons + " bài học";
    }

    public String getProgressPercentageText() {
        return progress + "% hoàn thành";
    }

    public boolean isCompleted() {
        return progress == 100 || "completed".equals(status);
    }

    public boolean canContinueLearning() {
        return "active".equals(status) && !isCompleted();
    }
}
