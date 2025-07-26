package com.vantinh.tienganh.models;

import java.util.Date;
import java.util.List;

public class Quiz {
    private String id;
    private String title;
    private String courseId;
    private String courseName;
    private String teacherId;
    private List<QuizQuestion> questions;
    private Date createdAt;
    private boolean active;

    public Quiz() {
        // Empty constructor required for Firestore
    }

    public Quiz(String title, String courseId, String courseName, String teacherId, 
                List<QuizQuestion> questions, Date createdAt, boolean active) {
        this.title = title;
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacherId = teacherId;
        this.questions = questions;
        this.createdAt = createdAt;
        this.active = active;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
