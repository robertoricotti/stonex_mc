package packexcalib.exca;

public class PGNExtractor {

    public static int extractPGN(int canId) {
        // Estrai i campi necessari dal CAN ID
        int dataPage = (canId >> 16) & 0x01;     // Data Page: bit 16
        int pduFormat = (canId >> 16) & 0xFF;   // PDU Format: bit 16-23
        int pduSpecific = (canId >> 8) & 0xFF;  // PDU Specific: bit 8-15

        // Calcola il PGN in base al valore di PDU Format
        if (pduFormat < 240) {
            // PGN = Data Page * 2^16 + PDU Format * 2^8
            return (dataPage << 16) | (pduFormat << 8);
        } else {
            // PGN = Data Page * 2^16 + PDU Format * 2^8 + PDU Specific
            return (dataPage << 16) | (pduFormat << 8) | pduSpecific;
        }
    }
}
