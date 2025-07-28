package com.vantinh.tienganh;

import com.google.firebase.Timestamp;

public class FavoriteItem {
    private String id;
    private String studentId;
    private String lessonId;
    private String lessonTitle;
    private String courseId;
    private String courseTitle;
    private String lessonType;
    private String estimatedTime;
    private Timestamp favoriteDate;

    // Constructors
    public FavoriteItem() {}

    public FavoriteItem(String studentId, String lessonId, String lessonTitle, 
                       String courseId, String courseTitle, String lessonType, String estimatedTime) {
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.lessonType = lessonType;
        this.estimatedTime = estimatedTime;
        this.favoriteDate = Timestamp.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getLessonType() { return lessonType; }
    public void setLessonType(String lessonType) { this.lessonType = lessonType; }

    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

    public Timestamp getFavoriteDate() { return favoriteDate; }
    public void setFavoriteDate(Timestamp favoriteDate) { this.favoriteDate = favoriteDate; }
}
