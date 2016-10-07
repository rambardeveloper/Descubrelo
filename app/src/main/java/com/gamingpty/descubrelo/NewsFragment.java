package com.gamingpty.descubrelo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsFragment extends Fragment {

    View view;
    private static final String TAG = "NewsActivity";
    private List<Post> postList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NewsAdapter mAdapter;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private View mProgressView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.news_layout, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        mAdapter = new NewsAdapter(postList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        mProgressView = view.findViewById(R.id.load_progress);
        preparePostsData();

        return view;
    }

    private void preparePostsData() {

        showProgress(true);

        String url = AppConfig.NEWS_URL + ".json?user_email=" + AppController.getInstance().getPrefManager().getUser().getEmail() + "&user_token=" + AppController.getInstance().getPrefManager().getUser().getAuthToken();
        JsonArrayRequest postReq = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                Post post = new Post();
                                post.setId(obj.getString("id"));
                                post.setThumb(obj.getString("image"));
                                post.setTitle(obj.getString("title"));
                                post.setBody(obj.getString("body"));
                                post.setTimestamp(obj.getString("date"));

                                postList.add(post);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        mAdapter.notifyDataSetChanged();
                        showProgress(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                showProgress(false);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_email", AppController.getInstance().getPrefManager().getUser().getEmail());
                jsonParams.put("user_token", AppController.getInstance().getPrefManager().getUser().getAuthToken());
                return jsonParams;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(postReq);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

}
