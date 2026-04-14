package packexcalib.gnss;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GnssUtcOffsetConverter {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Conversione meccanica UTC + offset utente
     *
     * @param utcDate       "dd-MM-yyyy"
     * @param utcTime       "HH:mm:ss"
     * @param offsetMinutes minuti di offset rispetto a UTC
     * @return LocalDateTime corretto
     */
    public static LocalDateTime applyOffset(
            String utcDate,
            String utcTime,
            int offsetMinutes
    ) {

        // Parsing UTC
        LocalDate date = LocalDate.parse(utcDate, DATE_FMT);
        LocalTime time = LocalTime.parse(utcTime, TIME_FMT);
        LocalDateTime utcDateTime = LocalDateTime.of(date, time);

        // Applicazione offset
        return utcDateTime.plusMinutes(offsetMinutes);
    }
}
