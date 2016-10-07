package com.gamingpty.descubrelo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    private List<Post> NewsList;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView body;
        public ImageButton share;
        public ImageButton read_more;
        public NetworkImageView thumb;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            body = (TextView) view.findViewById(R.id.body);
            share = (ImageButton) view.findViewById(R.id.share_post);
            read_more = (ImageButton) view.findViewById(R.id.read_more);
            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();
            thumb = (NetworkImageView) view.findViewById(R.id.thumb);
        }
    }


    public NewsAdapter(List<Post> NewsList) {
        this.NewsList = NewsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Post post = NewsList.get(position);
        holder.thumb.setImageUrl(post.getThumb(), imageLoader);
        holder.title.setText(post.getTitle());
        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, post.getTitle());
                sendIntent.putExtra(Intent.EXTRA_TEXT, "http://app.gamingpty.com/posts/"+post.getTitle());
                sendIntent.setType("text/plain");
                view.getContext().startActivity(sendIntent);
            }
        });
        holder.read_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext().getApplicationContext(),PostActivity.class);
                intent.putExtra("post_id", post.getId());
                intent.putExtra("title", post.getTitle());
                view.getContext().startActivity(intent);
            }
        });
        holder.thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext().getApplicationContext(),PostActivity.class);
                intent.putExtra("post_id", post.getId());
                intent.putExtra("title", post.getTitle());
                view.getContext().startActivity(intent);
            }
        });
        //holder.body.setText(Post.getBody());
    }

    @Override
    public int getItemCount() {
        return NewsList.size();
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private NewsAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final NewsAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
