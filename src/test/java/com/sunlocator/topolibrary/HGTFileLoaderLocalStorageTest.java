package com.sunlocator.topolibrary;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class HGTFileLoaderLocalStorageTest {

    @Test
    public void loadHgtReadsBigEndianAndClampsBelowMinus500() throws Exception {
        Path tmpDir = Files.createTempDirectory("hgt-loader-test");
        File file = tmpDir.resolve("N00E000.hgt").toFile();

        short[] values = new short[]{100, 200, -600, 400};
        writeShorts(file, values);

        HGTFileLoader_LocalStorage loader = new HGTFileLoader_LocalStorage(tmpDir.toString() + File.separator);
        short[][] data = loader.loadHGT("N00E000.hgt", 2);

        assertEquals(100, data[0][0]);
        assertEquals(200, data[1][0]);
        assertEquals(-500, data[0][1]);
        assertEquals(400, data[1][1]);
    }

    @Test(expected = FileNotFoundException.class)
    public void loadHgtThrowsForMissingFile() throws Exception {
        Path tmpDir = Files.createTempDirectory("hgt-loader-missing");
        HGTFileLoader_LocalStorage loader = new HGTFileLoader_LocalStorage(tmpDir.toString() + File.separator);
        loader.loadHGT("N00E000.hgt", 2);
    }

    @Test(expected = HGTWorker.FileFormatException.class)
    public void loadHgtThrowsForShortFile() throws Exception {
        Path tmpDir = Files.createTempDirectory("hgt-loader-short");
        File file = tmpDir.resolve("N00E000.hgt").toFile();

        writeShorts(file, new short[]{1, 2, 3});

        HGTFileLoader_LocalStorage loader = new HGTFileLoader_LocalStorage(tmpDir.toString() + File.separator);
        loader.loadHGT("N00E000.hgt", 2);
    }

    private static void writeShorts(File file, short[] values) throws Exception {
        ByteBuffer bb = ByteBuffer.allocate(values.length * 2);
        for (short value : values) {
            bb.putShort(value);
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bb.array());
        }
    }
}
