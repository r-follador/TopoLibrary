/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sunlocator.topolibrary.GPX.GPXWorker;
import com.sunlocator.topolibrary.HGTDatafile;
import com.sunlocator.topolibrary.HGTFileLoader_LocalStorage;
import com.sunlocator.topolibrary.LatLon;
import com.sunlocator.topolibrary.MapWorker;
import io.jenetics.jpx.Track;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;


/**
 *
 * @author rainer
 */
public class GPXmain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File gpsTrack = new File("/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/GPXtesting/activity_6265044504.tcx");
        String directory_1DEM = "/home/rainer/Software_Dev/HGT_1DEM/";
        HGTFileLoader_LocalStorage hgtFileLoader = new HGTFileLoader_LocalStorage(directory_1DEM);

       try {
           System.out.println("Start loading");
           long start = System.currentTimeMillis();
           Track track = GPXWorker.loadGPXTracks(gpsTrack).trackList.get(0);
           System.out.println("Load time: "+(System.currentTimeMillis()-start)+" ms");
           System.out.println("Points: "+track.getSegments().get(0).getPoints().size());
           //BufferedImage map = GPXWorker.getMapPng(track);
           //MapWorker.resizeAndwriteImageToFile(1024, map, "/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/GPXtesting/gps-map.png");

           Track reduced = GPXWorker.reduceTrackSegments(track, 2);
           System.out.println("Points reduced: "+reduced.getSegments().get(0).getPoints().size());

           GPXWorker.TrackSummary trackSummary = GPXWorker.getTrackSummary(reduced);
           //BufferedImage mapReduced = GPXWorker.getMapPng(reduced);
           //MapWorker.resizeAndwriteImageToFile(1024, mapReduced, "/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/GPXtesting/gps-map-reduced.png");

           //GPXWorker.getHeight(reduced, hgtFileLoader);
       } catch (IOException e) {
           System.err.println(e);
       }
    }
}
