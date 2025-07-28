package com.vantinh.tienganh;

import java.util.Date;

public class InboxMessage {
    private String id;
    private String type; // "notification", "feedback_response"
    private String title;
    private String message;
    private String fromType; // "system", "teacher"
    private String fromName;
    private Date createdAt;
    private boolean isRead;

    // Additional data for course-related messages
    private String courseId;
    private String courseName;
    private String originalFeedback; // For feedback responses

    // Constructors
    public InboxMessage() {}

    public InboxMessage(String id, String type, String title, String message) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = false;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFromType() { return fromType; }
    public void setFromType(String fromType) { this.fromType = fromType; }

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getOriginalFeedback() { return originalFeedback; }
    public void setOriginalFeedback(String originalFeedback) { this.originalFeedback = originalFeedback; }

    // Helper methods
    public String getTypeDisplayName() {
        switch (type) {
            case "notification":
                return "Thông báo";
            case "feedback_response":
                return "Phản hồi";
            default:
                return "Tin nhắn";
        }
    }

    public String getFormattedDate() {
        if (createdAt == null) return "";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        return sdf.format(createdAt);
    }
}
