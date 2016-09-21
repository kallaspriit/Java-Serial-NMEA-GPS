package gps;

import java.util.HashMap;
import java.util.Map;

public class NMEA {

    interface SentenceParser {
        boolean parse(String[] tokens, GpsState position);
    }

    private class GPGGA implements SentenceParser {
        public boolean parse(String[] tokens, GpsState position) {
            position.time = Float.parseFloat(tokens[1]);
            position.lat = Latitude2Decimal(tokens[2], tokens[3]);
            position.lon = Longitude2Decimal(tokens[4], tokens[5]);
            position.quality = Integer.parseInt(tokens[6]);
            position.altitude = Float.parseFloat(tokens[9]);

            return true;
        }
    }

    private class GPGGL implements SentenceParser {
        public boolean parse(String[] tokens, GpsState position) {
            position.lat = Latitude2Decimal(tokens[1], tokens[2]);
            position.lon = Longitude2Decimal(tokens[3], tokens[4]);
            position.time = Float.parseFloat(tokens[5]);

            return true;
        }
    }

    private class GPRMC implements SentenceParser {
        public boolean parse(String[] tokens, GpsState position) {
            position.time = Float.parseFloat(tokens[1]);
            position.lat = Latitude2Decimal(tokens[3], tokens[4]);
            position.lon = Longitude2Decimal(tokens[5], tokens[6]);
            position.velocity = Float.parseFloat(tokens[7]);
            position.dir = Float.parseFloat(tokens[8]);

            return true;
        }
    }

    private class GPVTG implements SentenceParser {
        public boolean parse(String[] tokens, GpsState position) {
            position.dir = Float.parseFloat(tokens[3]);

            return true;
        }
    }

    private class GPRMZ implements SentenceParser {
        public boolean parse(String[] tokens, GpsState position) {
            position.altitude = Float.parseFloat(tokens[1]);

            return true;
        }
    }

    private static float Latitude2Decimal(String lat, String NS) {
        float result = (Float.parseFloat(lat.substring(2)) / 60.0f) + Float.parseFloat(lat.substring(0, 2));

        if (NS.startsWith("S")) {
            result *= -1.0f;
        }

        return result;
    }

    private static float Longitude2Decimal(String lon, String WE) {
        float med = (Float.parseFloat(lon.substring(3)) / 60.0f) + Float.parseFloat(lon.substring(0, 3));

        if (WE.startsWith("W")) {
            med *= -1.0f;
        }

        return med;
    }

    @SuppressWarnings("WeakerAccess")
    public class GpsState {
        public float time = 0.0f;
        public float lat = 0.0f;
        public float lon = 0.0f;
        public boolean hasFix = false;
        public int quality = 0;
        public float dir = 0.0f;
        public float altitude = 0.0f;
        public float velocity = 0.0f;

        void updatefix() {
            hasFix = quality > 0;
        }

        public String toString() {
            return String.format("POSITION: lat: %f, lon: %f, time: %f, Q: %d, dir: %f, alt: %f, vel: %f", lat, lon, time, quality, dir, altitude, velocity);
        }
    }

    private GpsState position = new GpsState();

    private static final Map<String, SentenceParser> sentenceParsers = new HashMap<>();

    NMEA() {
        sentenceParsers.put("GPGGA", new GPGGA());
        sentenceParsers.put("GPGGL", new GPGGL());
        sentenceParsers.put("GPRMC", new GPRMC());
        sentenceParsers.put("GPRMZ", new GPRMZ());
        sentenceParsers.put("GPVTG", new GPVTG());
    }

    GpsState getUpdatedStatus(String line) {
        if (line.startsWith("$")) {
            String nmea = line.substring(1);
            String[] tokens = nmea.split(",");
            String type = tokens[0];

            if (sentenceParsers.containsKey(type)) {
                try {
                    sentenceParsers.get(type).parse(tokens, position);
                } catch (Exception e) {}
            }

            position.updatefix();
        }

        return position;
    }
}