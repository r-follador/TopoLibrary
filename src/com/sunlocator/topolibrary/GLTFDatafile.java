/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sunlocator.topolibrary;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;

/**
 *
 * @author rainer
 */
public class GLTFDatafile {
    
    private final ArrayList<GLTFMesh> meshes = new ArrayList<>();
    
    public GLTFDatafile() {
    }
    
    public void addGLTFMesh(HGTDatafile hgt, boolean enclosement) {
        meshes.add((new GLTFMeshTerrain(hgt)));

        if (enclosement) {
            meshes.add((new GLTFMeshEnclosement(hgt.data, hgt.cellsLon_X, hgt.cellsLat_Y, hgt.cellWidth_LatMeters, hgt.cellWidth_LonMeters, 0, 0)));
        }
    }

    public void addGLTFMesh(HGTDatafile hgt, boolean enclosement, int x_offset, int y_offset) {
        meshes.add((new GLTFMeshTerrain(hgt, true, x_offset, y_offset)));
        if (enclosement) {
            meshes.add((new GLTFMeshEnclosement(hgt.data, hgt.cellsLon_X, hgt.cellsLat_Y, hgt.cellWidth_LatMeters, hgt.cellWidth_LonMeters, x_offset, y_offset)));
        }
    }

    public void addGLTFMesh(short[][] data, int cellsLon_X, int cellsLat_Y, double cellWidth_LatMeters, double cellWidth_LonMeters, boolean enclosement, boolean UV, float offset_x, float offset_y) {
        meshes.add((new GLTFMeshTerrain(data, cellsLon_X, cellsLat_Y, cellWidth_LatMeters, cellWidth_LonMeters, UV, offset_x, offset_y)));

        if (enclosement) {
            meshes.add((new GLTFMeshEnclosement(data, cellsLon_X, cellsLat_Y, cellWidth_LatMeters, cellWidth_LonMeters, offset_x, offset_y)));
        }
    }

    private static class GLTFMesh
    {
        String payloadVertices = ""; //float Array to Base64
        String payloadIndices = ""; //int array to base64


        int indicesbytelength = 0;
        int verticesbytelength = 0;

        int countIndices = 0;
        int countVertices = 0;

        String payloadTexcoord = ""; //float array to base64


        public String getPayloadtexcoord() {
            return payloadTexcoord;
        }
        boolean UV = false;

        public boolean getUV() {
            return UV;
        }

        int texcoordbytelength = 0;

        public int getTexcoordbytelength() {
            return texcoordbytelength;
        }

        public float getMin_y() {
            return min_y;
        }

        public float getMin_x() {
            return min_x;
        }

        public float getMin_height() {
            return min_height;
        }

        public float getMax_y() {
            return max_y;
        }

        public float getMax_x() {
            return max_x;
        }

        public float getMax_height() {
            return max_height;
        }

        float min_y = 0, min_x = 0, min_height = 0, max_y = 0, max_x = 0, max_height = 0;

        public String getPayloadVertices() {
            return payloadVertices;
        }

        public String getPayloadIndices() {
            return  payloadIndices;
        }

        public int getIndicesbyteLength() {
            return indicesbytelength;
        }

        public int getVerticesbytelength() {
            return verticesbytelength;
        }

        public int getCountIndices() {
            return countIndices;
        }

        public int getCountVertices() {
            return countVertices;
        }
    }

    private static class GLTFMeshEnclosement extends GLTFMesh{

        public GLTFMeshEnclosement(HGTDatafile hgt) {
            this(hgt, 0,0);
        }

        public GLTFMeshEnclosement(HGTDatafile hgt, float offset_x, float offset_y ) {
            this(hgt.data, hgt.cellsLon_X, hgt.cellsLat_Y, hgt.cellWidth_LatMeters, hgt.cellWidth_LonMeters, offset_x, offset_y);
        }

        public GLTFMeshEnclosement(short[][] data, int cellsLon_X, int cellsLat_Y, double cellWidth_LatMeters, double cellWidth_LonMeters, float offset_x, float offset_y) {
            //Indices
            int[] indices = new int[(((cellsLon_X+cellsLat_Y)*2)-1)*6];

            int indicesCount = 0;


            for (int i=0; i<((cellsLon_X+cellsLat_Y)*2)-1; i++) {

                indices[indicesCount++]=i*2;
                indices[indicesCount++]=(i*2)+1;
                indices[indicesCount++]=(i*2)+2;

                indices[indicesCount++]=(i*2)+2;
                indices[indicesCount++]=(i*2)+1;
                indices[indicesCount++]=(i*2)+3;

            }


            //Vertices
            int verticesArraySize = (cellsLat_Y + cellsLon_X)*3*4;
            float[] verticesArray = new float[verticesArraySize];


            int cellCount = 0;

            float top_y = (float) ((cellsLat_Y / 2)) * (float) cellWidth_LatMeters;
            float left_x = (float) ((cellsLon_X / 2)) * (float) cellWidth_LonMeters * -1;

            max_y = top_y+offset_y;
            min_x = left_x+offset_x;
            min_height = 0;
            max_height = -1000;
            min_y = ((float) ((cellsLat_Y-1) - (cellsLat_Y / 2)) * (float) cellWidth_LatMeters * -1.0f)+offset_y;
            max_x = ((float) ((cellsLon_X-1) - (cellsLon_X / 2)) * (float) cellWidth_LonMeters)+offset_x;

            //North enclosure
            for (int x = 0; x < cellsLon_X; x++) {
                float x_m = (float) (x - (cellsLon_X / 2)) * (float) cellWidth_LonMeters;
                verticesArray[cellCount++]=x_m+offset_x;
                verticesArray[cellCount++]=top_y+offset_y;
                verticesArray[cellCount++]=0f;

                verticesArray[cellCount++]=x_m+offset_x;
                verticesArray[cellCount++]=top_y+offset_y;
                verticesArray[cellCount++]= (float) data[x][0];

                if ((float) data[x][0] > max_height) {
                    max_height = (float) data[x][0];
                }
            }

            //East enclosure
            for (int y = 0; y < cellsLat_Y; y++) {
                float y_m = (float) (y - (cellsLat_Y / 2)) * (float) cellWidth_LatMeters * -1.0f; //y-axis is flipped compared to array y-axis
                verticesArray[cellCount++]=(left_x * -1)+offset_x;
                verticesArray[cellCount++]=y_m+offset_y;
                verticesArray[cellCount++]=0f;

                verticesArray[cellCount++]=(left_x * -1)+offset_x;
                verticesArray[cellCount++]=y_m+offset_y;
                verticesArray[cellCount++]=(float) data[cellsLon_X-1][y];

                if ((float) data[cellsLon_X-1][y] > max_height) {
                    max_height = (float) data[cellsLon_X-1][y];
                }
            }

            //South enclosure
            float bottom_y = min_y;
            //From right to left
            for (int x = cellsLon_X-1; x >= 0; x--) {
                float x_m = (float) (x - (cellsLon_X / 2)) * (float) cellWidth_LonMeters;
                verticesArray[cellCount++] = x_m+offset_x;
                verticesArray[cellCount++] = bottom_y;
                verticesArray[cellCount++] = 0f;

                verticesArray[cellCount++] = x_m+offset_x;
                verticesArray[cellCount++] = bottom_y;
                verticesArray[cellCount++] = (float) data[x][cellsLat_Y-1];

                if ((float) data[x][cellsLat_Y-1] > max_height) {
                    max_height = (float) data[x][cellsLat_Y-1];
                }
            }

            //West enclosure
            //for (int y = 0; y < cellsLat_Y; y++) {
            for (int y = cellsLat_Y-1; y >=0; y--) {
                float y_m = (float) (y - (cellsLat_Y / 2)) * (float) cellWidth_LatMeters * -1.0f; //y-axis is flipped compared to array y-axis
                verticesArray[cellCount++]=left_x+offset_x;
                verticesArray[cellCount++]=y_m+offset_y;
                verticesArray[cellCount++]=0f;

                verticesArray[cellCount++]=left_x+offset_x;
                verticesArray[cellCount++]=y_m+offset_y;
                verticesArray[cellCount++]=(float) data[0][y];

                if ((float) data[0][y] > max_height) {
                    max_height = (float) data[0][y];
                }
            }

            countIndices = indices.length;
            countVertices = verticesArray.length / 3;
            indicesbytelength = (indices.length * Integer.SIZE)/8;
            verticesbytelength = (verticesArray.length * Float.SIZE)/8;

            payloadVertices = Base64.getEncoder().encodeToString(floatToLittleEndianByteArray(verticesArray));
            payloadIndices = Base64.getEncoder().encodeToString(intToLittleEndianByteArray(indices));
        }
    }
    
    private static class GLTFMeshTerrain extends GLTFMesh{

        public GLTFMeshTerrain(HGTDatafile hgt) {
            this(hgt, true, 0,0);
        }

        public GLTFMeshTerrain(HGTDatafile hgt, boolean UV, float offset_x, float offset_y ) {
            this(hgt.data, hgt.cellsLon_X, hgt.cellsLat_Y, hgt.cellWidth_LatMeters, hgt.cellWidth_LonMeters, UV, offset_x, offset_y);
        }

        public GLTFMeshTerrain(short[][] data, int cellsLon_X, int cellsLat_Y, double cellWidth_LatMeters, double cellWidth_LonMeters, boolean UV, float offset_x, float offset_y) {
            this.UV = UV;
            //Indices
            int indicesWidth = cellsLon_X;
            int indicesHeight = cellsLat_Y;

            int[] indices = new int[((indicesWidth-1)*(indicesHeight-1))*6];
            int indicesCount = 0;


            for (int x=1; x<indicesWidth; x++) {
                for (int y=1; y<indicesHeight; y++) {
                    int index = y * indicesWidth + x;
                    int above_index = index - indicesWidth;
                    int left_index = index - 1;
                    int across_index = above_index - 1;
                    indices[indicesCount++] = above_index;
                    indices[indicesCount++] = across_index;
                    indices[indicesCount++] = left_index;
                    indices[indicesCount++] = above_index;
                    indices[indicesCount++] = left_index;
                    indices[indicesCount++] = index;
                }
            }


            //Vertices
            int verticesArraySize = ((cellsLat_Y) * (cellsLon_X))*3;
            float[] verticesArray = new float[verticesArraySize];


            int cellCount = 0;

            float top_y = (float) ((cellsLat_Y / 2)) * (float) cellWidth_LatMeters;
            float left_x = (float) ((cellsLon_X / 2)) * (float) cellWidth_LonMeters * -1;

            max_y = top_y+offset_y;
            min_x = left_x+offset_x;
            min_height = Float.MAX_VALUE;
            max_height = -1000;


            for (int y = 0; y < cellsLat_Y; y++) {

                float y_m = (float) (y - (cellsLat_Y / 2)) * (float) cellWidth_LatMeters * -1.0f; //y-axis is flipped compared to array y-axis


                for (int x = 0; x < cellsLon_X; x++) {
                    float x_m = (float) (x - (cellsLon_X / 2)) * (float) cellWidth_LonMeters;
                    float height = (float) data[x][y];

                    verticesArray[cellCount++]=x_m+offset_x;
                    verticesArray[cellCount++]=y_m+offset_y;
                    verticesArray[cellCount++]=height;

                    if (height > max_height)
                        max_height = height;
                    if (height < min_height)
                        min_height = height;
                }

            }

            min_y = ((float) ((cellsLat_Y-1) - (cellsLat_Y / 2)) * (float) cellWidth_LatMeters * -1.0f)+(float)offset_y;
            max_x = ((float) ((cellsLon_X-1) - (cellsLon_X / 2)) * (float) cellWidth_LonMeters)+(float)offset_x;


            //Texcoord a.k.a. UV
            if (UV) {
                int texcoordArraySize = (verticesArraySize / 3) * 2;
                float[] texcoordArray = new float[texcoordArraySize];
                cellCount = 0;



                for (int y = 0; y < cellsLat_Y; y++) {

                    float y_uv = 1f - ((float) y / (float) (cellsLat_Y - 1));

                    for (int x = 0; x < cellsLon_X; x++) {
                        float x_uv = (float) x / (float) (cellsLon_X - 1);
                        texcoordArray[cellCount++] = x_uv;
                        texcoordArray[cellCount++] = y_uv;

                    }
                }

                texcoordbytelength = (texcoordArray.length * Float.SIZE)/8;
                payloadTexcoord = Base64.getEncoder().encodeToString(floatToLittleEndianByteArray(texcoordArray));
            } //end if UV

            countIndices = indices.length;
            countVertices = verticesArray.length / 3;
            indicesbytelength = (indices.length * Integer.SIZE)/8;
            verticesbytelength = (verticesArray.length * Float.SIZE)/8;

            payloadVertices = Base64.getEncoder().encodeToString(floatToLittleEndianByteArray(verticesArray));
            payloadIndices = Base64.getEncoder().encodeToString(intToLittleEndianByteArray(indices));

        }
    }
    
    
    public String getString() {

        
            StringBuilder stringBuffer = new StringBuilder();
            
            stringBuffer.append("{\n" +
                "\"scenes\" : [\n" +
                "{\n" +
                "   \"nodes\" : [");
            
                for (int i=0; i<meshes.size(); i++) {
                     stringBuffer.append(i);
                     if (i != meshes.size()-1)
                        stringBuffer.append(",");
                }   
                    
                 stringBuffer.append("]\n" +
                "}\n" +
                "],\n" +
                "\n" +
                "\"nodes\" : [\n");
                for (int i=0; i<meshes.size(); i++) {
                    stringBuffer.append("{\n" +
                    "   \"mesh\" : "+i+"\n" +
                    "}\n");
                    if (i != meshes.size()-1)
                        stringBuffer.append(",");
                }
                stringBuffer.append("],\n" +
                "\"meshes\" : [\n");

                int meshesCount = 0;
                for (int i=0; i<meshes.size(); i++) {
                    stringBuffer.append("{\n" +
                    "   \"primitives\" : [ {\n" +
                    "        \"indices\" : "+(meshesCount++)+",\n" +
                    "       \"attributes\" : {\n" +
                    "           \"POSITION\" : "+(meshesCount++)+"\n" +
                            (meshes.get(i).getUV()?",           \"TEXCOORD_0\" : "+(meshesCount++)+"\n":"") +
                    "       }\n" +
                    "   } ]\n" +
                    "}\n");
                    if (i != meshes.size()-1)
                        stringBuffer.append(",");
                };
                stringBuffer.append("],\n" +
                "\n" +
                "\"buffers\" : [\n"); //little endian
                for (int i=0; i<meshes.size(); i++) {
                    GLTFMesh mesh = meshes.get(i);
                    stringBuffer.append("{\n" +
                        "   \"uri\" : \"data:application/octet-stream;base64,"+mesh.getPayloadIndices()+"\",\n" + //buffer 0: indices
                        "   \"byteLength\" : "+((mesh.getPayloadIndices().length()/4)*3)+"\n" + //byte length to change
                        "},\n" +
                        "{\n" +
                        "   \"uri\" : \"data:application/octet-stream;base64,"+mesh.getPayloadVertices()+"\",\n" + //buffer 1: vertex positions
                        "   \"byteLength\" : "+((mesh.getPayloadVertices().length()/4)*3)+"\n" + //byte length to change
                        "}");
                    if (meshes.get(i).getUV()) {
                        stringBuffer.append(",\n" +
                        "{\n" +
                        "   \"uri\" : \"data:application/octet-stream;base64,"+mesh.getPayloadtexcoord()+"\",\n" + //buffer 2: texcoord UV positions
                        "   \"byteLength\" : "+((mesh.getPayloadtexcoord().length()/4)*3)+"\n" + //byte length to change
                        "}\n");
                    };
                    
                    if (i != meshes.size()-1)
                        stringBuffer.append(",");
                }

                meshesCount = 0;
                stringBuffer.append("],\n" +
                "\"bufferViews\" : [\n");  //indices
                        
                for (int i=0; i<meshes.size(); i++) {
                    GLTFMesh mesh = meshes.get(i);
                    stringBuffer.append(
                    "{\n" +
                    "   \"buffer\" : "+(meshesCount++)+",\n" +
                    "   \"byteOffset\" : 0,\n" +
                    "   \"byteLength\" : "+mesh.getIndicesbyteLength()+",\n" +
                    "   \"target\" : 34963\n" + //target: ELEMENT_ARRAY_BUFFER
                    "},\n" +
                    "{\n" +
                    "   \"buffer\" : "+(meshesCount++)+",\n" +   //vertices
                    "   \"byteOffset\" : 0,\n" +  //byteoffset where positions start
                    "   \"byteLength\" : "+mesh.getVerticesbytelength()+",\n" +
                    "   \"target\" : 34962\n" + //target: ARRAY_BUFFER
                    "}");

                    if (mesh.getUV()) {
                        stringBuffer.append(",\n" +
                        "{\n" +
                        "   \"buffer\" : "+(meshesCount++)+",\n" +   //vertices
                        "   \"byteOffset\" : 0,\n" +  //byteoffset where positions start
                        "   \"byteLength\" : "+mesh.getTexcoordbytelength()+",\n" +
                        "   \"target\" : 34962\n" + //target: ARRAY_BUFFER
                                "}\n");
                    };
                    if (i != meshes.size()-1)
                        stringBuffer.append(",");
                }


                meshesCount=0;
                stringBuffer.append("],\n" +
                "\"accessors\" : [\n");
                
                for (int i=0; i<meshes.size(); i++) {
                    GLTFMesh mesh = meshes.get(i);
                    stringBuffer.append(
                    "{\n" +
                    "   \"bufferView\" : "+(meshesCount++)+",\n" +
                    "   \"byteOffset\" : 0,\n" +
                    "   \"componentType\" : 5125,\n" +  //5123: UNSIGNED_INT (4 byte): note: we use signed int: max 2147483647
                    "   \"count\" : "+mesh.getCountIndices()+",\n" +
                    "   \"type\" : \"SCALAR\",\n" +
                    "   \"max\" : [ "+(mesh.getCountVertices()-1)+" ],\n" +
                    "   \"min\" : [ 0 ]\n" +
                    "},\n" +
                    "{\n" +
                    "   \"bufferView\" : "+(meshesCount++)+",\n" +
                    "   \"byteOffset\" : 0,\n" +
                    "   \"componentType\" : 5126,\n" + //5126: FLOAT (4 byte)
                    "   \"count\" : "+mesh.getCountVertices()+",\n" +
                    "   \"type\" : \"VEC3\",\n" +
                    "   \"max\" : [ "+mesh.getMax_x()+", "+mesh.getMax_y()+", "+mesh.getMax_height()+" ],\n" + //Max/min
                    "   \"min\" : [ "+mesh.getMin_x()+", "+mesh.getMin_y()+", "+mesh.getMin_height()+" ]\n" +  //max/min
                    "}");

                    if (mesh.getUV()) {
                        stringBuffer.append(",\n" +
                                "{\n" +
                                "   \"bufferView\" : " + (meshesCount++) + ",\n" +
                                "   \"byteOffset\" : 0,\n" +
                                "   \"componentType\" : 5126,\n" + //5126: FLOAT (4 byte)
                                "   \"count\" : " + mesh.getCountVertices() + ",\n" +
                                "   \"type\" : \"VEC2\",\n" +
                                "   \"max\" : [ " + 1 + ", " + 1 + " ],\n" + //Max/min
                                "   \"min\" : [ " + 0 + ", " + 0 + " ]\n" +  //max/min
                                "}\n");
                    };
                    
                    if (i != meshes.size()-1)
                        stringBuffer.append(",");
                };
                            
                stringBuffer.append("],\n" +
                    "\n" +
                    "\"asset\" : {\n" +
                        "\"version\" : \"2.0\"\n" +
                    "}\n" +
                "}");
        
        return stringBuffer.toString();
    }
    
    
    
    private static byte[] floatToLittleEndianByteArray(float[] floatArray) { //https://stackoverflow.com/questions/41990732/how-to-convert-double-array-to-base64-string-and-vice-versa-in-java
        
        int[] intArray = new int[floatArray.length];
        
        for (int i=0; i<floatArray.length;i++)  {
            intArray[i] = Integer.reverseBytes(Float.floatToIntBits(floatArray[i])); //make little endian
        }
        
        ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE * floatArray.length);
        buf.asIntBuffer().put(intArray);
        return buf.array();
    }
    
    private static byte[] intToLittleEndianByteArray(int[] intArray) {
        for (int i=0; i<intArray.length;i++)  {
            intArray[i] = Integer.reverseBytes(intArray[i]); //make little endian
        }
        
        ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE * intArray.length);
        buf.asIntBuffer().put(intArray);
        return buf.array();
    }
    
    private static byte[] shortToLittleEndianByteArray(short[] shortArray) {
        
        for (int i=0; i<shortArray.length;i++)  {
            shortArray[i] = Short.reverseBytes(shortArray[i]); //make little endian
        }
        
        
        ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE * shortArray.length);
        buf.asShortBuffer().put(shortArray);
        return buf.array();
    }
    
}
