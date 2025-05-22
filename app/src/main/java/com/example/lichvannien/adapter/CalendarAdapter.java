package com.example.lichvannien.adapter;

import android.util.Log;
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

    private static final String TAG = "CalendarAdapter";
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
        Log.d(TAG, "updateData called with " + (newDays != null ? newDays.size() : 0) + " days");
        this.calendarDays = newDays != null ? new ArrayList<>(newDays) : new ArrayList<>();
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
        if (position < calendarDays.size()) {
            CalendarDay day = calendarDays.get(position);
            if (day != null) {
                holder.bind(day, position);
            }
        }
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

            // Tìm các view components
            cardView = (CardView) itemView;
            if (cardView == null) {
                cardView = (CardView) itemView; // Fallback nếu không tìm thấy ID
            }
            tvSolarDay = itemView.findViewById(R.id.tvSolarDay);
            tvLunarDay = itemView.findViewById(R.id.tvLunarDay);
            tvCanChi = itemView.findViewById(R.id.tvCanChi);
            viewHolidayIndicator = itemView.findViewById(R.id.viewHolidayIndicator);

            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < calendarDays.size()) {
                    CalendarDay day = calendarDays.get(position);
                    if (day != null) {
                        selectedDay = day;
                        if (onDayClickListener != null) {
                            onDayClickListener.onDayClick(day);
                        }
                        notifyDataSetChanged();
                    }
                }
            });
        }

        public void bind(CalendarDay day, int position) {
            if (day == null) return;

            try {
                // Hiển thị ngày dương
                if (tvSolarDay != null) {
                    tvSolarDay.setText(String.valueOf(day.solarDay));
                }

                // Hiển thị ngày âm
                if (tvLunarDay != null) {
                    if (day.lunarDay == 1 && day.lunarMonth == 1) {
                        tvLunarDay.setText("TẾT");
                    } else {
                        tvLunarDay.setText(String.valueOf(day.lunarDay));
                    }
                }

                // Thiết lập màu sắc cho ngày dương
                setDayColors(day, position);

                // Hiển thị indicator cho ngày lễ
                setHolidayIndicator(day);

                // Hiển thị Can Chi cho ngày đặc biệt
                setCanChiDisplay(day);

            } catch (Exception e) {
                Log.e(TAG, "Error binding day: " + e.getMessage());
            }
        }

        private void setDayColors(CalendarDay day, int position) {
            if (tvSolarDay == null || tvLunarDay == null || cardView == null) return;

            try {
                if (day.isToday) {
                    // Ngày hôm nay - màu đỏ với nền trắng
                    tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                    tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                } else if (!day.isCurrentMonth) {
                    // Ngày không thuộc tháng hiện tại - màu xám nhạt
                    tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                    tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                } else if (day.equals(selectedDay)) {
                    // Ngày được chọn - màu xanh với nền
                    tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                    tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark));
                } else if (day.isHoliday) {
                    // Ngày lễ - màu đỏ
                    tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                    tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                } else {
                    // Ngày bình thường
                    int dayOfWeek = position % 7;
                    if (dayOfWeek == 0) {
                        // Chủ nhật - màu đỏ
                        tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                    } else if (dayOfWeek == 6) {
                        // Thứ 7 - màu xanh
                        tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark));
                    } else {
                        // Các ngày trong tuần - màu đen
                        tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
                    }
                    tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting day colors: " + e.getMessage());
                // Fallback màu sắc mặc định
                tvSolarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
                tvLunarDay.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            }
        }

        private void setHolidayIndicator(CalendarDay day) {
            if (viewHolidayIndicator == null) return;

            try {
                if (day.isHoliday && day.isCurrentMonth) {
                    viewHolidayIndicator.setVisibility(View.VISIBLE);
                    viewHolidayIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                } else {
                    viewHolidayIndicator.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting holiday indicator: " + e.getMessage());
                viewHolidayIndicator.setVisibility(View.GONE);
            }
        }

        private void setCanChiDisplay(CalendarDay day) {
            if (tvCanChi == null) return;

            try {
                // Hiển thị Can Chi cho ngày 1 và 15 âm lịch
                if (day.isCurrentMonth && (day.lunarDay == 1 || day.lunarDay == 15)) {
                    String[] canChiParts = day.canChi.split(" ");
                    tvCanChi.setText(canChiParts.length > 1 ? canChiParts[1] : day.canChi);
                    tvCanChi.setVisibility(View.VISIBLE);

                    if (day.isToday || day.equals(selectedDay)) {
                        tvCanChi.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                    } else {
                        tvCanChi.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark));
                    }
                } else {
                    tvCanChi.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting Can Chi display: " + e.getMessage());
                tvCanChi.setVisibility(View.GONE);
            }
        }
    }
}