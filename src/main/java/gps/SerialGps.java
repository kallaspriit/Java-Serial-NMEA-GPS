package gps;

import gnu.io.*;

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

    public void start() throws UnsupportedCommOperationException, NoSuchPortException, PortInUseException, IOException {
        NMEA nmea = new NMEA();

        CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
        SerialPort serialPort = (SerialPort) portId.open("gps.SerialGps application", 5000);
        serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

        InputStream inStream = serialPort.getInputStream();

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
