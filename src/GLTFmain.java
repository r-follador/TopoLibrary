/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sunlocator.topolibrary.*;
import com.sunlocator.topolibrary.MapTile.GLTFWorker;
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
        LatLonBoundingBox boundingBox = new LatLonBoundingBox(new LatLon(-16.5004, -151.7415), 10000);

        try {
            String directory = "/home/rainer/Software_Dev/HGT/";
            //String directory = "/home/rainer/temp_sshftps/tmp_dem-data/Mapzen_3DEM/"; //sshfs rainer@private.genewarrior.com:/home/rainer temp_sshftps/
            HGTFileLoader_LocalStorage hgtFileLoader_3DEM = new HGTFileLoader_LocalStorage(directory);

            /**String json = HGTWorker.getJSON(hgt,true);
            PrintWriter out = new PrintWriter("/home/rainer/Software_Dev/JavaProjects/SunLocatorTopo/web/terrain.json");
            out.print(json);
            out.close();**/

           //LOD
            GLTFDatafile gltfFile = new GLTFWorker.GLTFBuilder(boundingBox, hgtFileLoader_3DEM)
                    .setEnclosement(true)
                    .setScaleFactor(0.0001f)
                    .exaggerateHeight(1.5f)
                    .isZUp(false)
                    //.setTextureUrl("https://api.maptiler.com/maps/ch-swisstopo-lbm/%d/%d/%d.png?key=xxx").build();
                    .setTextureUrl("https://api.maptiler.com/tiles/satellite-v2/%d/%d/%d.jpg?key=xxx").build();



            //GLTFDatafile gltfDatafile = HGTWorker.getLODGLTF_3DEM(boundingBox, hgtFileLoader_3DEM);





            //PrintWriter out3 = new PrintWriter("/home/rainer/Software_Dev/IdeaProjects/SunTopoStatic/problem_2.gltf");
            //out3.print(gltfDatafile.getString());
            //out3.close();

            PrintWriter out3_problem = new PrintWriter("/home/rainer/Software_Dev/IdeaProjects/cubetrekXR/problem.gltf");
            out3_problem.print(gltfFile.getString());
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
