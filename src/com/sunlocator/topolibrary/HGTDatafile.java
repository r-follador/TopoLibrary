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
public class HGTDatafile {
    short[][] data;
    public LatLonBoundingBox bounds;
    public int cellsLat_Y; //
    public int cellsLon_X;

    /**
     * Number of cells per row/column in a 3DEM datafile (per 1°)
     */
    public static int DEM3_cells_per_row = 1201;

    /**
     * Number of cells per row/column in a 1DEM datafile (per 1°)
     */
    public static int DEM1_cells_per_row = 3601;
    
    /**
     * width of a cell in degrees along latitude (N-S)
     */
    public double cellWidth_LatDegree;
    /**
     * width of a cell in degrees along longitude (W-E)
     */
    public double cellWidth_LonDegree;
    /**
     * width of a cell in meters along latitude (N-S)
     */
    public double cellWidth_LatMeters;
    /**
     * width of a cell in meters along longitude (W-E)
     */
    public double cellWidth_LonMeters;
    
    public HGTDatafile(LatLonBoundingBox bounds, short[][] data) {
        this.bounds = bounds;
        this.data = data;
        cellsLat_Y = data[0].length;
        cellsLon_X = data.length;
        
        cellWidth_LatDegree = bounds.widthLatDegree/(double)cellsLat_Y;
        cellWidth_LonDegree = bounds.widthLonDegree/(double)cellsLon_X;
        cellWidth_LatMeters = bounds.widthLatMeters/(double)cellsLat_Y;
        cellWidth_LonMeters = bounds.widthLonMeters/(double)cellsLon_X;
    }
}
