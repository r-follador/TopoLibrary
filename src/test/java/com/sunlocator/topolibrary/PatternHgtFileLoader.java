package com.sunlocator.topolibrary;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PatternHgtFileLoader implements HGTFileLoader {

    public interface CellValueProvider {
        short valueAt(int x, int y, String filename, int cellsPerRow);
    }

    private final CellValueProvider provider;
    private final Map<String, short[][]> cache = new ConcurrentHashMap<>();

    public PatternHgtFileLoader(CellValueProvider provider) {
        this.provider = provider;
    }

    @Override
    public short[][] loadHGT(String filename, int cellsPerRow) throws IOException {
        String cacheKey = filename + "#" + cellsPerRow;
        return cache.computeIfAbsent(cacheKey, key -> {
            short[][] data = new short[cellsPerRow][cellsPerRow];
            for (int x = 0; x < cellsPerRow; x++) {
                for (int y = 0; y < cellsPerRow; y++) {
                    data[x][y] = provider.valueAt(x, y, filename, cellsPerRow);
                }
            }
            return data;
        });
    }
}
