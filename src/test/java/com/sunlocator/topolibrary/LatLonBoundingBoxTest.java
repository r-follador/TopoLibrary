package com.sunlocator.topolibrary;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LatLonBoundingBoxTest {

    @Test
    public void centerConstructorKeepsCenterAndApproxWidth() {
        LatLon center = new LatLon(46.86, 9.59);
        LatLonBoundingBox box = new LatLonBoundingBox(center, 1000);

        assertEquals(center.getLatitude(), box.getCenter().getLatitude(), 1e-6);
        assertEquals(center.getLongitude(), box.getCenter().getLongitude(), 1e-6);
        assertEquals(1000.0, box.getWidthLatMeters(), 10.0);
    }

    @Test
    public void addPaddingExpandsBox() {
        LatLonBoundingBox box = new LatLonBoundingBox(new LatLon(46.86, 9.59), 1000);
        LatLonBoundingBox padded = box.addPadding(250);

        assertTrue(padded.getWidthLatMeters() > box.getWidthLatMeters());
        assertTrue(padded.getWidthLonMeters() > box.getWidthLonMeters());
    }

    @Test
    public void containsIsBoundaryInclusive() {
        LatLonBoundingBox box = new LatLonBoundingBox(10, 9, 20, 21);

        assertTrue(box.isPositionWithinBbox(new LatLon(10, 20)));
        assertTrue(box.isPositionWithinBbox(new LatLon(9, 21)));
        assertFalse(box.isPositionWithinBbox(new LatLon(8.9999, 20.5)));
    }
}
