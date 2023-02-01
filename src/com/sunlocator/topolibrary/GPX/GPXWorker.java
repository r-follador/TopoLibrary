package com.sunlocator.topolibrary.GPX;


import com.sunlocator.topolibrary.*;
import io.jenetics.jpx.*;
import io.jenetics.jpx.Point;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GPXWorker {

    //Based on https://github.com/jenetics/jpx

    public static ConversionOutput loadGPXTracks(InputStream inputStream) throws IOException {
        return new ConversionOutput(GPX.Reader.of(GPX.Reader.Mode.LENIENT).read(inputStream).getTracks());
    }

    public static ConversionOutput loadFitTracks(InputStream inputStream) throws IOException {
        return Converter.loadFitTracks(new BufferedInputStream(inputStream));
    }

    public static ConversionOutput loadGPXTracks(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        ConversionOutput trackList = loadGPXTracks(fis);
        fis.close();
        return trackList;
    }

    public static ConversionOutput loadFitTracks(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        ConversionOutput trackList = loadFitTracks(fis);
        fis.close();
        return trackList;
    }

    public static class ConversionOutput {
        public ConversionOutput(List<Track> t) {
            this.trackList = t;
        }
        public List<Track> trackList = null;
        public String sportString = "";
        public String subsportString = "";
    }


    /**
     * The actual bounding box of a track
     * @param track
     * @return
     */
    public static LatLonBoundingBox getTrueTrackBoundingBox(Track track) {
        double N_bound = -100;
        double S_bound = 100;
        double W_bound = 200;
        double E_bound = -200;

        for (TrackSegment segment : track.getSegments()) {
            //Log.d("TRACK"+i, "  segment " + j + ":");
            for (Point trackPoint : segment.getPoints()) {
                //Log.d("TRACK"+i+" segment "+j, "    point: lat " + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude());
                N_bound = Math.max(trackPoint.getLatitude().doubleValue(), N_bound);
                S_bound = Math.min(trackPoint.getLatitude().doubleValue(), S_bound);
                E_bound = Math.max(trackPoint.getLongitude().doubleValue(),E_bound);
                W_bound = Math.min(trackPoint.getLongitude().doubleValue(),W_bound);
            }
        }
        return new LatLonBoundingBox(N_bound, S_bound, W_bound, E_bound);
    }

    /**
     * Convert BoundingBox to one suitable for output (square bounding box with margins)
     * @param trueBoundingBox
     * @return
     */
    public static LatLonBoundingBox optimizeBoundingBox(LatLonBoundingBox trueBoundingBox) {
        LatLonBoundingBox latLonBoundingBoxTerrain = new LatLonBoundingBox(trueBoundingBox.getCenter(),(int)(2*Math.max(trueBoundingBox.getWidthLatMeters(), trueBoundingBox.getWidthLonMeters())));
        LatLonBoundingBox outputBoundingBox = HGTWorker.convertSuggestedBBox4LOD_3DEM(latLonBoundingBoxTerrain);

        return outputBoundingBox;
    }

    public static BufferedImage getMapPng(Track track) throws IOException {
        LatLonBoundingBox latLonBoundingBox = optimizeBoundingBox(getTrueTrackBoundingBox(track));

        BufferedImage image = MapWorker.getMapPng(latLonBoundingBox);
        double height = image.getHeight();
        double width = image.getWidth();
        double px_per_lon = width/latLonBoundingBox.getWidthLonDegree();
        double px_per_lat = height/latLonBoundingBox.getWidthLatDegree();

        Graphics2D g = image.createGraphics();
        g.setColor(Color.red);
        g.setStroke(new BasicStroke(2));


        for (TrackSegment segment : track.getSegments()) {
            if (segment.getPoints().size() < 2)
                continue;
            Path2D path = new Path2D.Double();
            float x=(float)((segment.getPoints().get(0).getLongitude().doubleValue()-latLonBoundingBox.getW_Bound())*px_per_lon);
            float y=(float)(height-((segment.getPoints().get(0).getLatitude().doubleValue()-latLonBoundingBox.getS_Bound())*px_per_lat));
            path.moveTo(x,y);
            //Log.d("TRACK"+i, "  segment " + j + ":");
            for (Point trackPoint : segment.getPoints()) {
                x=(float)((trackPoint.getLongitude().doubleValue()-latLonBoundingBox.getW_Bound())*px_per_lon);
                y=(float)(height-((trackPoint.getLatitude().doubleValue()-latLonBoundingBox.getS_Bound())*px_per_lat));
                path.lineTo(x,y);
            }
            g.draw(path);
        }

        return image;

    }

    /**
     * Downsample Track using Ramer–Douglas–Peucker algorithm
     * @param original
     * @param epsilon in meters
     * @return
     */
    public static Track reduceTrackSegments(Track original, double epsilon) {
        Track.Builder trackBuilder = Track.builder().name(original.getName().orElse("")).desc(original.getDescription().orElse(""));

        for (TrackSegment segment : original.getSegments()) {
            List<WayPoint> reducedSegment = new ArrayList<>();
            ramerDouglasPeucker(segment.getPoints(), epsilon, reducedSegment);
            trackBuilder.addSegment(TrackSegment.builder().points(reducedSegment).build());
        }

        return trackBuilder.build();
    }

    private static void ramerDouglasPeucker(List<WayPoint> pointList, double epsilon, List<WayPoint> out) {
        //from https://rosettacode.org/wiki/Ramer-Douglas-Peucker_line_simplification#Java
        if (pointList.size() < 2) throw new IllegalArgumentException("Not enough points to simplify");

        // Find the point with the maximum distance from line between the start and end
        double dmax = 0.0;
        int index = 0;
        int end = pointList.size() - 1;
        for (int i = 1; i < end; ++i) {
            double d = perpendicularDistance(pointList.get(i), pointList.get(0), pointList.get(end));
            if (d > dmax) {
                index = i;
                dmax = d;
            }
        }

        // If max distance is greater than epsilon, recursively simplify
        if (dmax > epsilon) {
            List<WayPoint> recResults1 = new ArrayList<>();
            List<WayPoint> recResults2 = new ArrayList<>();
            List<WayPoint> firstLine = pointList.subList(0, index + 1);
            List<WayPoint> lastLine = pointList.subList(index, pointList.size());
            ramerDouglasPeucker(firstLine, epsilon, recResults1);
            ramerDouglasPeucker(lastLine, epsilon, recResults2);

            // build the result list
            out.addAll(recResults1.subList(0, recResults1.size() - 1));
            out.addAll(recResults2);
            if (out.size() < 2) throw new RuntimeException("Problem assembling output");
        } else {
            // Just return start and end points
            out.clear();
            out.add(pointList.get(0));
            out.add(pointList.get(pointList.size() - 1));
        }
    }

    private static double perpendicularDistance(WayPoint pt, WayPoint lineStart, WayPoint lineEnd) {
        double dx = lineEnd.getLatitude().doubleValue() - lineStart.getLatitude().doubleValue();
        dx = HGTWorker.lengthOfDegreeLatitude()*dx; //convert from latitude distance to meters
        double dy = lineEnd.getLongitude().doubleValue() - lineStart.getLongitude().doubleValue();
        dy = HGTWorker.lengthOfDegreeLongitude(lineEnd.getLatitude().doubleValue())*dy; //convert from longitude distance to meters

        // Normalize
        double mag = Math.hypot(dx, dy);
        if (mag > 0.0) {
            dx /= mag;
            dy /= mag;
        }
        double pvx = pt.getLatitude().doubleValue() - lineStart.getLatitude().doubleValue();
        pvx = HGTWorker.lengthOfDegreeLatitude()*pvx; //convert from latitude distance to meters
        double pvy = pt.getLongitude().doubleValue() - lineStart.getLongitude().doubleValue();
        pvy = HGTWorker.lengthOfDegreeLongitude(pt.getLatitude().doubleValue())*pvy; //convert from longitude distance to meters

        // Get dot product (project pv onto normalized direction)
        double pvdot = dx * pvx + dy * pvy;

        // Scale line direction vector and subtract it from pv
        double ax = pvx - pvdot * dx;
        double ay = pvy - pvdot * dy;

        return Math.hypot(ax, ay);
    }

    public static TrackSummary getTrackSummary(Track track) {
        int up = 0;
        int down = 0;
        long time = 0;
        double dist = 0;
        int highestpoint = -400000;
        int lowestpoint = 400000;

        int points = 0;


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss SSS");


        for (TrackSegment segment : track.getSegments()) {
            points += segment.getPoints().size();
            for (int i=1; i<segment.getPoints().size(); i++) {

                WayPoint previousPoint = segment.getPoints().get(i-1);
                WayPoint thisPoint = segment.getPoints().get(i);
                LatLon thisLatLon = new LatLon(thisPoint.getLatitude().doubleValue(), thisPoint.getLongitude().doubleValue());
                LatLon previousLatLon = new LatLon(previousPoint.getLatitude().doubleValue(), previousPoint.getLongitude().doubleValue());

                int ele = thisPoint.getElevation().get().intValue()-previousPoint.getElevation().get().intValue();
                if (thisPoint.getElevation().get().intValue() > highestpoint)
                    highestpoint = thisPoint.getElevation().get().intValue();
                if (thisPoint.getElevation().get().intValue() < lowestpoint)
                    lowestpoint = thisPoint.getElevation().get().intValue();
                dist += HGTWorker.distanceBetweenPoints(thisLatLon, previousLatLon);
                long timediff = ChronoUnit.SECONDS.between(previousPoint.getTime().get(), thisPoint.getTime().get());
                if (ele<0)
                    down -= ele;
                else
                    up += ele;
                time += timediff;
            }
        }

        TrackSummary out = new TrackSummary();
        out.elevationDown = down;
        out.elevationUp = up;
        out.duration = (int)(time/60);
        out.points = points;
        out.distance = (int)dist;
        out.segments = track.getSegments().size();
        out.highestpointEle = highestpoint;
        out.lowestpointEle = lowestpoint;

        return out;
    }

    public static class TrackSummary {


        public int getElevationUp() {
            return elevationUp;
        }

        public void setElevationUp(int elevationUp) {
            this.elevationUp = elevationUp;
        }

        public int getElevationDown() {
            return elevationDown;
        }

        public void setElevationDown(int elevationDown) {
            this.elevationDown = elevationDown;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getSegments() {
            return segments;
        }

        public void setSegments(int segments) {
            this.segments = segments;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public int getHighestpointEle() {
            return highestpointEle;
        }

        public void setHighestpointEle(int highestpointEle) {
            this.highestpointEle = highestpointEle;
        }

        public int getLowestpointEle() {
            return lowestpointEle;
        }

        public void setLowestpointEle(int lowestpointEle) {
            this.lowestpointEle = lowestpointEle;
        }

        public int distance;
        public int elevationUp;
        public int elevationDown;
        public int duration;
        public int segments;
        public int points;
        public int highestpointEle;
        public int lowestpointEle;

        public String toString() {
            return "Distance: "+distance + " m \n"+
                    "Elevation Up/Down: "+ elevationUp +"m / "+elevationDown +"m \n" +
                    "Duration: "+duration+"min \n"+
                    "Total Segments/Points: "+segments+"/"+points + "\n"+
                    "Highest/Lowest Elevation: "+highestpointEle + "m /" + lowestpointEle + "m";
        }
    }

    /**
     * Replaces the elevation data of track with newElevationData
     * @param track
     * @param newElevationData
     */
    public static Track replaceElevationData(Track track, ArrayList<short[]> newElevationData) {
        Track.Builder tb = Track.builder();
        if (track.getSegments().size()!= newElevationData.size())
            throw new RuntimeException("newElevationData has different dimensions than track");
        TrackSegment.Builder tsb = TrackSegment.builder();
        for (int i=0; i<track.getSegments().size(); i++) {
            if (track.getSegments().get(i).getPoints().size()!= newElevationData.get(i).length)
                throw new RuntimeException("newElevationData has different dimensions than track");
            for (int j=0; j<track.getSegments().get(i).getPoints().size(); j++) {
                WayPoint wp = track.getSegments().get(i).getPoints().get(j);
                WayPoint np = WayPoint.of(wp.getLatitude(), wp.getLongitude(), Length.of(newElevationData.get(i)[j], Length.Unit.METER), wp.getTime().orElse(Instant.now()));
                tsb.addPoint(np);
            }
        }
        return tb.addSegment(tsb.build()).build();
    }

    public static ArrayList<short[]> getElevationDataAsArray(Track track) {
        ArrayList<short[]> output = new ArrayList<>();

        for (TrackSegment segment : track.getSegments()) {
            short[] out = new short[segment.getPoints().size()];

            for (int i=0;i<segment.getPoints().size();i++) {
                out[i] = segment.getPoints().get(i).getElevation().get().shortValue();
            }
            output.add(out);
        }
        return output;
    }

    /**
     * Subtract the difference of the means between gpsEle and modelEle form gpsEle
     * @param gpsEle
     * @param modelEle
     * @return
     */
    public static ArrayList<short[]> normalizeElevationData(ArrayList<short[]> gpsEle, ArrayList<short[]> modelEle) {
        ArrayList<short[]> output = new ArrayList<>();

        if (gpsEle.size() != modelEle.size())
            throw new RuntimeException("gpsEle list has different dimensions than modelEle list");

        for (int i = 0; i<gpsEle.size();  i++) {
            if (gpsEle.get(i).length != modelEle.get(i).length)
                throw new RuntimeException("gpsEle list has different dimensions than modelEle list");

            output.add(addNumberToArray(gpsEle.get(i), (getMean(modelEle.get(i))-getMean(gpsEle.get(i)))));
        }
        return output;
    }

    /**
     * Normalize elevation data of track by minimizing the mean of the track elevations to the mean of the DEM data elevations.
     * @param track
     * @param hgtFileLoader_1DEM
     * @param hgtFileLoader_3DEM
     * @throws IOException
     */
    public static Track normalizeElevationData(Track track, HGTFileLoader hgtFileLoader_1DEM, HGTFileLoader hgtFileLoader_3DEM) throws IOException {
        ArrayList<short[]> modelEle = getElevationDataFromHGT(track, hgtFileLoader_1DEM, hgtFileLoader_3DEM);
        ArrayList<short[]> gpsEle = getElevationDataAsArray(track);

        return replaceElevationData(track, normalizeElevationData(gpsEle, modelEle));
    }

    /**
     * Replace elevation data of track by DEM elevation data
     * @param track
     * @param hgtFileLoader_1DEM
     * @param hgtFileLoader_3DEM
     * @throws IOException
     */
    public static Track replaceElevationData(Track track, HGTFileLoader hgtFileLoader_1DEM, HGTFileLoader hgtFileLoader_3DEM) throws IOException {
        ArrayList<short[]> modelEle = getElevationDataFromHGT(track, hgtFileLoader_1DEM, hgtFileLoader_3DEM);

        return replaceElevationData(track, modelEle);
    }

    private static short getMean(short[] data) {
        long sum = 0L;
        for (short datum : data) {
            sum += datum;
        }
        return (short)(sum/data.length);
    }

    private static short[] addNumberToArray(short[] data, int toAdd) {
        short[] output = new short[data.length];
        for (int i=0; i<data.length; i++) {
            output[i] = (short)(data[i]+toAdd);
        }
        return output;
    }


    final static short nonInit = (short)-2222;

    /**
     * Returns the elevation data based on the 1DEM HGT file for every point in a Track.
     * The arraylist contains a short[] array for every tracksegment.
     * @param track
     * @param hgtFileLoader_1DEM
     * @param hgtFileLoader_3DEM
     * @return
     * @throws IOException
     */
    public static ArrayList<short[]> getElevationDataFromHGT(Track track, HGTFileLoader hgtFileLoader_1DEM, HGTFileLoader hgtFileLoader_3DEM) throws IOException {
        String hgtDatafile_3DEM_name = "";
        HGTDatafile hgtDatafile_3DEM = null;
        String hgtDatafile_1DEM_name = "";
        HGTDatafile hgtDatafile_1DEM = null;

        ArrayList<short[]> output = new ArrayList<>();

        for (TrackSegment segment : track.getSegments()) {
            short[] out = new short[segment.getPoints().size()];
            Arrays.fill(out, nonInit);
            int index=-1;
            while((index=getNonInitIndex(out))!=-1) {
                LatLon latLon = new LatLon(segment.getPoints().get(index).getLatitude().doubleValue(), segment.getPoints().get(index).getLongitude().doubleValue());

                //if matching hgtDatafile is not loaded, try to load first 1DEM; if not available 3DEM
                if (!HGTWorker.getFileName_1DEM(latLon).equals(hgtDatafile_1DEM_name) && !HGTWorker.getFileName_3DEM(latLon).equals(hgtDatafile_3DEM_name)) {
                    try {
                        hgtDatafile_1DEM = HGTWorker.loadHGTFile_1DEM(latLon, hgtFileLoader_1DEM);
                        hgtDatafile_1DEM_name = HGTWorker.getFileName_1DEM(latLon);
                    } catch (FileNotFoundException e) {
                        hgtDatafile_3DEM = HGTWorker.loadHGTFile_3DEM(latLon, hgtFileLoader_3DEM);
                        hgtDatafile_3DEM_name = HGTWorker.getFileName_3DEM(latLon);
                    }
                }

                boolean loaded1DEM = false;

                if (HGTWorker.getFileName_1DEM(latLon).equals(hgtDatafile_1DEM_name)) {
                    out[index]=getEleAtPoint(hgtDatafile_1DEM, latLon);
                    loaded1DEM = true;
                } else if (HGTWorker.getFileName_3DEM(latLon).equals(hgtDatafile_3DEM_name)) {
                    out[index]=getEleAtPoint(hgtDatafile_3DEM, latLon);
                }

                //check if any other points can be queried from the same hgtfile; prevent too many open/close operations
                for (int i=index+1; i<out.length; i++) {
                    LatLon latLon2 = new LatLon(segment.getPoints().get(i).getLatitude().doubleValue(), segment.getPoints().get(i).getLongitude().doubleValue());
                    if ((loaded1DEM && HGTWorker.getFileName_1DEM(latLon2).equals(hgtDatafile_1DEM_name)) || HGTWorker.getFileName_3DEM(latLon2).equals(hgtDatafile_3DEM_name)) {
                        out[i] = getEleAtPoint((loaded1DEM ? hgtDatafile_1DEM : hgtDatafile_3DEM), latLon2);
                    }
                }
            }
            output.add(out);
        }

        return output;
    }

    private static int getNonInitIndex(short[] shortArray) {
        for (int i=0; i<shortArray.length; i++) {
            if (shortArray[i]==nonInit)
                return i;
        }
        return -1;
    }

    private static short getEleAtPoint(HGTDatafile hgtDatafile, LatLon latLon) {
        int x=(int)((latLon.getLongitude()-hgtDatafile.bounds.getW_Bound())/hgtDatafile.cellWidth_LonDegree);
        int y=(int)((hgtDatafile.bounds.getN_Bound()-latLon.getLatitude())/hgtDatafile.cellWidth_LatDegree);
        return hgtDatafile.getData()[x][y];
    }

    public static short getElevationDataFromHGT(LatLon latLon, HGTFileLoader hgtFileLoader_1DEM) throws IOException {
        HGTDatafile hgtDatafile = HGTWorker.loadHGTFile_1DEM(latLon, hgtFileLoader_1DEM);
        return getEleAtPoint(hgtDatafile, latLon);
    }

}
