package com.example.simpleexpensetracker.ui.theme;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.simpleexpensetracker.DatabaseHelper;
import com.example.simpleexpensetracker.MainActivity;
import com.example.simpleexpensetracker.R;
import com.example.simpleexpensetracker.data.Notification;
import com.example.simpleexpensetracker.ui.theme.adapter.NotificationAdapter;
import java.util.ArrayList;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ArrayList<Notification> notificationList;
    private DatabaseHelper db;
    private TextView tvNoNotifications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        db = new DatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications);
        TextView tvClearAll = view.findViewById(R.id.tvClearAll);

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        db.markAllNotificationsAsRead();

        tvClearAll.setOnClickListener(v -> {
            db.deleteAllNotifications();
            loadNotifications();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateNotificationBadge();
        }
    }

    private void loadNotifications() {
        if (getContext() == null || db == null) {
            return;
        }

        notificationList.clear();
        Cursor cursor = db.getAllNotifications();
        if (cursor != null) {
            int messageIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_MESSAGE);
            int timestampIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_TIMESTAMP);

            while (cursor.moveToNext()) {
                notificationList.add(new Notification(
                        cursor.getString(messageIndex),
                        cursor.getString(timestampIndex)
                ));
            }
            cursor.close();
        }

        if (notificationList.isEmpty()) {
            tvNoNotifications.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoNotifications.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }
}
