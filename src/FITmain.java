/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sunlocator.topolibrary.GPX.GPXWorker;
import com.sunlocator.topolibrary.HGTFileLoader_LocalStorage;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

        File fitTrackhearrate = new File("/home/rainer/Downloads/Garmin/Heartrate/coros-466210103410851844.fit");

       try {
           System.out.println("Start loading FIT");
           long start = System.currentTimeMillis();
           GPXWorker.ConversionOutput conversionOutput = GPXWorker.loadFitTracks(fitTrackhearrate);
           Track track = conversionOutput.trackList.get(0);

           System.out.println("Type/Subtype: " + conversionOutput.sportString + " "+conversionOutput.subsportString);


           //Track track = GPXWorker.loadFitTracks(fitTrack).get(0);
           System.out.println("Load time: "+(System.currentTimeMillis()-start)+" ms");
           GPXWorker.TrackSummary trackSummary = GPXWorker.getTrackSummary(track);
           System.out.println(trackSummary.toString());
           System.out.println(track.getSegments().get(0).getPoints().get(0).getTime().get());

           System.out.println("--------_");

           List<TrackSegment> reducedSegmentList = new ArrayList<>(GPXWorker.reduceTrackSegments(track, 2).getSegments());

           GPXWorker.TrackSummary trackSummary2 = GPXWorker.getTrackSummary(reducedSegmentList);
           System.out.println(trackSummary2.toString());
           System.out.println(reducedSegmentList.get(0).getPoints().get(0).getTime().get());



           //System.out.println("Points: "+track.getSegments().get(0).getPoints().size());

           //Track reduced = GPXWorker.reduceTrackSegments(track, 2);
           //System.out.println("Points reduced: "+reduced.getSegments().get(0).getPoints().size());

           //GPXWorker.TrackSummary trackSummary = GPXWorker.getTrackSummary(reduced);
       } catch (IOException e) {
           System.err.println(e);
       }
    }
}
