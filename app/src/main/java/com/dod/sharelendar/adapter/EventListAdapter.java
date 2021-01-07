package com.dod.sharelendar.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dod.sharelendar.R;
import com.dod.sharelendar.data.EventModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder>{

    List<EventModel> list;
    Context context;

    FirebaseFirestore db;

    public EventListAdapter(List<EventModel> list, Context context) {
        this.list = list;
        this.context = context;

        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EventListAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventModel vo = list.get(position);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 수정 페이지(권한 확인 및 자기껀지 확인)
            }
        });

        holder.color.setBackgroundColor(Color.parseColor(vo.getColor()));
        holder.name.setText(vo.getEventName());
        if(vo.getEventComment().isEmpty()){
            holder.comment.setVisibility(View.GONE);
        }else {
            holder.comment.setText(vo.getEventComment());
        }
        holder.nickName.setText(vo.getUserNickname());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout layout;
        View color;
        TextView name;
        TextView comment;
        TextView nickName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.layout);
            color = itemView.findViewById(R.id.event_color);
            name = itemView.findViewById(R.id.event_name);
            comment = itemView.findViewById(R.id.event_comment);
            nickName = itemView.findViewById(R.id.nickname);
        }
    }
}
