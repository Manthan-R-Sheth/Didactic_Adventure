package org.self.vendorapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by manthan on 22/9/16.
 */
public class VendorShops implements ClusterItem {
    private final LatLng mPosition;

    public VendorShops(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
