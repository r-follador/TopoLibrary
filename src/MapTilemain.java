
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
        MapTile tile = new MapTile(10, latLon);
        System.out.println(tile.toString());
        LatLonBoundingBox bb = tile.getBoundingBox();
        System.out.println("Boudning box: "+bb.toString());

        MapTile.XY topleft = MapTile.TileXYtoPixelXY(tile);
        System.out.println(String.format("NW pixel: %d, %d", topleft.x, topleft.y));
        LatLon topleftLL = MapTile.convertPixelXYtoLatLong(topleft, tile.zoom);
        System.out.println(String.format("NW lat lon: %f, %f", topleftLL.getLatitude(), topleftLL.getLongitude()));

        MapTile.XY point = tile.getLocalPixelPositionofLatLon(latLon);
        System.out.println(String.format("Local pixel position xy: %d, %d", point.x, point.y));


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
        frame.setSize(700, 700);
        Container contentPane = frame.getContentPane();
        JLabel sentenceLabel= new JLabel(new ImageIcon(image));
        contentPane.add(sentenceLabel);
        frame.setVisible(true);
    }

    private static String formatURL(MapTile mapTile) {
        return String.format("https://api.maptiler.com/maps/basic/%d/%d/%d.png?key=***REMOVED***", mapTile.zoom, mapTile.x, mapTile.y);
    }
}
