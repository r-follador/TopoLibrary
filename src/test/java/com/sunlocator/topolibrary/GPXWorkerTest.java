package com.sunlocator.topolibrary;

import com.sunlocator.topolibrary.GPX.GPXWorker;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GPXWorkerTest {

    @Test
    public void trueTrackBoundingBoxMatchesExtremes() {
        Track track = TestTrackFactory.trackFromArrays(
                new double[]{46.8, 46.81, 46.79},
                new double[]{9.5, 9.6, 9.55},
                new double[]{1000, 1010, 1005},
                Instant.parse("2024-01-01T00:00:00Z"),
                60
        );

        LatLonBoundingBox box = GPXWorker.getTrueTrackBoundingBox(track);

        assertEquals(46.81, box.getN_Bound(), 0.0);
        assertEquals(46.79, box.getS_Bound(), 0.0);
        assertEquals(9.5, box.getW_Bound(), 0.0);
        assertEquals(9.6, box.getE_Bound(), 0.0);
    }

    @Test
    public void reduceTrackSegmentsKeepsEndpoints() {
        List<WayPoint> points = Arrays.asList(
                WayPoint.of(0.0, 0.0),
                WayPoint.of(0.0001, 0.5),
                WayPoint.of(0.0, 1.0)
        );
        Track track = Track.builder().addSegment(TrackSegment.builder().points(points).build()).build();

        Track reduced = GPXWorker.reduceTrackSegments(track, 20_000);

        assertEquals(2, reduced.getSegments().get(0).getPoints().size());
        assertEquals(0.0, reduced.getSegments().get(0).getPoints().get(0).getLongitude().doubleValue(), 0.0);
        assertEquals(1.0, reduced.getSegments().get(0).getPoints().get(1).getLongitude().doubleValue(), 0.0);
    }

    @Test
    public void encode2EpaMatchesKnownReference() {
        TrackSegment segment = TrackSegment.builder().points(Arrays.asList(
                WayPoint.of(38.5, -120.2),
                WayPoint.of(40.7, -120.95),
                WayPoint.of(43.252, -126.453)
        )).build();

        assertEquals("_p~iF~ps|U_ulLnnqC_mqNvxq`@", GPXWorker.encode2EPA(segment));
    }

    @Test
    public void trackSummaryComputesUpDownAndDuration() {
        Track track = TestTrackFactory.trackFromArrays(
                new double[]{46.8, 46.8005, 46.8010},
                new double[]{9.5, 9.5005, 9.5010},
                new double[]{1000, 1010, 1002},
                Instant.parse("2024-01-01T00:00:00Z"),
                60
        );

        GPXWorker.TrackSummary summary = GPXWorker.getTrackSummary(track);

        assertEquals(10, summary.getElevationUp());
        assertEquals(8, summary.getElevationDown());
        assertEquals(2, summary.getDuration());
        assertEquals(1, summary.getSegments());
        assertEquals(3, summary.getPoints());
        assertTrue(summary.getDistance() > 0);
    }

    @Test(expected = RuntimeException.class)
    public void replaceElevationDataRejectsDimensionMismatch() {
        Track track = TestTrackFactory.trackFromArrays(
                new double[]{46.8, 46.81},
                new double[]{9.5, 9.6},
                new double[]{1000, 1005},
                Instant.parse("2024-01-01T00:00:00Z"),
                60
        );

        ArrayList<short[]> mismatch = new ArrayList<>();
        mismatch.add(new short[]{1});
        GPXWorker.replaceElevationData(track, mismatch);
    }

    @Test
    public void normalizeElevationDataShiftsByMeanDifference() {
        ArrayList<short[]> gps = new ArrayList<>();
        gps.add(new short[]{100, 120, 140});

        ArrayList<short[]> model = new ArrayList<>();
        model.add(new short[]{80, 100, 120});

        ArrayList<short[]> out = GPXWorker.normalizeElevationData(gps, model);
        assertEquals(80, out.get(0)[0]);
        assertEquals(100, out.get(0)[1]);
        assertEquals(120, out.get(0)[2]);
    }

    @Test
    public void getElevationDataFallsBackTo3DemWhen1DemMissing() throws Exception {
        HGTFileLoader missing1Dem = (filename, cellsPerRow) -> {
            throw new FileNotFoundException(filename);
        };

        PatternHgtFileLoader dem3 = new PatternHgtFileLoader((x, y, filename, cellsPerRow) -> (short) (x + y));

        Track track = TestTrackFactory.trackFromArrays(
                new double[]{46.75},
                new double[]{9.25},
                new double[]{1000},
                Instant.parse("2024-01-01T00:00:00Z"),
                60
        );

        ArrayList<short[]> out = GPXWorker.getElevationDataFromHGT(track.getSegments(), missing1Dem, dem3);
        assertEquals(1, out.size());
        assertEquals(600, out.get(0)[0]);
    }

    @Test
    public void convertMultilineStringToTrackPreservesPoints() {
        GeometryFactory gf = new GeometryFactory();
        LineString l1 = gf.createLineString(new Coordinate[]{new Coordinate(9.5, 46.8), new Coordinate(9.6, 46.9)});
        LineString l2 = gf.createLineString(new Coordinate[]{new Coordinate(9.7, 46.7), new Coordinate(9.8, 46.6)});
        MultiLineString mls = gf.createMultiLineString(new LineString[]{l1, l2});

        Track track = GPXWorker.convertMultlinestringToTrack(mls);

        assertEquals(2, track.getSegments().size());
        assertEquals(2, track.getSegments().get(0).getPoints().size());
        assertEquals(2, track.getSegments().get(1).getPoints().size());
    }
}
