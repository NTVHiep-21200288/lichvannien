package com.example.lichvannien.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lichvannien.R;
import com.example.lichvannien.model.CalendarDay;
import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarDayViewHolder> {

    private List<CalendarDay> calendarDays = new ArrayList<>();
    private CalendarDay selectedDay;
    private OnDayClickListener onDayClickListener;

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    public CalendarAdapter(OnDayClickListener onDayClickListener) {
        this.onDayClickListener = onDayClickListener;
    }

    public void updateData(List<CalendarDay> newDays) {
        this.calendarDays = newDays;
        notifyDataSetChanged();
    }

    public void setSelectedDay(CalendarDay day) {
        this.selectedDay = day;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarDayViewHolder holder, int position) {
        CalendarDay day = calendarDays.get(position);
        holder.bind(day, position);
    }

    @Override
    public int getItemCount() {
        return calendarDays.size();
    }

    public static void setupGridLayoutManager(RecyclerView recyclerView) {
        GridLayoutManager layoutManager = new GridLayoutManager(recyclerView.getContext(), 7);
        recyclerView.setLayoutManager(layoutManager);
    }

    public class CalendarDayViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvSolarDay;
        private TextView tvLunarDay;
        private TextView tvCanChi;
        private View viewHolidayIndicator;

        public CalendarDayViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvSolarDay = itemView.findViewById(R.id.tvSolarDay);
            tvLunarDay = itemView.findViewById(R.id.tvLunarDay);
            tvCanChi = itemView.findViewById(R.id.tvCanChi);
            viewHolidayIndicator = itemView.findViewById(R.id.viewHolidayIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CalendarDay day = calendarDays.get(position);
                    selectedDay = day;
                    if (onDayClickListener != null) {
                        onDayClickListener.onDayClick(day);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        public void bind(CalendarDay day, int position) {
            tvSolarDay.setText(String.valueOf(day.solarDay));
            tvLunarDay.setText(String.valueOf(day.lunarDay));

            // Set solar day color based on day type
            if (day.isToday) {
                tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.today_color));
            } else if (!day.isCurrentMonth) {
                tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_disabled));
                tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_disabled));
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.background_disabled));
            } else if (day.isHoliday) {
                tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.holiday_color));
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            } else if (day.equals(selectedDay)) {
                tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.selected_color));
            } else {
                // Check if it's Sunday or Saturday
                int dayOfWeek = position % 7;
                if (dayOfWeek == 0) { // Sunday
                    tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.sunday_color));
                } else if (dayOfWeek == 6) { // Saturday
                    tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.saturday_color));
                } else {
                    tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                }
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            }

            // Set lunar day color
            if (!day.isCurrentMonth) {
                tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_disabled));
            } else if (day.isToday || day.equals(selectedDay)) {
                tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            } else {
                tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }

            // Show holiday indicator
            if (day.isHoliday && day.isCurrentMonth) {
                viewHolidayIndicator.setVisibility(View.VISIBLE);
                viewHolidayIndicator.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.circle_indicator));
            } else {
                viewHolidayIndicator.setVisibility(View.GONE);
            }

            // Show Can Chi for special days (1st and 15th of lunar month)
            if (day.isCurrentMonth && (day.lunarDay == 1 || day.lunarDay == 15)) {
                String[] canChiParts = day.canChi.split(" ");
                tvCanChi.setText(canChiParts.length > 1 ? canChiParts[1] : day.canChi); // Only show Chi
                tvCanChi.setVisibility(View.VISIBLE);
                if (day.isToday || day.equals(selectedDay)) {
                    tvCanChi.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                } else {
                    tvCanChi.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.accent_color));
                }
            } else {
                tvCanChi.setVisibility(View.GONE);
            }

            // Special formatting for lunar new year
            if (day.lunarDay == 1 && day.lunarMonth == 1) {
                tvLunarDay.setText("TẾT");
                tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.holiday_color));
            }
        }
    }
}