package com.sunlocator.topolibrary.GPX;/*
Modified from
http://velo100.ru/garmin-fit-to-gpx
https://github.com/MaksVasilev/fit2gpx

*/

import com.garmin.fit.*;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import java.util.*;

class Converter {

    private static final String[] fields_for_search = {"position_lat", "position_long", "gps_accuracy", "altitude", "enhanced_altitude", "speed", "enhanced_speed", "vertical_speed",
            "vertical_oscillation", "stance_time_percent", "stance_time", "vertical_ratio", "stance_time_balance", "step_length",    // running dinamics
            "grade", "cadence", "fractional_cadence", "distance", "temperature", "calories", "heart_rate", "power", "accumulated_power",
            "left_right_balance", "left_power_phase", "right_power_phase", "left_power_phase_peak", "right_power_phase_peak",       // bike dinamics
            "left_torque_effectiveness", "right_torque_effectiveness", "left_pedal_smoothness", "right_pedal_smoothness",
            "combined_pedal_smoothness", "left_pco", "right_pco", "grit", "flow",
            "absolute_pressure"};
    private static final Integer[] fieldindex_for_search = {
            108,    // Respiratory
            90,     // Performance Contition
            61, 66    // ?
    };
    private static final String[] activities_fiels = {"duration", "position_lat", "position_long", "gps_accuracy", "altitude", "enhanced_altitude", "speed", "enhanced_speed", "vertical_speed",
            "vertical_oscillation", "stance_time_percent", "stance_time", "vertical_ratio", "stance_time_balance", "step_length",    // running dinamics
            "grade", "cadence", "fractional_cadence", "distance", "temperature", "calories", "heart_rate", "power", "accumulated_power",
            "left_right_balance", "left_right_balance_persent", "left_power_phase_start", "left_power_phase_end", "right_power_phase_start",
            "right_power_phase_end", "left_power_phase_peak_start", "left_power_phase_peak_end", "right_power_phase_peak_start", "right_power_phase_peak_end",
            "left_torque_effectiveness", "right_torque_effectiveness", "left_pedal_smoothness", "right_pedal_smoothness",
            "combined_pedal_smoothness", "left_pco", "right_pco", "grit", "flow", "absolute_pressure",
            "respiratory", "performance_contition", "field_num_61", "field_num_66",
            "fixed"};
    private static final HashMap<String, String> connect_iq_fields = new HashMap<>(); // field name, units
    private final TreeMap<String, Map<String, String>> Buffer = new TreeMap<>(); // buffer for read full info as "key = set of (field = value)" - all data to CSV, GPX
    private final SimpleDateFormat ISODateFormatCSV = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");  // формат вывода в csv ISO/ГОСТ
    List<Track> activityList = new ArrayList<>();
    String sportType = "";
    String subsportType = "";
    private long TimeStamp;
    private final SimpleDateFormat DateFormatCSV = ISODateFormatCSV;
    private BufferedInputStream InputStream;
    private boolean EmptyTrack = true;      // признак того, что трек не содержит координат
    private long StartTime;
    private boolean StartTimeFlag = false;

    protected static GPXWorker.ConversionOutput loadFitTracks(BufferedInputStream is) throws IOException {
        Converter converter = new Converter();
        converter.setFITInputStream(is);
        converter.run();

        GPXWorker.ConversionOutput output = new GPXWorker.ConversionOutput(converter.getListTrack());
        output.sportString = converter.sportType;
        output.subsportString = converter.subsportType;
        return output;
    }

    private static String append(Object obj, String string) {
        if (obj instanceof String) {
            return obj + string;
        } else {
            return string;
        }
    }

    private static double checkD(String D) {    // check Double
        double d;
        if (D.equals("")) {
            return -1.0;
        }
        try {
            d = Double.parseDouble(D.replace(",", "."));
        } catch (NumberFormatException ignoreD) {
            return -1.0;
        }
        return d;
    }

    private Number semicircleToDegree(Field field) {
        if (field != null && "semicircles".equals(field.getUnits())) {
            final long semicircle = field.getLongValue();
            return semicircle * (180.0 / Math.pow(2.0, 31.0)); // degrees = semicircles * ( 180 / 2^31 )
        } else {
            return null;
        }
    }

    private double round(double d, int p) {
        double dd = Math.pow(10, p);
        return Math.round(d * dd) / dd;
    }

    private String rounds(double d, int p) {
        double dd = Math.pow(10, p);
        return String.valueOf(Math.round(d * dd) / dd);
    }

    private List<Track> getListTrack() {
        return activityList;
    }

    void setFITInputStream(BufferedInputStream inputStream) {
        this.InputStream = inputStream;
    }

    int run() throws IOException {  // Основной поэтапный цикл работы конвертера
        converter_clear();           // clean for reuse in loop

        EmptyTrack = true;

        int readStatus = this.read();       // read file to buffer
        if (readStatus != 0) {
            throw new IOException("FIT readstatus: " + readStatus);
        }

        int fixstatus = this.fix();         // try to fix data in non corrupted file
        if (fixstatus != 0) {
            throw new IOException("FIT fixstatus: " + fixstatus);
        }

        this.format();   // format output to write in file
        return 0;
    }

    private void converter_clear() {
        Buffer.clear();
        StartTimeFlag = false;
    }

    private int fix() {             // fix various error and hole in data (#1, #13, #17)

        if (EmptyTrack) {
            return 201;
        }

        String last_lat = "";
        String last_lon = "";
        String last_ele = "";
        Double last_dist = 0.0;
        Double prev_dist = 0.0;
        Date date = new Date();
        Date prev_date = new Date();

        for (Map.Entry<String, Map<String, String>> m : Buffer.entrySet()) {
            try {
                date = DateFormatCSV.parse(m.getKey());
            } catch (ParseException ignore) {
            }

            Map<String, String> row1 = m.getValue();

            if (row1.containsKey("heart_rate") && "0".equals(row1.get("heart_rate"))) {    // generic: zero value of heart rate
                row1.remove("heart_rate");
            }

            // use altitude only if it present and enhanced_altitude not (#13)              // generic: alt -> enh_alt if it not present
            if (row1.get("altitude") != null && row1.get("enhanced_altitude") == null) {
                row1.put("enhanced_altitude", row1.get("altitude"));
                row1.put("fixed", append(row1.get("fixed"), "no-enh-ele,"));
            }

            // use speed only if it present and enhanced_speed not (#13)              // generic: speed -> enh_speed if it not present
            if (row1.get("speed") != null && row1.get("enhanced_speed") == null) {
                row1.put("enhanced_speed", row1.get("speed"));
                row1.put("fixed", append(row1.get("fixed"), "no-enh-speed,"));
            }

            // out of range values
            if (row1.containsKey("ciq_dose_rate") && Double.parseDouble(row1.get("ciq_dose_rate")) <= 0.0) {    // generic: zero or negative value of dose rate
                row1.remove("ciq_dose_rate");
            }


            // fix BRYTON hole in data: lat/lon (#1)
            double speed;

            if (row1.containsKey("distance")) {
                try {
                    last_dist = Double.parseDouble(row1.get("distance"));
                } catch (Exception ignore) {
                    last_dist = 0.0;
                }
            }

            if (row1.containsKey("enhanced_speed")) {
                try {
                    speed = Double.parseDouble(row1.get("enhanced_speed"));
                } catch (Exception ignore) {                              // generic: Speed from parce error
                    speed = 0.0;
                    row1.put("speed", "0.0");
                    row1.put("enhanced_speed", "0.0");
                    row1.put("fixed", append(row1.get("fixed"), "non-number-speed-to-zero,"));
                }
            } else {                                                     // generic: Speed from null
                speed = 0.0;
                row1.put("speed", "0.0");
                row1.put("enhanced_speed", "0.0");
                row1.put("fixed", append(row1.get("fixed"), "empty-speed-to-zero,"));
            }

            if (speed == 0.0) {                                            // generic: Speed from distance if speed = 0 and distance incremented
                if (last_dist > prev_dist) {
                    speed = (last_dist - prev_dist) / (date.getTime() - prev_date.getTime()) * 1000;
                    row1.put("speed", String.valueOf(speed));
                    row1.put("enhanced_speed", String.valueOf(speed));
                    row1.put("fixed", append(row1.get("fixed"), "speed-from-distance,"));
                }
            }

            prev_date = date;

            if (row1.containsKey("position_lat") && row1.containsKey("position_long")) {        // Fix 01-Bryton-hole-ele/Bryton-hole-coord - Bryton hole fix
                last_lat = row1.get("position_lat");
                last_lon = row1.get("position_long");
                EmptyTrack = false;

            } else if (!last_lat.equals("") && !last_lon.equals("") && (last_dist.equals(prev_dist))) {  // fix (01) only if distance not incremended
                row1.put("position_lat", last_lat);
                row1.put("position_long", last_lon);
                row1.put("fixed", append(row1.get("fixed"), "Bryton-hole-coord,"));
            }
            prev_dist = last_dist;

            // fix BRYTON hole in data: elevation (#1)
            if (row1.containsKey("enhanced_altitude")) {
                last_ele = row1.get("enhanced_altitude");
            } else if (!last_ele.equals("")) {
                row1.put("altitude", last_ele);
                row1.put("enhanced_altitude", last_ele);
                row1.put("fixed", append(row1.get("fixed"), "Bryton-hole-ele,"));
            }

            Buffer.put(m.getKey(), new HashMap<>() {
                {
                    this.putAll(row1);
                }
            });   // write change to buffer
            row1.clear();
        }                                                                            // End 01-Bryton-hole-ele/Bryton-hole-coord

        // fill all null lat/lon data before first real coordinates to this
        for (Map.Entry<String, Map<String, String>> map02b : Buffer.entrySet()) {        // Fix 02-Bryton-start-coord - Bryton start without coordinates fix
            if (map02b.getValue().get("position_lat") != null && map02b.getValue().get("position_long") != null) {
                String first_latlon = map02b.getKey();
                String lat = map02b.getValue().get("position_lat");
                String lon = map02b.getValue().get("position_long");

                for (Map.Entry<String, Map<String, String>> map02b_i : Buffer.entrySet()) {
                    if (!map02b_i.getKey().equals(first_latlon)) {
                        Map<String, String> row2 = map02b_i.getValue();
                        row2.put("position_lat", lat);
                        row2.put("position_long", lon);
                        row2.put("fixed", append(row2.get("fixed"), "Bryton-start-coord,"));
                        Buffer.put(map02b_i.getKey(), new HashMap<>() {
                            {
                                this.putAll(row2);
                            }
                        });   // write change to buffer
                        row2.clear();
                    } else {
                        break;
                    }
                }
                break;
            }
        }                                                                           // End 02-Bryton-start-coord

        // fill all null elevation data before first real ele to this ele
        for (Map.Entry<String, Map<String, String>> map03b : Buffer.entrySet()) {        // Fix 03-Bryton-start-ele - Bryton start without elevation fix
            if (map03b.getValue().get("enhanced_altitude") != null) {
                String first_ele = map03b.getKey();
                String ele = map03b.getValue().get("altitude");

                for (Map.Entry<String, Map<String, String>> map03b_i : Buffer.entrySet()) {
                    if (!map03b_i.getKey().equals(first_ele)) {
                        Map<String, String> row3 = map03b_i.getValue();
                        row3.put("enhanced_altitude", ele);
                        row3.put("altitude", ele);
                        row3.put("fixed", append(row3.get("fixed"), "Bryton-start-ele,"));

                        Buffer.put(map03b_i.getKey(), new HashMap<>() {
                            {
                                this.putAll(row3);
                            }
                        });   // write change to buffer
                        row3.clear();
                    } else {
                        break;
                    }
                }
                break;
            }
        }                                                                             // End 03-Bryton-start-ele

        double last_lat_d = 0.0;                                                      // Fix 04-Swim-no-coord - empty coordinates for Swim, if distance increment
        double last_lon_d = 0.0;
        last_dist = 0.0;


        for (Map.Entry<String, Map<String, String>> m : Buffer.entrySet()) {
            double lat;
            double lon;
            double dist = 0.0;

            Map<String, String> row0 = m.getValue();
            String start;
            String end;

            if (row0.get("position_lat") != null && row0.get("position_long") != null) {
                if (row0.get("distance") != null) {
                    last_lat_d = checkD(row0.get("position_lat"));
                    last_lon_d = checkD(row0.get("position_long"));
                    last_dist = checkD(row0.get("distance"));
                }
            }

            if (row0.get("position_lat") == null && row0.get("position_long") == null) {     // Search for first entry with empty coordinates
                start = m.getKey();

                ArrayList<Double> dist_steps = new ArrayList<>();

                for (Map.Entry<String, Map<String, String>> n : Buffer.subMap(start, Buffer.lastKey() + 1).entrySet()) {
                    Map<String, String> row00 = n.getValue();
                    if (row00.get("distance") != null) {
                        try {
                            Double d = Double.parseDouble(row00.get("distance"));
                            dist_steps.add(d - last_dist);
                        } catch (Exception ignore) {
                        }
                    } else {
                        dist_steps.add(0.0);
                    }

                    if (row00.get("distance") != null) {
                        dist = checkD(row00.get("distance"));
                    }
                    if (row00.get("position_lat") != null && row00.get("position_long") != null && (dist > last_dist)) {  // Search for end of hole
                        lat = checkD(row00.get("position_lat"));
                        lon = checkD(row00.get("position_long"));
                        end = n.getKey();

                        Double delta_dist = dist - last_dist;
                        Double delta_lat = lat - last_lat_d;
                        Double delta_lon = lon - last_lon_d;

                        int st = 0;
                        for (Map.Entry<String, Map<String, String>> insert : Buffer.subMap(start, end).entrySet()) {
                            Map<String, String> row_insert = insert.getValue();

                            Double step_dist_persent = (dist_steps.get(st) / delta_dist);
                            Double step_lat = last_lat_d + (delta_lat * step_dist_persent);   // increase lat/lon proportionally increasing distance
                            Double step_lon = last_lon_d + (delta_lon * step_dist_persent);

                            row_insert.put("position_lat", String.valueOf(step_lat));
                            row_insert.put("position_long", String.valueOf(step_lon));
                            row_insert.put("fixed", append(row_insert.get("fixed"), "Swim-no-coord,"));
                            if (row_insert.get("distance") == null) {
                                row_insert.put("distance", String.valueOf(last_dist));
                            }

                            Buffer.put(insert.getKey(), new HashMap<>() {
                                {
                                    this.putAll(row_insert);
                                }
                            });   // write change to buffer
                            row_insert.clear();
                            st++;
                        }
                        dist_steps.clear();
                        break;
                    }
                }
            }
        }                                                                           // End 04-Swim-no-coord
        return 0;
    }

    private int read() throws IOException {    // Try to read input file // Чукча-читатель

        Decode decode = new Decode();

        BufferedMesgBroadcaster mesgBroadcaster = new BufferedMesgBroadcaster(decode);
        FileIdMesgListener fileIdMesgListener = mesg -> {
            String _Product = "";
            String _Manufacturer;

            int __product = 0;
            int __manufacturer = 0;

            if (mesg.getManufacturer() != null) {
                __manufacturer = mesg.getManufacturer();
                _Manufacturer = " (" + Manufacturer.getStringFromValue(mesg.getManufacturer()) + ")";

                if (mesg.getProduct() != null) {

                    __product = mesg.getProduct();

                    if (__manufacturer == Manufacturer.GARMIN) {
                        _Product = GarminProduct.getStringFromValue(mesg.getGarminProduct()) + _Manufacturer;
                    } else if (mesg.getManufacturer() == Manufacturer.FAVERO_ELECTRONICS) {
                        _Product = FaveroProduct.getStringFromValue(mesg.getFaveroProduct()) + _Manufacturer;
                    } else if (mesg.getManufacturer() == Manufacturer.BRYTON) {
                        _Product = _Manufacturer;
                    } else {
                        _Product = "Device ID: " + mesg.getProduct() + _Manufacturer;
                    }
                }
            }

            SimpleDateFormat hashDate = new SimpleDateFormat("yyyyMMddHHmmss");
            hashDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        };

        MesgListener mesgListener = mesg -> {
            if (mesg.getName().equals("session")) {
                if (mesg.getFieldShortValue("sport") != null) {
                    sportType = Sport.getStringFromValue(Sport.getByValue(mesg.getFieldShortValue("sport")));
                } else if (mesg.getFieldShortValue("sub_sport") != null) {
                    subsportType = Sport.getStringFromValue(Sport.getByValue(mesg.getFieldShortValue("sub_sport")));
                }
            }

            if (mesg.getFieldStringValue("timestamp") != null && mesg.getName().equals("record")) {

                Map<String, String> fields = new HashMap<>();

                TimeStamp = (mesg.getFieldLongValue("timestamp") * 1000) + DateTime.OFFSET;

                if (!StartTimeFlag) {
                    StartTime = TimeStamp;
                    StartTimeFlag = true;
                }

                long duration_ms = (TimeStamp - StartTime);
                fields.put("duration", String.format("%02d:%02d:%02d", (duration_ms / (1000 * 60 * 60)), ((duration_ms / (1000 * 60)) % 60), ((duration_ms / 1000) % 60)));

                // search all known fields (array fieldnames)
                for (String field : fields_for_search) {
                    if (mesg.getFieldStringValue(field) != null) {
                        String value = mesg.getFieldStringValue(field);
                        if (field.equals("position_lat") || field.equals("position_long")) {
                            value = semicircleToDegree(mesg.getField(field)).toString();
                        }
                        // fields with multiply values
                        if (field.equals("left_power_phase") || field.equals("right_power_phase") ||
                                field.equals("left_power_phase_peak") || field.equals("right_power_phase_peak")) {
                            fields.put(field + "_start", mesg.getFieldStringValue(field, 0));
                            fields.put(field + "_end", mesg.getFieldStringValue(field, 1));
                        } else if (field.equals("left_right_balance")) {
                            fields.put(field, value);
                            // human readable balance
                            fields.put(field + "_persent", String.valueOf((mesg.getFieldDoubleValue("left_right_balance") / 3.6) - 50.0));
                        } else {
                            fields.put(field, value);
                            // System.out.println(field + "|" + value);
                        }
                    }
                }

                // for field without name and unknown fields use list of indexes
                for (Integer field : fieldindex_for_search) {
                    if (mesg.getFieldStringValue(field) != null) {
                        String value = mesg.getFieldStringValue(field);
                        switch (field) {
                            case 108:
                                fields.put("respiratory", value);
                                break;
                            case 90:
                                fields.put("performance_contition", value);
                                break;
                            default:
                                fields.put("field_num_" + field, value);
                                break;
                        }
                    }
                }

                for (DeveloperField field : mesg.getDeveloperFields()) {
                    connect_iq_fields.put("ciq_" + field.getName(), field.getUnits());
                    fields.put("ciq_" + field.getName(), field.getStringValue());
                }

                String RecordedDate = Long.toString(TimeStamp);

                // if records with this time already present, then merge existing key=value to current set
                // part of Bryton fixes
                if (Buffer.containsKey(RecordedDate)) {
                    for (String key : Buffer.get(RecordedDate).keySet()) {
                        fields.put(key, Buffer.get(RecordedDate).get(key));
                    }
                }

                if (fields.containsKey("position_lat") && fields.containsKey("position_long")) {
                    EmptyTrack = false;
                }   // flag for track

                Buffer.put(RecordedDate, new HashMap<>() {                                         // write all field to buffer
                    {
                        put("GPXtime", Long.toString(TimeStamp));
                        this.putAll(fields);
                    }
                });
            }
        };

        HrMesgListener hrListener = mesg -> {
        };

        //mesgBroadcaster.addListener(fileIdMesgListener);
        mesgBroadcaster.addListener(mesgListener);

        //mesgBroadcaster.addListener(hrListener);
        //MesgBroadcastPlugin hr_plugin = new HrToRecordMesgBroadcastPlugin();
        //mesgBroadcaster.registerMesgBroadcastPlugin(hr_plugin);

        try {
            mesgBroadcaster.run(new BufferedInputStream(InputStream));
            mesgBroadcaster.broadcast();
        } catch (FitRuntimeException e) {
            throw new IOException("ErrorParsingFile");
        }
        InputStream.close();
        return 0;
    }

    private void format() {     // format output from buffer to text

        if (EmptyTrack) {
            return;
        }

        Track.Builder activityTrackBuilder = Track.builder();
        TrackSegment.Builder activitySegmentBuilder = TrackSegment.builder();

        for (Map.Entry<String, Map<String, String>> m : Buffer.entrySet()) {
            WayPoint.Builder wp = WayPoint.builder();
            if (m.getValue().get("position_lat") != null && m.getValue().get("position_long") != null) {
                wp.lat(Double.parseDouble(m.getValue().get("position_lat"))).lon(Double.parseDouble(m.getValue().get("position_long")));
            }

            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(m.getValue().get("GPXtime"))), ZoneId.of("UTC"));

            wp.time(zdt);
            if (m.getValue().get("enhanced_altitude") != null) {
                wp.ele(Double.parseDouble(m.getValue().get("enhanced_altitude")));
            }
            boolean extention = m.getValue().get("power") != null || m.getValue().get("enhanced_speed") != null;
            boolean tpextention = m.getValue().get("temperature") != null || m.getValue().get("heart_rate") != null || m.getValue().get("cadence") != null || m.getValue().get("enhanced_speed") != null || m.getValue().get("distance") != null;
            if (extention || tpextention) {
                if (m.getValue().get("power") != null) {
                }
                if (m.getValue().get("enhanced_speed") != null) {
                }
                if (tpextention) {
                    if (m.getValue().get("temperature") != null) {
                    }
                    if (m.getValue().get("heart_rate") != null) {
                    }
                    if (m.getValue().get("cadence") != null) {
                    }
                    if (m.getValue().get("enhanced_speed") != null) {
                    }
                    if (m.getValue().get("distance") != null) {
                    }
                }
            }
            activitySegmentBuilder.addPoint(wp.build());
        }
        activityTrackBuilder.addSegment(activitySegmentBuilder.build());
        activityList.add(activityTrackBuilder.build());
    }

}
