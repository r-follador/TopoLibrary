package com.sunlocator.topolibrary;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HGTWorkerCoreTest {

    @Test
    public void fileNameGenerationHandlesHemispheres() {
        assertEquals("N46E009", HGTWorker.getFileName_3DEM(new LatLon(46.8, 9.5)));
        assertEquals("S17W152", HGTWorker.getFileName_1DEM(new LatLon(-16.5, -151.7)));
    }

    @Test
    public void degreeDistanceConversionsAreConsistent() {
        double meters = 1234.5;
        double latDegrees = HGTWorker.distance2degrees_latitude(meters);
        double lonDegrees = HGTWorker.distance2degrees_longitude(meters, 46.8);

        assertEquals(meters, HGTWorker.degrees2distance_latitude(latDegrees), 0.0001);
        assertEquals(meters, HGTWorker.degrees2distance_longitude(lonDegrees, 46.8), 0.0001);
    }

    @Test
    public void previewHgtCropsNorthWestAndAdjustsBounds() {
        short[][] data = new short[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        HGTDatafile source = new HGTDatafile(new LatLonBoundingBox(47, 46, 9, 10), data, HGTDatafile.HGT_Type.hgtOther);

        HGTDatafile preview = HGTWorker.getPreviewHGTDatafile(source, 2, 3);

        assertEquals(2, preview.cellsLon_X);
        assertEquals(3, preview.cellsLat_Y);
        assertEquals(1, preview.getData()[0][0]);
        assertEquals(7, preview.getData()[1][2]);
    }

    @Test
    public void downsampleAveragesNeighboringCells() {
        short[][] data = new short[][]{
                {0, 10, 20, 30},
                {1, 11, 21, 31},
                {2, 12, 22, 32},
                {3, 13, 23, 33}
        };
        HGTDatafile source = new HGTDatafile(new LatLonBoundingBox(47, 46, 9, 10), data, HGTDatafile.HGT_Type.hgtOther);

        HGTDatafile down = HGTWorker.downsampleHGTDatafile(source);

        assertEquals(2, down.cellsLon_X);
        assertEquals(2, down.cellsLat_Y);
        assertEquals(11, down.getData()[0][0]);
        assertEquals(12, down.getData()[1][0]);
        assertEquals(26, down.getData()[0][1]);
        assertEquals(27, down.getData()[1][1]);
    }

    @Test
    public void writeHgtRoundTripsWithLocalLoader() throws Exception {
        short[][] data = new short[][]{
                {100, 200},
                {300, 400}
        };
        File tmp = Files.createTempFile("hgt-write", ".hgt").toFile();
        assertTrue(HGTWorker.writeHGT(tmp, data, 2));

        HGTFileLoader_LocalStorage loader = new HGTFileLoader_LocalStorage(tmp.getParentFile().getAbsolutePath() + File.separator);
        short[][] loaded = loader.loadHGT(tmp.getName(), 2);

        assertEquals(100, loaded[0][0]);
        assertEquals(200, loaded[0][1]);
        assertEquals(300, loaded[1][0]);
        assertEquals(400, loaded[1][1]);
    }
}
