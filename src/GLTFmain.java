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
public class GLTFmain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //LatLon latlon = new LatLon(46.86365, 9.59043);
        LatLon latlon = new LatLon(	51.1056, -115.3573);
        LatLonBoundingBox boundingBox = new LatLonBoundingBox(latlon, 10000);
       
       
       try {
            String directory = "/home/rainer/Software_Dev/HGT/";
            HGTFileLoader_LocalStorage hgtFileLoader = new HGTFileLoader_LocalStorage(directory);

           
           
            /**String json = HGTWorker.getJSON(hgt,true);
            PrintWriter out = new PrintWriter("/home/rainer/Software_Dev/JavaProjects/SunLocatorTopo/web/terrain.json");
            out.print(json);
            out.close();**/


           //LOD

            GLTFDatafile gltfFile = HGTWorker.getLODGLTF_3DEM(boundingBox, hgtFileLoader);


            PrintWriter out3 = new PrintWriter("/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/terrain_LOD.gltf");
            out3.print(gltfFile.getString());
            out3.close();

           BufferedImage map_old = MapWorker.getMapPng(boundingBox);
           MapWorker.resizeAndwriteImageToFile(1024, map_old, "/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/terrain.map_old.png");

           //not LOD
           /**
           HGTDatafile hgt = HGTWorker.loadFromBoundingBox_3DEM(boundingBox, hgtFileLoader);
           GLTFDatafile gltfFile_noLod = new GLTFDatafile();
           gltfFile_noLod.addGLTFMesh(hgt, true);

           PrintWriter out = new PrintWriter("/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/terrain_noLOD.gltf");
           out.print(gltfFile_noLod.getString());
           out.close();
           out.close();
            **/

       } catch (IOException e) {
           System.err.println(e);
       }
    }
}
