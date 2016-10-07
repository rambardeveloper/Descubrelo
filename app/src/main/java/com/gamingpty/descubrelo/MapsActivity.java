package com.gamingpty.descubrelo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "DirectionsActivity";
    String placeId;
    String placeTitle;
    private GoogleMap mMap;
    ArrayList<LatLng> markerPoints;
    private Map<Marker, Place> allMarkersMap = new HashMap<>();
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        placeId = intent.getStringExtra("place_id");
        placeTitle = intent.getStringExtra("title");
        getSupportActionBar().setTitle(placeTitle);
        getSupportActionBar().setElevation(0);

        markerPoints = new ArrayList<>();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else {

            mMap.setMyLocationEnabled(true);
            mMap.setOnMarkerClickListener(this);
            mMap.setOnInfoWindowClickListener(this);

            String url = AppConfig.PLACES_URL + "/" + placeId + ".json?user_email=" + AppController.getInstance().getPrefManager().getUser().getEmail() + "&user_token=" + AppController.getInstance().getPrefManager().getUser().getAuthToken();
            Map hParams = new HashMap<>();
            hParams.put("user", 1);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url, new JSONObject(hParams), new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                Place place = new Place();
                                place.setId(response.getString("id"));
                                place.setThumb(response.getString("image"));
                                place.setName(response.getString("name"));
                                place.setAddress(response.getString("address"));

                                if (mMap != null) {
                                    // Access to the location has been granted to the app.
                                    markerPoints.add(new LatLng(LocationService.mCurrentLocation.getLatitude(), LocationService.mCurrentLocation.getLongitude()));
                                    MarkerOptions start_options = new MarkerOptions();
                                    start_options.position(new LatLng(LocationService.mCurrentLocation.getLatitude(), LocationService.mCurrentLocation.getLongitude()));
                                    start_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));

                                    markerPoints.add(new LatLng(Double.parseDouble(response.getString("latitude")), Double.parseDouble(response.getString("longitude"))));
                                    MarkerOptions end_options = new MarkerOptions();
                                    end_options.title(response.getString("name"));
                                    end_options.snippet(response.getString("address"));
                                    end_options.position(new LatLng(Double.parseDouble(response.getString("latitude")), Double.parseDouble(response.getString("longitude"))));
                                    end_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                                    Marker marker = mMap.addMarker(end_options);
                                    allMarkersMap.put(marker, place);
                                    marker.showInfoWindow();
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(end_options.getPosition()));
                                    //mMap.moveCamera(CameraUpdateFactory.scrollBy(0,450));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 500, null);

                                    String url = getDirectionsUrl(start_options.getPosition(), end_options.getPosition());
                                    DownloadTask downloadTask = new DownloadTask();
                                    downloadTask.execute(url);

                                }

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
            mMap.setInfoWindowAdapter(this);
        }


    }

    @Override
    public void onInfoWindowClick(Marker marker) {

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

        View infoView = getLayoutInflater().inflate(R.layout.info_window, null);
        Place place = allMarkersMap.get(marker);

        TextView title = (TextView) infoView.findViewById(R.id.title);
        TextView body = (TextView) infoView.findViewById(R.id.body);
        ImageView thumb = (ImageView) infoView.findViewById(R.id.thumb);
        title.setText(place.getName());
        body.setText(place.getAddress());
        if (place.getThumb() != null) {
            Picasso.with(getApplicationContext()).load(place.getThumb()).into(thumb, new MarkerCallback(marker));
        }
        return infoView;
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "key=AIzaSyDUtLCvdJ_lgoWW-IfKiyAgRRUirtwcm-Q";

        // Waypoints
        String waypoints = "";
        for(int i=2;i<markerPoints.size();i++){
            LatLng point  = (LatLng) markerPoints.get(i);
            if(i==2)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("E downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service

            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            if(result.size()<1){
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        continue;
                    }else if(j==1){ // Get duration from the list
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.MAGENTA);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }
}
