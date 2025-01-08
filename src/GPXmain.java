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
import io.jenetics.jpx.TrackSegment;

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
        File gpsTrack = new File("/home/rainer/Downloads/Garmin/Problem_tracks/2024-04-19_12-32_Fri.gpx");
        String directory_1DEM = "/home/rainer/Software_Dev/HGT_1DEM/";
        HGTFileLoader_LocalStorage hgtFileLoader = new HGTFileLoader_LocalStorage(directory_1DEM);

       try {
           System.out.println("Start loading");
           long start = System.currentTimeMillis();
           Track track = GPXWorker.loadGPXTracks(gpsTrack).trackList.get(0);
           System.out.println("Load time: "+(System.currentTimeMillis()-start)+" ms");
           System.out.println("Number of segments: "+ track.getSegments().size());
           for (TrackSegment segment : track.getSegments()) {
               System.out.println("- Points: "+segment.getPoints().size());
           }
           //BufferedImage map = GPXWorker.getMapPng(track);
           //MapWorker.resizeAndwriteImageToFile(1024, map, "/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/GPXtesting/gps-map.png");

           Track reduced = GPXWorker.reduceTrackSegments(track, 2);
           for (TrackSegment segment : reduced.getSegments()) {
               System.out.println("- Points reduced: "+segment.getPoints().size());
           }

           GPXWorker.TrackSummary trackSummary = GPXWorker.getTrackSummary(reduced);
           System.out.println("Track summary: "+trackSummary);
           //BufferedImage mapReduced = GPXWorker.getMapPng(reduced);
           //MapWorker.resizeAndwriteImageToFile(1024, mapReduced, "/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/GPXtesting/gps-map-reduced.png");

           //GPXWorker.getHeight(reduced, hgtFileLoader);
       } catch (IOException e) {
           e.printStackTrace();
           System.err.println(e);
       }
    }
}
