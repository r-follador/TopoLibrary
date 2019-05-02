/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sunlocator.topolibrary.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;


/**
 *
 * @author rainer
 */
public class HGTmain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LatLon latlon = new LatLon(46.86365, 9.59043);
        //LatLon latlon = new LatLon(35.360940, 138.727257);
        LatLonBoundingBox boundingBox = new LatLonBoundingBox(latlon, 30000);
       
       
       try {
            String directory = "/home/rainer/Downloads/HGT/";
            HGTFileLoader_LocalStorage hgtFileLoader = new HGTFileLoader_LocalStorage(directory);
            HGTDatafile hgt = HGTWorker.loadFromBoundingBox_3DEM(boundingBox, hgtFileLoader);
            //GLTFDatafile gltfFile = HGTWorker.getLODGLTF(boundingBox, directory);

            System.out.println("-------------");
            //System.out.println(hgt.bounds);

           
           
            /**String json = HGTWorker.getJSON(hgt,true);
            PrintWriter out = new PrintWriter("/home/rainer/JavaProjects/SunLocatorTopo/web/terrain.json");
            out.print(json);
            out.close();**/


            GLTFDatafile gltfFile = new GLTFDatafile();
            gltfFile.addGLTFMesh(hgt, true);

            
            PrintWriter out3 = new PrintWriter("/home/rainer/IdeaProjects/SunTopoStatic/terrain.gltf");
            out3.print(gltfFile.getString());
            out3.close();
            
            //System.out.println(json);


           LatLonBoundingBox innerBoundingBox = new LatLonBoundingBox(boundingBox.getCenter(), (int)(boundingBox.getWidthLatMeters()));
           System.out.println(innerBoundingBox.toString());

           BufferedImage map = MapWorker.getMapPng(innerBoundingBox);
           MapWorker.resizeAndwriteImageToFile(1024, map, "/home/rainer/IdeaProjects/SunTopoStatic/terrain.map.png");
       } catch (IOException e) {
           System.err.println(e);
       }
    }
}
