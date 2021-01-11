/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sunlocator.topolibrary.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;


/**
 *
 * @author rainer
 */
public class HGTmain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //LatLon latlon = new LatLon(46.86365, 9.59043);
        LatLon latlon = new LatLon(	51.1056, -115.3573);
        LatLonBoundingBox boundingBox = new LatLonBoundingBox(latlon, 10000);
       
       
       try {

           BufferedImage map_old = MapWorker.getMapPng(boundingBox);
           //BufferedImage map = MapWorker.getMapRasterTiles(boundingBox);
           //MapWorker.resizeAndwriteImageToFile(1024, map, "/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/terrain.map.png");
           MapWorker.resizeAndwriteImageToFile(1024, map_old, "/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/terrain.map_old.png");


       } catch (IOException e) {
           System.err.println(e);
       }
    }
}
