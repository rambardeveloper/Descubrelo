package com.gamingpty.descubrelo;

import android.content.Context;
import android.content.Intent;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.Manifest;

public class PlaceActivity extends AppCompatActivity {

    private static final String TAG = "PlaceActivity";
    private String placeId;
    private String placeTitle;
    private List<Photo> photoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PhotosAdapter mAdapter;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        placeId = intent.getStringExtra("place_id");
        placeTitle = intent.getStringExtra("title");

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitle(placeTitle);
        toolbar.setTitle(placeTitle);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext().getApplicationContext(), MapsActivity.class);
                intent.putExtra("place_id", placeId);
                intent.putExtra("title", placeTitle);
                view.getContext().startActivity(intent);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        ImageButton btn_rating = (ImageButton) findViewById(R.id.btn_rating);
        btn_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext().getApplicationContext(), RateActivity.class);
                intent.putExtra("place_id", placeId);
                intent.putExtra("title", placeTitle);
                startActivityForResult(intent, 1);
            }
        });

        ImageButton btn_info = (ImageButton) findViewById(R.id.btn_info);
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        ImageButton btn_schedule = (ImageButton) findViewById(R.id.btn_schedule);
        btn_schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext().getApplicationContext(), ScheduleActivity.class);
                intent.putExtra("place_id", placeId);
                intent.putExtra("title", placeTitle);
                startActivityForResult(intent, 1);
            }
        });

        ImageButton btn_food = (ImageButton) findViewById(R.id.btn_food);
        btn_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext().getApplicationContext(), FoodActivity.class);
                intent.putExtra("place_id", placeId);
                intent.putExtra("title", placeTitle);
                startActivityForResult(intent, 1);
            }
        });

        ImageButton btn_promos = (ImageButton) findViewById(R.id.btn_promos);
        btn_promos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext().getApplicationContext(), PromosActivity.class);
                intent.putExtra("place_id", placeId);
                intent.putExtra("title", placeTitle);
                startActivityForResult(intent, 1);
            }
        });

        ImageButton btn_services = (ImageButton) findViewById(R.id.btn_services);
        btn_services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext().getApplicationContext(), ServicesActivity.class);
                intent.putExtra("place_id", placeId);
                intent.putExtra("title", placeTitle);
                startActivityForResult(intent, 1);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new PhotosAdapter(photoList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setFocusable(false);
        recyclerView.setAdapter(mAdapter);

        preparePlaceData();
        preparePhotosData();
    }

    private void preparePlaceData() {

        final TextView address = (TextView) findViewById(R.id.placeAddress);
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        final NetworkImageView thumb = (NetworkImageView) findViewById(R.id.placeImage);

        String url = AppConfig.PLACES_URL + "/" + placeId + ".json?user_email=" + AppController.getInstance().getPrefManager().getUser().getEmail() + "&user_token=" + AppController.getInstance().getPrefManager().getUser().getAuthToken();
        Map hParams = new HashMap<>();
        hParams.put("user", 1);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, new JSONObject(hParams), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(TAG, response.toString());
                        //hidePDialog();

                        try {

                            address.setText(Html.fromHtml(response.getString("address")));
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

    private void preparePhotosData() {

        Photo photo1 = new Photo();
        photo1.setId("1");
        photo1.setThumb("http://loremflickr.com/320/240");
        photo1.setTitle("Place photo");
        photo1.setDescription("Place photo");

        photoList.add(photo1);

        Photo photo2 = new Photo();
        photo2.setId("3");
        photo2.setThumb("http://loremflickr.com/321/240");
        photo2.setTitle("Place photo");
        photo2.setDescription("Place photo");

        photoList.add(photo2);

        Photo photo3 = new Photo();
        photo3.setId("3");
        photo3.setThumb("http://loremflickr.com/322/240");
        photo3.setTitle("Place photo");
        photo3.setDescription("Place photo");

        photoList.add(photo3);

        Photo photo4 = new Photo();
        photo4.setId("3");
        photo4.setThumb("http://loremflickr.com/323/240");
        photo4.setTitle("Place photo");
        photo4.setDescription("Place photo");

        photoList.add(photo4);

        Photo photo5 = new Photo();
        photo5.setId("3");
        photo5.setThumb("http://loremflickr.com/324/240");
        photo5.setTitle("Place photo");
        photo5.setDescription("Place photo");

        photoList.add(photo5);

        mAdapter.notifyDataSetChanged();

        //showProgress(true);

        /*String url = AppConfig.PLACES_URL + ".json?user_email=" + AppController.getInstance().getPrefManager().getUser().getEmail() + "&user_token=" + AppController.getInstance().getPrefManager().getUser().getAuthToken();
        JsonArrayRequest postReq = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                Photo photo = new Photo();
                                photo.setId(obj.getString("id"));
                                photo.setThumb(obj.getString("image"));
                                photo.setTitle(obj.getString("name"));
                                photo.setDescription(obj.getString("address"));

                                photoList.add(photo);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        mAdapter.notifyDataSetChanged();
                        //showProgress(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                //showProgress(false);
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
        AppController.getInstance().addToRequestQueue(postReq);*/

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

        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, placeTitle);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "http://app.gamingpty.com/places/"+placeId);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
