package com.sunlocator.topolibrary.MapTile;

import com.sunlocator.topolibrary.HGTWorker;
import com.sunlocator.topolibrary.LatLon;
import com.sunlocator.topolibrary.LatLonBoundingBox;

import java.util.Objects;

public class MapTile {
    public int zoom;
    public int x;
    public int y;

    /**
     * MapTile with defined zoom level and x and y coordinates of tile
     *
     * @param zoom
     * @param x
     * @param y
     */
    public MapTile(int zoom, int x, int y) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
    }

    /**
     * MapTile containing defined LatLon coordinates at given zoom level
     *
     * @param zoom
     * @param latLon
     */
    public MapTile(int zoom, LatLon latLon) {
        double sin = Math.sin(latLon.getLatitude() * d2r),
                z2 = Math.pow(2, zoom),
                x = z2 * (latLon.getLongitude() / 360d + 0.5d),
                y = z2 * (0.5d - 0.25d * Math.log((1d + sin) / (1d - sin)) / Math.PI);

        // Wrap Tile X
        x = x % z2;
        if (x < 0) x = x + z2;
        //X and Y is fractional; floor them
        this.zoom = zoom;
        this.x = (int) Math.floor(x);
        this.y = (int) Math.floor(y);
    }

    //Based on https://github.com/mapbox/tilebelt/blob/master/index.js
    //and https://docs.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system

    static final double d2r = Math.PI / 180;
    static final double r2d = 180 / Math.PI;

    public LatLonBoundingBox getBoundingBox() {
        double e = tile2lon(x + 1, zoom);
        double w = tile2lon(x, zoom);
        double s = tile2lat(y + 1, zoom);
        double n = tile2lat(y, zoom);
        return new LatLonBoundingBox(n,s,w,e);
    }

    private static double tile2lon(int x, int zoom) {
        return (double) x / Math.pow(2, zoom) * 360d - 180d;
    }

    private static double tile2lat(int y, int zoom) {
        double n = Math.PI - 2d * Math.PI * y / Math.pow(2d, zoom);
        return r2d * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
    }

    /**
     * Get the 4 tiles zoomed in once (zoom level higher)
     *
     * @return
     */
    MapTile[] getChildren() {
        MapTile[] out = new MapTile[4];
        out[0] = new MapTile(zoom + 1, x * 2, y * 2);
        out[1] = new MapTile(zoom + 1, x * 2 + 1, y * 2);
        out[2] = new MapTile(zoom + 1, x * 2 + 1, y * 2 + 1);
        out[3] = new MapTile(zoom + 1, x * 2, y * 2 + 1);
        return out;
    }

    /**
     * Get the tile zoomed out once (zoom level lower)
     *
     * @return
     */
    MapTile getParent() {
        return new MapTile(zoom - 1, x >> 1, y >> 1);
    }

    /**
     * Get Sibling tiles (including me)
     *
     * @return
     */
    MapTile[] getSiblings() {
        return getParent().getChildren();
    }

    /**
     * Check if I am part of an array of tiles
     *
     * @param tileArray
     * @return
     */
    boolean amIpartOfTileArray(MapTile[] tileArray) {
        for (MapTile thatTile : tileArray) {
            if (thatTile.equals(this))
                return true;
        }
        return false;
    }

    /**
     * Approximate ground resolution (in meters per pixel) of this tile
     * Calculated at the Northern border of this tile
     * This assumes a tile is 256x256px
     *
     * @return
     */
    public double getGroundResolution() {
        double latitude = tile2lat(this.y, this.zoom);
        return getGroundResolution(latitude, this.zoom);
    }

    /**
     * Get World Pixel Coordinates of the NW (top left) corner of this tile.
     * This assumes a tile is 256x256px
     *
     * @return
     */
    public XY getWorldPixelPositionOfTopleftCorner() {
        return TileXYtoPixelXY(this);
    }

    /**
     * Local pixel coordinate of Lat Lon
     * This assumes a tile is 256x256px
     * Returns null if LatLon is not in tile
     *
     * @param latLon
     * @return
     */
    public XY getLocalPixelPositionofLatLon(LatLon latLon) {
        XY worldCoordinates = convertLatLongToPixelXY(latLon, this.zoom);
        XY topLeftCoordinates = this.getWorldPixelPositionOfTopleftCorner();
        int x = worldCoordinates.x - topLeftCoordinates.x;
        int y = worldCoordinates.y - topLeftCoordinates.y;

        if (x < 0 || x > 255 || y < 0 || y > 255)
            return null;

        return new XY(x, y);
    }

    ////////////////////

    /**
     * Determines the ground resolution (in meters per pixel) at specified latitude and zoom level
     * This assumes a tile is 256x256px
     *
     * @param latitude
     * @param zoom
     * @return
     */
    public static double getGroundResolution(double latitude, int zoom) {
        return Math.cos(latitude * d2r) * 2d * Math.PI * HGTWorker.RadiusOfEarth / mapSize(zoom);
    }

    /**
     * Map size at a given zoom level
     * This assumes a tile is 256x256px
     *
     * @param zoom
     * @return
     */
    public static long mapSize(int zoom) {
        return 256 << zoom;
    }

    /**
     * Converts a point from latitude/longitude WGS-84 coordinates (in degrees) into pixel XY coordinates at a specified level of detail
     * This assumes a tile is 256x256px
     *
     * @param latLon
     * @param zoom   the zoom level
     * @return pixel in world coordinates at given zoom level
     */
    public static XY convertLatLongToPixelXY(LatLon latLon, int zoom) {
        double x = (latLon.getLongitude() + 180d) / 360d;
        double sinLatitude = Math.sin(latLon.getLatitude() * d2r);
        double y = 0.5d - Math.log((1d + sinLatitude) / (1d - sinLatitude)) / (4 * Math.PI);

        long mapSize = mapSize(zoom);
        int pixelX = (int) clip(x * mapSize + 0.5, 0, mapSize - 1);
        int pixelY = (int) clip(y * mapSize + 0.5, 0, mapSize - 1);
        return new XY(pixelX, pixelY);
    }

    /**
     * Converts pixel position at a specified level of detail into latitude/longitude WGS-84 coordinates (in degrees)
     * This assumes a tile is 256x256px
     *
     * @param pixel in world coordinates
     * @param zoom  the zoom level
     * @return latitude/longitude
     */
    public static LatLon convertPixelXYtoLatLong(XY pixel, int zoom) {
        System.err.println("warning: there's likely an error");
        double mapSize = mapSize(zoom);
        double x = (clip(pixel.x, 0, mapSize - 1) / mapSize) - 0.5d;
        double y = 0.5d - (clip(pixel.y, 0, mapSize - 1) / mapSize);

        double latitude = 90d - 360d * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
        double longitude = 360d * x;
        return new LatLon(latitude, longitude);
    }

    /**
     * Convert World pixel coordinates into tile position
     * This assumes a tile is 256x256px
     *
     * @param pixel in world coordinates
     * @param zoom  the zoom level
     * @return
     */
    public static MapTile PixelXYToTileXY(XY pixel, int zoom) {
        return new MapTile(zoom, pixel.x / 256, pixel.y / 256);
    }

    /**
     * Convert tile position (upper-left corner) into world pixel coordinates
     *
     * @param tile
     * @return
     */
    public static XY TileXYtoPixelXY(MapTile tile) {
        return new XY(tile.x * 256, tile.y * 256);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapTile that = (MapTile) o;
        return zoom == that.zoom &&
                x == that.x &&
                y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(zoom, x, y);
    }

    @Override
    public String toString() {
        return "MapTile{" +
                "zoom=" + zoom +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    /**
     * Clip double within min and max bound
     *
     * @param n
     * @param minValue
     * @param maxValue
     * @return
     */
    private static double clip(double n, double minValue, double maxValue) {
        return Math.min(Math.max(n, minValue), maxValue);
    }


    public static class XY {
        public int x;
        public int y;

        public XY(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
