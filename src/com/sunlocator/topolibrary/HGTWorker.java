/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sunlocator.topolibrary;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author rainer
 */
public class HGTWorker {

    public static final double RadiusOfEarth = 6378137; //used by OpenStreetMap: https://wiki.openstreetmap.org/wiki/Zoom_levels

    /**
     * Length in meters of one degree longitude (West-East), depends on latitude
     *
     * @param latitude
     * @return
     */
    public static double lengthOfDegreeLongitude(double latitude) { //WEST/OST
        return (RadiusOfEarth * (Math.cos(Math.toRadians(latitude)) * Math.PI / 180.0d));
    }

    /**
     * Length in meters of one degree latitude (North-South), independent of
     * lat/lon
     *
     * @return
     */
    public static double lengthOfDegreeLatitude() { //NORD/SÜD
        final double out = (RadiusOfEarth * Math.PI / 180.0d);
        return out;
    }

    /**
     * convert north-south distance in meters to degrees of latitude
     *
     * @param dist distance in meters
     * @return
     */
    public static double distance2degrees_latitude(double dist) {
        return dist / lengthOfDegreeLatitude();
    }

    /**
     * convert north-south distance in degrees to meters of latitude
     *
     * @param dist distance in degrees
     * @return
     */
    public static double degrees2distance_latitude(double dist) {
        return dist * lengthOfDegreeLatitude();
    }


    /**
     * convert east-west distance in degrees to meters of longitude
     *
     * @param dist distance in degrees
     * @return
     */
    public static double degrees2distance_longitude(double dist, double lat) {
        return dist * lengthOfDegreeLongitude(lat);
    }



    /**
     * convert east-west distance in meters to degrees of longitude; depends on
     * latitude
     *
     * @param dist
     * @param lat
     * @return
     */
    public static double distance2degrees_longitude(double dist, double lat) {
        return dist / lengthOfDegreeLongitude(lat);
    }

    /**
     * 3DEM Rules:
     * -1201 cells per row (3sec per cell)
     * -1*1 degree per file
     * -Lower left (SW corner) is filename, ex. N53W003 (North 53, West 3)
     *
     * @param position
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HGTDatafile loadHGTFile_3DEM(LatLon position, HGTFileLoader hgtFileLoader) throws FileNotFoundException, IOException {
        String filename = getFileName_3DEM(position) + ".hgt";
        //System.out.println("load file: "+filename);
        final int cellsPerRow = HGTDatafile.DEM3_cells_per_row;
        short[][] data = hgtFileLoader.loadHGT(filename, cellsPerRow);
        int lat_floored = (int) Math.floor(position.Lat);
        int lon_floored = (int) Math.floor(position.Lon);
        LatLonBoundingBox bounds = new LatLonBoundingBox(lat_floored + 1, lat_floored, lon_floored, lon_floored + 1);
        HGTDatafile out = new HGTDatafile(bounds, data);
        out.cellWidth_LatDegree = 1d/(double)cellsPerRow;
        out.cellWidth_LonDegree = 1d/(double)cellsPerRow;
        out.cellWidth_LatMeters = HGTWorker.degrees2distance_latitude(out.cellWidth_LatDegree);
        out.cellWidth_LonMeters = HGTWorker.degrees2distance_longitude(out.cellWidth_LonDegree, bounds.center.getLatitude());
        return out;
    }


    
    /**
     * 1DEM Rules:
     * -3601 cells per row (1sec per cell)
     * -1*1 degree per file
     * -Lower left (SW corner) is filename, ex. N53W003 (North 53, West 3)
     *
     * @param position
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HGTDatafile loadHGTFile_1DEM(LatLon position, HGTFileLoader hgtFileLoader) throws FileNotFoundException, IOException {
        String filename =  getFileName_1DEM(position) + ".hgt";
        //System.out.println("load file: "+filename);
        final int cellsPerRow = HGTDatafile.DEM1_cells_per_row;
        short[][] data = hgtFileLoader.loadHGT(filename, cellsPerRow);
        int lat_floored = (int) Math.floor(position.Lat);
        int lon_floored = (int) Math.floor(position.Lon);
        LatLonBoundingBox bounds = new LatLonBoundingBox(lat_floored + 1, lat_floored, lon_floored, lon_floored + 1);
        HGTDatafile out = new HGTDatafile(bounds, data);
        out.cellWidth_LatDegree = 1d/(double)cellsPerRow;
        out.cellWidth_LonDegree = 1d/(double)cellsPerRow;
        out.cellWidth_LatMeters = HGTWorker.degrees2distance_latitude(out.cellWidth_LatDegree);
        out.cellWidth_LonMeters = HGTWorker.degrees2distance_longitude(out.cellWidth_LonDegree, bounds.center.getLatitude());
        return out;
    }

    private static String getFileName_3DEM(LatLon position) {
        int lat_floored = (int) Math.floor(position.Lat);
        boolean isNorth = lat_floored >= 0;
        int lon_floored = (int) Math.floor(position.Lon);
        boolean isEast = lon_floored >= 0;

        return (isNorth ? "N" : "S") + String.format("%02d", Math.abs(lat_floored)) + (isEast ? "E" : "W") + String.format("%03d", Math.abs(lon_floored));
    }
    
    private static String getFileName_1DEM(LatLon position) {
        return getFileName_3DEM(position);
    }

    private static short[][] copyDataArray(short[][] data, int x_start, int y_start, int x_length, int y_length) {

        short[][] outArray = new short[x_length][y_length];
        for (int x = x_start; x < x_start + x_length; x++) {
            System.arraycopy(data[x], y_start, outArray[x - x_start], 0, y_length);
        }
        return outArray;
    }

    private static void copyDataArray(short[][] source, int x_start, int y_start, int x_length, int y_length, short[][] target, int target_start_x, int target_start_y) {
        for (int x = x_start; x < x_start + x_length; x++) {
            System.arraycopy(source[x], y_start, target[target_start_x + x - x_start], target_start_y, y_length);
        }
    }

    public static short[][] load_3DEM(LatLon topleft, int cells_x_lon, int cells_y_lat, HGTFileLoader hgtFileLoader) throws  IOException {
        return load_3DEM(topleft, cells_x_lon, cells_y_lat, 0, 0, hgtFileLoader);
    }


    public static short[][] load_3DEM(LatLon topleft, int cells_x_lon, int cells_y_lat, int xcell_offset, int ycell_offset, HGTFileLoader hgtFileLoader) throws  IOException {
        short[][] data = new short[cells_x_lon][cells_y_lat];

        final double cellWidth = 1d/(double) HGTDatafile.DEM3_cells_per_row;

        int start_x_topleft = (int)((topleft.getLongitude()-Math.floor(topleft.getLongitude()))/cellWidth) + xcell_offset;
        int lon = (int) Math.floor(topleft.getLongitude()); //left

        if (start_x_topleft>= HGTDatafile.DEM3_cells_per_row) { //if the start is already on a new file
            lon += (start_x_topleft/ HGTDatafile.DEM3_cells_per_row);
            start_x_topleft %= HGTDatafile.DEM3_cells_per_row;
        } else if (start_x_topleft < 0) {
            lon += Math.floorDiv(start_x_topleft, HGTDatafile.DEM3_cells_per_row);
            start_x_topleft = Math.floorMod(start_x_topleft, HGTDatafile.DEM3_cells_per_row);
        }

        int cell_count_x = 0;
        int missing_len_x = cells_x_lon;

        while (cell_count_x < cells_x_lon) {
            int len_x = Math.min(HGTDatafile.DEM3_cells_per_row - start_x_topleft, missing_len_x);

            int start_y_topleft = (int)((Math.ceil(topleft.getLatitude())-topleft.getLatitude())/cellWidth) + ycell_offset;

            int cell_count_y = 0;
            int lat = (int) Math.ceil(topleft.getLatitude()); //top

            if (start_y_topleft>= HGTDatafile.DEM3_cells_per_row) { //if the start is already in the next file
                lat -= (start_y_topleft/ HGTDatafile.DEM3_cells_per_row);
                start_y_topleft %= HGTDatafile.DEM3_cells_per_row;
            } else if (start_y_topleft < 0) {
                lat -= Math.floorDiv(start_y_topleft, HGTDatafile.DEM3_cells_per_row);
                start_y_topleft = Math.floorMod(start_y_topleft, HGTDatafile.DEM3_cells_per_row);
            }

            int missing_len_y = cells_y_lat;

            while (cell_count_y < cells_y_lat) {

                int len_y = Math.min(HGTDatafile.DEM3_cells_per_row - start_y_topleft, missing_len_y);

                HGTDatafile hgtFile = loadHGTFile_3DEM(new LatLon(lat-0.1d,lon+0.1d), hgtFileLoader);

                copyDataArray(hgtFile.data, start_x_topleft, start_y_topleft, len_x, len_y, data, cell_count_x, cell_count_y);

                start_y_topleft = 1; //the datapoints at the borders might be duplicated in the files ?
                missing_len_y -= len_y;
                cell_count_y += len_y;
                lat--;
            }
            start_x_topleft = 1;
            missing_len_x -= len_x;
            cell_count_x += len_x;
            lon++;

        }
        return data;
    }


    public static HGTDatafile loadFromBoundingBox_3DEM(LatLonBoundingBox bounds, HGTFileLoader hgtFileLoader) throws IOException {
        int cells_x_lon = (int) (bounds.widthLonDegree *(double) HGTDatafile.DEM3_cells_per_row);
        int cells_y_lat = (int) (bounds.widthLatDegree *(double) HGTDatafile.DEM3_cells_per_row);

        LatLon topLeftLatLon = bounds.getTopLeft();

        short[][] data = load_3DEM(topLeftLatLon, cells_x_lon, cells_y_lat, hgtFileLoader);

        return new HGTDatafile(bounds, data);
    }

    @Deprecated
    public static HGTDatafile loadFromBoundingBox_3DEM_OLD_VERSION(LatLonBoundingBox bounds, HGTFileLoader hgtFileLoader) throws IOException {
        if (bounds.widthLatDegree >= 1 || bounds.widthLonDegree >= 1) {
            throw new UnsupportedOperationException("Bounds larger/equal than 1° not supported");
        }

        LatLon topLeftLatLon = bounds.getTopLeft();

        HGTDatafile topLeftHgtFile = loadHGTFile_3DEM(topLeftLatLon, hgtFileLoader);

        int cells_x_lon = (int) (bounds.widthLonDegree / topLeftHgtFile.cellWidth_LonDegree);
        int cells_y_lat = (int) (bounds.widthLatDegree / topLeftHgtFile.cellWidth_LatDegree);


        short[][] data = new short[cells_x_lon][cells_y_lat];

        int start_x_topleft = (int) ((bounds.getW_Bound() - topLeftHgtFile.bounds.getW_Bound()) / topLeftHgtFile.cellWidth_LonDegree);
        int start_y_topleft = (int) ((topLeftHgtFile.bounds.getN_Bound() - bounds.getN_Bound()) / topLeftHgtFile.cellWidth_LatDegree);
        int len_x = Math.min(topLeftHgtFile.cellsLon_X - start_x_topleft, cells_x_lon);
        int len_y = Math.min(topLeftHgtFile.cellsLat_Y - start_y_topleft, cells_y_lat);


        copyDataArray(topLeftHgtFile.data, start_x_topleft, start_y_topleft, len_x, len_y, data, 0, 0);

        //Top Right
        int missing_len_x = cells_x_lon - len_x;
        if (missing_len_x > 0) {
            System.out.println("Missing top right");
            HGTDatafile topRightHgtFile = loadHGTFile_3DEM(new LatLon(bounds.getN_Bound(), bounds.getE_Bound()), hgtFileLoader);
            copyDataArray(topRightHgtFile.data, 1, start_y_topleft, missing_len_x, len_y, data, len_x, 0);
        }

        //Bottom Left
        int missing_len_y = cells_y_lat - len_y;
        if (missing_len_y > 0) {
            System.out.println("Missing bottom left");
            HGTDatafile bottomLeftHgtFile = loadHGTFile_3DEM(new LatLon(bounds.getS_Bound(), bounds.getW_Bound()), hgtFileLoader);
            copyDataArray(bottomLeftHgtFile.data, start_x_topleft, 1, len_x, missing_len_y, data, 0, len_y);
        }

        //Bottom right
        if (missing_len_y > 0 && missing_len_x > 0) {
            System.out.println("Missing bottom right");
            HGTDatafile bottomRightHgtFile = loadHGTFile_3DEM(new LatLon(bounds.getS_Bound(), bounds.getE_Bound()), hgtFileLoader);
            copyDataArray(bottomRightHgtFile.data, 1, 1, missing_len_x, missing_len_y, data, len_x, len_y);
        }

        LatLonBoundingBox hgtfileBoundingbox = new LatLonBoundingBox(topLeftLatLon.Lat, topLeftLatLon.Lat - (cells_y_lat * topLeftHgtFile.cellWidth_LatDegree), topLeftLatLon.Lon, topLeftLatLon.Lon + (cells_x_lon * topLeftHgtFile.cellWidth_LonDegree));
        return new HGTDatafile(hgtfileBoundingbox, data);

    }

    public static HGTDatafile getPreviewHGTDatafile(HGTDatafile input, int size) {
        return getPreviewHGTDatafile(input, size, size);

    }

    public static HGTDatafile getPreviewHGTDatafile(HGTDatafile input, int size_x, int size_y) {
        if (input.cellsLat_Y < size_y || input.cellsLon_X < size_x) {
            throw new ArrayIndexOutOfBoundsException("Input HGTDatafile is smaller than the requested size (" + input.cellsLon_X + "*" + input.cellsLat_Y + ")");
        }
        short[][] data = copyDataArray(input.data, 0, 0, size_x, size_y);

        double N_bound = input.bounds.getN_Bound();
        double W_bound = input.bounds.getW_Bound();
        double S_bound = N_bound - input.cellWidth_LatDegree * size_y;
        double E_bound = W_bound + input.cellWidth_LonDegree * size_x;

        LatLonBoundingBox bounds = new LatLonBoundingBox(N_bound, S_bound, W_bound, E_bound);

        return new HGTDatafile(bounds, data);

    }

    public static HGTDatafile downsampleHGTDatafile(HGTDatafile input) {
        int size_x = input.cellsLon_X;
        int size_y = input.cellsLat_Y;

        short[][] data = new short[size_x / 2][size_y / 2];

        for (int x = 0; x < size_x / 2; x++) {
            int x_orig = x * 2 + 1;
            for (int y = 0; y < size_y / 2; y++) {
                int y_orig = y * 2 + 1;
                
                int count = 0;
                
                int vertCount = 0;
                
                
                for (int x_ = x_orig - 1; x_ < x_orig + 2; x_++) {
                    if (x_ < 0 || x_ > size_x - 1) {
                        continue;
                    }
                    for (int y_ = y_orig - 1; y_ < y_orig + 2; y_++) {
                        if (y_ < 0 || y_ > size_y - 1) {
                            continue;
                        }
                        
                        vertCount+=input.data[x_][y_];
                        count++;

                    }
                }
                
                data[x][y]=(short)(vertCount/count);
            }
        }
        
        return new HGTDatafile(input.bounds, data);

    }

    public static String getJSON(HGTDatafile hgt, boolean enclosement) {

        StringBuilder data = new StringBuilder("");

        float top_y = (float) ((hgt.cellsLat_Y / 2)) * (float) hgt.cellWidth_LatMeters;
        float left_x = (float) ((hgt.cellsLon_X / 2)) * (float) hgt.cellWidth_LonMeters * -1;

        if (enclosement) {
            data.append(left_x).append(",").append(top_y).append(",").append(0f).append(",");
            for (int x = 0; x < hgt.cellsLon_X; x++) {
                float x_m = (float) (x - (hgt.cellsLon_X / 2)) * (float) hgt.cellWidth_LonMeters;
                data.append(x_m).append(",").append(top_y).append(",").append(0f).append(",");
            }
            data.append(left_x * -1).append(",").append(top_y).append(",").append(0f).append(",");

            data.append("\n");
        }

        for (int y = 0; y < hgt.cellsLat_Y; y++) {

            float y_m = (float) (y - (hgt.cellsLat_Y / 2)) * (float) hgt.cellWidth_LatMeters * -1.0f; //y-axis is flipped compared to array y-axis

            if (enclosement) {
                data.append(left_x).append(",").append(y_m).append(",").append(0f).append(",");
            }

            for (int x = 0; x < hgt.cellsLon_X; x++) {
                float x_m = (float) (x - (hgt.cellsLon_X / 2)) * (float) hgt.cellWidth_LonMeters;
                float height = (float) hgt.data[x][y];
                data.append(x_m).append(",").append(y_m).append(",").append(height).append(",");
            }
            if (enclosement) {
                data.append(left_x * -1).append(",").append(y_m).append(",").append(0f).append(",");
            }
            data.append("\n");
        }

        if (enclosement) {
            float bottom_y = top_y * -1f;

            data.append(left_x).append(",").append(bottom_y).append(",").append(0f).append(",");

            for (int x = 0; x < hgt.cellsLon_X; x++) {
                float x_m = (float) (x - (hgt.cellsLon_X / 2)) * (float) hgt.cellWidth_LonMeters;
                data.append(x_m).append(",").append(bottom_y).append(",").append(0f).append(",");
            }

            data.append(left_x * -1).append(",").append(bottom_y).append(",").append(0f).append(",");
        }

        String out = "{\n"
                + "    \"metadata\": {\n"
                + "        \"version\": 4,\n"
                + "        \"type\": \"BufferGeometry\"\n"
                + "    },\n"
                + "    \"uuid\": \"AF2ADB07-FBC5-4BAE-AD60-123456789ABC\",\n"
                + "    \"type\": \"BufferGeometry\",\n"
                + "    \"data\": {\n"
                + "        \"attributes\": {\n"
                + "            \"position\": {\n"
                + "                \"itemSize\": 3,\n"
                + "                \"type\": \"Float32Array\",\n"
                + "                \"array\": [" + data.substring(0, data.length() - 1) + "]\n"
                + "            },\n"
                + "            \"size_x_y\": {\n"
                + "                \"itemSize\": 1,\n"
                + "                \"type\": \"Uint8Array\",\n"
                + "                \"array\": [" + (hgt.cellsLon_X + (enclosement ? 2 : 0)) + "," + (hgt.cellsLat_Y + (enclosement ? 2 : 0)) + "," + hgt.bounds.widthLonMeters + "," + hgt.bounds.widthLatMeters + "]\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}";

        return out;
    }

    /**
     *
     * @param hgtDatafile
     * @param neighbor 0: North; 1: NE; 2: East; 3: SE; 4: South; 5: SW; 6: West; 7: NW
     * @return
     */
    public static LatLonBoundingBox getNeighborBoundingBox(HGTDatafile hgtDatafile, byte neighbor) {

        double S_bound=-1, E_bound=-1, W_bound=-1, N_bound=-1;

        if (neighbor==0 || neighbor == 1 || neighbor == 7) {
            N_bound = hgtDatafile.bounds.widthLatDegree+hgtDatafile.bounds.getN_Bound();
            S_bound = hgtDatafile.bounds.getN_Bound();
        } else if (neighbor==2 || neighbor==6) {
            N_bound = hgtDatafile.bounds.getN_Bound();
            S_bound = hgtDatafile.bounds.getS_Bound();
        } else if (neighbor==3 || neighbor==4 || neighbor==5) {
            N_bound = hgtDatafile.bounds.getS_Bound();
            S_bound = hgtDatafile.bounds.getS_Bound()-hgtDatafile.bounds.widthLatDegree;
        }

        if (neighbor==1 || neighbor==2 || neighbor==3) {
            E_bound = hgtDatafile.bounds.widthLonDegree+hgtDatafile.bounds.getE_Bound();
            W_bound = hgtDatafile.bounds.getE_Bound();
        } else if (neighbor == 0 || neighbor == 4) {
            E_bound = hgtDatafile.bounds.getE_Bound();
            W_bound = hgtDatafile.bounds.getW_Bound();
        } else if (neighbor == 5 || neighbor == 6 || neighbor==7) {
            E_bound = hgtDatafile.bounds.getW_Bound();
            W_bound = hgtDatafile.bounds.getW_Bound() - hgtDatafile.bounds.widthLonDegree;
        }

        LatLonBoundingBox neighborBoundingBox = new LatLonBoundingBox(N_bound, S_bound, W_bound, E_bound);
        return neighborBoundingBox;
    };

    public static HGTDatafile loadNeighbor_3DEM(HGTDatafile hgtDatafile, byte neighbor, HGTFileLoader hgtFileLoader) throws IOException {
        HGTDatafile neighborHGT = loadFromBoundingBox_3DEM(getNeighborBoundingBox(hgtDatafile, neighbor), hgtFileLoader);

        return  neighborHGT;
    }

    public static GLTFDatafile getLODGLTF(LatLonBoundingBox boundingBox, HGTFileLoader hgtFileLoader) throws IOException {

        int lonCellNumber = (int)(boundingBox.widthLonDegree * (float) HGTDatafile.DEM3_cells_per_row);
        int latCellNumber = (int)(boundingBox.widthLatDegree * (float) HGTDatafile.DEM3_cells_per_row);

        float cellWidth_LatMeters = (float)(boundingBox.widthLatMeters / (double)latCellNumber);
        float cellWidth_LonMeters = (float)(boundingBox.widthLonMeters / (double)lonCellNumber);

        while (latCellNumber % 6 != 3)
            latCellNumber++;

        while (lonCellNumber % 6 != 3)
            lonCellNumber++;

        LatLon topLeftLatLon = boundingBox.getTopLeft();



        int slice_x_width = lonCellNumber/3;
        int slice_y_width = latCellNumber/3;

        GLTFDatafile gltfFile = new GLTFDatafile();

        for (int x = 0; x<3; x++) {
            for (int y=0; y<3; y++) {


                float offset_x = (float)(x-1)*(slice_x_width*cellWidth_LonMeters);
                float offset_y = (float)(1-y)*(slice_y_width*cellWidth_LatMeters);

                if (!(x==1 && y == 1)) {
                    short[][] data = load_3DEM(topLeftLatLon, slice_x_width+8, slice_y_width+8, x*slice_x_width-4, y*slice_y_width-4, hgtFileLoader);
                    data = downsampleHGTDatafile(new HGTDatafile(boundingBox, data)).data;
                    data = downsampleHGTDatafile(new HGTDatafile(boundingBox, data)).data;
                    gltfFile.addGLTFMesh(data, (slice_x_width + 8)/4, (slice_y_width + 8)/4, cellWidth_LatMeters*4f, cellWidth_LonMeters*4f, false, false, offset_x, offset_y);

                } else {
                    short[][] data = load_3DEM(topLeftLatLon, slice_x_width, slice_y_width, x*slice_x_width, y*slice_y_width, hgtFileLoader);
                    gltfFile.addGLTFMesh(data, slice_x_width, slice_y_width, cellWidth_LatMeters, cellWidth_LonMeters, true, true, offset_x, offset_y);
                }
            }
        }


        return gltfFile;
    }
    
    
    
    public static class FileFormatException extends RuntimeException {
        String message = "";
        public FileFormatException(String message) {
            this.message = message;
        }
    }
    

}
