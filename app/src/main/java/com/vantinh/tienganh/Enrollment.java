package com.vantinh.tienganh;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Enrollment {
    private String id;
    private String courseID;
    private String courseName;
    private Date enrollmentDate;
    private String studentEmail;
    private String studentID;
    private String fullName;

    // Constructor mặc định cho Firebase
    public Enrollment() {
        this.enrollmentDate = new Date();
    }

    // Constructor đầy đủ (bỏ teacherName)
    public Enrollment(String courseID, String courseName, String studentEmail,
                     String studentID, String fullName) {
        this.courseID = courseID;
        this.courseName = courseName;
        this.studentEmail = studentEmail;
        this.studentID = studentID;
        this.fullName = fullName;
        this.enrollmentDate = new Date();
    }

    // Constructor với enrollmentDate
    public Enrollment(String courseID, String courseName, Date enrollmentDate,
                     String studentEmail, String studentID, String fullName) {
        this.courseID = courseID;
        this.courseName = courseName;
        this.enrollmentDate = enrollmentDate != null ? enrollmentDate : new Date();
        this.studentEmail = studentEmail;
        this.studentID = studentID;
        this.fullName = fullName;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourseID() { return courseID; }
    public void setCourseID(String courseID) { this.courseID = courseID; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Date getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(Date enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getStudentId() { return studentID; }
    public void setStudentId(String studentId) { this.studentID = studentId; }

    // Chuyển đổi thành Map cho Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("courseID", courseID);
        map.put("courseName", courseName);
        map.put("enrollmentDate", enrollmentDate);
        map.put("studentEmail", studentEmail);
        map.put("studentID", studentID);
        map.put("fullName", fullName);
        return map;
    }

    // Tạo Enrollment từ Map
    public static Enrollment fromMap(Map<String, Object> map) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId((String) map.get("id"));
        enrollment.setCourseID((String) map.get("courseID"));
        enrollment.setCourseName((String) map.get("courseName"));
        enrollment.setStudentEmail((String) map.get("studentEmail"));
        enrollment.setStudentID((String) map.get("studentID"));
        enrollment.setFullName((String) map.get("fullName"));

        if (map.get("enrollmentDate") instanceof Date) {
            enrollment.setEnrollmentDate((Date) map.get("enrollmentDate"));
        } else if (map.get("enrollmentDate") instanceof com.google.firebase.Timestamp) {
            enrollment.setEnrollmentDate(((com.google.firebase.Timestamp) map.get("enrollmentDate")).toDate());
        }

        return enrollment;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id='" + id + '\'' +
                ", courseID='" + courseID + '\'' +
                ", courseName='" + courseName + '\'' +
                ", enrollmentDate=" + enrollmentDate +
                ", studentEmail='" + studentEmail + '\'' +
                ", studentID='" + studentID + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
