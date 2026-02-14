# TopoLibrary Agent Guide

This file is the operational memory for agents working on this repository.

## 1) Repository Purpose

TopoLibrary is a Java library for terrain and activity processing:
- Read SRTM HGT files (1DEM/3DEM)
- Query elevation at coordinates or along tracks
- Convert terrain areas to GLTF meshes
- Process GPX/FIT tracks (load, simplify, summarize, normalize elevation)
- Map tile math and bounding box utilities

Primary package:
- `com.sunlocator.topolibrary`

Demo entrypoints (examples/manual experimentation):
- `src/HGTmain.java`
- `src/GLTFmain.java`
- `src/GPXmain.java`
- `src/GPXtoGLTFmain.java`
- `src/MapTilemain.java`
- `src/FITmain.java`
- `src/FitCorrectElevation.java`
- `src/HGTmapzen.java`

## 2) Build and Test Setup

- Build tool: Gradle
- Java target: release 17
- Test framework: JUnit 4

Important `build.gradle` detail:
- Main sources are under `src/`
- `src/test/**` is explicitly excluded from main
- Tests live in:
  - `src/test/java`
  - `src/test/resources`

Commands:
- Run all tests: `./gradlew test --no-daemon`
- Run one test class: `./gradlew test --tests "*ClassName*" --no-daemon`

## 3) External Dependencies

- JPX (`io.jenetics:jpx`) for GPX structures/parsing
- JTS (`org.locationtech.jts:jts-core`) for geometry support
- Garmin FIT SDK via local `libs/fit.jar`

## 4) Module Map (What to Edit for What)

### Terrain / HGT
- `HGTFileLoader` (interface)
- `HGTFileLoader_LocalStorage` (filesystem implementation)
- `HGTWorker` (loading windows, conversions, GLTF-related terrain prep)
- `HGTDatafile` (terrain grid + metadata)

### Coordinate primitives
- `LatLon`
- `LatLonBoundingBox`

### Map tiles
- `MapTile/MapTile`
- `MapTile/MapTileWorker`

### GPX/FIT
- `GPX/GPXWorker`
- `GPX/Converter` (FIT -> track conversion internals)

### GLTF generation
- `GLTFDatafile`
- `MapTile/GLTFWorker` (`GLTFBuilder`)

### Network map rendering (not unit-test friendly)
- `MapWorker`

## 5) Critical Data Conventions

### HGT naming
- Tile name denotes SW corner, e.g. `N46E009.hgt` covers:
  - latitude 46 to 47
  - longitude 9 to 10

### DEM sizes
- 3DEM: 1201 x 1201
- 1DEM: 3601 x 3601

### Array orientation
- Most terrain arrays are indexed as `data[x][y]`
  - `x`: longitude index (west -> east)
  - `y`: latitude index (north -> south)

### Units
- Distances: meters
- Coordinates: degrees (WGS84)
- Track duration summary: minutes

## 6) Public API Hotspots (Most Reused)

- `HGTWorker.getFileName_3DEM(...)`
- `HGTWorker.loadHGTFile_3DEM(...)`
- `HGTWorker.load_3DEM(...)`
- `HGTWorker.loadFromBoundingBox_3DEM(...)`
- `HGTWorker.writeHGT(...)`
- `GPXWorker.loadGPXTracks(...)`
- `GPXWorker.loadFitTracks(...)`
- `GPXWorker.reduceTrackSegments(...)`
- `GPXWorker.getTrackSummary(...)`
- `GPXWorker.getElevationDataFromHGT(...)`
- `MapTile` conversion helpers
- `GLTFWorker.GLTFBuilder`
- `GLTFDatafile.getString()`

## 7) Known Behavioral Pitfalls / Risks

1. `MapTile.convertPixelXYtoLatLong(...)` prints a warning that there may be an error.
2. `GPXWorker.getTrackSummary(...)` assumes elevation and timestamps are present.
3. `MapTileWorker.getTilesFromBoundingBox(...)` does not support dateline-spanning bboxes.
4. `MapWorker` uses live HTTP calls; not suitable for deterministic unit tests.
5. `HGTWorker.FileFormatException` stores message in custom field (not standard RuntimeException message handling).
6. `HGTWorker.loadFromBoundingBox_1DEM(...)` has historical TODO context; validate behavior with tests before relying on edge cases.

## 8) Current Test Strategy

### Principle
- Deterministic first, no live network in unit tests.

### Test locations
- `src/test/java/com/sunlocator/topolibrary/**`
- `src/test/resources/fixtures/**`

### Helpers available
- `PatternHgtFileLoader` (fake deterministic HGT source)
- `TestTrackFactory` (synthetic track construction)

### Covered areas
- Coordinate math and bounds
- Map tile conversions and bbox tile coverage
- HGT loader and binary round-trip behavior
- HGT window stitching (including boundary crossing)
- GPX simplification, summary, encoding, and elevation replacement logic
- GLTF mesh/string sanity
- Real HGT fixture integration sanity

## 9) Fixture Policy

Default:
- Use synthetic fixtures and fake loaders.

Real HGT fixtures in repo:
- `src/test/resources/fixtures/hgt-real/N46E009.hgt`
- `src/test/resources/fixtures/hgt-real/N46E010.hgt`

Constraint:
- Keep copied real HGT fixtures small in count (hard cap 10, preferred <=3).

Do not require:
- Runtime dependency on `/home/rainer/Software_Dev/HGT` for tests.

## 10) Documentation Policy

- Public APIs must be documented in code with Javadoc.
- Use `docs/JAVADOC_TEMPLATE.md`.
- Include in Javadocs:
  - units
  - array/coordinate orientation
  - failure behavior (`@throws`)

Placement rule:
- API semantics -> Javadoc in source files
- Contributor workflow/testing rules -> `agent.md`
- User quick-start and cookbook -> `README.md`

## 11) Safe Change Workflow for Agents

When implementing changes:
1. Read affected module + existing tests first.
2. Add/adjust tests for behavior change.
3. Keep network-independent tests deterministic.
4. Run `./gradlew test --no-daemon`.
5. Update Javadocs for changed public behavior.
6. If adding new conventions, update this file.

When asked for reviews:
- Prioritize correctness regressions, edge cases, and missing tests.
- Reference concrete files and line numbers.

## 12) What Not to Do

- Do not add live network dependency to unit tests.
- Do not rely on local absolute paths in committed tests.
- Do not move logic into demo `*main.java` files.
- Do not change file/array orientation conventions silently.

## 13) Fast Orientation Checklist (for next session)

If you start cold, do this first:
1. Read `README.md`
2. Read `agent.md`
3. Run `./gradlew test --no-daemon`
4. Inspect target module + matching tests under `src/test/java`
5. Implement with tests first/alongside

