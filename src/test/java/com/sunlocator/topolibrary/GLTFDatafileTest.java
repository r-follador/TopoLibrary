package com.sunlocator.topolibrary;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GLTFDatafileTest {

    @Test
    public void addMeshWithUvAndEnclosementProducesTwoMeshes() {
        GLTFDatafile file = new GLTFDatafile();

        short[][] data = new short[][]{
                {10, 20, 30},
                {15, 25, 35},
                {20, 30, 40}
        };

        GLTFDatafile.GLTFMesh mesh = file.addGLTFMesh(
                data,
                3,
                3,
                1.0,
                1.0,
                true,
                0f,
                0f,
                1f,
                1f,
                new GLTFDatafile.UvTexture(true, false),
                new GLTFDatafile.UvTexture(false, false),
                true
        );

        mesh.metadata.put("Tile_X", "123");

        assertEquals(2, file.getMeshes().size());
        assertTrue(mesh.getCountIndices() > 0);
        assertTrue(mesh.getCountVertices() > 0);

        String gltf = file.getString();
        assertTrue(gltf.contains("\"asset\""));
        assertTrue(gltf.contains("\"TEXCOORD_0\""));
        assertTrue(gltf.contains("\"buffers\""));
        assertTrue(gltf.contains("\"Tile_X\": \"123\""));
    }
}
