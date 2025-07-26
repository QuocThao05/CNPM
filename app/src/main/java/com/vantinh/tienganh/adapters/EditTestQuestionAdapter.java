package com.vantinh.tienganh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vantinh.tienganh.R;
import com.vantinh.tienganh.models.SimpleTestQuestion;
import java.util.List;

public class EditTestQuestionAdapter extends RecyclerView.Adapter<EditTestQuestionAdapter.ViewHolder> {

    private List<SimpleTestQuestion> questions;
    private OnQuestionClickListener editListener;
    private OnQuestionClickListener deleteListener;

    public interface OnQuestionClickListener {
        void onQuestionClick(SimpleTestQuestion question);
    }

    public EditTestQuestionAdapter(List<SimpleTestQuestion> questions,
                                  OnQuestionClickListener editListener,
                                  OnQuestionClickListener deleteListener) {
        this.questions = questions;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_test_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SimpleTestQuestion question = questions.get(position);

        holder.tvQuestionNumber.setText("Câu " + (position + 1));
        holder.tvQuestion.setText(question.getQuestion());

        // Hiển thị các lựa chọn
        List<String> options = question.getCorrectAnswer();
        if (options != null && options.size() >= 4) {
            holder.tvOptionA.setText("A. " + options.get(0));
            holder.tvOptionB.setText("B. " + options.get(1));
            holder.tvOptionC.setText("C. " + options.get(2));
            holder.tvOptionD.setText("D. " + options.get(3));
        }

        // Hiển thị đáp án đúng
        int correctIndex = question.getOptions();
        String correctLabel = "";
        switch (correctIndex) {
            case 0: correctLabel = "A"; break;
            case 1: correctLabel = "B"; break;
            case 2: correctLabel = "C"; break;
            case 3: correctLabel = "D"; break;
        }
        holder.tvCorrectAnswer.setText("Đáp án đúng: " + correctLabel);

        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onQuestionClick(question);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onQuestionClick(question);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionNumber, tvQuestion, tvOptionA, tvOptionB, tvOptionC, tvOptionD, tvCorrectAnswer;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionNumber = itemView.findViewById(R.id.tv_question_number);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvOptionA = itemView.findViewById(R.id.tv_option_a);
            tvOptionB = itemView.findViewById(R.id.tv_option_b);
            tvOptionC = itemView.findViewById(R.id.tv_option_c);
            tvOptionD = itemView.findViewById(R.id.tv_option_d);
            tvCorrectAnswer = itemView.findViewById(R.id.tv_correct_answer);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
