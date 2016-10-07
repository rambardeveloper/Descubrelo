package com.gamingpty.descubrelo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlacesFragment extends Fragment implements SearchView.OnQueryTextListener {

    View view;
    private static final String TAG = "PlacesActivity";
    private List<Place> placeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PlacesAdapter mAdapter;
    private View mProgressView;
    private BottomSheetBehavior mBottomSheetBehavior;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.places_layout, container, false);
        setHasOptionsMenu(true);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        mAdapter = new PlacesAdapter(placeList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        View bottomSheetLayout = getActivity().findViewById(R.id.linear_layout_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

        mProgressView = view.findViewById(R.id.load_progress);
        preparePlacesData();

        recyclerView.addOnItemTouchListener(new PlacesAdapter.RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new PlacesAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // when chat is clicked, launch full chat thread activity
                Place place = placeList.get(position);
                Intent intent = new Intent(getActivity(), PlaceActivity.class);
                intent.putExtra("place_id", place.getId());
                intent.putExtra("title", place.getName());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {


            }
        }));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_places, menu);

        final MenuItem item = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                        Log.d(TAG, "STATE_COLLAPSED");
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        mAdapter.setFilter(placeList);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        Log.d(TAG, "STATE_EXPANDED");
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        return true; // Return true to expand action view
                    }
                });
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Place> filteredModelList = filter(placeList, newText);
        mAdapter.setFilter(filteredModelList);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private List<Place> filter(List<Place> models, String query) {
        query = query.toLowerCase();

        final List<Place> filteredModelList = new ArrayList<>();
        for (Place model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private void preparePlacesData() {

        showProgress(true);

        String url = AppConfig.PLACES_URL + ".json?user_email=" + AppController.getInstance().getPrefManager().getUser().getEmail() + "&user_token=" + AppController.getInstance().getPrefManager().getUser().getAuthToken();
        JsonArrayRequest postReq = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                Place place = new Place();
                                place.setId(obj.getString("id"));
                                place.setThumb(obj.getString("image"));
                                place.setName(obj.getString("name"));
                                place.setAddress(obj.getString("address"));

                                placeList.add(place);

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