package org.self.vendorapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.CheckBox;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.maps.android.clustering.ClusterManager;

public class HomeScreen extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener, ClusterManager.OnClusterItemClickListener<VendorShops> {


    public SensorManager mSensorManager;
    private CustomMapFragment mapFragment;
    private CameraFragment cameraFragment;
    private CheckBox checkBox1;
//    private SlidingUpPanelLayout slidingUpPanelLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.activity_home_screen);
        mapFragment = new CustomMapFragment();
        cameraFragment = new CameraFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_content, mapFragment, "Map Fragment").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.main_content,cameraFragment, "Camera Fragment").commit();
        getSupportFragmentManager().beginTransaction().hide(cameraFragment).commit();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        checkBox1 = (CheckBox) findViewById(R.id.enabled);
//        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapFragment != null) setUpMapIfNeeded();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
    }

    private void setUpMapIfNeeded() {
        if (mapFragment.getGoogleMap() == null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int xRotation = Math.round(event.values[0] / 2) * 2;
        int yRotation = Math.round((-event.values[1]) / 2) * 2;
        if (yRotation < 0) {
            yRotation = 0;
            mapFragment.updateCamera(xRotation, yRotation);
        } else if (yRotation < 75) {
            if (!mapFragment.isVisible())
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down)
                        .show(mapFragment)
                        .commit();
            if (cameraFragment.isVisible())
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_up)
                        .hide(cameraFragment)
                        .commit();
            mapFragment.updateCamera(xRotation, yRotation);
        } else if (yRotation < 105) {
            if (mapFragment.isVisible())
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down)
                        .hide(mapFragment)
                        .commit();
            if (!cameraFragment.isVisible())
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_up)
                        .show(cameraFragment)
                        .commit();
        }
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
    public void onMapReady(GoogleMap googleMap) {
        try {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                mapFragment.setGoogleMap(googleMap);
                mapFragment.setUpMap();
            }
        } catch (SecurityException se) {
            Log.e("Permissions error", se.getMessage());
        }
    }


    @Override
    public boolean onClusterItemClick(VendorShops vendorShops) {
//        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        return false;
    }
}
