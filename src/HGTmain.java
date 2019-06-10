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
        //LatLon latlon = new LatLon(46.86365, 9.59043);
        LatLon latlon = new LatLon(46.808972, 9.319959);
        LatLonBoundingBox boundingBox = new LatLonBoundingBox(latlon, 30000);
       
       
       try {
            String directory = "/home/rainer/Downloads/HGT/";
            HGTFileLoader_LocalStorage hgtFileLoader = new HGTFileLoader_LocalStorage(directory);
            GLTFDatafile gltfFile = HGTWorker.getLODGLTF(boundingBox, hgtFileLoader);
           
           
            /**String json = HGTWorker.getJSON(hgt,true);
            PrintWriter out = new PrintWriter("/home/rainer/JavaProjects/SunLocatorTopo/web/terrain.json");
            out.print(json);
            out.close();**/


           //LOD
            PrintWriter out3 = new PrintWriter("/home/rainer/IdeaProjects/SunTopoStatic/terrain_LOD.gltf");
            out3.print(gltfFile.getString());
            out3.close();
           LatLonBoundingBox innerBoundingBox = new LatLonBoundingBox(boundingBox.getCenter(), (int)(boundingBox.getWidthLatMeters()/3));
           System.out.println(innerBoundingBox.toString());

           BufferedImage map = MapWorker.getMapPng(innerBoundingBox);
           MapWorker.resizeAndwriteImageToFile(1024, map, "/home/rainer/IdeaProjects/SunTopoStatic/terrain.map.png");

           //not LOD
           HGTDatafile hgt = HGTWorker.loadFromBoundingBox_3DEM(innerBoundingBox, hgtFileLoader);
           GLTFDatafile gltfFile_noLod = new GLTFDatafile();
           gltfFile_noLod.addGLTFMesh(hgt, true);

           PrintWriter out = new PrintWriter("/home/rainer/IdeaProjects/SunTopoStatic/terrain_noLOD.gltf");
           out.print(gltfFile_noLod.getString());
           out.close();
           out.close();

       } catch (IOException e) {
           System.err.println(e);
       }
    }
}
