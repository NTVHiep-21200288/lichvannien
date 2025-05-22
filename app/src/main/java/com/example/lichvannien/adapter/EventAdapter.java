package com.example.lichvannien.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lichvannien.R;
import com.example.lichvannien.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private Context context;
    private OnEventClickListener onEventClickListener;

    public EventAdapter(Context context) {
        this.context = context;
    }

    public static void setupLinearLayoutManager(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
        holder.itemView.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateData(List<Event> newEvents) {
        this.events.clear();
        if (newEvents != null) {
            this.events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.onEventClickListener = listener;
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private View viewEventTypeIndicator;
        private TextView tvEventTitle;
        private TextView tvEventTime;
        private TextView tvEventNote;
        private ImageView ivEventType;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            viewEventTypeIndicator = itemView.findViewById(R.id.viewEventTypeIndicator);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventNote = itemView.findViewById(R.id.tvEventNote);
            ivEventType = itemView.findViewById(R.id.ivEventType);
        }

        public void bind(Event event) {
            tvEventTitle.setText(event.getTitle());
            
            // Set time display
            if (event.isAllDay()) {
                tvEventTime.setText("Cả ngày");
            } else {
                String timeText = event.getStartTime();
                if (!TextUtils.isEmpty(event.getEndTime())) {
                    timeText += " - " + event.getEndTime();
                }
                tvEventTime.setText(timeText);
            }
            
            // Set note if available
            if (!TextUtils.isEmpty(event.getNote())) {
                tvEventNote.setText(event.getNote());
                tvEventNote.setVisibility(View.VISIBLE);
            } else {
                tvEventNote.setVisibility(View.GONE);
            }
            
            // Set event type indicator color and icon
            setEventTypeAppearance(event.getEventType());
        }
        
        private void setEventTypeAppearance(String eventType) {
            int color = context.getResources().getColor(R.color.primary_color);
            int iconRes = R.drawable.ic_event;
            
            if (eventType != null) {
                switch (eventType.toLowerCase()) {
                    case "công việc":
                        color = Color.parseColor("#4285F4"); // Blue
                        iconRes = R.drawable.ic_category;
                        break;
                    case "cá nhân":
                        color = Color.parseColor("#34A853"); // Green
                        iconRes = R.drawable.ic_event;
                        break;
                    case "gia đình":
                        color = Color.parseColor("#FBBC05"); // Yellow
                        iconRes = R.drawable.ic_event;
                        break;
                    case "ngày lễ":
                        color = Color.parseColor("#EA4335"); // Red
                        iconRes = R.drawable.ic_event;
                        break;
                    case "nhắc nhở":
                        color = Color.parseColor("#9C27B0"); // Purple
                        iconRes = R.drawable.ic_alarm;
                        break;
                }
            }
            
            // Apply styles
            viewEventTypeIndicator.setBackgroundColor(color);
            ivEventType.setImageResource(iconRes);
        }
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }
}
