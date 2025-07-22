package com.vantinh.tienganh;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String email;
    private String fullName;
    private String address;
    private String role;
    private Date createdAt;
    private Date updatedAt;

    // Constructor mặc định cho Firebase
    public User() {}

    // Constructor đầy đủ
    public User(String id, String email, String fullName, String address, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.address = address;
        this.role = role;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Constructor cho việc tạo user mới với thông tin cơ bản
    public User(String id, String email, String role) {
        this.id = id;
        this.email = email;
        this.fullName = getDefaultFullNameFromRole(role);
        this.address = ""; // Địa chỉ mặc định trống
        this.role = role;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public String getRole() {
        return role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
        this.updatedAt = new Date();
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = new Date();
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        this.updatedAt = new Date();
    }

    public void setAddress(String address) {
        this.address = address;
        this.updatedAt = new Date();
    }

    public void setRole(String role) {
        this.role = role;
        this.updatedAt = new Date();
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Phương thức tiện ích
    private String getDefaultFullNameFromRole(String role) {
        if (role == null) return "Người dùng";

        switch (role.toLowerCase()) {
            case "admin":
                return "Quản trị viên";
            case "teacher":
                return "Giáo viên";
            case "student":
                return "Học viên";
            default:
                return "Người dùng";
        }
    }

    // Chuyển đổi sang Map để lưu vào Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("email", email);
        map.put("fullName", fullName);
        map.put("address", address);
        map.put("role", role);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }

    // Tạo User từ Map (dùng khi đọc từ Firestore)
    public static User fromMap(Map<String, Object> map) {
        User user = new User();
        user.setId((String) map.get("id"));
        user.setEmail((String) map.get("email"));
        user.setFullName((String) map.get("fullName"));
        user.setAddress((String) map.get("address"));
        user.setRole((String) map.get("role"));

        // Xử lý date objects
        if (map.get("createdAt") instanceof Date) {
            user.setCreatedAt((Date) map.get("createdAt"));
        } else if (map.get("createdAt") instanceof com.google.firebase.Timestamp) {
            user.setCreatedAt(((com.google.firebase.Timestamp) map.get("createdAt")).toDate());
        }

        if (map.get("updatedAt") instanceof Date) {
            user.setUpdatedAt((Date) map.get("updatedAt"));
        } else if (map.get("updatedAt") instanceof com.google.firebase.Timestamp) {
            user.setUpdatedAt(((com.google.firebase.Timestamp) map.get("updatedAt")).toDate());
        }

        return user;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", address='" + address + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
