package com.example.gps_bus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

    ArrayList<inform> lst;
    Context mContext;
    LayoutInflater mInflate;

    public RecyclerAdapter(Context context, ArrayList<inform> ifrm){
        this.lst=ifrm;
        this.mInflate=LayoutInflater.from(context);
        this.mContext=context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflate.from(parent.getContext()).inflate(R.layout.item, parent, false);
        ItemViewHolder viewHolder=new ItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder,
                                 int position) {
        // Item을 하나, 하나 보여주는(bind 되는) 함수입니다.
        holder.busName1.setText(lst.get(position).routeName);
        holder.low1.setText(lst.get(position).lowPlate1);
        holder.low3.setText(lst.get(position).lowPlate2);
        holder.predictTime1.setText(lst.get(position).predictTime1);
        holder.predictTime3.setText(lst.get(position).predictTime2);
        holder.location1.setText(lst.get(position).locationNo1);
        holder.location3.setText(lst.get(position).locationNo2);
        holder.remainSeat1.setText(lst.get(position).remainSeat1);
        holder.remainSeat3.setText(lst.get(position).remainSeat2);
        holder.busPass1.setText(lst.get(position).flag);
    }

    @Override
    public int getItemCount() {
        // RecyclerView의 총 개수 입니다.
        return lst.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView busName1;
        private TextView location1;
        private TextView predictTime1;
        private TextView low1;
        private TextView remainSeat1;
        private TextView location3;
        private TextView predictTime3;
        private TextView low3;
        private TextView remainSeat3;
        private TextView busPass1;

        public ItemViewHolder(View itemView) {
            super(itemView);

            busName1=itemView.findViewById(R.id.busName1);
            location1=itemView.findViewById(R.id.location1);
            predictTime1=itemView.findViewById(R.id.predictTime1);
            low1=itemView.findViewById(R.id.low1);
            remainSeat1=itemView.findViewById(R.id.remainSeat1);
            location3=itemView.findViewById(R.id.location3);
            predictTime3=itemView.findViewById(R.id.predictTime3);
            low3=itemView.findViewById(R.id.low3);
            remainSeat3=itemView.findViewById(R.id.remainSeat3);
            busPass1=itemView.findViewById(R.id.busPass1);
        }
    }
}

