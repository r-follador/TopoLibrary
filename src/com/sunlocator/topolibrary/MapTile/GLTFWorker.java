package com.sunlocator.topolibrary.MapTile;

import com.sunlocator.topolibrary.*;

import java.io.IOException;

public class GLTFWorker {
    public static class GLTFBuilder {

        private LatLonBoundingBox boundingBox;
        private int zoomLevel;
        private HGTFileLoader hgtFileLoader;
        private String textureUrl = null;

        private float scaleFactor = 1f;

        private boolean zUp = true;

        private boolean enclosement = false;
        public GLTFBuilder(LatLonBoundingBox boundingBox, HGTFileLoader hgtFileLoader) {
            this.boundingBox = boundingBox;
            this.zoomLevel = calculateZoomlevel(boundingBox);
            this.hgtFileLoader = hgtFileLoader;
        }

        public GLTFBuilder setEnclosement(boolean enclosement) {
            this.enclosement = enclosement;
            return this;
        }

        public GLTFBuilder setZoomlevel(int zoomlevel) {
            this.zoomLevel = zoomlevel;
            return this;
        }

        /**
         *
         * @param textureUrl in the form of https://api.maptiler.com/maps/basic/%d/%d/%d.png where ½d, %d, ½d is zoom, x, y (or null if unused)
         * @return
         */
        public GLTFBuilder setTextureUrl(String textureUrl) {
            this.textureUrl = textureUrl;
            return this;
        }

        /**
         * Scale 1 is in meters
         * @param scaleFactor
         * @return
         */
        public GLTFBuilder setScaleFactor(float scaleFactor) {
            this.scaleFactor = scaleFactor;
            return this;
        }

        public GLTFBuilder isZUp(boolean zUp) {
            this.zUp = zUp;
            return this;
        }

        public GLTFDatafile build() throws IOException {
            return buildGLTF(this);
        }




    }

    private static int calculateZoomlevel(LatLonBoundingBox boundingBox) {
        int zoom = 14;
        while (MapTileWorker.calculateRequiredTilesFromBoundingBox(boundingBox, zoom) > 48) {
            zoom--;
        }
        return zoom;
    }

    private static GLTFDatafile buildGLTF(GLTFBuilder builder) throws IOException {

        MapTile[][] mtiles = MapTileWorker.getTilesFromBoundingBox(builder.boundingBox, builder.zoomLevel);
        int tileWidth = mtiles.length;
        int tileHeight = mtiles[0].length;

        LatLon center = builder.boundingBox.getCenter();

        GLTFDatafile gltfFile = new GLTFDatafile();

        GLTFDatafile.UvTexture enclosementTexture = new GLTFDatafile.UvTexture(false, false);
        enclosementTexture.metallicFactor=1;

        //For each tile
        for (int x=0; x<tileWidth; x++) {
            for (int y=0; y<tileHeight; y++) {

                LatLonBoundingBox tileBbox = mtiles[x][y].getBoundingBox();

                int start_x_topleft = (int)((tileBbox.getW_Bound()-Math.floor(tileBbox.getW_Bound()))*(double) HGTDatafile.DEM3_cells_per_row);
                int start_x_bottomright = (int)((tileBbox.getE_Bound()-Math.floor(tileBbox.getE_Bound()))*(double) HGTDatafile.DEM3_cells_per_row);
                int lonCellNumber;
                if (Math.floor(tileBbox.getW_Bound()) == Math.floor(tileBbox.getE_Bound()))
                    lonCellNumber = start_x_bottomright - start_x_topleft + 1;
                else //spanning two DEM files
                    lonCellNumber = (start_x_bottomright)+(HGTDatafile.DEM3_cells_per_row-start_x_topleft)+(((int)Math.floor(tileBbox.getE_Bound()-(int)Math.floor(tileBbox.getW_Bound()))-1)*HGTDatafile.DEM1_cells_per_row);

                int start_y_topleft = (int)((Math.ceil(tileBbox.getN_Bound())-tileBbox.getN_Bound())*(double) HGTDatafile.DEM3_cells_per_row);
                int start_y_bottomright = (int)((Math.ceil(tileBbox.getS_Bound())-tileBbox.getS_Bound())*(double) HGTDatafile.DEM3_cells_per_row);
                int latCellNumber;

                if (Math.ceil(tileBbox.getN_Bound()) == Math.ceil(tileBbox.getS_Bound()))
                    latCellNumber = start_y_bottomright - start_y_topleft + 1;
                else //spanning two DEM files
                    latCellNumber = (start_y_bottomright)+(HGTDatafile.DEM3_cells_per_row-start_y_topleft)+((int)Math.ceil(tileBbox.getN_Bound()-(int)Math.ceil(tileBbox.getS_Bound()))-1)*HGTDatafile.DEM1_cells_per_row;

                double cellWidth_LatMeters = HGTWorker.degrees2distance_latitude(tileBbox.getWidthLatDegree())/(double)(latCellNumber-1);
                double cellWidth_LonMeters = HGTWorker.degrees2distance_longitude(tileBbox.getWidthLonDegree(), center.getLatitude())/(double)(lonCellNumber-1);

                float offset_x = (float)HGTWorker.degrees2distance_longitude(tileBbox.getTopLeft().getLongitude()-center.getLongitude(), center.getLatitude());
                float offset_y = (float)HGTWorker.degrees2distance_latitude(tileBbox.getTopLeft().getLatitude()-center.getLatitude());

                short[][] data = HGTWorker.load_3DEM(tileBbox.getTopLeft(), lonCellNumber, latCellNumber, 0,0, builder.hgtFileLoader);

                GLTFDatafile.UvTexture UVTexture;
                if (builder.textureUrl != null && !builder.textureUrl.isEmpty()) {
                    UVTexture = new GLTFDatafile.UvTexture(String.format(builder.textureUrl, mtiles[x][y].zoom, mtiles[x][y].x, mtiles[x][y].y));
                } else {
                    UVTexture = new GLTFDatafile.UvTexture(true, false);
                }

                boolean enclosementNeeded =  builder.enclosement && (x==0 || x == tileWidth-1 || y==0 || y==tileHeight-1);

                GLTFDatafile.GLTFMesh mesh = gltfFile.addGLTFMesh(data, lonCellNumber, latCellNumber, cellWidth_LatMeters, cellWidth_LonMeters, enclosementNeeded, offset_x, offset_y, builder.scaleFactor, UVTexture, enclosementTexture, builder.zUp);

                mesh.metadata.put("Tile_Z", String.valueOf(mtiles[x][y].zoom));
                mesh.metadata.put("Tile_X", String.valueOf(mtiles[x][y].x));
                mesh.metadata.put("Tile_Y", String.valueOf(mtiles[x][y].y));
                mesh.metadata.put("NBound", String.valueOf(tileBbox.getN_Bound()));
                mesh.metadata.put("SBound", String.valueOf(tileBbox.getS_Bound()));
                mesh.metadata.put("WBound", String.valueOf(tileBbox.getW_Bound()));
                mesh.metadata.put("EBound", String.valueOf(tileBbox.getE_Bound()));
                mesh.metadata.put("Distance1DegreeLatitude", String.valueOf(HGTWorker.degrees2distance_latitude(1d)));
                mesh.metadata.put("Distance1DegreeLongitude", String.valueOf(HGTWorker.degrees2distance_longitude(1d, center.getLatitude())));
                mesh.metadata.put("cellWidth_LatMeters", String.valueOf(cellWidth_LatMeters));
                mesh.metadata.put("cellWidth_LonMeters", String.valueOf(cellWidth_LonMeters));
                /**System.out.printf("Max x / y: %f %f \n", mesh.getMax_x(), mesh.getMax_y());
                 System.out.printf("Min x / y: %f %f \n", mesh.getMin_x(), mesh.getMin_y());
                 System.out.printf("Cell width long / lat: %f %f\n", cellWidth_LonMeters, cellWidth_LatMeters);**/
            }
        }

        return gltfFile;
    }
}
