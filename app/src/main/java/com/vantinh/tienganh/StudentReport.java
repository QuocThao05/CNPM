package com.vantinh.tienganh;

import java.util.Date;

public class StudentReport {
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String courseId;
    private String courseName;
    private Date enrollmentDate;
    private int totalQuizzes;
    private int completedQuizzes;
    private double averageScore;
    private double progress; // Percentage
    private String status; // COMPLETED, IN_PROGRESS, NOT_STARTED
    private Date lastActivity;

    // Constructors
    public StudentReport() {}

    public StudentReport(String studentId, String studentName, String courseId, String courseName) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseName = courseName;
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

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }

    // Helper methods
    public String getStatusText() {
        switch (status) {
            case "COMPLETED":
                return "Hoàn thành";
            case "IN_PROGRESS":
                return "Đang học";
            case "NOT_STARTED":
                return "Chưa bắt đầu";
            default:
                return "Không xác định";
        }
    }

    public String getProgressText() {
        return String.format("%.1f%%", progress);
    }

    public String getScoreText() {
        return String.format("%.1f điểm", averageScore);
    }

    public String getQuizProgressText() {
        return completedQuizzes + "/" + totalQuizzes + " bài kiểm tra";
    }
}
