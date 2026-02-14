package com.sunlocator.topolibrary;

import io.jenetics.jpx.Length;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class TestTrackFactory {
    private TestTrackFactory() {
    }

    public static WayPoint wayPoint(double lat, double lon, double eleMeters, Instant time) {
        return WayPoint.builder()
                .lat(lat)
                .lon(lon)
                .ele(Length.of(eleMeters, Length.Unit.METER))
                .time(time)
                .build();
    }

    public static Track trackFromPoints(List<WayPoint> points) {
        return Track.builder().addSegment(TrackSegment.builder().points(points).build()).build();
    }

    public static Track trackFromArrays(double[] lat, double[] lon, double[] eleMeters, Instant start, long stepSeconds) {
        if (lat.length != lon.length || lat.length != eleMeters.length) {
            throw new IllegalArgumentException("Array sizes must match");
        }
        List<WayPoint> points = new ArrayList<>();
        for (int i = 0; i < lat.length; i++) {
            points.add(wayPoint(lat[i], lon[i], eleMeters[i], start.plusSeconds(stepSeconds * i)));
        }
        return trackFromPoints(points);
    }
}
