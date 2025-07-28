package com.vantinh.tienganh;

import java.util.Date;

public class Feedback {
    private String id;
    private String courseId;
    private String courseName;
    private String message;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String status; // pending, responded
    private Date feedbackRequest;
    private String teacherResponse;
    private Date responseDate;

    public Feedback() {
        // Empty constructor for Firebase
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getFeedbackRequest() {
        return feedbackRequest;
    }

    public void setFeedbackRequest(Date feedbackRequest) {
        this.feedbackRequest = feedbackRequest;
    }

    public String getTeacherResponse() {
        return teacherResponse;
    }

    public void setTeacherResponse(String teacherResponse) {
        this.teacherResponse = teacherResponse;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
    }

    // Helper methods
    public boolean hasResponse() {
        return teacherResponse != null && !teacherResponse.trim().isEmpty();
    }

    public String getFormattedDate() {
        if (feedbackRequest != null) {
            return android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", feedbackRequest).toString();
        }
        return "";
    }

    public String getFormattedResponseDate() {
        if (responseDate != null) {
            return android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", responseDate).toString();
        }
        return "";
    }
}
