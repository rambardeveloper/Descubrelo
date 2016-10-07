package com.gamingpty.descubrelo;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback, SearchView.OnQueryTextListener {

    MapView mMapView;
    public static GoogleMap map;

    private static final String TAG = "NearbyActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Toolbar mToolbar;
    private BottomSheetBehavior mBottomSheetBehavior;
    private BottomSheetBehavior mBottomSheetBehaviorDetail;
    private LatLng currentLocation;
    private Map<Marker, Place> allMarkersMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        //mToolbar.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.map_layout, container, false);
        setHasOptionsMenu(true);

        final LayoutInflater inf = inflater;
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.getMapAsync(this);
        map = mMapView.getMap();

        View bottomSheetLayout = getActivity().findViewById(R.id.linear_layout_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

        View bottomSheetDetail = getActivity().findViewById(R.id.bottom_sheet_detail);
        mBottomSheetBehaviorDetail = BottomSheetBehavior.from(bottomSheetDetail);

        currentLocation = new LatLng(LocationService.mCurrentLocation.getLatitude(), LocationService.mCurrentLocation.getLongitude());
        preparePlacesData();

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            Log.d(TAG, "permission missing");
        } else if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap.setMyLocationEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            map.animateCamera(CameraUpdateFactory.zoomTo(12), 500, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
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
        //final List<Place> filteredModelList = filter(placeList, newText);
        //mAdapter.setFilter(filteredModelList);
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

        String url = AppConfig.NEARBY_URL + ".json?user_email=" + AppController.getInstance().getPrefManager().getUser().getEmail() + "&user_token=" + AppController.getInstance().getPrefManager().getUser().getAuthToken();
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

                                MarkerOptions end_options = new MarkerOptions();
                                end_options.position(new LatLng(Double.parseDouble(obj.getString("latitude")), Double.parseDouble(obj.getString("longitude"))));
                                end_options.title(obj.getString("name"));
                                end_options.snippet(obj.getString("address"));
                                //end_options.icon(BitmapDescriptorFactory.fromResource(R.drawable.busmarker));
                                end_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                                Marker marker = map.addMarker(end_options);
                                allMarkersMap.put(marker, place);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());

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
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setInfoWindowAdapter(this);


    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Place place = allMarkersMap.get(marker);
        Intent intent = new Intent(getActivity(), PlaceActivity.class);
        intent.putExtra("place_id", place.getId());
        intent.putExtra("title", place.getName());
        startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        //Log.d(TAG, "STATE_EXPANDED");
        //mBottomSheetBehaviorDetail.setState(BottomSheetBehavior.STATE_EXPANDED);
        //map.moveCamera(CameraUpdateFactory.scrollBy(0,50));

        return false;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
        //return prepareInfoView(marker);
    }

    @Override
    public View getInfoContents(Marker marker) {
        //return null;
        return prepareInfoView(marker);

    }

    private View prepareInfoView(final Marker marker){

        View infoView = getActivity().getLayoutInflater().inflate(R.layout.info_window, null);
        Place place = allMarkersMap.get(marker);

        TextView title = (TextView) infoView.findViewById(R.id.title);
        TextView body = (TextView) infoView.findViewById(R.id.body);
        ImageView thumb = (ImageView) infoView.findViewById(R.id.thumb);
        title.setText(place.getName());
        body.setText(place.getAddress());
        if (place.getThumb() != null) {
            Picasso.with(getContext()).load(place.getThumb()).into(thumb, new MarkerCallback(marker));
        }
        return infoView;
    }

}
