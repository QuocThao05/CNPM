package com.vantinh.tienganh;

import java.util.Date;

public class CourseStudent {
    private String studentId;
    private String studentName;
    private String studentEmail;
    private Date enrollmentDate;
    private String enrollmentId;
    private String status; // Thêm field status
    private int totalQuizzes;
    private int completedQuizzes;
    private double averageScore;
    private double progress; // Percentage
    private Date lastActivity;

    // Constructors
    public CourseStudent() {}

    public CourseStudent(String studentId, String studentName, String studentEmail) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.status = "approved"; // Default status
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getStatus() { // Thêm getter cho status
        return status;
    }

    public void setStatus(String status) { // Thêm setter cho status
        this.status = status;
    }

    public int getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(int totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public int getCompletedQuizzes() {
        return completedQuizzes;
    }

    public void setCompletedQuizzes(int completedQuizzes) {
        this.completedQuizzes = completedQuizzes;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }

    // Helper methods
    public String getProgressText() {
        return String.format("%.1f%%", progress);
    }

    public String getScoreText() {
        return String.format("%.1f điểm", averageScore);
    }

    public String getQuizProgressText() {
        return completedQuizzes + "/" + totalQuizzes + " bài kiểm tra";
    }

    public String getProgressStatus() {
        if (progress >= 100) {
            return "Hoàn thành";
        } else if (progress >= 50) {
            return "Đang tiến bộ";
        } else if (progress > 0) {
            return "Mới bắt đầu";
        } else {
            return "Chưa bắt đầu";
        }
    }
}
