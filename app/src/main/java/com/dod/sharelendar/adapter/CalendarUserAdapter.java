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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dod.sharelendar.R;
import com.dod.sharelendar.UserCalendarOptionActivity;
import com.dod.sharelendar.data.UserModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class CalendarUserAdapter extends RecyclerView.Adapter<CalendarUserAdapter.ViewHolder> {

    List<UserModel> list;
    Map<String, String> map;
    String calUuid;
    Context context;

    private String url = "https://firebasestorage.googleapis.com/v0/b/sharelendar-8841b.appspot.com/o/";
    private String endUrl = "?alt=media";

    public CalendarUserAdapter(List<UserModel> list, Map<String, String> map, String calUuid, Context context) {
        this.list = list;
        this.map = map;
        this.calUuid = calUuid;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.calendar_user_list, parent, false) ;
        CalendarUserAdapter.ViewHolder vh = new CalendarUserAdapter.ViewHolder(view) ;

        return vh ;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel vo = list.get(position);

        try {
            Glide.with(context)
                    .load(url + urlEncoding(vo.getProfileImg()) + endUrl)
                    .override(160, 160)
                    .centerCrop()
                    .into(holder.image);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        holder.nickname.setText(vo.getNickname());

        String div = map.get(vo.getEmail());
        holder.div.setText("권한 : " + getKoreanDiv(div));

        holder.layout.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserCalendarOptionActivity.class);
            intent.putExtra("calUuid", calUuid);
            intent.putExtra("div", div);
            intent.putExtra("user", vo);
            ((Activity)context).startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public String urlEncoding(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }

    private String getKoreanDiv(String div){
        if(div.equals("host")){
            return "방장";
        }else if(div.equals("admin")){
            return "관리자";
        }else {
            return "일반";
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout layout;
        ImageView image;
        TextView nickname;
        TextView div;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.layout);
            image = itemView.findViewById(R.id.image);
            nickname = itemView.findViewById(R.id.nickname);
            div = itemView.findViewById(R.id.div);
        }
    }
}
