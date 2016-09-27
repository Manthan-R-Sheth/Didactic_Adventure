package org.self.vendorapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.phenotype.Flag;
import com.google.maps.android.clustering.ClusterManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class HomeScreen extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private static final int NO_OF_VALUES = 10;
    private GoogleMap mMap;
    private boolean mapCameraMovedForCurrentLocation = false;
    private ClusterManager<VendorShops> mClusterManager;
    private SensorManager mSensorManager;
    private LatLng latLng;
    private CheckBox checkBox1;

    //To be used for showing shop details
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private Stack<Float> vals = new Stack<>();
    private float total = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkBox1 = (CheckBox) findViewById(R.id.enabled);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        int xRotation = Math.round(event.values[0]/2)*2;
        int yRotation = Math.round((-event.values[1])/2)*2;
        if(yRotation > 90){
            yRotation = 90;
        }
        if(yRotation < 0) {
            yRotation = 0;
        }
//        tvX.setText("X: "+Float.toString(xRotation));
//        tvY.setText("Y: "+Float.toString(yRotation));

        updateCamera(xRotation,yRotation);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //not in use
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }
    }

    private void setUpMap() {
        setUpClustering();
        try {
            mMap.getUiSettings().setRotateGesturesEnabled(false);
            mMap.getUiSettings().setScrollGesturesEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(false);
            mMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            String locationProvider = locationManager.getBestProvider(criteria, true);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    onLocationsChanged(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };


            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                onLocationsChanged(lastKnownLocation);
            }

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20000, 0, locationListener);
        } catch (SecurityException se) {
            Log.e("Permissions error", se.getMessage());
        }
    }

    private void setUpClustering() {
        mClusterManager = new ClusterManager<VendorShops>(this, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        addItems();
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<VendorShops>() {
            @Override
            public boolean onClusterItemClick(VendorShops vendorShops) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                return true;
            }
        });
    }

    private void addItems() {
        FetchData fetchData = new FetchData();
        String url = "http://didactic.6te.net/app/vendor_list.php";
        fetchData.execute(url);
    }

    private void onLocationsChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        latLng = new LatLng(latitude, longitude);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        if (!mapCameraMovedForCurrentLocation) {

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,18), 2000, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mSensorManager.registerListener(HomeScreen.this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
                }

                @Override
                public void onCancel() {

                }
            });
            mapCameraMovedForCurrentLocation = !mapCameraMovedForCurrentLocation;
//            mMap.addMarker(new MarkerOptions()
//                    .position(latLng)
//                    .title("Marker").draggable(false));

            // Get back the mutable Circle
//            Circle circle = mMap.addCircle(new CircleOptions()
//                    .center(latLng)
//                    .radius(1000) // radius provided by user
//                    .strokeWidth(10)
//                    .strokeColor(Color.CYAN)
//                    .fillColor(Color.argb(0, 0, 150, 0))
//                    .clickable(true));
//            mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
//                @Override
//                public void onCircleClick(Circle circle) {
//
//                }
//            });
        }
    }

    public void updateCamera(float bearing, float tilt) {
//        CameraPosition currentPosition = mMap.getCameraPosition();
        if (latLng != null) {
            CameraPosition newPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .bearing(bearing)
                    .tilt(tilt)
                    .zoom(mMap.getCameraPosition().zoom)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPosition));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                mMap = googleMap;
                setUpMap();
                // for the system's orientation sensor registered listeners

            }
        } catch (SecurityException se) {
            Log.e("Permissions error", se.getMessage());
        }
    }

    public class FetchData extends AsyncTask<String, String, List<VendorDataModel>> {

        @Override
        protected List<VendorDataModel> doInBackground(String... urls) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            List<VendorDataModel> vendorDataList = null;
            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                StringBuilder buffer = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String finalJson = buffer.toString();
                Log.e("JSON", finalJson);
                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("result");
                vendorDataList = new ArrayList<>();
                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    VendorDataModel model = new VendorDataModel();
                    model.setSno(finalObject.getString("sno"));
                    model.setName(finalObject.getString("name"));
                    model.setLat(finalObject.getString("lat"));
                    model.setLong(finalObject.getString("long"));
                    vendorDataList.add(model);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return vendorDataList;
        }

        @Override
        protected void onPostExecute(List<VendorDataModel> result) {
            super.onPostExecute(result);
            // swipeRefreshLayout.setRefreshing(false);
            if (result != null) {
                // feedAdapter.clear();
                if (result.size() > 0) {
                    for (VendorDataModel vendorshop : result) {
                        mClusterManager.addItem(new VendorShops(Double.parseDouble(vendorshop.getLat()),
                                Double.parseDouble(vendorshop.getaLong())));
                    }
                } else {
                    //no data present
                }
            } else {
                //if result is NULL
            }
        }
    }

    private float movingAverage(float val, boolean initFlag) {
        if (initFlag) vals = new Stack<>();
        else {
            if (vals.size() == NO_OF_VALUES)
                total -= vals.pop();
        }
        total += vals.push(val);
        return total/vals.size();
    }
}
