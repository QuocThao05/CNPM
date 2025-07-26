package com.vantinh.tienganh.models;

import java.util.List;

public class QuizQuestion {
    private String title;           // Tên bài kiểm tra (trường mới)
    private String question;        // Nội dung câu hỏi
    private List<String> options;   // Các lựa chọn A, B, C, D
    private int correctAnswer;      // Chỉ số đáp án đúng (0-3)

    public QuizQuestion() {
        // Empty constructor required for Firestore
    }

    public QuizQuestion(String title, String question, List<String> options, int correctAnswer) {
        this.title = title;
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    // Backward compatibility methods
    public int getCorrectAnswers() {
        return correctAnswer;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswer = correctAnswers;
    }

    public String getQuestionText() {
        return question;
    }

    public void setQuestionText(String questionText) {
        this.question = questionText;
    }
}
