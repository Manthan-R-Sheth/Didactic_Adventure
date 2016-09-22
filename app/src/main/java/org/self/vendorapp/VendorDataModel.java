package org.self.vendorapp;

/**
 * Created by manthan on 22/9/16.
 */
public class VendorDataModel {
    private String aLong;
    private String lat;
    private String name;

    public String getSno() {
        return sno;
    }

    public String getName() {
        return name;
    }

    public String getLat() {
        return lat;
    }

    public String getaLong() {
        return aLong;
    }

    private String sno;

    public void setSno(String sno) {
        this.sno = sno;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLong(String aLong) {
        this.aLong = aLong;
    }
}
