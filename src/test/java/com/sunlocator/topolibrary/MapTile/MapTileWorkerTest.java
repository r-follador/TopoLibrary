package com.sunlocator.topolibrary.MapTile;

import com.sunlocator.topolibrary.LatLon;
import com.sunlocator.topolibrary.LatLonBoundingBox;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapTileWorkerTest {

    @Test
    public void getTilesFromBoundingBoxSingleTile() {
        LatLon center = new LatLon(46.8508, 9.5320);
        MapTile t = new MapTile(12, center);
        LatLonBoundingBox bbox = new LatLonBoundingBox(center, 150);

        MapTile[][] tiles = MapTileWorker.getTilesFromBoundingBox(bbox, 12);

        assertEquals(1, tiles.length);
        assertEquals(1, tiles[0].length);
        assertEquals(t, tiles[0][0]);
    }

    @Test
    public void calculateRequiredTilesMatchesGridSize() {
        MapTile t = new MapTile(12, new LatLon(46.8508, 9.5320));
        LatLonBoundingBox left = t.getBoundingBox();
        LatLonBoundingBox right = new MapTile(12, t.x + 1, t.y).getBoundingBox();

        LatLonBoundingBox combined = new LatLonBoundingBox(
                left.getCenter().getLatitude() + 0.0001,
                left.getCenter().getLatitude() - 0.0001,
                left.getCenter().getLongitude(),
                right.getCenter().getLongitude()
        );

        assertEquals(2, MapTileWorker.calculateRequiredTilesFromBoundingBox(combined, 12));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void datelineSpanningBoundingBoxNotSupported() {
        LatLonBoundingBox bbox = new LatLonBoundingBox(10, 0, 179, -179);
        MapTileWorker.getTilesFromBoundingBox(bbox, 3);
    }
}
