package com.example.simpleexpensetracker.ui.theme.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.simpleexpensetracker.R;
import com.example.simpleexpensetracker.data.Notification;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final ArrayList<Notification> notificationList;

    public NotificationAdapter(ArrayList<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTimestamp.setText(formatTimestamp(notification.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTimestamp = itemView.findViewById(R.id.tvNotificationTimestamp);
        }
    }

    private String formatTimestamp(String dbTimestamp) {
        if (dbTimestamp == null || dbTimestamp.isEmpty()) {
            return "";
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getDefault());

        try {
            Date date = inputFormat.parse(dbTimestamp);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return dbTimestamp;
        }
        return dbTimestamp;
    }
}
