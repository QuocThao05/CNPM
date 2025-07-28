package com.vantinh.tienganh;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class DataMigrationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvStatus;
    private Button btnMigrateEnrollments;
    private Button btnCheckEnrollments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_migration);

        db = FirebaseFirestore.getInstance();
        
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        btnMigrateEnrollments = findViewById(R.id.btn_migrate_enrollments);
        btnCheckEnrollments = findViewById(R.id.btn_check_enrollments);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Data Migration");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        btnCheckEnrollments.setOnClickListener(v -> checkEnrollmentsStatus());
        btnMigrateEnrollments.setOnClickListener(v -> migrateEnrollmentsData());
    }

    private void checkEnrollmentsStatus() {
        updateStatus("Đang kiểm tra dữ liệu enrollments...");
        
        db.collection("enrollments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalEnrollments = queryDocumentSnapshots.size();
                    int enrollmentsWithStatus = 0;
                    int enrollmentsWithoutStatus = 0;
                    
                    StringBuilder report = new StringBuilder();
                    report.append("=== KIỂM TRA ENROLLMENTS ===\n");
                    report.append("Tổng số enrollments: ").append(totalEnrollments).append("\n\n");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String docId = doc.getId();
                        String studentId = doc.getString("studentID");
                        String courseId = doc.getString("courseID");
                        String status = doc.getString("status");
                        
                        if (status != null && !status.isEmpty()) {
                            enrollmentsWithStatus++;
                            report.append("✓ ").append(docId).append(" - Status: ").append(status).append("\n");
                        } else {
                            enrollmentsWithoutStatus++;
                            report.append("✗ ").append(docId).append(" - THIẾU STATUS (Student: ")
                                   .append(studentId).append(", Course: ").append(courseId).append(")\n");
                        }
                    }
                    
                    report.append("\n=== TỔNG KẾT ===\n");
                    report.append("Có status: ").append(enrollmentsWithStatus).append("\n");
                    report.append("Thiếu status: ").append(enrollmentsWithoutStatus).append("\n");
                    
                    if (enrollmentsWithoutStatus > 0) {
                        report.append("\n⚠️ CẦN MIGRATION DỮ LIỆU!");
                    } else {
                        report.append("\n✅ TẤT CẢ DỮ LIỆU ĐÃ ĐẦY ĐỦ!");
                    }

                    updateStatus(report.toString());
                    
                    Log.d("DataMigration", report.toString());
                })
                .addOnFailureListener(e -> {
                    String error = "Lỗi kiểm tra dữ liệu: " + e.getMessage();
                    updateStatus(error);
                    Log.e("DataMigration", error, e);
                });
    }

    private void migrateEnrollmentsData() {
        updateStatus("Đang bắt đầu migration dữ liệu...");
        
        db.collection("enrollments")
                .whereEqualTo("status", null) // Tìm các document không có trường status
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        updateStatus("✅ Không có dữ liệu nào cần migration!");
                        return;
                    }
                    
                    int totalToMigrate = queryDocumentSnapshots.size();
                    updateStatus("Tìm thấy " + totalToMigrate + " enrollments cần thêm status...");
                    
                    final int[] migratedCount = {0};
                    final int[] errorCount = {0};
                    
                    // Thêm trường status = "approved" cho tất cả enrollments hiện có
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String docId = doc.getId();
                        
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("status", "approved"); // Mặc định các enrollment hiện có đều là đã được duyệt
                        
                        db.collection("enrollments").document(docId)
                                .update(updateData)
                                .addOnSuccessListener(aVoid -> {
                                    migratedCount[0]++;
                                    Log.d("DataMigration", "Updated enrollment " + docId + " with status = approved");
                                    
                                    // Kiểm tra xem đã hoàn thành chưa
                                    if (migratedCount[0] + errorCount[0] == totalToMigrate) {
                                        String finalReport = "=== MIGRATION HOÀN THÀNH ===\n" +
                                                "Tổng số: " + totalToMigrate + "\n" +
                                                "Thành công: " + migratedCount[0] + "\n" +
                                                "Lỗi: " + errorCount[0] + "\n\n" +
                                                "✅ Bây giờ bạn có thể test lại ứng dụng!";
                                        updateStatus(finalReport);
                                        
                                        Toast.makeText(this, "Migration hoàn thành!", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    errorCount[0]++;
                                    Log.e("DataMigration", "Error updating enrollment " + docId, e);
                                    
                                    // Kiểm tra xem đã hoàn thành chưa
                                    if (migratedCount[0] + errorCount[0] == totalToMigrate) {
                                        String finalReport = "=== MIGRATION HOÀN THÀNH (CÓ LỖI) ===\n" +
                                                "Tổng số: " + totalToMigrate + "\n" +
                                                "Thành công: " + migratedCount[0] + "\n" +
                                                "Lỗi: " + errorCount[0] + "\n\n" +
                                                "⚠️ Có một số lỗi xảy ra!";
                                        updateStatus(finalReport);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    String error = "Lỗi migration: " + e.getMessage();
                    updateStatus(error);
                    Log.e("DataMigration", error, e);
                });
        
        // Ngoài ra, cũng tìm và cập nhật những enrollment không có trường status
        db.collection("enrollments")
                .get()
                .addOnSuccessListener(allDocs -> {
                    for (QueryDocumentSnapshot doc : allDocs) {
                        if (!doc.contains("status")) {
                            String docId = doc.getId();
                            
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("status", "approved");
                            
                            db.collection("enrollments").document(docId)
                                    .update(updateData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("DataMigration", "Added status field to enrollment " + docId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("DataMigration", "Error adding status to enrollment " + docId, e);
                                    });
                        }
                    }
                });
    }

    private void updateStatus(String status) {
        runOnUiThread(() -> {
            tvStatus.setText(status);
            Log.d("DataMigration", status);
        });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
