/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sunlocator.topolibrary.*;
import com.sunlocator.topolibrary.MapTile.MapTileWorker;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;


/**
 *
 * @author rainer
 */
public class GLTFmain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LatLonBoundingBox boundingBox = new LatLonBoundingBox(new LatLon(13.4506574,144.5086325), 3000);

        LatLonBoundingBox boundingBox_problem = new LatLonBoundingBox( 46.33175800051563, 46.331, 6.9873046875, 7);

        try {
            //String directory = "/home/rainer/Software_Dev/HGT/";
            String directory = "/home/rainer/temp_sshftps/tmp_dem-data/Mapzen_3DEM/"; //sshfs rainer@private.genewarrior.com:/home/rainer temp_sshftps/
            HGTFileLoader_LocalStorage hgtFileLoader_3DEM = new HGTFileLoader_LocalStorage(directory);

           
           
            /**String json = HGTWorker.getJSON(hgt,true);
            PrintWriter out = new PrintWriter("/home/rainer/Software_Dev/JavaProjects/SunLocatorTopo/web/terrain.json");
            out.print(json);
            out.close();**/


           //LOD

            GLTFDatafile gltfFile_problem = HGTWorker.getTileGLTF_3DEM(boundingBox, calculateZoomlevel(boundingBox), false, hgtFileLoader_3DEM, null);

            //GLTFDatafile gltfDatafile = HGTWorker.getLODGLTF_3DEM(boundingBox, hgtFileLoader_3DEM);





            //PrintWriter out3 = new PrintWriter("/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/problem_2.gltf");
            //out3.print(gltfDatafile.getString());
            //out3.close();

            PrintWriter out3_problem = new PrintWriter("/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/problem.gltf");
            out3_problem.print(gltfFile_problem.getString());
            out3_problem.close();

           //BufferedImage map_old = MapWorker.getMapPng(boundingBox);
           //MapWorker.resizeAndwriteImageToFile(1024, map_old, "/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/terrain.map_old.png");

           //not LOD
/**
           HGTDatafile hgt = HGTWorker.loadFromBoundingBox_3DEM(boundingBox, hgtFileLoader);
           GLTFDatafile gltfFile_noLod = new GLTFDatafile();
           gltfFile_noLod.addGLTFMesh(hgt, true, new GLTFDatafile.UvTexture(new URL("https://raw.githubusercontent.com/KhronosGroup/glTF-Tutorials/master/gltfTutorial/images/testTexture.png")));

           PrintWriter out = new PrintWriter("/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/terrain_noLOD.gltf");
           out.print(gltfFile_noLod.getString());
           out.close();
           out.close();

**/
       } catch (IOException e) {
           System.err.println(e);
       }
    }

    public static int calculateZoomlevel(LatLonBoundingBox boundingBox) {
        int zoom = 14;
        while (MapTileWorker.calculateRequiredTilesFromBoundingBox(boundingBox, zoom) > 48) {
            zoom--;
        }
        return zoom;
    }
}
