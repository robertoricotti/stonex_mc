package iredes;

import static iredes.IredesUtils.localName;

import android.util.Xml;


import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.*;



public final class DrillPlanParser {

    private static final String NS = null;

    public DrillPlan_IR parse(InputStream input) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(input, null);

        String planId = null;
        String planName = null;
        String project = null;
        String workOrder = null;

        List<DrillHole_IR> holes = new ArrayList<>();
        Map<String, List<String>> boomSequences = new HashMap<>();

        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String tag = localName(parser);

                if ("PlanId".equals(tag)) {
                    planId = readSimpleText(parser, "PlanId");
                } else if ("PlanName".equals(tag)) {
                    planName = readSimpleText(parser, "PlanName");
                } else if ("Project".equals(tag)) {
                    project = readSimpleText(parser, "Project");
                } else if ("WorkOrder".equals(tag)) {
                    workOrder = readSimpleText(parser, "WorkOrder");
                } else if ("DrillPlan".equals(tag)) {
                    readDrillPlan(parser, holes, boomSequences);
                } else {
                    skip(parser);
                }
            }
            event = parser.next();
        }

        // Costruisco Pattern_IR a partire da BoomSeq (se presente)
        List<Pattern_IR> patterns = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : boomSequences.entrySet()) {
            String boomId = e.getKey();
            List<String> seqHoleIds = e.getValue();

            List<DrillHole_IR> orderedHoles = new ArrayList<>();
            for (String hid : seqHoleIds) {
                for (DrillHole_IR h : holes) {
                    if (hid.equals(h.getHoleId())) {
                        orderedHoles.add(h);
                        break;
                    }
                }
            }
            patterns.add(new Pattern_IR("BOOM-" + boomId, "Boom " + boomId, orderedHoles));
        }

        return new DrillPlan_IR(
                planId,
                planName,
                project,
                workOrder,
                holes,
                patterns
        );
    }

    // ----------------------------------------------------------
    // Lettura DrillPlan / Hole / BoomSeq
    // ----------------------------------------------------------

    private void readDrillPlan(XmlPullParser parser,
                               List<DrillHole_IR> holes,
                               Map<String, List<String>> boomSequences) throws Exception {

        parser.require(XmlPullParser.START_TAG, NS, parser.getName());

        while (!(parser.next() == XmlPullParser.END_TAG &&
                "DrillPlan".equals(localName(parser)))) {

            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String tag = localName(parser);

            switch (tag) {
                case "Hole":
                    holes.add(readHole(parser));
                    break;
                case "BoomSeq":
                    readBoomSeq(parser, boomSequences);
                    break;
                case "NumberOfHoles":
                    // possiamo leggerlo o ignorarlo (non critico)
                    readSimpleText(parser, "NumberOfHoles");
                    break;
                default:
                    skip(parser);
            }
        }
    }

    private DrillHole_IR readHole(XmlPullParser parser) throws Exception {
        parser.require(XmlPullParser.START_TAG, NS, parser.getName());

        String holeId = null;
        String holeName = null;
        Point3D_IR start = null;
        Point3D_IR end = null;

        while (!(parser.next() == XmlPullParser.END_TAG &&
                "Hole".equals(localName(parser)))) {

            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String tag = localName(parser);

            switch (tag) {
                case "HoleId":
                    holeId = readSimpleText(parser, "HoleId");
                    break;
                case "HoleName":
                    holeName = readSimpleText(parser, "HoleName");
                    break;
                case "StartPoint":
                    start = readPoint(parser);
                    break;
                case "EndPoint":
                    end = readPoint(parser);
                    break;
                default:
                    skip(parser);
            }
        }

        return new DrillHole_IR(
                holeId,
                holeName,
                null,   // patternId per ora non lo deduciamo qui
                start,
                end
        );
    }

    private void readBoomSeq(XmlPullParser parser,
                             Map<String, List<String>> boomSequences) throws Exception {

        parser.require(XmlPullParser.START_TAG, NS, parser.getName());
        String boomId = null;
        List<String> sequenceHoleIds = new ArrayList<>();

        while (!(parser.next() == XmlPullParser.END_TAG &&
                "BoomSeq".equals(localName(parser)))) {

            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String tag = localName(parser);

            if ("BoomId".equals(tag)) {
                boomId = readSimpleText(parser, "BoomId");
            } else if ("Sequence".equals(tag)) {
                String hid = readSequence(parser);
                if (hid != null) sequenceHoleIds.add(hid);
            } else {
                skip(parser);
            }
        }

        if (boomId != null) {
            boomSequences.put(boomId, sequenceHoleIds);
        }
    }

    private String readSequence(XmlPullParser parser) throws Exception {
        parser.require(XmlPullParser.START_TAG, NS, parser.getName());
        String holeId = null;

        while (!(parser.next() == XmlPullParser.END_TAG &&
                "Sequence".equals(localName(parser)))) {

            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String tag = localName(parser);

            if ("HoleId".equals(tag)) {
                holeId = readSimpleText(parser, "HoleId");
            } else {
                // SeqNum o altro -> ignoriamo, non critico
                skip(parser);
            }
        }
        return holeId;
    }

    // ----------------------------------------------------------
    // Lettura PointX/Y/Z
    // ----------------------------------------------------------

    private Point3D_IR readPoint(XmlPullParser parser) throws Exception {
        parser.require(XmlPullParser.START_TAG, NS, parser.getName());

        Double x = null, y = null, z = null;

        while (!(parser.next() == XmlPullParser.END_TAG &&
                ("StartPoint".equals(localName(parser)) || "EndPoint".equals(localName(parser))))) {

            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String tag = localName(parser);

            switch (tag) {
                case "PointX":
                    x = Double.parseDouble(readSimpleText(parser, parser.getName()));
                    break;
                case "PointY":
                    y = Double.parseDouble(readSimpleText(parser, parser.getName()));
                    break;
                case "PointZ":
                    z = Double.parseDouble(readSimpleText(parser, parser.getName()));
                    break;
                default:
                    skip(parser);
            }
        }

        return new Point3D_IR(x, y, z);
    }

    // ----------------------------------------------------------
    // Utility lettura/skip
    // ----------------------------------------------------------

    private String readSimpleText(XmlPullParser parser, String expectedLocalName) throws Exception {
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