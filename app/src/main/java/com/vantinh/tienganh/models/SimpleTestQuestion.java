package com.vantinh.tienganh.models;

import java.util.List;

public class SimpleTestQuestion {
    private String documentId;          // ID document Firebase (để edit/delete)
    private String courseId;            // ID khóa học
    private int correctAnswer;          // Number - Index của đáp án đúng (0-3) - SỬA ĐỔI
    private List<String> options;       // Array - 4 đáp án A,B,C,D - SỬA ĐỔI
    private String question;            // String - Nội dung câu hỏi

    public SimpleTestQuestion() {
        // Empty constructor required for Firestore
    }

    public SimpleTestQuestion(String documentId, String courseId, int correctAnswer, List<String> options, String question) {
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

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
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
                ", correctAnswer=" + correctAnswer +
                ", options=" + options +
                ", question='" + question + '\'' +
                '}';
    }
}
