package com.vantinh.tienganh.models;

import java.util.Date;

public class LessonProgress {
    private String id;
    private String studentId;
    private String courseId;
    private String lessonId;
    private boolean isCompleted;
    private Date completedAt;
    private Date createdAt;
    private Date updatedAt;

    public LessonProgress() {
        // Constructor mặc định cho Firestore
    }

    public LessonProgress(String studentId, String courseId, String lessonId, boolean isCompleted) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.isCompleted = isCompleted;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        if (isCompleted) {
            this.completedAt = new Date();
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
        this.updatedAt = new Date();
        if (completed && this.completedAt == null) {
            this.completedAt = new Date();
        }
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
