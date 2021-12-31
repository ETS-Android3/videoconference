package io.agora.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.agora.openvcall.R;
import io.agora.responsemodels.NotificationList;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    private List<NotificationList> notificationList;
    private Context mContext;

    public NotificationAdapter(Context context, ArrayList<NotificationList> List) {
        mContext = context;
        notificationList = List;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNotification,tvNotificationTime;

        public MyViewHolder(View view) {
            super(view);
            tvNotification = view.findViewById(R.id.tv_notification);
            tvNotificationTime = view.findViewById(R.id.tv_notification_time);


        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {


        holder.tvNotification.setText(notificationList.get(position).getMessage());
        holder.tvNotificationTime.setText(notificationList.get(position).getTimeString());



    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

}