package utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import serial.SerialPortManager;

public class CanFileTransfer {
    public static boolean sending;
    private static final byte SOF = 0x01; // Start of Frame
    private static final byte EOF = 0x04; // End of Frame
    private static final int CAN_PAYLOAD_SIZE = 8; // CAN Payload size (excluding SOF, EOF, and CRC)
    private static final int DELAY_MS = 10; // Delay between packets in milliseconds
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface ProgressCallback {
        void onProgressUpdate(int percentage);
    }

    public static void sendFileViaCAN(String filePath, int channel, int id, ProgressCallback callback) {
        sending = true;
        executorService.submit(() -> {
            File file = new File(filePath);
            int fileLength = (int) file.length();
            int totalBytesSent = 0;

            try (FileInputStream fis = new FileInputStream(file)) {
                // Send SOF + Length of file (Big Endian U16, e.g., new byte[] {87, 16} means a dimension of 4183 bytes)
                byte[] lengthHeader = createLengthHeader(fileLength);
                MyDeviceManager.CanWrite(true,channel, id, lengthHeader.length, lengthHeader);


                // Send file data in packets
                byte[] buffer = new byte[CAN_PAYLOAD_SIZE - 1]; // -1 for EOF byte
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] packet = new byte[bytesRead + 1];
                    System.arraycopy(buffer, 0, packet, 0, bytesRead);
                    packet[bytesRead] = EOF; // Adding EOF to the end of the packet
                    MyDeviceManager.CanWrite(true,channel, id, packet.length, packet);

                    totalBytesSent += bytesRead;

                    // Update progress
                    int percentage = (totalBytesSent * 100) / fileLength;
                    callback.onProgressUpdate(percentage);

                    try {
                        Thread.sleep(DELAY_MS); // Delay between packets
                    } catch (InterruptedException e) {
                        sending = false;
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during delay between packets", e);
                    }
                }

                // Send EOF to mark the end of transmission
                MyDeviceManager.CanWrite(true,channel, id, 1, new byte[]{EOF});

                // Send CRC for all data in the file
                byte[] crcPacket = new byte[2];
                short crc16 = calculateCRC16(filePath);
                ByteBuffer.wrap(crcPacket).putShort(crc16);
                MyDeviceManager.CanWrite(true,channel, id, crcPacket.length, crcPacket);
                sending = false;

                // Final progress update to 100%
                callback.onProgressUpdate(100);

            } catch (IOException e) {
                sending = false;
                e.printStackTrace(); // Handle the exception appropriately
            }
        });
    }
    public static void sendFileViaSerial(String filePath, ProgressCallback callback) {
        sending = true;
        executorService.submit(() -> {
            File file = new File(filePath);
            int fileLength = (int) file.length();
            int totalBytesSent = 0;


            try (FileInputStream fis = new FileInputStream(file)) {
                // Send SOF + Length of file
                byte[] lengthHeader = createLengthHeader(fileLength);
                SerialPortManager.instance().sendCommand(lengthHeader);
                Log.d("SerialFileTransfer", "Sent SOF and length header: " + bytesToHex(lengthHeader)); // Log the length header in hex

                try {
                    Thread.sleep(200); // Optional delay
                } catch (InterruptedException e) {
                    sending = false;
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during delay before file sent", e);
                }
                totalBytesSent += lengthHeader.length;



                // Send file data in chunks
                byte[] buffer = new byte[fileLength];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] dataToSend = Arrays.copyOf(buffer, bytesRead); // Extract the actual data read
                    SerialPortManager.instance().sendCommand(dataToSend);
                    Log.d("SerialFileTransfer", bytesToAscii(dataToSend)); // Log the data packet as ASCII
                    totalBytesSent += bytesRead;

                    int percentage = (totalBytesSent * 100) / fileLength;
                    callback.onProgressUpdate(percentage);
                }
                // Send EOF to mark the end of transmission
                SerialPortManager.instance().sendCommand(new byte[]{EOF});
                Log.d("SerialFileTransfer", "Sent EOF: " + EOF);
                totalBytesSent += 1;

                // Send CRC for the entire file data
                byte crc8 = calculateCRC8(filePath);
                SerialPortManager.instance().sendCommand(new byte[]{crc8});
                Log.d("SerialFileTransfer", "Sent CRC: " + crc8);
                totalBytesSent += 1;

                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                sending = false;

                // Final log and progress update
                Log.d("SerialFileTransfer", "Total bytes actually sent: " + totalBytesSent);
                callback.onProgressUpdate(100);

            } catch (IOException e) {
                sending = false;
                Log.e("SerialFileTransfer", "Error during file transfer", e);
            }
        });
    }



    private static byte[] createLengthHeader(int length) {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put(SOF); // Start of Frame
        buffer.putShort((short) length); // File length
        return buffer.array();
    }

    private static short calculateCRC16(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            byte[] fileData = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(fileData)) != -1) {
                if (buffer.remaining() < bytesRead) {
                    // Expand the buffer if necessary
                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + 1024);
                    buffer.flip();
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                }
                buffer.put(fileData, 0, bytesRead);
            }
            buffer.put(EOF); // Add EOF to calculate CRC16
            return calculateCRC16(buffer.array(), 0, buffer.position());
        }
    }

    private static short calculateCRC16(byte[] data, int offset, int length) {
        int crc = 0xFFFF; // Initial value of CRC
        for (int i = offset; i < offset + length; i++) {
            crc ^= (data[i] & 0xFF); // XOR of byte with CRC
            for (int j = 0; j < 8; j++) { // Calculate CRC bit by bit
                if ((crc & 0x01) != 0) {
                    crc = (crc >>> 1) ^ 0xA001; // Apply the CRC polynomial
                } else {
                    crc >>>= 1; // Shift right without XOR
                }
            }
        }
        return (short) crc; // Return CRC as short
    }

    private static byte calculateCRC8(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            byte crc = 0x00;
            int byteRead;
            while ((byteRead = fis.read()) != -1) {
                crc ^= (byte) byteRead;
            }
            return crc;
        }
    }

    // Utility method to convert byte array to a hexadecimal string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X ", b)); // Convert byte to hex with leading zero if needed
        }
        return hexString.toString().trim(); // Return the formatted string
    }

    // Utility method to convert byte array to ASCII string (or any other encoding)
    private static String bytesToAscii(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8); // Ensure the data is readable ASCII or UTF-8
    }
}
