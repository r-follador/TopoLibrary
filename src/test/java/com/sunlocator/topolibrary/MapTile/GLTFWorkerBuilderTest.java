package com.sunlocator.topolibrary.MapTile;

import com.sunlocator.topolibrary.GLTFDatafile;
import com.sunlocator.topolibrary.LatLon;
import com.sunlocator.topolibrary.LatLonBoundingBox;
import com.sunlocator.topolibrary.PatternHgtFileLoader;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GLTFWorkerBuilderTest {

    @Test
    public void builderProducesGltfWithMetadataAndMeshes() throws Exception {
        LatLonBoundingBox bbox = new LatLonBoundingBox(new LatLon(46.863760, 9.590417), 1200);
        PatternHgtFileLoader loader = new PatternHgtFileLoader((x, y, filename, cellsPerRow) -> (short) ((x + y) % 500));

        GLTFDatafile file = new GLTFWorker.GLTFBuilder(bbox, loader)
                .setZoomlevel(11)
                .setEnclosement(true)
                .setScaleFactor(0.001f)
                .exaggerateHeight(1.25f)
                .isZUp(true)
                .build();

        String gltf = file.getString();

        assertTrue(file.getMeshes().size() > 0);
        assertTrue(gltf.contains("\"scenes\""));
        assertTrue(gltf.contains("\"meshes\""));
        assertTrue(gltf.contains("\"Tile_X\""));
    }
}
