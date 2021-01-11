/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sunlocator.topolibrary;

/**
 * More info about HGT: https://wiki.openstreetmap.org/wiki/SRTM
 * @author rainer
 */
public class HGTDatafile {

    public enum HGT_Type {
        DEM1, DEM3, hgtOther
    }

    short[][] data;
    public LatLonBoundingBox bounds;
    public int cellsLat_Y; //
    public int cellsLon_X;

    public String toString() {
        return bounds.toString()+"\n"+"Number of Cells Lon(X)/Lat(Y): "+cellsLon_X+"/"+cellsLat_Y;
    }

    /**
     * Number of cells per row/column in a 3DEM datafile (per 1°)
     */
    public static int DEM3_cells_per_row = 1201;

    /**
     * Size in degree of one row/column in a 3DEM datafile
     */
    public static double  DEM3_cellWidth_LatDegree = 1.0d/DEM3_cells_per_row;

    /**
     * Size in degree of one row/column in a 3DEM datafile
     */
    public static double DEM3_cellWidth_LonDegree = DEM3_cellWidth_LatDegree;

    /**
     * Number of cells per row/column in a 1DEM datafile (per 1°)
     */
    public static int DEM1_cells_per_row = 3601;

    /**
     * Size in degree of one row/column in a 1DEM datafile
     */
    public static double  DEM1_cellWidth_LatDegree = 1.0d/DEM1_cells_per_row;

    /**
     * Size in degree of one row/column in a 31DEM datafile
     */
    public static double DEM1_cellWidth_LonDegree = DEM1_cellWidth_LatDegree;
    
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

    /**
     * Type of this file (DEM1, DEM3 or other)
     */
    public HGT_Type hgt_type;
    
    public HGTDatafile(LatLonBoundingBox bounds, short[][] data, HGT_Type hgt_type) {
        this.bounds = bounds;
        this.data = data;
        cellsLat_Y = data[0].length;
        cellsLon_X = data.length;
        this.hgt_type = hgt_type;
        
        cellWidth_LatDegree = bounds.widthLatDegree/(double)cellsLat_Y;
        cellWidth_LonDegree = bounds.widthLonDegree/(double)cellsLon_X;
        cellWidth_LatMeters = bounds.widthLatMeters/(double)cellsLat_Y;
        cellWidth_LonMeters = bounds.widthLonMeters/(double)cellsLon_X;
    }

    public short[][] getData() {
        return data;
    }
}
