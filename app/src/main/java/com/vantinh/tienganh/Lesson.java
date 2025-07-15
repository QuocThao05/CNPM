package com.vantinh.tienganh;

import java.util.Date;
import java.util.List;

public class Lesson {
    private String id;
    private String title;
    private String content;
    private String courseId;
    private String teacherId;
    private int order;
    private String type; // text, video, audio, quiz
    private String videoUrl;
    private String audioUrl;
    private List<String> attachments;
    private Date createdAt;
    private Date updatedAt;
    private boolean isPublished;
    private int estimatedTime; // in minutes

    public Lesson() {
        // Required empty constructor for Firebase
    }

    public Lesson(String title, String content, String courseId, String teacherId, int order) {
        this.title = title;
        this.content = content;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.order = order;
        this.type = "text";
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isPublished = false;
        this.estimatedTime = 30;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }

    public int getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(int estimatedTime) { this.estimatedTime = estimatedTime; }
}
