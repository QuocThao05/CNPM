package com.vantinh.tienganh.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import com.vantinh.tienganh.R;
import java.util.ArrayList;
import java.util.List;

public class QuestionViewHelper {

    public static class QuestionViewData {
        public View questionView;
        public EditText etQuestion;
        public EditText[] etOptions;
        public RadioGroup rgCorrectAnswer;
        public TextView tvQuestionNumber;
        public ImageButton btnDelete;

        public QuestionViewData() {
            etOptions = new EditText[4];
        }
    }

    private Context context;
    private LinearLayout questionsContainer;
    private List<QuestionViewData> questionViews;
    private OnQuestionCountChangeListener listener;

    public interface OnQuestionCountChangeListener {
        void onQuestionCountChanged(int count);
    }

    public QuestionViewHelper(Context context, LinearLayout questionsContainer) {
        this.context = context;
        this.questionsContainer = questionsContainer;
        this.questionViews = new ArrayList<>();
    }

    public void setOnQuestionCountChangeListener(OnQuestionCountChangeListener listener) {
        this.listener = listener;
    }

    public void addQuestion() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View questionView = inflater.inflate(R.layout.item_dynamic_quiz_question, questionsContainer, false);

        QuestionViewData questionData = new QuestionViewData();
        questionData.questionView = questionView;
        questionData.tvQuestionNumber = questionView.findViewById(R.id.tv_question_number);
        questionData.etQuestion = questionView.findViewById(R.id.et_question);
        questionData.etOptions[0] = questionView.findViewById(R.id.et_option_a);
        questionData.etOptions[1] = questionView.findViewById(R.id.et_option_b);
        questionData.etOptions[2] = questionView.findViewById(R.id.et_option_c);
        questionData.etOptions[3] = questionView.findViewById(R.id.et_option_d);
        questionData.rgCorrectAnswer = questionView.findViewById(R.id.rg_correct_answer);
        questionData.btnDelete = questionView.findViewById(R.id.btn_delete_question);

        // Set unique IDs for RadioButtons to avoid conflicts
        int questionIndex = questionViews.size();
        int baseId = 2000 + (questionIndex * 10);
        RadioButton rbA = questionView.findViewById(R.id.rb_option_a);
        RadioButton rbB = questionView.findViewById(R.id.rb_option_b);
        RadioButton rbC = questionView.findViewById(R.id.rb_option_c);
        RadioButton rbD = questionView.findViewById(R.id.rb_option_d);

        // Kiểm tra null pointer
        if (rbA == null || rbB == null || rbC == null || rbD == null) {
            android.util.Log.e("QuestionViewHelper", "RadioButton not found in layout!");
            return;
        }

        rbA.setId(baseId + 1);
        rbB.setId(baseId + 2);
        rbC.setId(baseId + 3);
        rbD.setId(baseId + 4);

        // Setup toggle functionality for RadioButtons với log debug
        android.util.Log.d("QuestionViewHelper", "Setting up RadioButton listeners for question " + (questionIndex + 1));
        setupRadioButtonToggle(questionData.rgCorrectAnswer, rbA, "A");
        setupRadioButtonToggle(questionData.rgCorrectAnswer, rbB, "B");
        setupRadioButtonToggle(questionData.rgCorrectAnswer, rbC, "C");
        setupRadioButtonToggle(questionData.rgCorrectAnswer, rbD, "D");

        // Set delete button click listener
        if (questionData.btnDelete != null) {
            questionData.btnDelete.setOnClickListener(v -> removeQuestion(questionData));
        }

        questionViews.add(questionData);
        questionsContainer.addView(questionView);

        updateQuestionNumbers();
        updateDeleteButtonsVisibility();

        if (listener != null) {
            listener.onQuestionCountChanged(questionViews.size());
        }
    }

    private void setupRadioButtonToggle(RadioGroup radioGroup, RadioButton radioButton, String optionName) {
        android.util.Log.d("QuestionViewHelper", "Setting up basic listener for option " + optionName);

        // Loại bỏ hoàn toàn logic toggle phức tạp - chỉ để RadioGroup tự quản lý
        // Không cần OnClickListener custom vì RadioButton có sẵn behavior đúng
        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                android.util.Log.d("QuestionViewHelper", "Option " + optionName + " is now selected");
            }
        });
    }

    private void clearAllTags(RadioGroup radioGroup) {
        // Clear tags của tất cả RadioButton trong group
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View child = radioGroup.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout linearLayout = (LinearLayout) child;
                for (int j = 0; j < linearLayout.getChildCount(); j++) {
                    View subChild = linearLayout.getChildAt(j);
                    if (subChild instanceof RadioButton) {
                        subChild.setTag(null);
                    }
                }
            } else if (child instanceof RadioButton) {
                child.setTag(null);
            }
        }
    }

    private void clearOtherButtonTags(RadioGroup radioGroup, RadioButton selectedButton) {
        RadioButton rbA = radioGroup.findViewById(R.id.rb_option_a);
        RadioButton rbB = radioGroup.findViewById(R.id.rb_option_b);
        RadioButton rbC = radioGroup.findViewById(R.id.rb_option_c);
        RadioButton rbD = radioGroup.findViewById(R.id.rb_option_d);

        if (rbA != null && rbA != selectedButton) rbA.setTag(false);
        if (rbB != null && rbB != selectedButton) rbB.setTag(false);
        if (rbC != null && rbC != selectedButton) rbC.setTag(false);
        if (rbD != null && rbD != selectedButton) rbD.setTag(false);
    }

    private void setupRadioGroupChangeListener(RadioGroup radioGroup) {
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            android.util.Log.d("QuestionViewHelper", "RadioGroup checked changed to: " + checkedId);
        });
    }

    public void removeQuestion(QuestionViewData questionData) {
        if (questionViews.size() <= 2) {
            // Không cho phép xóa nếu chỉ còn 2 câu hỏi
            return;
        }

        questionsContainer.removeView(questionData.questionView);
        questionViews.remove(questionData);

        updateQuestionNumbers();
        updateDeleteButtonsVisibility();

        if (listener != null) {
            listener.onQuestionCountChanged(questionViews.size());
        }
    }

    public void removeLastQuestion() {
        if (questionViews.size() <= 2) {
            return;
        }

        QuestionViewData lastQuestion = questionViews.get(questionViews.size() - 1);
        removeQuestion(lastQuestion);
    }

    private void updateQuestionNumbers() {
        for (int i = 0; i < questionViews.size(); i++) {
            questionViews.get(i).tvQuestionNumber.setText("Câu hỏi " + (i + 1) + ":");
        }
    }

    private void updateDeleteButtonsVisibility() {
        boolean showDeleteButtons = questionViews.size() > 2;
        for (QuestionViewData questionData : questionViews) {
            questionData.btnDelete.setVisibility(showDeleteButtons ? View.VISIBLE : View.GONE);
        }
    }

    public List<QuestionViewData> getQuestionViews() {
        return questionViews;
    }

    public int getQuestionCount() {
        return questionViews.size();
    }

    public void initializeWithDefaultQuestions() {
        // Thêm 2 câu hỏi mặc định
        addQuestion();
        addQuestion();
    }
}
