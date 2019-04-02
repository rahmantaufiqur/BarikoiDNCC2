package xyz.barikoi.barikoidncc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import barikoi.barikoilocation.BarikoiAPI;
import barikoi.barikoilocation.NearbyPlace.NearbyPlaceAPI;
import barikoi.barikoilocation.NearbyPlace.NearbyPlaceListener;
import barikoi.barikoilocation.PlaceModels.NearbyPlace;
import barikoi.barikoilocation.PlaceModels.ReverseGeoPlace;
import barikoi.barikoilocation.ReverseGeo.ReverseGeoAPI;
import barikoi.barikoilocation.ReverseGeo.ReverseGeoAPIListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener {
    String[] permissions={
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
    };
    private MapView mapView;
    private MapboxMap map;
    public final static int MULTIPLE_PERMISSIONS=5;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationPlugin;
    private Location originLocation;
    private Button selectplace,submit;
    private BottomSheetBehavior bottomSheetBehavior,suggestSheetBehavior;
    private LinearLayout viewBottomSheet;
    private View viewSuggestionSheet;
    private RecyclerView suggView;
    private PlaceAddressAdapter placeArrayAdapter;
    Spinner buildingType,buildingCategory,roadPlacement;
    EditText floorNumbers,sftperFloor;
    TextView price;
    String[] builType={"পাকা","সেমি-পাকা","কাঁচা"};
    String[] builcategory={"আবাসিক","বানিজ্যিক","শিল্প-কারখানা"};
    String[] roadside={"মেইন রাস্তার পাশে","রাস্তার ৩০০ ফিটের মধ্যে"};
    TextView priceRateShow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Mapbox.getInstance(this,getString(R.string.access_token));
        BarikoiAPI.getINSTANCE(this,"MTExMTpKQkZZMzNIQk45");
        mapView=findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        init();
    }
    public void init(){
        priceRateShow=findViewById(R.id.price_rate_show);
        floorNumbers=findViewById(R.id.building_floors);
        sftperFloor=findViewById(R.id.squarefeetperfloor);
        price=findViewById(R.id.price_rate_show);
        viewBottomSheet = findViewById(R.id.price_bottom_sheet) ;
        bottomSheetBehavior = BottomSheetBehavior.from (viewBottomSheet) ;
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        viewSuggestionSheet= findViewById(R.id.suggestlistsheet);
        suggestSheetBehavior=BottomSheetBehavior.from(viewSuggestionSheet);
        suggestSheetBehavior.setHideable(true);
        suggestSheetBehavior.setSkipCollapsed(true);
        suggestSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        buildingType=findViewById(R.id.spinnerbuildingType);
        buildingCategory=findViewById(R.id.spinnerbuilding_usage_type);
        roadPlacement=findViewById(R.id.spinnerbuilding_road_side_type);

        ArrayAdapter<String> dataAdaptertype = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, builType);
        ArrayAdapter<String> dataAdapterCategory = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, builcategory);
        ArrayAdapter<String> dataAdapterRoad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roadside);

        buildingType.setAdapter(dataAdaptertype);
        buildingCategory.setAdapter(dataAdapterCategory);
        roadPlacement.setAdapter(dataAdapterRoad);

        buildingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                buildingType.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        buildingCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                buildingCategory.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        roadPlacement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                roadPlacement.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        selectplace = this.findViewById(R.id.buttonselectplace);
        selectplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggView=findViewById(R.id.suggestlist);
                suggestSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                LatLng target=map.getCameraPosition().target;
                double lat=target.getLatitude();
                double lon=target.getLongitude();
                    NearbyPlaceAPI.builder(MainActivity.this)
                            .setLatLng(lat,lon)
                            .setLimit(10)
                            .setDistance(.5)
                            .build()
                            .generateNearbyPlaceList(new NearbyPlaceListener() {
                                @Override
                                public void onPlaceListReceived(ArrayList<NearbyPlace> places) {
                                    placeArrayAdapter=new PlaceAddressAdapter(places, new PlaceAddressAdapter.OnPlaceItemSelectListener() {
                                        @Override
                                        public void onPlaceItemSelected(NearbyPlace mItem, int position) {
                                            suggestSheetBehavior.setHideable(true);
                                            suggestSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        }
                                    });
                                    suggView.setAdapter(placeArrayAdapter);
                                    placeArrayAdapter.notifyDataSetChanged();

                                }

                                @Override
                                public void onFailure(String message) {

                                }
                            });

                    //initsuggestionList(lat,lon);
                    findViewById(R.id.buttonskip).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            suggestSheetBehavior.setHideable(true);
                            suggestSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    });
                //Toast.makeText(AddPlaceActivity.this,lat+" "+lon, Toast.LENGTH_LONG).show();

                /*if (mMap.getCameraPosition().zoom >= 17.0f) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    init();
                }
                else {
                    Toast.makeText(AddPlaceActivity.this, getString(R.string.need_more_zoom), Toast.LENGTH_LONG).show();
                }*/
            }
        });
        submit=findViewById(R.id.buttonbook);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int floorNo=Integer.parseInt(floorNumbers.getText().toString());
                int squarefeet=Integer.parseInt(sftperFloor.getText().toString());
                int totalSqft=floorNo*squarefeet;
                String type=buildingType.getSelectedItem().toString();
                String category=buildingCategory.getSelectedItem().toString();
                String placement=roadPlacement.getSelectedItem().toString();
                double price=calculate(type,category,placement);
                double result=totalSqft*price*1.2;
                priceRateShow.setText(String.valueOf(result));
                Intent intent=new Intent(MainActivity.this,AssessmentActivity.class);
                intent.putExtra("floorNo",floorNo);
                intent.putExtra("squareFeet",squarefeet);
                intent.putExtra("totalSQT",totalSqft);
                intent.putExtra("buildingType",type);
                intent.putExtra("buildingCategory",category);
                intent.putExtra("buildingPlacement",placement);
                intent.putExtra("ratepersqt",price);
                intent.putExtra("totalTax",result);
                startActivity(intent);
                finish();
            }
        });
    }
 public double calculate(String type,String category,String placement ){
        double rate=1.0;
     switch (placement){
         case "মেইন রাস্তার পাশে":
             switch (category){
                 case "আবাসিক":
                     switch (type){
                         case "পাকা":
                             rate=7;
                             break;
                         case "সেমি-পাকা":
                             rate=6;
                             break;
                         case "কাঁচা":
                             rate=5;
                             break;
                     }
             break;
                 case "বানিজ্যিক":
                     switch (type){
                         case "পাকা":
                             rate=10;
                             break;
                         case "সেমি-পাকা":
                             rate=8;
                             break;
                         case "কাঁচা":
                             rate=7;
                             break;
                     }
                 break;
                 case "শিল্প-কারখানা":
                     switch (type){
                         case "পাকা":
                             rate=8;
                             break;
                         case "সেমি-পাকা":
                             rate=7;
                             break;
                         case "কাঁচা":
                             rate=6;
                             break;
                     }
                  break;
             }
            break;
         case "রাস্তার ৩০০ ফিটের মধ্যে":
             switch (category){
                 case "আবাসিক":
                     switch (type){
                         case "পাকা":
                             rate=6.5;
                             break;
                         case "সেমি-পাকা":
                             rate=5;
                             break;
                         case "কাঁচা":
                             rate=4;
                             break;
                     }
                     break;
                 case "বানিজ্যিক":
                     switch (type){
                         case "পাকা":
                             rate=8.1;
                             break;
                         case "সেমি-পাকা":
                             rate=6;
                             break;
                         case "কাঁচা":
                             rate=5;
                             break;
                     }
                     break;
                 case "শিল্প-কারখানা":
                     switch (type){
                         case "পাকা":
                             rate=7;
                             break;
                         case "সেমি-পাকা":
                             rate=6;
                             break;
                         case "কাঁচা":
                             rate=5;
                             break;
                     }
                     break;
             }
             break;
     }
     return rate;
 }
   /* private void initsuggestionList(double lat,double lon){
        suggestSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        suggestSheetBehavior.setHideable(false);
        StringRequest request = new StringRequest(Request.Method.GET,
                Api.addplacesugg+"?longitude="+lon+"&latitude="+lat,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            loading.setVisibility(View.GONE);
                            JSONArray placearray = new JSONArray(response.toString());
                            ArrayList<com.barikoi.barikoi.CustomModels.Place> newplaces=JsonUtils.getPlaces(placearray);
                            IconFactory iconFactory = IconFactory.getInstance(AddPlaceActivity.this);
                            Icon icon = iconFactory.fromResource(R.drawable.mapmarkerforshowplaces);
                            for(com.barikoi.barikoi.CustomModels.Place p : newplaces){
                                LatLng point=new LatLng(Double.parseDouble(p.getLat()),Double.parseDouble(p.getLon()));
                                map.addMarker(new com.mapbox.mapboxsdk.annotations.MarkerOptions().position(point).icon(icon));
                            }
                            if(newplaces.size()>0) {
                                placeArrayAdapter=new PlaceAddressAdapter(newplaces, (mItem, position) -> initInputFromPlace(mItem));
                                suggView=findViewById(R.id.suggestlist);
                                suggView.setAdapter(placeArrayAdapter);
                                areaText.setText(newplaces.get(0).getArea());
                                postalText.setText(newplaces.get(0).getPostalcode());
                                cityText.setText(newplaces.get(0).getCity());
                            }else{
                                //Toast.makeText(AddPlaceActivity.this,lat+" "+lon, Toast.LENGTH_LONG).show();

                                suggestSheetBehavior.setHideable(true);
                                suggestSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.setVisibility(View.GONE);
                        //Toast.makeText(getActivity(),"Network error",Toast.LENGTH_SHORT).show();
                    }
                }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if(!token.equals("")){
                    params.put("Authorization", "bearer "+token);
                }
                return params;
            }
        };
        queue.add(request);
        loading.setVisibility(View.VISIBLE);
        findViewById(R.id.buttonskip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInputData();
                suggestSheetBehavior.setHideable(true);
                suggestSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            }
        });
    }*/
    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }
    /**
     * This function checks for location permission, if granted initializes location plugin
     */
    private void enableLocation(){
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create an instance of LOST location engine
            initializeLocationEngine();
        } else {
            checkPermissions();
        }
    }
    /**
     * It sets the location Engine and gets the last known location
     */
    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);
        locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        locationPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationPlugin.setRenderMode(RenderMode.COMPASS);
        // currentLocation.displayLocation();
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        } else {
            Toast.makeText(this, "addlocengine", Toast.LENGTH_SHORT).show();
            locationEngine.addLocationEngineListener(this);
        }
    }
    private void setCameraPosition(LatLng location){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 13));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if (grantResults.length > 0) {
                    String permissionsDenied = "";
                    for (String per : permissions) {
                        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                            permissionsDenied += "\n" + per;

                        }

                    }
                    // Show permissionsDenied
                    if(permissionsDenied.length()>0)
                        new AlertDialog.Builder(this)
                                .setMessage("cannot proceed, permission denied:"+permissionsDenied)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                })
                                .create()
                                .show();
                    else{
                      enableLocation();
                    }
                }
                return;
            }
        }
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map=mapboxMap;
        enableLocation();
        FloatingActionButton fab=findViewById(R.id.myLocationButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                @SuppressLint("MissingPermission")
                Location lastLocation = locationEngine.getLastLocation();
                if (lastLocation!=null) {
                    originLocation = lastLocation;
                    Double lat = lastLocation.getLatitude();
                    Double lon = lastLocation.getLongitude();
                    LatLng latLng = new LatLng(lat, lon);
                    map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
                else{
                    locationEngine.requestLocationUpdates();
                }
            }
        });

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
       locationEngine.requestLocationUpdates();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            originLocation = location;
            Double lat = location.getLatitude();
            Double lon = location.getLongitude();
            LatLng latLng = new LatLng(lat, lon);
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            /*  generatelist(lat,lon);*/

            locationEngine.removeLocationEngineListener(this);
        }
        else{
            locationEngine.requestLocationUpdates();
        }
    }
    @SuppressLint("MissingPermission")
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
