package io.agora.adapter;


import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.agora.openvcall.AGApplication;
import io.agora.openvcall.R;
import io.agora.openvcall.ui.layout.GridVideoViewContainer;
import io.agora.responsemodels.RoomRequest;


public class GridViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {


    private Context context;
    private ArrayList<RoomRequest> list;
    private Listener listener;


    public GridViewAdapter(Context context, ArrayList<RoomRequest> list, Listener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;

    }



    @Override
    public @NonNull
    RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.grid_item_new_device, parent, false);

        return new MyViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            RoomRequest roomRequest = list.get(position);
            ((MyViewHolder) holder).bind(position, roomRequest);
        }





    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }




    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvRoomName, tvDeviceDescription;
        ImageView imgStatus;
        GridVideoViewContainer mGridVideoViewContainer;
        LinearLayout llView;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvRoomName = (TextView) itemView.findViewById(R.id.tvRoomName);
            imgStatus = (ImageView) itemView.findViewById(R.id.img_status);
            tvDeviceDescription = (TextView) itemView.findViewById(R.id.tv_device_description);
            llView = (LinearLayout) itemView.findViewById(R.id.llView);
            mGridVideoViewContainer = (GridVideoViewContainer) itemView.findViewById(R.id.grid_video_view_container);
            setFont(tvRoomName);
         //   setFont(tvActiveUsers);
            llView.setOnClickListener(this);

        }

        public void bind(int position, RoomRequest roomRequest) {

            tvRoomName.setText(roomRequest.getDeviceName());


            if (Integer.parseInt(roomRequest.getActiveUserCount()) >= 2) {
                imgStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.radio_active));
             //   tvActiveUsers.setText(roomRequest.getActiveUserCount());
            } else {
                imgStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.radio_inactive));
               // tvActiveUsers.setText(roomRequest.getActiveUserCount());
            }

             tvDeviceDescription.setText(roomRequest.getDeviceDescription());

        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                switch (v.getId()) {

                    case R.id.llView:

                        listener.onClick(getAdapterPosition());

                        break;

                }
            }
        }
    }


    public interface Listener {

        void onClick(int position);

    }

    public void setFont(TextView tv) {
        Typeface face = Typeface.createFromAsset(this.context.getAssets(), "fonts/Poppins-Light.ttf");
        tv.setTypeface(face);
    }
}