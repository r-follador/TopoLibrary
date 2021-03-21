/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sunlocator.topolibrary;

import com.sunlocator.topolibrary.MapTile.MapTile;
import com.sunlocator.topolibrary.MapTile.MapTileWorker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

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
        return (RadiusOfEarth * Math.PI / 180.0d);
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
     * Load 3DEM file
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
        HGTDatafile out = new HGTDatafile(bounds, data, HGTDatafile.HGT_Type.DEM3);
        out.cellWidth_LatDegree = 1d/(double)cellsPerRow;
        out.cellWidth_LonDegree = 1d/(double)cellsPerRow;
        out.cellWidth_LatMeters = HGTWorker.degrees2distance_latitude(out.cellWidth_LatDegree);
        out.cellWidth_LonMeters = HGTWorker.degrees2distance_longitude(out.cellWidth_LonDegree, bounds.center.getLatitude());
        return out;
    }


    
    /**
     * Load 1DEM file
     * 1DEM Rules:
     * -3601 cells per row (1sec per cell)
     * -1*1 degree per file
     * -Lower left (SW corner) is filename, ex. N53W003 (North 53, West 3)
     *
     * @param position that's within the requested HGT file
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
        HGTDatafile out = new HGTDatafile(bounds, data, HGTDatafile.HGT_Type.DEM1);
        out.cellWidth_LatDegree = 1d/(double)cellsPerRow;
        out.cellWidth_LonDegree = 1d/(double)cellsPerRow;
        out.cellWidth_LatMeters = HGTWorker.degrees2distance_latitude(out.cellWidth_LatDegree);
        out.cellWidth_LonMeters = HGTWorker.degrees2distance_longitude(out.cellWidth_LonDegree, bounds.center.getLatitude());
        return out;
    }

    /**
     * Get the name of the DEM3 file containing the position
     * @param position
     * @return
     */
    private static String getFileName_3DEM(LatLon position) {
        int lat_floored = (int) Math.floor(position.Lat);
        boolean isNorth = lat_floored >= 0;
        int lon_floored = (int) Math.floor(position.Lon);
        boolean isEast = lon_floored >= 0;

        return (isNorth ? "N" : "S") + String.format("%02d", Math.abs(lat_floored)) + (isEast ? "E" : "W") + String.format("%03d", Math.abs(lon_floored));
    }

    /**
     * Get the name of the DEM1 file containing the position
     * @param position
     * @return
     */
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

    public static short[][] load_3DEM(LatLon topleft, int cells_x_lon, int cells_y_lat, HGTFileLoader hgtFileLoader_3DEM) throws  IOException {
        return load_3DEM(topleft, cells_x_lon, cells_y_lat, 0, 0, hgtFileLoader_3DEM);
    }

    public static short[][] load_1DEM(LatLon topleft, int cells_x_lon, int cells_y_lat, HGTFileLoader hgtFileLoader_1DEM) throws  IOException {
        return load_1DEM(topleft, cells_x_lon, cells_y_lat, 0, 0, hgtFileLoader_1DEM);
    }

    /**
     * Load data from HGT files (3DEM)
     * @param topleft Northwestern most corner
     * @param cells_x_lon number of longitudinal cells to read
     * @param cells_y_lat number of latitudinal cells to read
     * @param xcell_offset x offset (lon)
     * @param ycell_offset y offset (lat)
     * @param hgtFileLoader_3DEM
     * @return array of shorts
     * @throws IOException
     */
    public static short[][] load_3DEM(LatLon topleft, int cells_x_lon, int cells_y_lat, int xcell_offset, int ycell_offset, HGTFileLoader hgtFileLoader_3DEM) throws  IOException {
        short[][] data = new short[cells_x_lon][cells_y_lat];


        int start_x_topleft = (int)((topleft.getLongitude()-Math.floor(topleft.getLongitude()))*(double) HGTDatafile.DEM3_cells_per_row) + xcell_offset;
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

            int start_y_topleft = (int)((Math.ceil(topleft.getLatitude())-topleft.getLatitude())*(double) HGTDatafile.DEM3_cells_per_row) + ycell_offset;

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

                HGTDatafile hgtFile = loadHGTFile_3DEM(new LatLon(lat-0.1d,lon+0.1d), hgtFileLoader_3DEM);

                copyDataArray(hgtFile.data, start_x_topleft, start_y_topleft, len_x, len_y, data, cell_count_x, cell_count_y);

                start_y_topleft = 1; //the datapoints at the borders might be duplicated in the files ? --> yes, according to https://wiki.openstreetmap.org/wiki/SRTM
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

    /**
     * Load data from HGT files (1DEM)
     * @param topleft Northwestern most corner
     * @param cells_x_lon number of longitudinal cells to read
     * @param cells_y_lat number of latitudinal cells to read
     * @param xcell_offset x offset (lon)
     * @param ycell_offset y offset (lat)
     * @param hgtFileLoader_1DEM
     * @return array of shorts
     * @throws IOException
     */
    public static short[][] load_1DEM(LatLon topleft, int cells_x_lon, int cells_y_lat, int xcell_offset, int ycell_offset, HGTFileLoader hgtFileLoader_1DEM) throws  IOException {
        short[][] data = new short[cells_x_lon][cells_y_lat];

        final double cellWidth = 1d/(double) HGTDatafile.DEM1_cells_per_row;

        int start_x_topleft = (int)((topleft.getLongitude()-Math.floor(topleft.getLongitude()))/cellWidth) + xcell_offset;
        int lon = (int) Math.floor(topleft.getLongitude()); //left

        if (start_x_topleft>= HGTDatafile.DEM1_cells_per_row) { //if the start is already on a new file
            lon += (start_x_topleft/ HGTDatafile.DEM1_cells_per_row);
            start_x_topleft %= HGTDatafile.DEM1_cells_per_row;
        } else if (start_x_topleft < 0) {
            lon += Math.floorDiv(start_x_topleft, HGTDatafile.DEM1_cells_per_row);
            start_x_topleft = Math.floorMod(start_x_topleft, HGTDatafile.DEM1_cells_per_row);
        }

        int cell_count_x = 0;
        int missing_len_x = cells_x_lon;

        while (cell_count_x < cells_x_lon) {
            int len_x = Math.min(HGTDatafile.DEM1_cells_per_row - start_x_topleft, missing_len_x);

            int start_y_topleft = (int)((Math.ceil(topleft.getLatitude())-topleft.getLatitude())/cellWidth) + ycell_offset;

            int cell_count_y = 0;
            int lat = (int) Math.ceil(topleft.getLatitude()); //top

            if (start_y_topleft>= HGTDatafile.DEM1_cells_per_row) { //if the start is already in the next file
                lat -= (start_y_topleft/ HGTDatafile.DEM1_cells_per_row);
                start_y_topleft %= HGTDatafile.DEM1_cells_per_row;
            } else if (start_y_topleft < 0) {
                lat -= Math.floorDiv(start_y_topleft, HGTDatafile.DEM1_cells_per_row);
                start_y_topleft = Math.floorMod(start_y_topleft, HGTDatafile.DEM1_cells_per_row);
            }

            int missing_len_y = cells_y_lat;

            while (cell_count_y < cells_y_lat) {

                int len_y = Math.min(HGTDatafile.DEM1_cells_per_row - start_y_topleft, missing_len_y);

                HGTDatafile hgtFile = loadHGTFile_1DEM(new LatLon(lat-0.1d,lon+0.1d), hgtFileLoader_1DEM);

                copyDataArray(hgtFile.data, start_x_topleft, start_y_topleft, len_x, len_y, data, cell_count_x, cell_count_y);

                start_y_topleft = 1; //the datapoints at the borders might be duplicated in the files --> yes, according to https://wiki.openstreetmap.org/wiki/SRTM
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

    public static HGTDatafile loadFromBoundingBox_3DEM(LatLonBoundingBox bounds, HGTFileLoader hgtFileLoader_3DEM) throws IOException {
        int cells_x_lon = (int) (bounds.widthLonDegree *(double) HGTDatafile.DEM3_cells_per_row);
        int cells_y_lat = (int) (bounds.widthLatDegree *(double) HGTDatafile.DEM3_cells_per_row);

        LatLon topLeftLatLon = bounds.getTopLeft();

        short[][] data = load_3DEM(topLeftLatLon, cells_x_lon, cells_y_lat, hgtFileLoader_3DEM);

        return new HGTDatafile(bounds, data, HGTDatafile.HGT_Type.DEM3);
    }

    //TODO: this::::
    public static HGTDatafile loadFromBoundingBox_1DEM(LatLonBoundingBox bounds, HGTFileLoader hgtFileLoader_1DEM) throws IOException {

        int cells_x_lon = (int) (bounds.widthLonDegree *(double) HGTDatafile.DEM1_cells_per_row);
        int cells_y_lat = (int) (bounds.widthLatDegree *(double) HGTDatafile.DEM1_cells_per_row);

        LatLon topLeftLatLon = bounds.getTopLeft();

        short[][] data = load_1DEM(topLeftLatLon, cells_x_lon, cells_y_lat, hgtFileLoader_1DEM);

        throw new UnsupportedOperationException(); //NOT done yet
        //return new HGTDatafile(bounds, data, HGTDatafile.HGT_Type.DEM1);
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
        return new HGTDatafile(hgtfileBoundingbox, data, HGTDatafile.HGT_Type.DEM3);

    }

    public static HGTDatafile getPreviewHGTDatafile(HGTDatafile input, int size) {
        return getPreviewHGTDatafile(input, size, size);

    }

    /**
     * Get a preview HGTDatafile containing only the Northwestern DEM elements
     * @param input
     * @param size_x size in longitudinal DEM elements
     * @param size_y size in latitudinal DEM elements
     * @return
     */
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

        return new HGTDatafile(bounds, data, input.hgt_type);

    }

    /**
     * Downsample the input HGTFile by taking the average of neighboring height values
     * Returns a HGTDatafile with half the number of Lon-cells and half the number of Lat-cells (i.e. one quarter of the size of the original)
     * @param input
     * @return
     */
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
        
        return new HGTDatafile(input.bounds, data, HGTDatafile.HGT_Type.hgtOther);

    }

    @Deprecated
    /**
     * Deprecated
     * Use getLODGLTF_3DEM()
     * @param hgt
     * @param enclosement
     * @return
     */
    public static String getJSON(HGTDatafile hgt, boolean enclosement) {

        StringBuilder data = new StringBuilder();

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
                float height = hgt.data[x][y];
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

        return new LatLonBoundingBox(N_bound, S_bound, W_bound, E_bound);
    }

    public static HGTDatafile loadNeighbor_3DEM(HGTDatafile hgtDatafile, byte neighbor, HGTFileLoader hgtFileLoader) throws IOException {
        return loadFromBoundingBox_3DEM(getNeighborBoundingBox(hgtDatafile, neighbor), hgtFileLoader);
    }

    /**
     * Make LatLonBoundingBox whose DEM3 arrays width and height will be divisible by 4 +1
     * @param suggestedBoundingBox
     * @return extended LatLonBoundingBox
     */
    public static LatLonBoundingBox convertSuggestedBBox4LOD_3DEM(LatLonBoundingBox suggestedBoundingBox) {
        int lonCellNumber = (int)(suggestedBoundingBox.widthLonDegree * (float) HGTDatafile.DEM3_cells_per_row);
        int latCellNumber = (int)(suggestedBoundingBox.widthLatDegree * (float) HGTDatafile.DEM3_cells_per_row);

        while (latCellNumber % 4 != 1)
            latCellNumber++;

        while (lonCellNumber % 4 != 1)
            lonCellNumber++;

        double N_bound = suggestedBoundingBox.center.getLatitude()+((float)(latCellNumber/2)*HGTDatafile.DEM3_cellWidth_LatDegree);
        double S_bound = N_bound-(latCellNumber*HGTDatafile.DEM3_cellWidth_LatDegree);
        double W_bound = suggestedBoundingBox.center.getLongitude()-((float)(lonCellNumber/2)*HGTDatafile.DEM3_cellWidth_LonDegree);
        double E_bound = W_bound+(lonCellNumber*HGTDatafile.DEM3_cellWidth_LonDegree);

        return new LatLonBoundingBox(N_bound, S_bound, W_bound, E_bound);
    }

    /**
     * Make LatLonBoundingBox whose DEM1 arrays width and height will be divisible by 4 +1
     * @param suggestedBoundingBox
     * @return extended LatLonBoundingBox
     */
    public static LatLonBoundingBox convertSuggestedBBox4LOD_1DEM(LatLonBoundingBox suggestedBoundingBox) {
        int lonCellNumber = (int)(suggestedBoundingBox.widthLonDegree * (float) HGTDatafile.DEM1_cells_per_row);
        int latCellNumber = (int)(suggestedBoundingBox.widthLatDegree * (float) HGTDatafile.DEM1_cells_per_row);

        while (latCellNumber % 4 != 1)
            latCellNumber++;

        while (lonCellNumber % 4 != 1)
            lonCellNumber++;

        double N_bound = suggestedBoundingBox.center.getLatitude()+((float)(latCellNumber/2)*HGTDatafile.DEM1_cellWidth_LatDegree);
        double S_bound = N_bound-(latCellNumber*HGTDatafile.DEM1_cellWidth_LatDegree);
        double W_bound = suggestedBoundingBox.center.getLongitude()-((float)(lonCellNumber/2)*HGTDatafile.DEM1_cellWidth_LonDegree);
        double E_bound = W_bound+(lonCellNumber*HGTDatafile.DEM1_cellWidth_LonDegree);

        return new LatLonBoundingBox(N_bound, S_bound, W_bound, E_bound);
    }

    /**
     * Get a GLTFDatafile consisting of 3x3 terrain meshes. The one in the middle has the highest detail, the other eight are
     * 4fold downsampled (lower level of detail).
     * Use convertSuggestedBBox4LOD_3DEM() to get the actual bounding box.
     * @param suggestedBoundingBox LatLonBoundingBox with the approximate bounds. Bounds will be adjusted to contain an area
     *                             which is divisible by 4(+1) DEM fields to make downsampling and overlap possible.
     * @param hgtFileLoader
     * @return
     * @throws IOException
     */
    public static GLTFDatafile getLODGLTF_3DEM(LatLonBoundingBox suggestedBoundingBox, HGTFileLoader hgtFileLoader) throws IOException {

        LatLonBoundingBox boundingBox = convertSuggestedBBox4LOD_3DEM(suggestedBoundingBox);


        /**System.out.println("--suggested---");
        System.out.println(suggestedBoundingBox.toString());
        System.out.println("--calculated---");
        System.out.println(boundingBox.toString());**/

        int lonCellNumber = (int)Math.round(boundingBox.widthLonDegree/HGTDatafile.DEM3_cellWidth_LonDegree);
        int latCellNumber = (int)Math.round(boundingBox.widthLatDegree/HGTDatafile.DEM3_cellWidth_LatDegree);

        if (lonCellNumber%4 != 1)
            System.err.println("Error: loncelllnumber "+lonCellNumber);
        if (latCellNumber%4 != 1)
            System.err.println("Error: latcelllnumber "+latCellNumber);

        double cellWidth_LatMeters = boundingBox.widthLatMeters/(double)latCellNumber;
        double cellWidth_LonMeters = boundingBox.widthLonMeters/(double)lonCellNumber;


        LatLon topLeftLatLon = boundingBox.getTopLeft();

        GLTFDatafile gltfFile = new GLTFDatafile();

        GLTFDatafile.UvTexture uvTexture = new GLTFDatafile.UvTexture(false, false);

        for (int x=-1; x<=1; x++) {
            for (int y=-1; y<=1; y++) {
                if (!(x==0 && y == 0)) {
                    short[][] data = load_3DEM(topLeftLatLon, lonCellNumber+6, latCellNumber+6, (x*(lonCellNumber-1))-3, (y*(latCellNumber-1))-3, hgtFileLoader);
                    data = downsampleHGTDatafile(new HGTDatafile(suggestedBoundingBox, data, HGTDatafile.HGT_Type.hgtOther)).data;
                    data = downsampleHGTDatafile(new HGTDatafile(suggestedBoundingBox, data, HGTDatafile.HGT_Type.hgtOther)).data;

                    float offset_x = (float)(((lonCellNumber/4)*(cellWidth_LonMeters*4))*x);
                    if ((lonCellNumber/4)%2!=0) //uneven; fuck me if I know why
                        offset_x+=2f*cellWidth_LonMeters;
                    float offset_y = (float)(((latCellNumber/4)*(cellWidth_LatMeters*4))*y*-1f);
                    if ((latCellNumber/4)%2!=0) //uneven; fuck me if I know why
                        offset_y-=2f*cellWidth_LatMeters;
                    gltfFile.addGLTFMesh(data, (lonCellNumber+6)/4, (latCellNumber+6)/4, cellWidth_LatMeters*4f, cellWidth_LonMeters*4f, false, offset_x, offset_y, uvTexture, uvTexture);

                } else {
                    short[][] data = load_3DEM(topLeftLatLon, lonCellNumber, latCellNumber, 0,0, hgtFileLoader);
                    gltfFile.addGLTFMesh(data, lonCellNumber, latCellNumber, cellWidth_LatMeters, cellWidth_LonMeters, true, 0, 0, new GLTFDatafile.UvTexture(true, false), uvTexture);

                }
            }
        }
        return gltfFile;
    }


    /**
     *
     * @param boundingBox
     * @param zoomLevel
     * @param hgtFileLoader
     * @param textureURL in the form of https://api.maptiler.com/maps/basic/%d/%d/%d.png where ½d, %d, ½d is zoom, x, y (or null if unused)
     * @return
     * @throws IOException
     */
    public static GLTFDatafile getTileGLTF_3DEM(LatLonBoundingBox boundingBox, int zoomLevel, boolean enclosement, HGTFileLoader hgtFileLoader, String textureURL) throws IOException {
        MapTile[][] mtiles = MapTileWorker.getTilesFromBoundingBox(boundingBox, zoomLevel);
        int width = mtiles.length;
        int height = mtiles[0].length;

        LatLon center = boundingBox.center;

        GLTFDatafile gltfFile = new GLTFDatafile();

        GLTFDatafile.UvTexture enclosementTexture = new GLTFDatafile.UvTexture(false, false);
        enclosementTexture.metallicFactor=1;

        //For each tile
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {

                LatLonBoundingBox tileBbox = mtiles[x][y].getBoundingBox();
                //System.out.println(tileBbox.toString());

                int lonCellNumber = (int)Math.ceil(tileBbox.widthLonDegree*(double) HGTDatafile.DEM3_cells_per_row);
                int start_x_topleft = (int)((tileBbox.getW_Bound()-Math.floor(tileBbox.getW_Bound()))*(double) HGTDatafile.DEM3_cells_per_row);
                int start_x_bottomright = (int)((tileBbox.getE_Bound()-Math.floor(tileBbox.getE_Bound()))*(double) HGTDatafile.DEM3_cells_per_row);
                lonCellNumber = Math.abs(start_x_topleft-start_x_bottomright)+1;

                int latCellNumber = (int)Math.ceil(tileBbox.widthLatDegree*(double) HGTDatafile.DEM3_cells_per_row);
                int start_y_topleft = (int)((Math.ceil(tileBbox.getN_Bound())-tileBbox.getN_Bound())*(double) HGTDatafile.DEM3_cells_per_row);
                int start_y_bottomright = (int)((Math.ceil(tileBbox.getS_Bound())-tileBbox.getS_Bound())*(double) HGTDatafile.DEM3_cells_per_row);
                latCellNumber = Math.abs(start_y_topleft-start_y_bottomright)+1;

                //double cellWidth_LatMeters = HGTWorker.lengthOfDegreeLatitude()/(double)HGTDatafile.DEM3_cells_per_row;
                //double cellWidth_LonMeters = HGTWorker.lengthOfDegreeLongitude(center.getLatitude())/(double)HGTDatafile.DEM3_cells_per_row;

                double cellWidth_LatMeters = HGTWorker.degrees2distance_latitude(tileBbox.getWidthLatDegree())/(double)(latCellNumber-1);
                double cellWidth_LonMeters = HGTWorker.degrees2distance_longitude(tileBbox.getWidthLonDegree(), center.getLatitude())/(double)(lonCellNumber-1);

                float offset_x = (float)HGTWorker.degrees2distance_longitude(tileBbox.getTopLeft().getLongitude()-center.getLongitude(), center.getLatitude());
                float offset_y = (float)HGTWorker.degrees2distance_latitude(tileBbox.getTopLeft().getLatitude()-center.getLatitude());

                //float offset_x = 0;
                //float offset_y = 0;

                short[][] data = load_3DEM(tileBbox.getTopLeft(), lonCellNumber, latCellNumber, 0,0, hgtFileLoader);

                GLTFDatafile.UvTexture UVTexture;
                if (textureURL != null && !textureURL.isEmpty()) {
                    UVTexture = new GLTFDatafile.UvTexture(String.format(textureURL, mtiles[x][y].zoom, mtiles[x][y].x, mtiles[x][y].y));
                } else {
                    UVTexture = new GLTFDatafile.UvTexture(true, false);
                }

                //UVTexture.considerOverlap = 1f;


                boolean enclosementNeeded =  enclosement && (x==0 || x == width-1 || y==0 || y==height-1);

                GLTFDatafile.GLTFMesh mesh = gltfFile.addGLTFMesh(data, lonCellNumber, latCellNumber, cellWidth_LatMeters, cellWidth_LonMeters, enclosementNeeded, offset_x, offset_y, UVTexture, enclosementTexture);
                mesh.metadata.put("NBound", String.valueOf(tileBbox.getN_Bound()));
                mesh.metadata.put("SBound", String.valueOf(tileBbox.getS_Bound()));
                mesh.metadata.put("WBound", String.valueOf(tileBbox.getW_Bound()));
                mesh.metadata.put("EBound", String.valueOf(tileBbox.getE_Bound()));
                mesh.metadata.put("Distance1DegreeLatitude", String.valueOf(HGTWorker.degrees2distance_latitude(1d)));
                mesh.metadata.put("Distance1DegreeLongitude", String.valueOf(HGTWorker.degrees2distance_longitude(1d, center.getLatitude())));
                mesh.metadata.put("cellWidth_LatMeters", String.valueOf(cellWidth_LatMeters));
                mesh.metadata.put("cellWidth_LonMeters", String.valueOf(cellWidth_LonMeters));
                /**System.out.printf("Max x / y: %f %f \n", mesh.getMax_x(), mesh.getMax_y());
                System.out.printf("Min x / y: %f %f \n", mesh.getMin_x(), mesh.getMin_y());
                System.out.printf("Cell width long / lat: %f %f\n", cellWidth_LonMeters, cellWidth_LatMeters);**/
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
