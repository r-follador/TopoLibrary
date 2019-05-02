package com.sunlocator.topolibrary;

import java.io.*;

public class HGTFileLoader_LocalStorage implements HGTFileLoader {

    String directory;
    public HGTFileLoader_LocalStorage(String directory) {
        this.directory = directory;
    }

    @Override
    public short[][] loadHGT(String filename, int cellsPerRow) throws FileNotFoundException, IOException {
        final int bufsize = 2 * cellsPerRow;

        final int expectedArraySize = cellsPerRow * cellsPerRow;

        short[][] heightmap = new short[cellsPerRow][cellsPerRow]; //signed short max 32767

        File f = new File(directory+filename);
        if (!f.exists()) {
            throw new FileNotFoundException(filename);
        }

        InputStream is = new FileInputStream(f);

        byte[] b = new byte[bufsize];

        int readBytes = 0;

        int cellCount = 0;

        while ((readBytes = is.read(b)) != -1) {
            for (int i = 0; i < readBytes; i += 2) {
                short height = (short) ((b[i] & 0xFF) * 256 + (b[i + 1] & 0xFF));
                //System.out.println((b[i]& 0xFF)+"\t"+(b[i+1]& 0xFF)+"\t"+height);
                //heightmap[x][y] aka heightmap[lon][lat]
                heightmap[cellCount % cellsPerRow][cellCount / cellsPerRow] = height;
                cellCount++;
                if (cellCount > expectedArraySize) {
                    throw new HGTWorker.FileFormatException("File '" + filename + "' expected cell size " + cellsPerRow + "x" + cellsPerRow + " (=" + expectedArraySize + "): current count: " + cellCount);
                }
            }
        }

        if (cellCount < expectedArraySize) {
            throw new HGTWorker.FileFormatException("File '" + filename + "' expected cell size " + cellsPerRow + "x" + cellsPerRow + " (=" + expectedArraySize + "): current count: " + cellCount);
        }

        is.close();
        return heightmap;
    }
}
