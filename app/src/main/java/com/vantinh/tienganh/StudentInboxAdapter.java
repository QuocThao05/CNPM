package com.vantinh.tienganh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudentInboxAdapter extends RecyclerView.Adapter<StudentInboxAdapter.InboxViewHolder> {

    private List<InboxMessage> messageList;
    private OnMessageClickListener listener;

    public interface OnMessageClickListener {
        void onMessageClick(InboxMessage message);
    }

    public StudentInboxAdapter(List<InboxMessage> messageList, OnMessageClickListener listener) {
        this.messageList = messageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inbox_message, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        InboxMessage message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class InboxViewHolder extends RecyclerView.ViewHolder {
        private CardView cardMessage;
        private TextView tvMessageType;
        private TextView tvTitle;
        private TextView tvPreview;
        private TextView tvFromName;
        private TextView tvDate;
        private TextView tvCourseName;
        private View viewUnreadIndicator;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMessage = itemView.findViewById(R.id.card_message);
            tvMessageType = itemView.findViewById(R.id.tv_message_type);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPreview = itemView.findViewById(R.id.tv_preview);
            tvFromName = itemView.findViewById(R.id.tv_from_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            viewUnreadIndicator = itemView.findViewById(R.id.view_unread_indicator);

            cardMessage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessageClick(messageList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(InboxMessage message) {
            // Set message type
            tvMessageType.setText(message.getTypeDisplayName());

            // Set type color
            switch (message.getType()) {
                case "notification":
                    tvMessageType.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_light));
                    break;
                case "feedback_response":
                    tvMessageType.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_light));
                    break;
                default:
                    tvMessageType.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                    break;
            }

            // Set title
            tvTitle.setText(message.getTitle());

            // Set preview (first 100 characters of message)
            String preview = message.getMessage();
            if (preview != null && preview.length() > 100) {
                preview = preview.substring(0, 100) + "...";
            }
            tvPreview.setText(preview);

            // Set from name
            tvFromName.setText("Từ: " + message.getFromName());

            // Set date
            tvDate.setText(message.getFormattedDate());

            // Set course name if available
            if (message.getCourseName() != null && !message.getCourseName().isEmpty()) {
                tvCourseName.setVisibility(View.VISIBLE);
                tvCourseName.setText("Khóa học: " + message.getCourseName());
            } else {
                tvCourseName.setVisibility(View.GONE);
            }

            // Set read/unread indicator
            if (message.isRead()) {
                viewUnreadIndicator.setVisibility(View.GONE);
                cardMessage.setAlpha(0.8f);
                tvTitle.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            } else {
                viewUnreadIndicator.setVisibility(View.VISIBLE);
                cardMessage.setAlpha(1.0f);
                tvTitle.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            }
        }
    }
}
