package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CardView cardQuickFeedback, cardDetailedFeedback, cardSuggestions;
    private RatingBar ratingOverall, ratingContent, ratingInterface, ratingSupport;
    private RadioGroup rgFeedbackType, rgRecommend;
    private EditText etFeedbackText, etSuggestions, etEmail;
    private Spinner spinnerCategory;
    private Button btnSubmit, btnReset, btnViewHistory;
    private TextView tvFeedbackCount, tvThankYou;
    private RecyclerView rvFeedbackHistory;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        initViews();
        setupToolbar();
        setupClickListeners();
        loadUserFeedbackHistory();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        // Cards
        cardQuickFeedback = findViewById(R.id.card_quick_feedback);
        cardDetailedFeedback = findViewById(R.id.card_detailed_feedback);
        cardSuggestions = findViewById(R.id.card_suggestions);

        // Rating bars
        ratingOverall = findViewById(R.id.rating_overall);
        ratingContent = findViewById(R.id.rating_content);
        ratingInterface = findViewById(R.id.rating_interface);
        ratingSupport = findViewById(R.id.rating_support);

        // Radio groups
        rgFeedbackType = findViewById(R.id.rg_feedback_type);
        rgRecommend = findViewById(R.id.rg_recommend);

        // EditTexts
        etFeedbackText = findViewById(R.id.et_feedback_text);
        etSuggestions = findViewById(R.id.et_suggestions);
        etEmail = findViewById(R.id.et_email);

        // Spinner
        spinnerCategory = findViewById(R.id.spinner_category);

        // Buttons
        btnSubmit = findViewById(R.id.btn_submit);
        btnReset = findViewById(R.id.btn_reset);
        btnViewHistory = findViewById(R.id.btn_view_history);

        // TextViews
        tvFeedbackCount = findViewById(R.id.tv_feedback_count);
        tvThankYou = findViewById(R.id.tv_thank_you);

        // RecyclerView
        rvFeedbackHistory = findViewById(R.id.rv_feedback_history);

        if (rvFeedbackHistory != null) {
            rvFeedbackHistory.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Phản hồi & Đánh giá");
        }
    }

    private void setupClickListeners() {
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitFeedback());
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetForm());
        }

        if (btnViewHistory != null) {
            btnViewHistory.setOnClickListener(v -> toggleFeedbackHistory());
        }

        // Card clicks for different feedback types
        if (cardQuickFeedback != null) {
            cardQuickFeedback.setOnClickListener(v -> focusOnQuickFeedback());
        }

        if (cardDetailedFeedback != null) {
            cardDetailedFeedback.setOnClickListener(v -> focusOnDetailedFeedback());
        }

        if (cardSuggestions != null) {
            cardSuggestions.setOnClickListener(v -> focusOnSuggestions());
        }

        // Rating bar listeners
        setupRatingListeners();
    }

    private void setupRatingListeners() {
        if (ratingOverall != null) {
            ratingOverall.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    showRatingFeedback("overall", rating);
                }
            });
        }

        if (ratingContent != null) {
            ratingContent.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    showRatingFeedback("content", rating);
                }
            });
        }

        if (ratingInterface != null) {
            ratingInterface.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    showRatingFeedback("interface", rating);
                }
            });
        }

        if (ratingSupport != null) {
            ratingSupport.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    showRatingFeedback("support", rating);
                }
            });
        }
    }

    private void showRatingFeedback(String category, float rating) {
        String message = "";
        if (rating >= 4) {
            message = "Cảm ơn bạn đã đánh giá cao!";
        } else if (rating >= 3) {
            message = "Cảm ơn! Chúng tôi sẽ cải thiện hơn.";
        } else {
            message = "Xin lỗi! Hãy cho chúng tôi biết cách cải thiện.";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void focusOnQuickFeedback() {
        // Expand quick feedback section and highlight it
        if (cardQuickFeedback != null) {
            cardQuickFeedback.setCardElevation(8f);
        }
        if (cardDetailedFeedback != null) {
            cardDetailedFeedback.setCardElevation(2f);
        }
        if (cardSuggestions != null) {
            cardSuggestions.setCardElevation(2f);
        }
        Toast.makeText(this, "Chế độ phản hồi nhanh", Toast.LENGTH_SHORT).show();
    }

    private void focusOnDetailedFeedback() {
        // Expand detailed feedback section
        if (cardDetailedFeedback != null) {
            cardDetailedFeedback.setCardElevation(8f);
        }
        if (cardQuickFeedback != null) {
            cardQuickFeedback.setCardElevation(2f);
        }
        if (cardSuggestions != null) {
            cardSuggestions.setCardElevation(2f);
        }
        Toast.makeText(this, "Chế độ phản hồi chi tiết", Toast.LENGTH_SHORT).show();
    }

    private void focusOnSuggestions() {
        // Focus on suggestions section
        if (cardSuggestions != null) {
            cardSuggestions.setCardElevation(8f);
        }
        if (cardQuickFeedback != null) {
            cardQuickFeedback.setCardElevation(2f);
        }
        if (cardDetailedFeedback != null) {
            cardDetailedFeedback.setCardElevation(2f);
        }
        if (etSuggestions != null) {
            etSuggestions.requestFocus();
        }
    }

    private void submitFeedback() {
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để gửi phản hồi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect feedback data
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("userId", userId);
        feedbackData.put("timestamp", System.currentTimeMillis());

        // Ratings
        if (ratingOverall != null) {
            feedbackData.put("ratingOverall", ratingOverall.getRating());
        }
        if (ratingContent != null) {
            feedbackData.put("ratingContent", ratingContent.getRating());
        }
        if (ratingInterface != null) {
            feedbackData.put("ratingInterface", ratingInterface.getRating());
        }
        if (ratingSupport != null) {
            feedbackData.put("ratingSupport", ratingSupport.getRating());
        }

        // Text feedback
        if (etFeedbackText != null) {
            feedbackData.put("feedbackText", etFeedbackText.getText().toString().trim());
        }
        if (etSuggestions != null) {
            feedbackData.put("suggestions", etSuggestions.getText().toString().trim());
        }

        // Feedback type
        if (rgFeedbackType != null && rgFeedbackType.getCheckedRadioButtonId() != -1) {
            RadioButton selectedType = findViewById(rgFeedbackType.getCheckedRadioButtonId());
            if (selectedType != null) {
                feedbackData.put("feedbackType", selectedType.getText().toString());
            }
        }

        // Recommendation
        if (rgRecommend != null && rgRecommend.getCheckedRadioButtonId() != -1) {
            RadioButton selectedRecommend = findViewById(rgRecommend.getCheckedRadioButtonId());
            if (selectedRecommend != null) {
                feedbackData.put("wouldRecommend", selectedRecommend.getText().toString());
            }
        }

        // Validate required fields
        String feedbackText = etFeedbackText != null ? etFeedbackText.getText().toString().trim() : "";
        if (feedbackText.isEmpty() && ratingOverall != null && ratingOverall.getRating() == 0) {
            Toast.makeText(this, "Vui lòng nhập phản hồi hoặc đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable submit button
        if (btnSubmit != null) {
            btnSubmit.setEnabled(false);
            btnSubmit.setText("Đang gửi...");
        }

        // Submit to Firestore
        db.collection("feedback")
                .add(feedbackData)
                .addOnSuccessListener(documentReference -> {
                    showThankYouMessage();
                    resetForm();
                    updateFeedbackCount();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi gửi phản hồi: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Re-enable submit button
                    if (btnSubmit != null) {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Gửi phản hồi");
                    }
                });
    }

    private void showThankYouMessage() {
        if (tvThankYou != null) {
            tvThankYou.setVisibility(View.VISIBLE);
            tvThankYou.setText("Cảm ơn bạn đã gửi phản hồi! Ý kiến của bạn rất quan trọng với chúng tôi.");
        }
        Toast.makeText(this, "Phản hồi đã được gửi thành công!", Toast.LENGTH_LONG).show();
    }

    private void resetForm() {
        // Reset all form fields
        if (ratingOverall != null) ratingOverall.setRating(0);
        if (ratingContent != null) ratingContent.setRating(0);
        if (ratingInterface != null) ratingInterface.setRating(0);
        if (ratingSupport != null) ratingSupport.setRating(0);

        if (etFeedbackText != null) etFeedbackText.setText("");
        if (etSuggestions != null) etSuggestions.setText("");

        if (rgFeedbackType != null) rgFeedbackType.clearCheck();
        if (rgRecommend != null) rgRecommend.clearCheck();

        if (tvThankYou != null) tvThankYou.setVisibility(View.GONE);

        // Re-enable submit button
        if (btnSubmit != null) {
            btnSubmit.setEnabled(true);
            btnSubmit.setText("Gửi phản hồi");
        }

        // Reset card elevations
        resetCardElevations();
    }

    private void resetCardElevations() {
        if (cardQuickFeedback != null) cardQuickFeedback.setCardElevation(4f);
        if (cardDetailedFeedback != null) cardDetailedFeedback.setCardElevation(4f);
        if (cardSuggestions != null) cardSuggestions.setCardElevation(4f);
    }

    private void toggleFeedbackHistory() {
        if (rvFeedbackHistory != null) {
            int visibility = rvFeedbackHistory.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            rvFeedbackHistory.setVisibility(visibility);

            if (btnViewHistory != null) {
                btnViewHistory.setText(visibility == View.VISIBLE ? "Ẩn lịch sử" : "Xem lịch sử");
            }
        }
    }

    private void loadUserFeedbackHistory() {
        if (userId != null) {
            db.collection("feedback")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        updateFeedbackCount();
                        // Implementation to load feedback history into RecyclerView
                    })
                    .addOnFailureListener(e -> {
                        // Handle error silently
                    });
        }
    }

    private void updateFeedbackCount() {
        if (userId != null) {
            db.collection("feedback")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int count = queryDocumentSnapshots.size();
                        if (tvFeedbackCount != null) {
                            tvFeedbackCount.setText("Bạn đã gửi " + count + " phản hồi");
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Navigate back to dashboard
        Intent intent = new Intent(this, StudentDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}