package packexcalib.gnss;


import org.locationtech.proj4j.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.Locale;

public final class SpLocalization implements LocalizationModel {

    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static final CoordinateTransformFactory CT_FACTORY = new CoordinateTransformFactory();

    private final CoordinateTransform geoToProj;
    private final boolean use4;
    private final double Ca, Ck, Orgx, Orgy, x0, y0;
    private final boolean useHF;
    private final double a0,a1,a2,a3,a4,a5;

    private final ThreadLocal<ProjCoordinate> tlGeo  = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    private SpLocalization(CoordinateTransform geoToProj,
                           boolean use4,double Ca,double Ck,double Orgx,double Orgy,double x0,double y0,
                           boolean useHF,double a0,double a1,double a2,double a3,double a4,double a5) {
        this.geoToProj=geoToProj;
        this.use4=use4; this.Ca=Ca; this.Ck=Ck;
        this.Orgx=Orgx; this.Orgy=Orgy; this.x0=x0; this.y0=y0;
        this.useHF=useHF; this.a0=a0; this.a1=a1; this.a2=a2; this.a3=a3; this.a4=a4; this.a5=a5;
    }

    // ------------------------------------------------------------------
    // Lettura file .SP e costruzione automatica proiezione
    // ------------------------------------------------------------------
    public static SpLocalization fromSpFile(File f) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(f);
        Element head = doc.getDocumentElement();

        // --- Project parameter ---
        Element proj = (Element) head.getElementsByTagName("CoordinateSystem_ProjectParameter").item(0);
        double centralMeridianRad = d(txt(proj,"CentralMeridian","0"));
        double centralMeridianDeg = Math.toDegrees(centralMeridianRad);
        double k0 = d(txt(proj,"TK","1"));
        double x0 = d(txt(proj,"Tx","0"));
        double y0 = d(txt(proj,"Ty","0"));
        int type = (int) d(txt(proj,"Type","0"));

        // decide automaticamente la proiezione
        double lon0 = centralMeridianDeg;
        if (type >= 220 && type <= 230)
            lon0 = (type - 218) * 6 - 3;  // 220→9°, 221→15°, 222→21°, ecc.

        String projDef = String.format(Locale.US,
                "+proj=tmerc +lat_0=0 +lon_0=%.9f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=WGS84 +units=m +no_defs",
                lon0, k0, y0, x0);

        CoordinateReferenceSystem wgs84 =
                CRS_FACTORY.createFromParameters("WGS84","+proj=longlat +datum=WGS84 +no_defs");
        CoordinateReferenceSystem projected =
                CRS_FACTORY.createFromParameters("LOCALPROJ",projDef);
        CoordinateTransform geoToProj = CT_FACTORY.createTransform(wgs84, projected);

        // --- Four parameter ---
        Element four = (Element) head.getElementsByTagName("CoordinateSystem_FourParameter").item(0);
        boolean use4=isTrue(txt(four,"Use","0"));
        double Cx=d(txt(four,"Cx","0")), Cy=d(txt(four,"Cy","0")), Ca=d(txt(four,"Ca","0")),
                Ck=d(txt(four,"Ck","1")), Orgx=d(txt(four,"Orgx","0")), Orgy=d(txt(four,"Orgy","0"));
        double locx0 = Orgx + Cx;   // ✅ FIX: segno corretto (toglie +20.700 m sul Nord)
        double locy0 = Orgy + Cy;   // equivalente a prima, lasciamolo chiaro

        // --- Height fitting ---
        Element hf = (Element) head.getElementsByTagName("CoordinateSystem_HeightFittingParameter").item(0);
        boolean useHF=isTrue(txt(hf,"Use","0"));
        double a0=d(txt(hf,"a0","0")), a1=d(txt(hf,"a1","0")), a2=d(txt(hf,"a2","0")),
                a3=d(txt(hf,"a3","0")), a4=d(txt(hf,"a4","0")), a5=d(txt(hf,"a5","0")),
                fitx0=d(txt(hf,"x0","0")), fity0=d(txt(hf,"y0","0"));

        return new SpLocalization(geoToProj,use4,Ca,Ck,Orgx,Orgy,locx0,locy0,useHF,a0,a1,a2,a3,a4,a5);
    }

    // helper
    private static double d(String s){return (s==null||s.isEmpty())?0:Double.parseDouble(s);}
    private static String txt(Element p,String tag,String def){
        if(p==null)return def; NodeList nl=p.getElementsByTagName(tag);
        if(nl==null||nl.getLength()==0)return def;
        String s=nl.item(0).getTextContent();
        return (s==null||s.isEmpty())?def:s.trim();
    }
    private static boolean isTrue(String s){return s!=null&&("1".equals(s.trim())||"true".equalsIgnoreCase(s.trim()));}

    @Override
    public void toLocalFast(double lat, double lon, double h, double[] out){
        ProjCoordinate geo = tlGeo.get(), proj = tlProj.get();
        geo.x = lon; geo.y = lat;
        geoToProj.transform(geo, proj);
        double E = proj.x, N = proj.y;

        double X = E, Y = N; // placeholder

        if (use4) {
            double dN = N - Orgx;
            double dE = E - Orgy;
            double cosA = Math.cos(Ca), sinA = Math.sin(Ca);

            // componente ruotata nel frame locale (Nord/Est)
            double Np = dN * cosA - dE * sinA;  // Nord ruotato
            double Ep = dN * sinA + dE * cosA;  // Est ruotato

            // pivot corretti (dopo FIX nel parser): x0 = Orgx + Cx, y0 = Orgy + Cy
            double nord = x0 + Ck * Np;
            double est  = y0 + Ck * Ep;

            X = nord;
            Y = est;
        }

        double Z = h;
        if (useHF) {
            // il tuo file usa il fit in Z come correzione da sottrarre all’ellissoidale
            double dx = X - x0;  // usa locali: Nord-X rispetto a pivot (x0)
            double dy = Y - y0;  // locali: Est-Y rispetto a pivot (y0)
            double dh = a0 + a1*dx + a2*dy + a3*dx*dx + a4*dy*dy + a5*dx*dy;
            Z = h - dh;
        }

        // ⚠️ ordine richiesto: out = (Est, Nord, Quota)
        out[0] = Y;  // Est
        out[1] = X;  // Nord
        out[2] = Z;  // Quota
    }

}
