package com.sunlocator.topolibrary.MapTile;

import com.sunlocator.topolibrary.LatLonBoundingBox;

import java.awt.*;

public class MapTileWorker {

    /**
     * Get Array of Tiles encompassing a bounding box
     * @param bbox
     * @param zoom
     * @return
     */
    public static MapTile[][] getTilesFromBoundingBox(LatLonBoundingBox bbox, int zoom) {
        MapTile NW = new MapTile(zoom, bbox.getTopLeft());
        MapTile SE = new MapTile(zoom, bbox.getBottomRight());

        if (NW.equals(SE)) {
            return new MapTile[][]{{NW}};
        }

        if (NW.x <= SE.x && NW.y <= SE.y) {
            int width = SE.x - NW.x +1;
            int height = SE.y - NW.y +1;

            MapTile [][] out = new MapTile[width][height];

            for (int x_=0; x_<width; x_++) {
                for (int y_=0; y_<height; y_++) {
                    out[x_][y_]=new MapTile(zoom, NW.x + x_, NW.y + y_);
                }
            }

            return out;
        } else {
            throw new UnsupportedOperationException("MapTile Arrays spanning the dateline not yet supported.");
        }

    }

    public static Image stitchMaptiles(LatLonBoundingBox bbox, int width) {
        //TODO: get image from tiles
        int max_zoom = 28;
        return null;
    }
}
