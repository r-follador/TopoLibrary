package com.sunlocator.topolibrary;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapWorker {

    public static BufferedImage getMapPng(LatLonBoundingBox boundingBox) throws IOException {

        //test url:
        //https://api.maptiler.com/maps/basic/static/9.37,46.71,9.80,47.01/2000x2000.png?key=***REMOVED***&path=9.37,46.71|9.80,46.71|9.80,47.01|9.37,47.01|9.37,46.71

        //maptiler url
        //URL url =new URL("https://api.maptiler.com/maps/basic/static/"+boundingBox.getW_Bound()+","+boundingBox.getS_Bound()+","+boundingBox.getE_Bound()+","+boundingBox.getN_Bound()+"/"+mapSize_x+"x"+mapSize_y+".png?key=***REMOVED***");
        //URL url = new URL("https://api.maptiler.com/maps/basic/static/"+boundingBox.getCenter().getLongitude()+","+boundingBox.getCenter().getLatitude()+","+(zoomSetting.zoomLevel-1)+"/"+zoomSetting.widthPx+"x"+zoomSetting.widthPx+".png?key=***REMOVED***");

        //mapquest
        //https://www.mapquestapi.com/staticmap/v5/map?key=Zb3TQrlF7i81yeJiVzIPDPXs7qw2g67N&boundingBox=47.01,9.37,46.71,9.80&size=1024,1024&shape=46.71,9.37|46.71,9.80|47.01,9.80|47.01,9.37|46.71,9.37
        //String key = "Zb3TQrlF7i81yeJiVzIPDPXs7qw2g67N";
        //URL url = new URL("https://www.mapquestapi.com/staticmap/v5/map?key="+key+"&boundingBox="+boundingBox.getN_Bound()+","+boundingBox.getW_Bound()+","+boundingBox.getS_Bound()+","+boundingBox.getE_Bound()+"&size=1024,1024");

        //Thunderforest
        //https://tile.thunderforest.com/static/outdoors/9.59043,46.86365,15/1024x1024.png?apikey=694d0a46ebbe4fcea2afff9581a3a773
        //URL url = new URL("https://tile.thunderforest.com/static/landscape/"+boundingBox.getCenter().getLongitude()+","+boundingBox.getCenter().getLatitude()+","+zoomSetting.zoomLevel+"/"+zoomSetting.widthPx+"x"+zoomSetting.heightPx+".png?apikey=694d0a46ebbe4fcea2afff9581a3a773");


        OSMZoomSetting zoomSetting = getBestZoomLevel(boundingBox, 1024);
        URL url = new URL("https://api.maptiler.com/maps/dae70481-0d42-4345-867d-216c14f6ead8/static/"+boundingBox.getCenter().getLongitude()+","+boundingBox.getCenter().getLatitude()+","+(zoomSetting.zoomLevel-1)+"/"+zoomSetting.widthPx+"x"+zoomSetting.widthPx+".png?key=***REMOVED***");

        return getImageFromUrl(url);
    }

    private static BufferedImage getImageFromUrl(URL url) throws IOException {
        System.out.println("url to call: "+url.toString());
        //System.out.println(url.toString());
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5 * 1000);
        connection.setReadTimeout(20 * 1000);
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
        connection.connect();
        return ImageIO.read(connection.getInputStream());
    }

    /**
     * Download tile image from maptiler (x,y,zoom)
     * @param x
     * @param y
     * @param z
     * @return
     * @throws IOException
     */
    public static BufferedImage getMapPng(int x, int y, int z) throws IOException {
        URL url = new URL("https://api.maptiler.com/maps/dae70481-0d42-4345-867d-216c14f6ead8/"+z+"/"+x+"/"+y+"@2x.png?key=***REMOVED***");
        return getImageFromUrl(url);
    }

    /**
     * Returns the smallest tile (largest zoom factor) that covers the boundingBox
     * @param boundingBox
     * @return
     * @throws IOException
     */
    public static BufferedImage getMapRasterTiles(LatLonBoundingBox boundingBox) throws IOException {
        //TODO: see https://github.com/mapbox/tilebelt/blob/master/index.js

        int[] raster = bboxToTile(boundingBox);
        System.out.println("X,Y,Z: "+raster[0]+","+raster[1]+","+raster[2]);
        return getMapPng(raster[0], raster[1], raster[2]);

    }

    /**
     * Get the smallest tile to cover a bbox
     * @param boundingBox
     * @return x,y,zoom
     */
    private static int[] bboxToTile(LatLonBoundingBox boundingBox) {
        int[] min = pointToTile(boundingBox.getW_Bound(), boundingBox.getN_Bound(), 32);
        int[] max = pointToTile(boundingBox.getE_Bound(), boundingBox.getS_Bound(), 32);
        int[] bbox = {min[0], min[1], max[0], max[1]};

        int z = getBboxZoom(bbox);
        if (z == 0) {
            return new int[]{0, 0, 0};
        }
        int x = bbox[0] >>> (32 - z);
        int y = bbox[1] >>> (32 - z);

        return new int[]{x,y,z};
    }

    private static int getBboxZoom(int[] bbox) {
        int MAX_ZOOM = 28;
        for (int z = 0; z < MAX_ZOOM; z++) {
            int mask = 1 << (32 - (z + 1));
            if (((bbox[0] & mask) != (bbox[2] & mask)) ||
                    ((bbox[1] & mask) != (bbox[3] & mask))) {
                return z;
            }
        }
        return MAX_ZOOM;
    }

    /**
     * Get the tile for a point at a specified zoom level
     * @param lat
     * @param lon
     * @param z
     * @return array tile number
     */
    private static int[] pointToTile(double lat, double lon, int z) {
        double[] tile;
        int[] tileOutput = new int[2];
        tile = pointToTileFraction(lon, lat, z);
        tileOutput[0] = (int)Math.floor(tile[0]);
        tileOutput[1] = (int)Math.floor(tile[1]);
        return tileOutput;
    }

    /**
     * Get the precise fractional tile location for a point at a zoom level
     * @param lon
     * @param lat
     * @param z
     * @return
     */
    private static double[] pointToTileFraction(double lon, double lat, int z) {
        double sin = Math.sin(Math.toRadians(lat)),
                z2 = Math.pow(2, z),
                x = z2 * (lon / 360 + 0.5),
                y = z2 * (0.5 - 0.25 * Math.log((1 + sin) / (1 - sin)) / Math.PI);

        // Wrap Tile X
        x = x % z2;
        if (x < 0) x = x + z2;
        return new double[]{x, y, z};
    }



    private static OSMZoomSetting getBestZoomLevel(LatLonBoundingBox boundingBox, int desiredPixelWidth) {
        //256*2^zoomLevel is width/height of full world

        double widthMeters = boundingBox.getWidthLonMeters();

        double desiredhorizDistPerPix = widthMeters/(double)desiredPixelWidth;

        int zoomLevel = 2;

        double preTerm = (double)HGTWorker.RadiusOfEarth*2d*Math.PI*Math.cos(Math.toRadians(boundingBox.getCenter().getLatitude()));
        double horizDistPerPixel=0;

        for (; zoomLevel<21; zoomLevel++) {
            horizDistPerPixel = preTerm/Math.pow(2d, (8+zoomLevel));

            if (horizDistPerPixel < desiredhorizDistPerPix) {
                break;
            }

        }

        OSMZoomSetting out = new OSMZoomSetting();
        out.zoomLevel = zoomLevel;
        out.widthPx = (int)(boundingBox.getWidthLonMeters()/horizDistPerPixel);
        out.heightPx = (int)(boundingBox.getWidthLatMeters()/horizDistPerPixel);

        while (out.widthPx>2048) {
            out.widthPx/=2;
            out.heightPx/=2;
            out.zoomLevel--;
        }

        return out;
    }

    /**
     * Horizontal distance of a single pixel with a given zoom level
     * https://wiki.openstreetmap.org/wiki/Zoom_levels
     * @param zoomLevel
     * @return
     */
    private static double horizDistPerPixel(int zoomLevel, double latitude_radians) {
        return (double)HGTWorker.RadiusOfEarth*2d*Math.PI*Math.cos(latitude_radians)/Math.pow(2d, (8+zoomLevel));
    }

    public static BufferedImage resize(int size, BufferedImage img) throws IOException{
        Image tmp = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }


    public static void resizeAndwriteImageToFile(int size, BufferedImage img, String path) throws IOException{
        Image tmp = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        ImageIO.write(dimg, "png",new File(path));
    }

    private static class OSMZoomSetting {
        int heightPx;
        int widthPx;
        int zoomLevel;

        @Override
        public String toString() {
            return "ZoomLevel: "+zoomLevel+"\nWidth/Height: "+widthPx+"/"+heightPx;
        }
    }
}
