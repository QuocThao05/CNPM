package com.vantinh.tienganh;

import java.util.Date;

public class Enrollment {
    private String id;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String courseId;
    private String courseName;
    private String teacherId;
    private Date enrollmentDate;
    private String status; // PENDING, APPROVED, REJECTED
    private String message; // Tin nhắn từ giáo viên
    private Date approvedDate;
    private double progress; // Tiến độ học (0-100%)

    public Enrollment() {
        // Required empty constructor for Firebase
    }

    public Enrollment(String studentId, String studentName, String studentEmail,
                     String courseId, String courseName, String teacherId) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacherId = teacherId;
        this.enrollmentDate = new Date();
        this.status = "PENDING";
        this.progress = 0.0;
    }

    // Constructor for simple enrollment (used in CourseRequestManagementActivity)
    public Enrollment(String studentId, String courseId, Date enrollmentDate, double progress) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollmentDate = enrollmentDate;
        this.progress = progress;
        this.status = "APPROVED";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public Date getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(Date enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getApprovedDate() { return approvedDate; }
    public void setApprovedDate(Date approvedDate) { this.approvedDate = approvedDate; }

    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }

    public String getStatusDisplayName() {
        switch (status) {
            case "PENDING": return "Đang chờ duyệt";
            case "APPROVED": return "Đã duyệt";
            case "REJECTED": return "Bị từ chối";
            default: return "Không xác định";
        }
    }
}
