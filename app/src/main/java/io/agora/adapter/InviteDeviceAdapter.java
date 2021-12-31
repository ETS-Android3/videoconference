package io.agora.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.agora.openvcall.R;
import io.agora.responsemodels.RoomRequest;


public class InviteDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<RoomRequest> list;
    private Listener listener;
    private OnItemClickListener onItemClickListener;

    public InviteDeviceAdapter(Context context, ArrayList<RoomRequest> list, OnItemClickListener onItemClick) {
        this.context = context;
        this.list = list;
        this.onItemClickListener = onItemClick;
    }


    public interface OnItemClickListener {
        void onItemClick(String id, boolean isChecked);
    }


    @Override
    public @NonNull
    RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_invite_device_layout, parent, false);

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

        CheckedTextView tvRoomName;
        AppCompatCheckBox chkDevice;
       // LinearLayout llRoomName;


        public MyViewHolder(View itemView) {
            super(itemView);

        //    llRoomName = (LinearLayout) itemView.findViewById(R.id.llRoomName);
            tvRoomName = (CheckedTextView) itemView.findViewById(R.id.tvRoomName);
            chkDevice = (AppCompatCheckBox) itemView.findViewById(R.id.chk_device);
            setFont(tvRoomName);


        }

        public void bind(int position, RoomRequest roomRequest) {
            tvRoomName.setText(roomRequest.getDeviceName());
//            tvRoomName.setCheckMarkDrawable(R.drawable.empty_checkbox);
//            tvRoomName.setChecked(false);
           // llRoomName.setTag(0);
//            chkDevice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//                    if (isChecked) {
//
//                        onItemClickListener.onItemClick(roomRequest.getId(), isChecked);
//                        System.out.println("device_id===" + roomRequest.getId());
//                        System.out.println("isChecked===" + isChecked);
//                    } else {
//
//                        onItemClickListener.onItemClick(roomRequest.getId(), isChecked);
//                        System.out.println("device_id===" + roomRequest.getId());
//                        System.out.println("isChecked===" + isChecked);
//                    }
//                }
//            });




            tvRoomName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean value = tvRoomName.isChecked();
                    if (value) {
                        // set check mark drawable and set checked property to false

                        tvRoomName.setCheckMarkDrawable(R.drawable.empty_checkbox);
                        tvRoomName.setChecked(false);
                        onItemClickListener.onItemClick(roomRequest.getId(), !value);
                        System.out.println("device_id===" + roomRequest.getId());
                        System.out.println("value===" + value);
                    } else {
                        tvRoomName.setCheckMarkDrawable(R.drawable.fill_checkbox);
                        tvRoomName.setChecked(true);
                        onItemClickListener.onItemClick(roomRequest.getId(), !value);
                        System.out.println("device_id===" + roomRequest.getId());
                        System.out.println("isChecked===" + value);
                    }
                }
            });

//            llRoomName.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if ((Integer)llRoomName.getTag()==0) {
//                        llRoomName.setTag(1);
//                        onItemClickListener.onItemClick(roomRequest.getId(), true);
//                      //  System.out.println("device_id===" + roomRequest.getId());
//                       // System.out.println("isChecked===" + isChecked);
//                    } else {
//                        llRoomName.setTag(0);
//                        onItemClickListener.onItemClick(roomRequest.getId(), false);
//                       // System.out.println("device_id===" + roomRequest.getId());
//                        //System.out.println("isChecked===" + isChecked);
//                    }
//
//                }
//            });
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                switch (v.getId()) {

                    case R.id.tvRoomName:
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