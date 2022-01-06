/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sunlocator.topolibrary;

/**
 *
 * @author rainer
 */
public class LatLon {
    /**
     * North/South
     */
    double Lat;
    /**
     * West/East
     */
    double Lon;
    
    public LatLon(double Lat, double Lon) {

        if (Lat < -90 || Lat > 90 || Lon < -180 || Lon > 180)
            throw new UnsupportedOperationException("Lat/Lon Coordinates out of Bounds: (Lat/Lon) "+Lat+"/"+Lon);

        this.Lat = Lat;
        this.Lon = Lon;
    }

    /**
     * Calculate distance in meters between this LatLon to that LatLon using haversine formula (https://en.wikipedia.org/wiki/Haversine_formula)
     * @param that
     * @return distance in meters
     */
    public double calculateEuclidianDistance(LatLon that) {
        double dLat  = Math.toRadians((this.Lat - that.Lat));
        double dLong = Math.toRadians((this.Lon - that.Lon));

        double startLat = Math.toRadians(that.Lat);
        double endLat   = Math.toRadians(this.Lat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return HGTWorker.RadiusOfEarth * c; // <-- d
    }

    private static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
    
    public double getLatitude() {
        return Lat;
    }
    
    public double getLongitude() {
        return Lon;
    }
    
    public String toString() {
        return "Lat/Lon: "+Lat+"/"+Lon;
    }
}
