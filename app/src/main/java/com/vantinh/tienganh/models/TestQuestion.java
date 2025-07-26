package com.vantinh.tienganh.models;

import java.util.List;

public class TestQuestion {
    private String courseId;        // ID khóa học
    private String teacherId;       // ID giáo viên
    private List<String> correctAnswer;  // Array 4 đáp án A,B,C,D
    private int options;            // Index của đáp án đúng (0-3)
    private String question;        // Nội dung câu hỏi

    public TestQuestion() {
        // Empty constructor required for Firestore
    }

    public TestQuestion(String courseId, String teacherId, List<String> correctAnswer,
                       int options, String question) {
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.question = question;
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public List<String> getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(List<String> correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public int getOptions() {
        return options;
    }

    public void setOptions(int options) {
        this.options = options;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
