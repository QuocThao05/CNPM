package com.vantinh.tienganh;

import java.util.Date;
import java.util.List;

public class Lesson {
    private String id;
    private String title;
    private String content;
    private String courseId;
    private String teacherId;
    private String type; // text, video, audio, quiz
    private String category; // Grammar, Vocabulary, etc.
    private int estimatedTime; // in minutes
    private int order; // lesson order in course
    private Date createdAt;
    private Date updatedAt;
    private boolean isPublished;

    // Grammar-specific fields
    private String grammarRule;
    private String grammarStructure;
    private List<String> grammarExamples;
    private List<String> grammarUsage;
    private List<String> grammarNotes;

    // Vocabulary-specific fields (for future use)
    private List<String> vocabularyWords;
    private List<String> definitions;
    private List<String> pronunciations;

    // Learning progress fields
    private boolean isAccessible = false; // Có thể truy cập học hay không
    private boolean isLocked = true; // Bài học có bị khóa không
    private boolean isCompleted = false; // Đã hoàn thành chưa

    // Constructors
    public Lesson() {
        // Default constructor required for Firestore
    }

    public Lesson(String title, String content, String courseId, String teacherId) {
        this.title = title;
        this.content = content;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isPublished = false;
        this.type = "text";
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

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(int estimatedTime) { this.estimatedTime = estimatedTime; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }

    // Grammar-specific getters and setters
    public String getGrammarRule() { return grammarRule; }
    public void setGrammarRule(String grammarRule) { this.grammarRule = grammarRule; }

    public String getGrammarStructure() { return grammarStructure; }
    public void setGrammarStructure(String grammarStructure) { this.grammarStructure = grammarStructure; }

    public List<String> getGrammarExamples() { return grammarExamples; }
    public void setGrammarExamples(List<String> grammarExamples) { this.grammarExamples = grammarExamples; }

    public List<String> getGrammarUsage() { return grammarUsage; }
    public void setGrammarUsage(List<String> grammarUsage) { this.grammarUsage = grammarUsage; }

    public List<String> getGrammarNotes() { return grammarNotes; }
    public void setGrammarNotes(List<String> grammarNotes) { this.grammarNotes = grammarNotes; }

    // Vocabulary-specific getters and setters
    public List<String> getVocabularyWords() { return vocabularyWords; }
    public void setVocabularyWords(List<String> vocabularyWords) { this.vocabularyWords = vocabularyWords; }

    public List<String> getDefinitions() { return definitions; }
    public void setDefinitions(List<String> definitions) { this.definitions = definitions; }

    public List<String> getPronunciations() { return pronunciations; }
    public void setPronunciations(List<String> pronunciations) { this.pronunciations = pronunciations; }

    // Learning progress getters and setters
    public boolean isAccessible() { return isAccessible; }
    public void setAccessible(boolean accessible) { isAccessible = accessible; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    // Helper methods for display
    public String getTypeDisplayName() {
        if (type == null) return "📝 Văn bản";

        switch (type.toLowerCase()) {
            case "text":
                return "📝 Văn bản";
            case "video":
                return "🎥 Video";
            case "audio":
                return "🎵 Âm thanh";
            case "quiz":
                return "❓ Trắc nghiệm";
            case "grammar":
                return "📚 Ngữ pháp";
            case "vocabulary":
                return "📖 Từ vựng";
            default:
                return "📝 " + type;
        }
    }

    public String getEstimatedTimeString() {
        if (estimatedTime <= 0) return "⏱ Không xác định";

        if (estimatedTime < 60) {
            return "⏱ " + estimatedTime + " phút";
        } else {
            int hours = estimatedTime / 60;
            int minutes = estimatedTime % 60;
            if (minutes == 0) {
                return "⏱ " + hours + " giờ";
            } else {
                return "⏱ " + hours + "h " + minutes + "m";
            }
        }
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", estimatedTime=" + estimatedTime +
                ", order=" + order +
                ", isPublished=" + isPublished +
                '}';
    }
}
