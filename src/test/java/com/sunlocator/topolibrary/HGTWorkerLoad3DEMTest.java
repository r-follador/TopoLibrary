package com.sunlocator.topolibrary;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HGTWorkerLoad3DEMTest {

    @Test
    public void load3DemStitchesAcrossFileBoundary() throws Exception {
        HGTFileLoader loader = new HGTFileLoader() {
            @Override
            public short[][] loadHGT(String filename, int cellsPerRow) throws IOException {
                short value = filename.contains("E009") ? (short) 9 : (short) 10;
                short[][] out = new short[cellsPerRow][cellsPerRow];
                for (int x = 0; x < cellsPerRow; x++) {
                    for (int y = 0; y < cellsPerRow; y++) {
                        out[x][y] = value;
                    }
                }
                return out;
            }
        };

        short[][] stitched = HGTWorker.load_3DEM(new LatLon(46.5, 9.9992), 4, 3, loader);

        assertEquals(9, stitched[0][0]);
        assertEquals(10, stitched[1][0]);
        assertEquals(10, stitched[2][1]);
        assertEquals(10, stitched[3][2]);
    }
}
