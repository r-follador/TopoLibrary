# TopoLibrary

A Swiss Army Knife for HGT and GPX related topography tasks. 

A crucial function is to generate GLTF files from terrain data, see [CubeTrek.com](https://cubetrek.com)
and [CubeTrekXR](https://r-follador.github.io/cubetrekXR/) for a demonstration.

## GPS Files (GPX)
### Cookbook and Examples
#### Load GPX Track and print a summary

```
File gpxTrack = new File("20220731_090254.gpx");
try {
   Track track = GPXWorker.loadGPXTracks(gpxTrack).trackList.get(0);
   System.out.println("Points: "+track.getSegments().get(0).getPoints().size());
   
   GPXWorker.TrackSummary trackSummary = GPXWorker.getTrackSummary(track);
   System.out.println(trackSummary);
} catch (IOException e) {
   System.err.println(e);
}
```
Output is:
```
Points: 561
Distance: 25067 m 
Elevation Up/Down: 200m / 292m 
Duration: 128min 
Total Segments/Points: 1/561
Highest/Lowest Elevation: 1838m /1715m
```

#### Simplify GPS Track

Reduce a gpxTrack by downsampling using Ramer-Douglas-Peucker algorithm.
Epsilon in meters (e.g. 2)
```
Track reduced = GPXWorker.reduceTrackSegments(track, 2);
```

## HGT data
An HGT file is a Shuttle Radar Topography Mission (SRTM) data file.
See [openstreetmap.org/wiki/SRTM](https://wiki.openstreetmap.org/wiki/SRTM) for more infos.

HGT files use a normalized file naming scheme denoting the SW corner (e.g N20E100.hgt contains data from 20°N to 21°N and from 100°E to 101°E.)
One file contains an area of 1° x 1°.
TopoLibrary can work with either 1 arcsecond (called 1DEM) or 3 arcsecond (called 3DEM) HGT files.
1DEM: 3601x3601 cells (1 arcsec per cell; corresponds to approx. 30m at the equator, less everywhere else)
3DEM: 1201x1201 cells (3 arcsec per cell; corresponds to approx. 90m at the equator, less everywhere else)

TopoLibrary requires access to a folder/directory containing all HGT files in the correct naming scheme. It picks and reads the required HGT file automatically and returns a FileNotFoundException if this particular HGT file is missing.

### Cookbook and Examples
#### Get the Elevation of a single point from HGT files
```
HGTFileLoader_LocalStorage hgtFileLoader_1DEM = new HGTFileLoader_LocalStorage("HGT_1DEM/");
LatLon position = new LatLon(-16.5004, -151.7415);
short elevation = GPXWorker.getElevationDataFromHGT(position, hgtFileLoader_1DEM);
System.out.println("Elevation is "+ elevation +"m.a.s.l");
```

#### Get Elevation along a GPX track from HGT files
Preferentially uses HGT from hgtFileLoader_1DEM storage, falls back to hgtFileLoader_3DEM if not available.

```
File gpxTrack = new File("753608283.fit-replaced.gpx");
HGTFileLoader_LocalStorage hgtFileLoader_1DEM = new HGTFileLoader_LocalStorage("HGT_1DEM/");
HGTFileLoader_LocalStorage hgtFileLoader_3DEM = new HGTFileLoader_LocalStorage("HGT_3DEM/");

try {
   Track track = GPXWorker.loadGPXTracks(gpxTrack).trackList.get(0);

   ArrayList<short[]> eles = GPXWorker.getElevationDataFromHGT(reduced, hgtFileLoader_1DEM, hgtFileLoader_3DEM);
   
   for (int i=0; i<track.getSegments().size(); i++) {
       for (int j=0; j<track.getSegments().get(i).getPoints().size(); j++) {
           short ele = eles.get(i)[j];
           short gps_h = track.getSegments().get(i).getPoints().get(j).getElevation().get().shortValue();

           double lat = track.getSegments().get(i).getPoints().get(j).getLatitude().doubleValue();
           double lon = track.getSegments().get(i).getPoints().get(j).getLongitude().doubleValue();

           System.out.println(lat+"/"+lon+"\t"+ele+"\t"+gps_h);
       }
   }
} catch (IOException e) {
   System.err.println(e);
}
```


#### Get a GLTF
Generate a GLTF file from a Bounding Box.

```
LatLon latlon = new LatLon(	51.1056, -115.3573);
LatLonBoundingBox boundingBox = new LatLonBoundingBox(latlon, 10000);

HGTFileLoader_LocalStorage hgtFileLoader_3DEM = new HGTFileLoader_LocalStorage("HGT_3DEM/");

GLTFDatafile gltfFile = new GLTFWorker.GLTFBuilder(boundingBox, hgtFileLoader_3DEM)
        .setEnclosement(true)
        .setScaleFactor(0.0001f)
        .exaggerateHeight(1.5f)
        .isZUp(false)
        //.setTextureUrl("https://api.maptiler.com/maps/ch-swisstopo-lbm/%d/%d/%d.png?key=xxx").build();


PrintWriter out3 = new PrintWriter("terrain_tiles.gltf");
out3.print(gltfFile.getString());
out3.close();
```

## External Dependencies
- [JPX - Java GPX library](https://github.com/jenetics/jpx)
- [JTS Topology Suite](https://github.com/locationtech/jts)