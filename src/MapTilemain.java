
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

import com.sunlocator.topolibrary.LatLon;
import com.sunlocator.topolibrary.LatLonBoundingBox;
import com.sunlocator.topolibrary.MapTile.MapTile;

public class MapTilemain {

    public static void main(String[] args) throws IOException {


        LatLon latLon = new LatLon(46.8508,9.5320);
        MapTile tile = new MapTile(12, latLon);
        System.out.println(tile.toString());
        LatLonBoundingBox bb = tile.getBoundingBox();

        System.out.println("Bounding box: "+bb.toString());
        MapTile.XY topleft = MapTile.TileXYtoPixelXY(tile);
        System.out.printf("NW pixel: %d, %d%n", topleft.x, topleft.y);
        LatLon topleftLL = MapTile.convertPixelXYtoLatLong(topleft, tile.zoom);
        System.out.printf("NW lat lon: %f, %f%n", topleftLL.getLatitude(), topleftLL.getLongitude());

        MapTile.XY point = tile.getLocalPixelPositionofLatLon(latLon);
        System.out.printf("Local pixel position xy: %d, %d%n", point.x, point.y);


        URL url = new URL(formatURL(tile));

        Image image = null;

        System.out.println(url.toString());
        long t0 = System.currentTimeMillis();
        image = ImageIO.read(url);
        System.out.println("Download time: "+ (System.currentTimeMillis()-t0)+"ms");
        showImage(image);
    }




    ////////////////////////////////////////
    private static void showImage(Image image) {
        JFrame frame = new JFrame();
        frame.setSize(1024, 1024);
        Container contentPane = frame.getContentPane();
        JLabel sentenceLabel= new JLabel(new ImageIcon(image));
        contentPane.add(sentenceLabel);
        frame.setVisible(true);
    }

    private static String formatURL(MapTile mapTile) {
        return String.format("https://api.maptiler.com/maps/basic/%d/%d/%d@2x.png?key=xxx", mapTile.zoom, mapTile.x, mapTile.y);
    }
}
