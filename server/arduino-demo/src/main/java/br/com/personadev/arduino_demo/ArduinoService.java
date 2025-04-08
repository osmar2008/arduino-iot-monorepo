package br.com.personadev.arduino_demo;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class ArduinoService {
    private SerialPort serialPort;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public void initialize(String portName, int baudRate) {
        // Find the serial port by name
        serialPort = SerialPort.getCommPort(portName);

        // Configure the serial port
        serialPort.setBaudRate(baudRate);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);

        // Open the serial port
        if (serialPort.openPort()) {
            System.out.println("Connected to Arduino on port: " + portName);

            // Set up a listener for incoming data
            serialPort.addDataListener(new SerialPortMessageListener() {
                @Override
                public byte[] getMessageDelimiter() {
                    return new byte[] { (byte) '\n' };
                }

                @Override
                public boolean delimiterIndicatesEndOfMessage() {
                    return true;
                }

                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    byte[] newData = event.getReceivedData();
                    String message = new String(newData).trim();
                    if (message.isEmpty() || message.contains("DEBUG")) {
                        return; // Ignore empty messages
                    }
                    try {
                        messageQueue.put(message);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        } else {
            System.err.println("Failed to connect to Arduino on port: " + portName);
        }
    }

    public String getNextMessage() throws InterruptedException {
        return messageQueue.take();
    }

    public void close() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Disconnected from Arduino.");
        }
    }

    // Utility method to list available ports
    public static void listAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        System.out.println("Available ports:");
        for (SerialPort port : ports) {
            System.out.println(port.getSystemPortName() + " - " + port.getDescriptivePortName());
        }
    }
}