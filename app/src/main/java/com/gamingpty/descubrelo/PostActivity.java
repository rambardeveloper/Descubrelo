package com.gamingpty.descubrelo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = "PostActivity";
    private String postId;
    private String postTitle;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        postId = intent.getStringExtra("post_id");
        postTitle = intent.getStringExtra("title");

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitle(postTitle);
        toolbar.setTitle(postTitle);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, postTitle);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "http://app.gamingpty.com/posts/"+postId);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        final TextView body = (TextView) findViewById(R.id.postBody);
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        final NetworkImageView thumb = (NetworkImageView) findViewById(R.id.postImage);

        String url = AppConfig.NEWS_URL + "/" + postId + ".json?user_email=" + AppController.getInstance().getPrefManager().getUser().getEmail() + "&user_token=" + AppController.getInstance().getPrefManager().getUser().getAuthToken();
        Map hParams = new HashMap<>();
        hParams.put("user", 1);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, new JSONObject(hParams), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(TAG, response.toString());
                        //hidePDialog();

                        try {

                            body.setText(Html.fromHtml(response.getString("body")));
                            thumb.setImageUrl(response.getString("image"), imageLoader);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        //hidePDialog();
                        // TODO Auto-generated method stub

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        AppController.getInstance().addToRequestQueue(jsObjRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place, menu);
        //MenuItem search = menu.findItem(R.id.search);
        //search.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
