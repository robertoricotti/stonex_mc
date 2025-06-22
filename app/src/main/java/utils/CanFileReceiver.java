package utils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CanFileReceiver {

    private static final byte SOF = 0x01; // Start of Frame
    private static final byte EOF = 0x04; // End of Frame
    private static final int CAN_PAYLOAD_SIZE = 8; // Dimensione del payload CAN (escluso SOF, EOF, e CRC)

    private ByteArrayOutputStream fileData;
    private boolean receivingFile = false;
    private int fileLength = -1;
    private int receivedLength = 0;

    public CanFileReceiver() {
        fileData = new ByteArrayOutputStream();
    }

    public void receivePacket(byte[] packet) {
        if (packet.length == 0) return;

        if (packet[0] == SOF && !receivingFile) {
            // Inizio di un nuovo file
            fileLength = ByteBuffer.wrap(Arrays.copyOfRange(packet, 1, 3)).getShort() & 0xFFFF;
            receivingFile = true;
            receivedLength = 0;
            fileData.reset();
            //Log.d("Can RECEIVE","Inizio ricezione file di lunghezza: " + fileLength);
        } else if (receivingFile) {
            if (packet[packet.length - 1] == EOF) {
                // Pacchetto con EOF
                fileData.write(packet, 0, packet.length - 1);
                receivedLength += packet.length - 1;
            } else {
                // Pacchetto senza EOF
                fileData.write(packet, 0, packet.length);
                receivedLength += packet.length;
            }

            if (receivedLength >= fileLength) {
                // Fine della ricezione del file
                receivingFile = false;
                //Log.d("Can RECEIVE","Ricezione file completata. Lunghezza ricevuta: " + receivedLength);
                printFileData();
            }
        }
    }

    private void printFileData() {
        byte[] fileBytes = fileData.toByteArray();
        String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
        //Log.d("Can RECEIVE","Contenuto del file ricevuto:");
        //Log.d("Can RECEIVE",fileContent);
        // Verifica CRC
        short receivedCRC = calculateCRC16(fileBytes, 0, fileBytes.length);
        //Log.d("Can RECEIVE","CRC calcolato: " + receivedCRC);
    }

    private short calculateCRC16(byte[] data, int offset, int length) {
        int crc = 0xFFFF; // Valore iniziale del CRC

        for (int i = offset; i < offset + length; i++) {
            crc ^= (data[i] & 0xFF); // XOR del byte con il CRC

            for (int j = 0; j < 8; j++) { // Calcola il CRC bit per bit
                if ((crc & 0x01) != 0) {
                    crc = (crc >>> 1) ^ 0xA001; // Applica il polinomio CRC
                } else {
                    crc >>>= 1; // Shift a destra senza XOR
                }
            }
        }

        return (short) crc; // Restituisci il CRC come short
    }


}

