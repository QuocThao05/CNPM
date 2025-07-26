package com.vantinh.tienganh.models;

import java.util.List;

public class SimpleTestQuestion {
    private String documentId;          // ID document Firebase (để edit/delete)
    private String courseId;            // ID khóa học (trường mới)
    private List<String> correctAnswer; // Array - 4 đáp án A,B,C,D
    private int options;                // Number - Index của đáp án đúng (0-3)
    private String question;            // String - Nội dung câu hỏi

    public SimpleTestQuestion() {
        // Empty constructor required for Firestore
    }

    public SimpleTestQuestion(String documentId, String courseId, List<String> correctAnswer, int options, String question) {
        this.documentId = documentId;
        this.courseId = courseId;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.question = question;
    }

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
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

    @Override
    public String toString() {
        return "SimpleTestQuestion{" +
                "documentId='" + documentId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", question='" + question + '\'' +
                ", correctAnswer=" + correctAnswer +
                ", options=" + options +
                '}';
    }
}
