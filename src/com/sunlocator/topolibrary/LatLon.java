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
