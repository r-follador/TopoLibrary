package com.sunlocator.topolibrary;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LatLonTest {

    @Test(expected = UnsupportedOperationException.class)
    public void constructorRejectsOutOfBoundsLatitude() {
        new LatLon(91.0, 0.0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void constructorRejectsOutOfBoundsLongitude() {
        new LatLon(0.0, 181.0);
    }

    @Test
    public void distanceIsSymmetricAndZeroForSamePoint() {
        LatLon a = new LatLon(46.8, 9.5);
        LatLon b = new LatLon(47.1, 9.7);

        double d1 = a.calculateEuclidianDistance(b);
        double d2 = b.calculateEuclidianDistance(a);
        double d0 = a.calculateEuclidianDistance(a);

        assertEquals(d1, d2, 0.0001);
        assertEquals(0.0, d0, 0.0);
    }
}
