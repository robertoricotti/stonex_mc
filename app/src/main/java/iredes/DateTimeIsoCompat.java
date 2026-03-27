package iredes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeIsoCompat {
    private DateTimeIsoCompat() {}

    public static String normalize(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";
        return s.replace(' ', 'T');
    }

    public static LocalDateTime parse(String s) {
        String n = normalize(s);
        if (n.isEmpty()) return null;
        return LocalDateTime.parse(n, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
