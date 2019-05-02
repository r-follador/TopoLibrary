package com.sunlocator.topolibrary;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface HGTFileLoader {
    short[][] loadHGT(String filename, int cellsPerRow) throws FileNotFoundException, IOException;
}
