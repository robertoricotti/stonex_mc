package iredes;

import static iredes.IredesUtils.localName;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class NavDataParser {

    private static final String NS = null;

    public List<NavData_IR> parse(InputStream input) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(input, null);

        List<NavData_IR> result = new ArrayList<>();
        int event = parser.getEventType();

        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG &&
                    "NavSample".equals(localName(parser))) {
                result.add(readNavSample(parser));
            }
            event = parser.next();
        }

        return result;
    }

    private NavData_IR readNavSample(XmlPullParser parser) throws Exception {
        parser.require(XmlPullParser.START_TAG, NS, parser.getName());

        String timestampIso = null;
        String machineId = null;
        Point3D_IR pos = null;
        double heading = Double.NaN;
        double pitch = Double.NaN;
        double roll = Double.NaN;
        String activeHoleId = null;

        while (!(parser.next() == XmlPullParser.END_TAG &&
                "NavSample".equals(localName(parser)))) {

            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String tag = localName(parser);

            switch (tag) {
                case "Timestamp":
                    timestampIso = readSimpleText(parser);
                    break;
                case "MachineId":
                    machineId = readSimpleText(parser);
                    break;
                case "Position":
                    pos = readPoint(parser);
                    break;
                case "Heading":
                    heading = Double.parseDouble(readSimpleText(parser));
                    break;
                case "Pitch":
                    pitch = Double.parseDouble(readSimpleText(parser));
                    break;
                case "Roll":
                    roll = Double.parseDouble(readSimpleText(parser));
                    break;
                case "ActiveHoleId":
                    activeHoleId = readSimpleText(parser);
                    break;
                default:
                    skip(parser);
            }
        }

        return new NavData_IR(timestampIso, machineId, pos, heading, pitch, roll, activeHoleId);
    }

    private Point3D_IR readPoint(XmlPullParser parser) throws Exception {
        parser.require(XmlPullParser.START_TAG, NS, parser.getName());
        Double x = null, y = null, z = null;

        while (!(parser.next() == XmlPullParser.END_TAG &&
                "Position".equals(localName(parser)))) {

            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String tag = localName(parser);

            switch (tag) {
                case "PointX":
                    x = Double.parseDouble(readSimpleText(parser));
                    break;
                case "PointY":
                    y = Double.parseDouble(readSimpleText(parser));
                    break;
                case "PointZ":
                    z = Double.parseDouble(readSimpleText(parser));
                    break;
                default:
                    skip(parser);
            }
        }

        return new Point3D_IR(x, y, z);
    }

    private String readSimpleText(XmlPullParser parser) throws Exception {
        parser.require(XmlPullParser.START_TAG, NS, parser.getName());
        String text = "";
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, NS, parser.getName());
        return text.trim();
    }

    private void skip(XmlPullParser parser) throws Exception {
        if (parser.getEventType() != XmlPullParser.START_TAG) return;
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}