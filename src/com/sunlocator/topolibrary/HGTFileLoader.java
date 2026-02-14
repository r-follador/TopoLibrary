package com.sunlocator.topolibrary;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface HGTFileLoader {
    /**
     * Loads one HGT file into a {@code short[cellsPerRow][cellsPerRow]} grid.
     *
     * Array orientation is {@code data[x][y]} where {@code x} is longitude index
     * (west to east) and {@code y} is latitude index (north to south).
     *
     * @param filename HGT filename (for example {@code N46E009.hgt})
     * @param cellsPerRow expected row/column size (for example {@code 1201} or {@code 3601})
     * @return decoded elevation grid in meters above sea level
     * @throws FileNotFoundException if the file does not exist in the loader backend
     * @throws IOException if reading or format validation fails
     */
    short[][] loadHGT(String filename, int cellsPerRow) throws FileNotFoundException, IOException;
}
