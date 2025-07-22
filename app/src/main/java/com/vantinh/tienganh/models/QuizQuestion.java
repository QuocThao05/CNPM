package com.vantinh.tienganh.models;

import java.util.List;

public class QuizQuestion {
    private String question;
    private List<String> options;
    private int correctAnswers;

    public QuizQuestion() {
        // Empty constructor required for Firestore
    }

    public QuizQuestion(String question, List<String> options, int correctAnswers) {
        this.question = question;
        this.options = options;
        this.correctAnswers = correctAnswers;
    }

    // Getters and Setters
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

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    // Backward compatibility methods (để không bị lỗi với code cũ)
    public String getQuestionText() {
        return question;
    }

    public void setQuestionText(String questionText) {
        this.question = questionText;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswers;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswers = correctAnswerIndex;
    }
}
