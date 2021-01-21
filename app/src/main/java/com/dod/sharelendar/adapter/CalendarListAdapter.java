package com.dod.sharelendar.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dod.sharelendar.CalendarActivity;
import com.dod.sharelendar.R;
import com.dod.sharelendar.data.CalendarModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class CalendarListAdapter extends RecyclerView.Adapter<CalendarListAdapter.ViewHolder> {

    private List<CalendarModel> list;
    private Context context;

    private String url = "https://firebasestorage.googleapis.com/v0/b/sharelendar-8841b.appspot.com/o/";
    private String endUrl = "?alt=media";

    public CalendarListAdapter(List<CalendarModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.calendar_list, parent, false) ;
        CalendarListAdapter.ViewHolder vh = new CalendarListAdapter.ViewHolder(view) ;

        return vh ;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarModel vo = list.get(position);

        try {
            Glide.with(context)
                    .load(url + urlEncoding(vo.getImg()) + endUrl)
                    .override(240, 160)
                    .centerCrop()
                    .into(holder.image);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        holder.name.setText(vo.getCalendarName());
        holder.host.setText("호스트 : " + vo.getHostNickname());

        holder.layout.setOnClickListener((View.OnClickListener) v -> {
            Intent intent = new Intent(context, CalendarActivity.class);
            intent.putExtra("uuid", vo.getUuid());
            intent.putExtra("calName", vo.getCalendarName());
            ((Activity)context).startActivity(intent);
        });
    }

    public String urlEncoding(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout layout;
        ImageView image;
        TextView name;
        TextView host;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.layout);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            host = itemView.findViewById(R.id.host);
        }
    }
}
