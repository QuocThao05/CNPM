package com.vantinh.tienganh.models;

import java.util.Date;
import java.util.List;

public class Course {
    private String id;
    private String title;
    private String description;
    private String teacherId;
    private String imageUrl;
    private String level; // Beginner, Intermediate, Advanced
    private String category; // Grammar, Vocabulary, Listening, Speaking
    private int duration; // in hours
    private Date createdAt;
    private Date updatedAt;
    private boolean isActive;
    private int enrolledStudents;
    private double rating;
    private List<String> tags;

    public Course() {
        // Required empty constructor for Firebase
    }

    public Course(String title, String description, String teacherId,
                  String level, String category, int duration) {
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
        this.level = level;
        this.category = category;
        this.duration = duration;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
        this.enrolledStudents = 0;
        this.rating = 0.0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(int enrolledStudents) { this.enrolledStudents = enrolledStudents; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    // Thêm phương thức getCourseName() để tương thích với code hiện tại
    public String getCourseName() {
        return this.title;
    }

    public void setCourseName(String courseName) {
        this.title = courseName;
    }
}
