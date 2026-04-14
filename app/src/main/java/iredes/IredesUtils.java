package iredes;

import org.xmlpull.v1.XmlPullParser;

public final class IredesUtils {

    private IredesUtils() {
    }

    public static String localName(XmlPullParser parser) {
        String name = parser.getName();
        if (name == null) return null;
        int idx = name.indexOf(':');
        return (idx >= 0) ? name.substring(idx + 1) : name;
    }
}