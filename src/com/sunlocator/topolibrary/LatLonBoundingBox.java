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
public class LatLonBoundingBox {

    /**
     * Create new LatLonBoundingBox with defined bounds
     * @param N_Bound
     * @param S_Bound
     * @param W_Bound
     * @param E_Bound
     */
    public LatLonBoundingBox(double N_Bound, double S_Bound, double W_Bound, double E_Bound) {
        this.N_Bound = N_Bound;
        this.S_Bound = S_Bound;
        this.W_Bound = W_Bound;
        this.E_Bound = E_Bound;
        setWidths();
    }

    /**
     * Create square LatLonBoundingbox from a center and width
     * @param center
     * @param width in meters
     */
    public LatLonBoundingBox(LatLon center, int width) {
        int dist = width/2;
        double lat_diff = HGTWorker.distance2degrees_latitude(dist);
        N_Bound = center.Lat+lat_diff;
        S_Bound = center.Lat-lat_diff;
        
        double lon_diff = HGTWorker.distance2degrees_longitude(dist, center.Lat); //let's use the bounding lat at the center
        W_Bound = center.Lon-lon_diff;
        E_Bound = center.Lon+lon_diff;
        setWidths();
    }

    public LatLonBoundingBox addPadding(int meters) {
        double newNbound = N_Bound + HGTWorker.distance2degrees_latitude(meters);
        double newSbound = S_Bound - HGTWorker.distance2degrees_latitude(meters);
        double newWbound = W_Bound - HGTWorker.distance2degrees_longitude(meters, getCenter().Lat);
        double newEbound = E_Bound + HGTWorker.distance2degrees_longitude(meters, getCenter().Lat);

        return new LatLonBoundingBox(newNbound, newSbound, newWbound, newEbound);
    }
    
    private void setWidths() {
        widthLatDegree = N_Bound - S_Bound;
        widthLonDegree = E_Bound - W_Bound;
        center = new LatLon(S_Bound+(widthLatDegree/2d), W_Bound+(widthLonDegree/2d));
        widthLatMeters = widthLatDegree*HGTWorker.lengthOfDegreeLatitude();
        widthLonMeters = widthLonDegree*HGTWorker.lengthOfDegreeLongitude(center.Lat);
    }
    
    /**
     * Latitude; positive
     */
    private double N_Bound;
    /**
     * Latitude: negative
     */
    private double S_Bound;
    /**
     * Longitude: negative
     */
    private double W_Bound;
    /**
     * Longitude: positive
     */
    private double E_Bound;
    
    LatLon center;

    public double getN_Bound() {
        return N_Bound;
    }

    public double getS_Bound() {
        return S_Bound;
    }

    public double getW_Bound() {
        return W_Bound;
    }

    public double getE_Bound() {
        return E_Bound;
    }

    public LatLon getCenter() {
        return center;
    }

    public double getWidthLatDegree() {
        return widthLatDegree;
    }

    public double getWidthLonDegree() {
        return widthLonDegree;
    }

    public double getWidthLatMeters() {
        return widthLatMeters;
    }

    public double getWidthLonMeters() {
        return widthLonMeters;
    }

    double widthLatDegree;
    double widthLonDegree;
    
    double widthLatMeters;
    double widthLonMeters;

    /**
     * North Western Point
     * @return
     */
    public LatLon getTopLeft() {
        return new LatLon(getN_Bound(), getW_Bound());
    }

    /**
     * South Eastern Point
     * @return
     */
    public LatLon getBottomRight() {
        return new LatLon(getS_Bound(), getE_Bound());
    }

    public boolean isPositionWithinBbox(LatLon position) {
        return position.getLatitude() <= getN_Bound() && position.getLatitude() >= getS_Bound() && position.getLongitude() >= getW_Bound()
                && position.getLongitude() <= getE_Bound();
    }
    
    public String toString() {
        return "Center: "+center.toString()+"\nBounds: N/S: "+N_Bound+"/"+S_Bound+"; E/W: "+E_Bound+"/"+W_Bound+"\nWidth Lat/Lon degree "+widthLatDegree+"/"+widthLonDegree+"\nWidth Lat/Lon meters "+widthLatMeters+"/"+widthLonMeters;
    }
}
