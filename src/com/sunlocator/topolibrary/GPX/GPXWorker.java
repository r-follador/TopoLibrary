package com.sunlocator.topolibrary.GPX;


import com.sunlocator.topolibrary.*;
import io.jenetics.jpx.*;
import io.jenetics.jpx.Point;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GPXWorker {

    //Based on https://github.com/jenetics/jpx

    public static List<Track> loadGPXTracks(InputStream inputStream) throws IOException {
        return GPX.read(inputStream).getTracks();
    }

    public static List<Track> loadGPXTracks(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        List<Track> trackList = loadGPXTracks(fis);
        fis.close();
        return trackList;
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

    public static void getHeight(Track track, HGTFileLoader hgtFileLoader_1DEM) throws IOException {
        //TODO: cannot be used yet, only print out
        LatLonBoundingBox latLonBoundingBox = getTrueTrackBoundingBox(track);
        latLonBoundingBox = new LatLonBoundingBox(latLonBoundingBox.getN_Bound(), latLonBoundingBox.getS_Bound()-HGTDatafile.DEM1_cellWidth_LatDegree, latLonBoundingBox.getW_Bound(), latLonBoundingBox.getE_Bound()+HGTDatafile.DEM1_cellWidth_LonDegree);
        HGTDatafile hgtDatafile = HGTWorker.loadFromBoundingBox_1DEM(latLonBoundingBox, hgtFileLoader_1DEM);
        System.out.println(hgtDatafile.toString());

        for (TrackSegment segment : track.getSegments()) {
            for (Point trackPoint : segment.getPoints()) {
                int x=(int)((trackPoint.getLongitude().doubleValue()-hgtDatafile.bounds.getW_Bound())/hgtDatafile.cellWidth_LonDegree);
                int y=(int)((hgtDatafile.bounds.getN_Bound()-trackPoint.getLatitude().doubleValue())/hgtDatafile.cellWidth_LatDegree);
                short h = hgtDatafile.getData()[x][y];

                short gps_h = trackPoint.getElevation().orElse(null).shortValue();
                System.out.println(gps_h +"\t"+h + "\t"+(gps_h-h)+"\t\t"+trackPoint.getLatitude().doubleValue()+"/"+trackPoint.getLongitude().doubleValue());

            }
        }
    }

    public static short getHeight(LatLon latLon, HGTFileLoader hgtFileLoader_1DEM) throws IOException {
        HGTDatafile hgtDatafile = HGTWorker.loadHGTFile_1DEM(latLon, hgtFileLoader_1DEM);

        int x=(int)((latLon.getLongitude()-hgtDatafile.bounds.getW_Bound())/hgtDatafile.cellWidth_LonDegree);
        int y=(int)((hgtDatafile.bounds.getN_Bound()-latLon.getLatitude())/hgtDatafile.cellWidth_LatDegree);

        return hgtDatafile.getData()[x][y];
    }

}
