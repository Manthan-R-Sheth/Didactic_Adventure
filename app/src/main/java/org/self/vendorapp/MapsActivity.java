package org.self.vendorapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private boolean mapCameraMovedForCurrentLocation = false;
    private ClusterManager<VendorShops> mClusterManager;

    //To be used for showing shop details
    private SlidingPaneLayout slidingPaneLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapifNeeded();
    }

    private void setUpMapifNeeded() {
        if (mMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }
    }

    private void setUpMap() {
        setUpClustering();
        try {
            mMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            String locationProvider = locationManager.getBestProvider(criteria,true);
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
        }
        catch (SecurityException se)
        {
            Log.e("Permissions error",se.getMessage());
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
                return false;
            }
        });
    }

    private void addItems() {
        FetchData fetchData=new FetchData();
        String url="http://didactic.6te.net/app/vendor_list.php";
        fetchData.execute(url);
    }

    private void onLocationsChanged(Location location) {
        if(!mapCameraMovedForCurrentLocation) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            LatLng latLng = new LatLng(latitude, longitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 6));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
//            mMap.addMarker(new MarkerOptions()
//                    .position(latLng)
//                    .title("Marker").draggable(false));
            mapCameraMovedForCurrentLocation = !mapCameraMovedForCurrentLocation;

            // Get back the mutable Circle
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(1000) // radius provided by user
                    .strokeWidth(10)
                    .strokeColor(Color.CYAN)
                    .fillColor(Color.argb(0, 0, 150, 0))
                    .clickable(true));
            mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
                @Override
                public void onCircleClick(Circle circle) {
                    
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            googleMap.setMyLocationEnabled(true);
            mMap=googleMap;
            if (googleMap != null) {
                setUpMap();
            }
        }
        catch (SecurityException se)
        {
            Log.e("Permissions error",se.getMessage());
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
                Log.e("JSON",finalJson);
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

            } catch (IOException | JSONException e) {
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
                    for(VendorDataModel vendorshop:result)
                    {
                        mClusterManager.addItem(new VendorShops(Double.parseDouble(vendorshop.getLat()),
                                Double.parseDouble(vendorshop.getaLong())));
                    }
                }
                else {
                    //no data present
                }
            }
            else {
                 //if result is NULL
            }
        }
    }
}
