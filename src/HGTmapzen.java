/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sunlocator.topolibrary.*;

import java.awt.image.BufferedImage;
import java.io.*;


/**
 *
 * @author rainer
 */
public class HGTmapzen {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String dir = args[0];
        String fileInput = args[1];
        String fileOutput  = args[2];

        /**
        String dir = "/home/rainer/Software_Dev/HGT_1DEM/";
        String fileInput = "N46E009.hgt";
        String fileOutput  = "N46E009__2.hgt"; **/

       try {
            HGTFileLoader_LocalStorage hgtFileLoader_localStorage = new HGTFileLoader_LocalStorage(dir);
            short[][] data = hgtFileLoader_localStorage.loadHGT(fileInput, HGTDatafile.DEM1_cells_per_row);

            short min = 5000;
            short max = -100;

            for (int x=0; x<HGTDatafile.DEM1_cells_per_row; x++) {
                for (int y=0;y<HGTDatafile.DEM1_cells_per_row; y++) {
                    if (data[x][y]<0) {
                        data[x][y]=0;
                    }

                    if (min>data[x][y])
                        min=data[x][y];
                    if (max<data[x][y])
                        max=data[x][y];
                }
            }

            if (min==0 && max==0) {
                System.out.println(fileInput + "\t Marked for deletion (file is completely 0 elevation)");
            } else {
                System.out.println(fileInput + "\t" + min + "\t" + max);
                File output = new File(dir + fileOutput);
                HGTWorker.writeHGT(output, data, HGTDatafile.DEM1_cells_per_row);
            }
       } catch (IOException e) {
           System.err.println(e);
       }
    }

    public static void print(short[][] data) {
        for (int x = 0; x < HGTDatafile.DEM1_cells_per_row; x++) {
            System.out.print(data[x][0]+"\t");
        }
        System.out.println("|");
    }

}
