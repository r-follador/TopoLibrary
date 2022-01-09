/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sunlocator.topolibrary.GPX.GPXWorker;
import com.sunlocator.topolibrary.HGTFileLoader_LocalStorage;
import io.jenetics.jpx.Track;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 *
 * @author rainer
 */
public class FITmain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File fitTrack = new File("/home/rainer/Downloads/Garmin/B6575452.FIT");
        File gpxTrack = new File("/home/rainer/Downloads/Garmin/B6575452.FIT.gpx");

       try {
           System.out.println("Start loading FIT");
           long start = System.currentTimeMillis();
           List<Track> trackList = GPXWorker.loadFitTracks(fitTrack);


           //Track track = GPXWorker.loadFitTracks(fitTrack).get(0);
           System.out.println("Load time: "+(System.currentTimeMillis()-start)+" ms");
           GPXWorker.TrackSummary trackSummary = GPXWorker.getTrackSummary(trackList.get(0));
           System.out.println(trackSummary.toString());
           System.out.println(trackList.get(0).getSegments().get(0).getPoints().get(0).getTime().get());





           //System.out.println("Points: "+track.getSegments().get(0).getPoints().size());

           //Track reduced = GPXWorker.reduceTrackSegments(track, 2);
           //System.out.println("Points reduced: "+reduced.getSegments().get(0).getPoints().size());

           //GPXWorker.TrackSummary trackSummary = GPXWorker.getTrackSummary(reduced);
       } catch (IOException e) {
           System.err.println(e);
       }
    }
}
