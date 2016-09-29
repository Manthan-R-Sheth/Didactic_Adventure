package org.self.vendorapp;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

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

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Chirag on 28-09-2016.
 */

public class CustomMapFragment extends SupportMapFragment {

    private static final int NO_OF_VALUES = 10;
    private static final String url = "http://didactic.6te.net/app/vendor_list.php";
    private GoogleMap mMap;
    private boolean mapCameraMovedForCurrentLocation = false;
    private ClusterManager<VendorShops> mClusterManager;
    private LatLng latLng;
    private Stack<Float> vals = new Stack<>();
    private float total = 0f;
    private boolean isMapLoaded = false;

    public void setUpMap() {
        setUpClustering();
        try {
            mMap.getUiSettings().setRotateGesturesEnabled(false);
            mMap.getUiSettings().setScrollGesturesEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(false);
            mMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 0, locationListener);
        } catch (SecurityException se) {
            Log.e("Permissions error", se.getMessage());
        }
    }

    private void setUpClustering() {
        mClusterManager = new ClusterManager<>(getActivity(), mMap);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        addItems();
        mClusterManager.setOnClusterItemClickListener((HomeScreen) getActivity());
    }

    private void addItems() {
        FetchData fetchData = new FetchData();
        fetchData.execute(url);
    }

    private void onLocationsChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        latLng = new LatLng(latitude, longitude);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        if (!mapCameraMovedForCurrentLocation) {

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18), 2000, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    isMapLoaded = true;
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
        if (isMapLoaded) {
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

//    private float movingAverage(float val, boolean initFlag) {
//        if (initFlag) vals = new Stack<>();
//        else {
//            if (vals.size() == NO_OF_VALUES)
//                total -= vals.pop();
//        }
//        total += vals.push(val);
//        return total / vals.size();
//    }

    public GoogleMap getGoogleMap() {
        return this.mMap;
    }

    public void setGoogleMap(GoogleMap map) {
        this.mMap = map;
    }

}
