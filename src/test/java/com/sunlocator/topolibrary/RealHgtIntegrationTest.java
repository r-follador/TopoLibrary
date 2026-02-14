package com.sunlocator.topolibrary;

import com.sunlocator.topolibrary.MapTile.GLTFWorker;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RealHgtIntegrationTest {

    private HGTFileLoader_LocalStorage fixtureLoader() {
        URL url = RealHgtIntegrationTest.class.getClassLoader().getResource("fixtures/hgt-real");
        Assume.assumeTrue("Real HGT fixture directory missing", url != null);
        File dir = new File(url.getFile());
        return new HGTFileLoader_LocalStorage(dir.getAbsolutePath() + File.separator);
    }

    @Test
    public void loadReal3DemTileFromFixtureDirectory() throws Exception {
        HGTFileLoader_LocalStorage loader = fixtureLoader();

        HGTDatafile tile = HGTWorker.loadHGTFile_3DEM(new LatLon(46.863760, 9.590417), loader);

        assertEquals(HGTDatafile.HGT_Type.DEM3, tile.hgt_type);
        assertEquals(1201, tile.cellsLon_X);
        assertEquals(1201, tile.cellsLat_Y);
        assertEquals(47.0, tile.bounds.getN_Bound(), 0.0);
        assertEquals(46.0, tile.bounds.getS_Bound(), 0.0);
        assertEquals(9.0, tile.bounds.getW_Bound(), 0.0);
        assertEquals(10.0, tile.bounds.getE_Bound(), 0.0);
    }

    @Test
    public void load3DemStitchesAcrossRealTileBoundary() throws Exception {
        HGTFileLoader_LocalStorage loader = fixtureLoader();
        LatLon topLeft = new LatLon(46.90, 9.9995);

        short[][] stitched = HGTWorker.load_3DEM(topLeft, 8, 6, loader);
        assertEquals(8, stitched.length);
        assertEquals(6, stitched[0].length);

        HGTDatafile leftTile = HGTWorker.loadHGTFile_3DEM(topLeft, loader);
        HGTDatafile rightTile = HGTWorker.loadHGTFile_3DEM(new LatLon(topLeft.getLatitude(), 10.0005), loader);

        int startX = (int) ((topLeft.getLongitude() - Math.floor(topLeft.getLongitude())) * HGTDatafile.DEM3_cells_per_row);
        int startY = (int) ((Math.ceil(topLeft.getLatitude()) - topLeft.getLatitude()) * HGTDatafile.DEM3_cells_per_row);

        assertEquals(leftTile.getData()[startX][startY], stitched[0][0]);
        assertEquals(rightTile.getData()[1][startY], stitched[1][0]);
    }

    @Test
    public void realFixtureCanBuildTileGltf() throws Exception {
        HGTFileLoader_LocalStorage loader = fixtureLoader();

        GLTFDatafile file = new GLTFWorker.GLTFBuilder(new LatLonBoundingBox(new LatLon(46.863760, 9.990500), 1000), loader)
                .setZoomlevel(11)
                .setEnclosement(true)
                .setScaleFactor(0.0005f)
                .build();

        String gltf = file.getString();
        assertTrue(file.getMeshes().size() > 0);
        assertTrue(gltf.contains("\"meshes\""));
        assertTrue(gltf.contains("\"Tile_X\""));
    }
}
