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


        BufferedImage image =null;
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

        //System.out.println(url.toString());
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5 * 1000);
        connection.setReadTimeout(10 * 1000);
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
        connection.connect();
        return ImageIO.read(connection.getInputStream());
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
