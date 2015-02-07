package com.example.xplorexaymacaapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.communication.APIEndPoints;
import com.example.communication.Communication;
import com.example.communication.CommunicationResponse;
import com.example.entity.Course;
import com.example.entity.Event;
import com.example.mapdirection.MapDirection;
import com.example.xplorexaymacaapp.MapDirectionAsyncTask;
import com.example.xplorexaymacaapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends FragmentActivity implements LocationListener, OnMarkerClickListener,
        OnMapClickListener,OnItemClickListener{

    AlertDialog dialog;
    AlertDialog.Builder builder;
    public static final String TAG= MainActivity.class.getSimpleName();
    //private MapHelper mapHelper;
    private SQLiteDatabase db;
    //private MapData mapData;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LatLng currentLocation;
    long currentLocationId;
    Communication com;
    Location location;
    Polyline newPolyline;
    ArrayList<String> list = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    Context context;


    LocationManager locationManager;


    TextView locationName;
    TextView locationDescription;
    Button directionButton;
    Button addCourseButton;
    ImageView image;

    String mode;
    String[] directionOptions={"Driving","Walking"};

    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    DrawerLayout drawerLayout;
    ListView leftNavigationList;
    ExpandableListView contentOptionsList;
    ImageButton contentOptionsButton;

    String[] navigationOptions;
    List<String> contentOptions;
    HashMap<String,List<Object>>  contents;
    ActionBarDrawerToggle drawerListener;

    SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;
        loadMap();

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.main);
        mSlidingUpPanelLayout.setEnableDragViewTouchEvents(true);
        mSlidingUpPanelLayout.setOverlayed(true);

        //mSlidingUpPanelLayout.setDragView(findViewById(R.id.panel));

        locationName =(TextView)findViewById(R.id.location_name);
        locationDescription = (TextView)findViewById(R.id.location_description);
        directionButton = (Button)findViewById(R.id.getDirections);
        addCourseButton = (Button)findViewById(R.id.addCourse);
        image = (ImageView) findViewById(R.id.locationImage);


        drawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        leftNavigationList = (ListView)findViewById(R.id.leftNavigationMenu);
        navigationOptions = getResources().getStringArray(R.array.navigation_menu_options);

        //getActionBar().setTitle(navigationOptions[0]);

        leftNavigationList.setAdapter(new ArrayAdapter<String>(context, R.layout.navigation_list_item,R.id.navigation_list_item_text, navigationOptions));

        leftNavigationList.setOnItemClickListener(this);



        drawerListener = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, 0, 0){

            @Override
            public void onDrawerClosed(View drawerView) {
                // TODO Auto-generated method stub
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // TODO Auto-generated method stub

                super.onDrawerOpened(drawerView);

            }};

        drawerLayout.setDrawerListener(drawerListener);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        OnClickListener directionListener = new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                builder = new AlertDialog.Builder(context);
                builder.setTitle("Get Directions");
                final LatLng user = getUserLocation();
                final LatLng marker = getCurrentLocation();

                builder.setSingleChoiceItems(directionOptions, -1,  new DialogInterface.OnClickListener(){


                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case(0):
                                mode = MapDirection.MODE_DRIVING;
                                break;
                            case(1):
                                mode = MapDirection.MODE_WALKING;
                                break;


                        }

                    }});
                builder.setPositiveButton("Find Directions", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mSlidingUpPanelLayout.setPanelHeight(50);
                        if(user!=null){
                            findDirection(user.latitude, user.longitude, marker.latitude, marker.longitude, mode);
                        }
                        dialog.cancel();

                    }});
                dialog = builder.create();
                dialog.show();

            }};



        directionButton.setOnClickListener(directionListener);

    }








    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onPostCreate(savedInstanceState);
        drawerListener.syncState();
    }

    @Override
    public void onNewIntent(final Intent queryIntent) {
        super.onNewIntent(queryIntent);
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            String query = queryIntent.getStringExtra(SearchManager.QUERY);
            //this.doSearchQuery(queryIntent);
            searchLocation(query);
        } else if (Intent.ACTION_VIEW.equals(queryAction)) {

            Uri detailUri = queryIntent.getData();
            String sugg = queryIntent.getStringExtra(SearchManager.QUERY);
            String id = detailUri.getLastPathSegment();
            long locid = Long.parseLong(id);
            searchLocation(locid);
            searchView.setQuery(sugg, false);

            //this.doView(queryIntent);
        } else {
            Log.d(TAG, "Create intent NOT from search");
            map.clear();
            loadMap();
            //com.send(1,this,APIEndPoints.apiUrl, APIEndPoints.location_list, "");
        }
    }

    public void searchLocation(String query){
        /*Cursor cursor = mapData.getLocationByName(query);
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                double latitude = cursor.getDouble(cursor.getColumnIndex(LOCATION_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(LOCATION_LONGITUDE));
                zoomIn(latitude,longitude);
            }
        }*/
    }
    public void searchLocation(long id){
       /* Cursor cursor = mapData.getLocationById(id);
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                double latitude = cursor.getDouble(cursor.getColumnIndex(LOCATION_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(LOCATION_LONGITUDE));
                zoomIn(latitude,longitude);
            }
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default
        searchView.setOnSuggestionListener(new OnSuggestionListener(){

            @Override
            public boolean onSuggestionSelect(int position) {
                String suggestion = getSuggestion(position);
                searchView.setQuery(suggestion, true); // submit query now
                return true; // replace default search manager behaviour

            }


            @Override
            public boolean onSuggestionClick(int position) {
                // TODO Auto-generated method stub
                String suggestion = getSuggestion(position);
                searchView.setQuery(suggestion, true); // submit query now
                return true; // replace default search manager behaviour
            }
            private String getSuggestion(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(
                        position);
                String suggest1 = cursor.getString(cursor
                        .getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                return suggest1;
            }});



        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchViewMenuItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) searchViewMenuItem.getActionView();
        int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.ic_action_search);
        //searchView.setOnQueryTextListener(this);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(drawerListener.onOptionsItemSelected(item)){
            return true;
        }
        int menuid = item.getItemId();
        if(menuid == R.id.menu_refresh){
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
        }
        else if(menuid ==R.id.menu_locate){
            if(location!=null){
                double latitude= location.getLatitude();
                double longitude=location.getLongitude();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 16));
            }else{
                Toast.makeText(this, "Current location cannot be found ", Toast.LENGTH_LONG).show();
            }
        }
        else if(menuid ==R.id.action_settings){


        }

        return super.onOptionsItemSelected(item);
    }




    public void loadMap(){
        mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        map = mapFragment.getMap();
        map.setOnMapClickListener(this);
       /* SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String mapType = prefs.getString("mapType", "Normal");
        if(mapType.equals("Satellite")){
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }else{
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }*/
        if(map!=null){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(new Criteria(), true);
            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Toast.makeText(this,
                        "No network provider is enabled", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "No Provider Available");
            }else{

                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0, this);
                    Log.d("GPS", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null){
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            LatLng latLng = new LatLng(latitude, longitude);
                            map.addMarker(new MarkerOptions()
                                            .title("You are here")
                                            .position(latLng)
                                            .draggable(true)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            );


                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        }

                    }
                }else{
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0,
                            0, this);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null){
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            LatLng latLng = new LatLng(latitude, longitude);
                            map.addMarker(new MarkerOptions()
                                            .title("You are here")
                                            .position(latLng)
                                            .draggable(true)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            );


                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        }

                    }
                }
            }


            map.setOnMarkerClickListener(this);
        }else{
            Toast.makeText(this,
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
        }


    }

    public void zoomIn(double latitude,double longitude){
        LatLng latLng = new LatLng(latitude,longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
    }

    public LatLng getUserLocation(){
        if(location!=null){
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            return latLng;
        }
        return null;
    }

    public LatLng getCurrentLocation(){
        return currentLocation;
    }


    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS Disabled");
        builder.setCancelable(false);
        builder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent startGps = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(startGps);


            }});
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }});
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }




    @Override
    public boolean onMarkerClick(Marker marker) {
        // TODO Auto-generated method stub

        /*Log.d(TAG, "Marker Clicked");
        if(newPolyline!=null){
            newPolyline.remove();
        }
        Log.d(TAG, "marker lat: "+marker.getPosition().latitude+" marker long: "+marker.getPosition().longitude);
        Log.d(TAG, "title: "+marker.getTitle());

        if(marker.getTitle().equals("You are here")){

        }else{
            mSlidingUpPanelLayout.showPanel();
            mSlidingUpPanelLayout.setPanelHeight(200);


            String title = marker.getTitle();
            double latitude = marker.getPosition().latitude;
            double longitude = marker.getPosition().longitude;
            LatLng latLng = new LatLng(latitude,longitude);
            currentLocation = latLng;
            Log.d(TAG, "latitude: "+latitude+" longitude: "+longitude);
            long id = Long.parseLong(title);
            currentLocationId=id;
            //respondMarkerTitle(id);
            setLocationDetails(id);
            setLocationImage(id);
            zoomIn(latitude,longitude);
            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        }*/
        return true;

    }

    @Override
    public void onMapClick(LatLng point) {
       /* mSlidingUpPanelLayout.hidePanel();
        if(newPolyline!=null){
            newPolyline.remove();
        }
        // TODO Auto-generated method stub
        */




    }

    public  void findDirection(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode){
        Map<String,String> map = new HashMap<String,String>();
        map.put(MapDirectionAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(MapDirectionAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(MapDirectionAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(MapDirectionAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(MapDirectionAsyncTask.DIRECTIONS_MODE, mode);

        MapDirectionAsyncTask asyncTask = new MapDirectionAsyncTask(this);
        asyncTask.execute(map);

    }
    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints){
        //Polyline newPolyline;
        PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.BLUE);
        for(int i = 0 ; i < directionPoints.size() ; i++){
            rectLine.add(directionPoints.get(i));
        }
        newPolyline = map.addPolyline(rectLine);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0, this);
	
	/*ActionBar bar = getActionBar();
    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    bar.setListNavigationCallbacks(adapter, this);*/
    }







    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Intent i;
        switch(position){
            case 0:

                break;
            case 1:


                break;
            case 2:

        }


    }
}
