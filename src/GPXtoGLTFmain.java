/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sunlocator.topolibrary.*;
import com.sunlocator.topolibrary.GPX.GPXWorker;
import com.sunlocator.topolibrary.MapTile.MapTile;
import com.sunlocator.topolibrary.MapTile.MapTileWorker;
import io.jenetics.jpx.Track;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;


/**
 *
 * @author rainer
 */
public class GPXtoGLTFmain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File gpsTrack = new File("/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/GPXtesting/activity_2286936652.gpx");
        String directory_3DEM = "/home/rainer/Software_Dev/HGT/";
        HGTFileLoader_LocalStorage hgtFileLoader = new HGTFileLoader_LocalStorage(directory_3DEM);

       try {
           System.out.println("Start loading");
           long start = System.currentTimeMillis();
           Track track = GPXWorker.loadGPXTracks(gpsTrack).get(0);
           System.out.println("Load time: "+(System.currentTimeMillis()-start)+" ms");
           System.out.println("Points: "+track.getSegments().get(0).getPoints().size());
           Track reduced = GPXWorker.reduceTrackSegments(track, 2);

           LatLonBoundingBox boundingBox = GPXWorker.getTrueTrackBoundingBox(reduced);
           int zoom = 13;

           /**MapTile tile = new MapTile(zoom, boundingBox.getCenter());


           URL url = new URL(formatURL(tile));

           Image image = null;

           System.out.println(url.toString());
           long t0 = System.currentTimeMillis();
           image = ImageIO.read(url);
           System.out.println("Download time: "+ (System.currentTimeMillis()-t0)+"ms");
           showImage(image);**/


           GLTFDatafile gltfFile = HGTWorker.getTileGLTF_3DEM(boundingBox, 15, true, hgtFileLoader, "https://api.maptiler.com/maps/basic/%d/%d/%d.png?key=***REMOVED***");
           PrintWriter out3 = new PrintWriter("/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/terrain_tiles.gltf");
           out3.print(gltfFile.getString());
           out3.close();




           //BufferedImage map_old = MapWorker.getMapPng(boundingBox);
           //showImage(map_old);


       } catch (IOException e) {
           System.err.println(e);
       }
    }

    ////////////////////////////////////////
    private static void showImage(Image image) {
        JFrame frame = new JFrame();
        frame.setSize(1024, 1024);
        Container contentPane = frame.getContentPane();
        JLabel sentenceLabel= new JLabel(new ImageIcon(image));
        contentPane.add(sentenceLabel);
        frame.setVisible(true);
    }

    private static String formatURL(MapTile mapTile) {
        return String.format("https://api.maptiler.com/maps/basic/%d/%d/%d.png?key=***REMOVED***", mapTile.zoom, mapTile.x, mapTile.y);
    }
}
