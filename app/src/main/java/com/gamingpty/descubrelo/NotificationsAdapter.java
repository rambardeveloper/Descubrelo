package com.gamingpty.descubrelo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.MyViewHolder> {

    private List<Notification> NotificationsList;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView body;
        public TextView timestamp;
        public NetworkImageView thumb;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            body = (TextView) view.findViewById(R.id.body);
            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();
            //thumb = (NetworkImageView) view.findViewById(R.id.thumb);
            timestamp = (TextView) view.findViewById(R.id.timestamp);
        }
    }


    public NotificationsAdapter(List<Notification> NotificationsList) {
        this.NotificationsList = NotificationsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notifications_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Notification Notification = NotificationsList.get(position);
        //holder.thumb.setImageUrl(Notification.getThumb(), imageLoader);
        holder.title.setText(Notification.getTitle());
        holder.body.setText(Notification.getBody());
        holder.timestamp.setText(Notification.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return NotificationsList.size();
    }
}

