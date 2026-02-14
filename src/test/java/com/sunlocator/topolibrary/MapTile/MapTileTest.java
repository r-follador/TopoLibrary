package com.sunlocator.topolibrary.MapTile;

import com.sunlocator.topolibrary.LatLon;
import com.sunlocator.topolibrary.LatLonBoundingBox;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MapTileTest {

    @Test
    public void tileFromLatLonContainsOriginalPoint() {
        LatLon point = new LatLon(46.8508, 9.5320);
        MapTile tile = new MapTile(12, point);
        LatLonBoundingBox bbox = tile.getBoundingBox();

        assertTrue(bbox.isPositionWithinBbox(point));
    }

    @Test
    public void pixelConversionsAreConsistentWithTile() {
        MapTile tile = new MapTile(12, new LatLon(46.8508, 9.5320));
        MapTile.XY topLeft = MapTile.TileXYtoPixelXY(tile);

        MapTile tileFromPixel = MapTile.PixelXYToTileXY(topLeft, tile.zoom);
        assertEquals(tile.x, tileFromPixel.x);
        assertEquals(tile.y, tileFromPixel.y);
        assertEquals(tile.zoom, tileFromPixel.zoom);
    }

    @Test
    public void localPixelReturnsNullForPointOutsideTile() {
        MapTile tile = new MapTile(12, new LatLon(46.8508, 9.5320));
        MapTile.XY inside = tile.getLocalPixelPositionofLatLon(new LatLon(46.8508, 9.5320));
        MapTile.XY outside = tile.getLocalPixelPositionofLatLon(new LatLon(-46.8508, -9.5320));

        assertNotNull(inside);
        assertNull(outside);
    }
}
