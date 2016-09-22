package gps;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SerialGps {

    public interface StateListener {
        void onGpsStateUpdated(NMEA.GpsState state);
    }

    private String portName;
    private int baudRate;
    private boolean isRunning = false;
    private List<StateListener> stateListeners = new ArrayList<>();

    public SerialGps(String portName, int baudRate) {
        this.portName = portName;
        this.baudRate = baudRate;
    }

    public SerialGps(String portName) {
        this(portName, 4800);
    }

    public void addStateListener(StateListener stateListener) {
        stateListeners.add(stateListener);
    }

    public void start() {
        NMEA nmea = new NMEA();

        SerialPort[] serialPorts = SerialPort.getCommPorts();
        SerialPort gpsPort = null;

        for (SerialPort serialPort : serialPorts) {
            if (serialPort.getDescriptivePortName().toLowerCase().contains("serial")) {
                gpsPort = serialPort;
            }
        }

        if (gpsPort == null) {
            System.out.println("failed to find gps serial port");

            return;
        }

        System.out.println("using serial port: " + gpsPort.getDescriptivePortName());

        gpsPort.setBaudRate(4800);
        gpsPort.openPort();
        InputStream inStream = gpsPort.getInputStream();

        if (inStream == null) {
            System.out.println("opening port " + gpsPort.getDescriptivePortName() + " failed");

            return;
        }

        Thread thread = new Thread(() -> {
            String line = "";

            isRunning = true;

            while (isRunning) {
                try {
                    if (inStream.available() > 0) {
                        char b = (char) inStream.read();

                        if (b == '\n') {
                            NMEA.GpsState gpsState = nmea.getUpdatedStatus(line);

                            updateState(gpsState);

                            line = "";
                        } else {
                            line += b;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public void stop() throws InterruptedException {
        isRunning = false;
    }

    private void updateState(NMEA.GpsState gpsState) {
        stateListeners.forEach(stateListener -> stateListener.onGpsStateUpdated(gpsState));
    }

}
