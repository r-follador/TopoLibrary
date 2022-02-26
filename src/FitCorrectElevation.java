/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sunlocator.topolibrary.GPX.GPXWorker;
import com.sunlocator.topolibrary.HGTFileLoader_LocalStorage;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 *
 * @author rainer
 */
public class FitCorrectElevation {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //3288 in cubetrek, massive elevation errors
        File fitTrack = new File("/home/rainer/Downloads/Garmin/Strava/753608283.fit");
        String directory_1DEM = "/home/rainer/Software_Dev/HGT_1DEM/";
        HGTFileLoader_LocalStorage hgtFileLoader_1DEM = new HGTFileLoader_LocalStorage(directory_1DEM);
        HGTFileLoader_LocalStorage hgtFileLoader_3DEM = new HGTFileLoader_LocalStorage("/home/rainer/Software_Dev/HGT/");

       try {
           System.out.println("Start loading");
           long start = System.currentTimeMillis();
           Track track = GPXWorker.loadFitTracks(fitTrack).trackList.get(0);
           System.out.println("Load time: "+(System.currentTimeMillis()-start)+" ms");
           System.out.println("Points: "+track.getSegments().get(0).getPoints().size());
           Track reduced = GPXWorker.reduceTrackSegments(track, 2);
           System.out.println("Points reduced: "+reduced.getSegments().get(0).getPoints().size());

           GPXWorker.TrackSummary trackSummary = GPXWorker.getTrackSummary(reduced);
           System.out.println(trackSummary);

           System.out.println("---------------");

           ArrayList<short[]> eles = GPXWorker.getElevationDataFromHGT(reduced, hgtFileLoader_1DEM, hgtFileLoader_3DEM);


           ArrayList<short[]> normalizedEle = GPXWorker.normalizeElevationData(GPXWorker.getElevationDataAsArray(reduced), eles);

           for (int i=0; i<reduced.getSegments().size(); i++) {
               for (int j=0; j<reduced.getSegments().get(i).getPoints().size(); j++) {
                   short ele = eles.get(i)[j];
                   short gps_h = reduced.getSegments().get(i).getPoints().get(j).getElevation().get().shortValue();

                   double lat = reduced.getSegments().get(i).getPoints().get(j).getLatitude().doubleValue();
                   double lon = reduced.getSegments().get(i).getPoints().get(j).getLongitude().doubleValue();

                   System.out.println(lat+"/"+lon+"\t"+ele+"\t"+gps_h+"\t"+normalizedEle.get(i)[j] + "\td" + (normalizedEle.get(i)[j]-ele));
               }
           }

           reduced = GPXWorker.normalizeElevationData(reduced, hgtFileLoader_1DEM, hgtFileLoader_3DEM);
           GPX.write(GPX.builder().addTrack(reduced).build(), Paths.get(fitTrack.getAbsolutePath()+"-replaced.gpx"));

           //GPXWorker.getHeight(reduced, hgtFileLoader);
       } catch (IOException e) {
           System.err.println(e);
       }
    }
}
