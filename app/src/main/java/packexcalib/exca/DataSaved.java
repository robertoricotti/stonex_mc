package packexcalib.exca;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

import dxf.Arc;
import dxf.CanvasSegment;
import dxf.Circle;
import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Line;
import dxf.PNEZDPoint;
import dxf.Point3D;
import dxf.Polyline;
import dxf.Polyline_2D;
import dxf.Segment;
import gui.my_opengl.Point3DF;
import iredes.DrillHole_IR;
import iredes.DrillPlan_IR;
import iredes.NavData_IR;
import iredes.Pattern_IR;
import iredes.Point3D_Drill;
import iredes.Point3D_IR;
import iredes.RigStatus_IR;

public class DataSaved {
    public static double OffsetDegRoto;
    public static int ConnectionStatus;
    public static double L_RotoToBucket;
    public static double Offset_Engcon_Forward;
    public static double Offset_Engcon_Down;
    public static double DELTA_HDT_SMC;
    public static String SECONDO_S_CRS;
    public static int Drill_Text_Mode;
    public static double Raggio_Drill;
    public static int UTC_Offset;
    public static int Unit_Of_Measure;
    public static int Drill_Screen;
    public static int Drilling_Mode;
    public static boolean isDefiningAB = false;
    public static String alignAId = null;
    public static String alignBId = null;
    public static double ALLINEAMENTO_AB = 0;
    //Dragaggio
    public static double HighThreshold;
    public static double LowThreshold;
    public static double PassoGriglia;
    public static String DredgeFileName;
    public static int EnableMapping;
    public static String Drill_Antenna_Mounting = "BODY";
    public static String Drill_Mast_Position = "FORWARD";

    //settaggi idraulici
    public static String OUTPUT_HYDRO = "";
    public static int Interface_Type;
    public static double HYDRAULIC_WINDOW;
    public static double tolleranza_ZL;
    public static double tolleranza_ZR;
    public static double tolleranza_XY;
    public static double tolleranza_Slope;
    public static int Use_Blade_Pitch;
    public static double Mainfall_Distance;

    public static int minSpeedLeftUP;
    public static int maxSpeedLeftUP;
    public static int minSpeedLeftDW;
    public static int maxSpeedLeftDW;

    public static int minSpeedRightUP;
    public static int maxSpeedRightUP;
    public static int minSpeedRightDW;
    public static int maxSpeedRightDW;

    public static int minSpeedSS_A;
    public static int maxSpeedSS_A;
    public static int minSpeedSS_B;
    public static int maxSpeedSS_B;

    public static int CAT_Type;

    public static double Off_Incr_Step;

    public static int GAIN_LEFT;
    public static int GAIN_RIGHT;
    public static int HYDRAULIC_CONTROL_POINT_GRADER;//0=CENTER-RIGHT    1=CENTER-LEFT   2=LEFT-RIGHT
    public static int HYDRAULIC_CONTROL_POINT_DOZER;//0=CENTER-RIGHT    1=CENTER-LEFT

    public static int REVERSE_LEFT;
    public static int REVERSE_RIGHT;
    public static int REVERSE_SS;
    public static int OEM_REV_MAINFALL;
    public static int OEM_REV_UPDW;
    public static int OEM_REV_SS;



    public static int Wheel_Steer_Rev;
    public static int Wheel_Steer_Min;
    public static int Wheel_Steer_Med;
    public static int Wheel_Steer_Max;
    public static double Wheel_Steer_Range;
    public static double SteerWheel_Result;

    ///
    ///
    ///
    public static Point3D_Drill Selected_Point3D_Drill = null;
    public static int xyz_yxz;// 0= E N Q     ------    1=N E Q
    public static int coordOrder;
    public static String machineName;
    public static int lock3dRotation;
    public static int ckSchermo;
    public static int drwaMachieSchema;
    public static int screenOr;
    public static int boudrateCAN1;
    public static int boudrateCAN2;
    public static int lrBucket;

    public static int lrStick;

    public static int lrBoom1;

    public static int lrBoom2;

    public static int lrFrame;

    public static int lrTilt;
    public static int lrTool;


    public static int lrRotary;

    public static double Rotary_Diam;
    public static double drill_Bit_Len;
    public static double drill_Bit_Width;
    public static double drill_First_Rod_Len;
    public static double drill_Rod_Len;
    public static int numeroAste;
    public static double Tool_Delta_X;
    public static double Tool_Delta_Y;
    public static double Tool_Delta_Z;
    public static double offset_Tool_Roll;
    public static double offset_Tool_Pitch;
    public static double offset_Boom_Tool;
    public static double offsetRoll;

    public static double offsetPitch;

    public static double offsetBoom1;

    public static double offsetBoom2;

    public static double offsetStick;

    public static double offsetBucket;

    public static double offsetFlat;

    public static double offsetTilt;//offset angolo dxsx

    public static double offsetTiltDeltaAngle; // offset tilt delta

    public static double offsetH;

    public static double offsetZH;

    public static double offsetSwingExca;

    public static double offsetHDT;

    public static double offsetDogBone;

    public static double L_Pitch;

    public static double L_Roll;

    public static double L_Boom1;

    public static double L_Boom2;

    public static double L_Stick;


    public static double L1;

    public static double L2;

    public static double L3;

    public static double L4;

    public static double L_Bucket;

    public static double W_Bucket;
    public static double piccolaBucket;

    public static double miniPitch_L;

    public static double W_Blade_TOT;
    public static double W_Blade_LEFT;
    public static double W_Blade_RIGHT;
    public static double altezzaPali;
    public static double altezzaLama;

    public static double slopeY;

    public static double slopeX;

    public static double LSV;

    public static double LSH;

    public static double flat;

    public static double offsetLaserZH;

    public static double offsetLaserZR;

    public static double deadbandH;

    public static double deadbandFlatAngle;


    public static int bucketEdge;

    public static int portView;
    public static double deltaX;
    public static double deltaY;
    public static double deltaZ;
    public static double deltaGPS2;

    public static double L_Tilt;
    public static double offsetDegWTilt;


    public static double usuraLamaSX;
    public static double usuraLamaCX;
    public static double usuraLamaDX;


    public static int enOUT;//abilita USCITA 12 VOUT

    public static int projectionFlag;


    public static int radioMode;
    public static int reqSpeed;
    public static int gpsType;
    public static int my_comPort;//GPS com
    public static String macaddress;//GPS MAC
    public static String deviceName;//GPS MAC
    public static String S_macAddress_CAN;//CAN MAC


    public static double offsetBubble_X;
    public static double offsetBubble_Y;
    public static double bubble_DB;
    public static int useTiltEbubble;


    public static int isCanOpen;
    public static int Dozer_UpsideDown;

    public static double scale_Factor;
    public static double scale_Factor3D;

    public static double scale_FactorVista1D;
    public static double scale_FactorVista2D;


    public static int shortcutIndex;
    public static double start2DX;
    public static double start2DY;
    public static double start2DZ;
    public static double offsetmDeltaX;
    public static double offsetmDeltaY;
    public static String language;
    public static int colorMode;
    public static int laserOn;
    public static double monumentSet;
    public static double monumentRelease;
    public static int profileSelected;
    public static double[][] puntiProfilo;
    public static boolean gpsOk;
    public static Coordinate[] puntiProgetto;
    public static String[] idPunti;

    public static double Max_CQ3D;

    public static int damp_Fr;
    public static int damp_B1;
    public static int damp_B2;
    public static int damp_St;
    public static int damp_Bk;
    public static int damp_Tl;

    public static int hasQuick;
    public static double Drill_tolleranza_Axis = 0.05; // 5 cm (esempio)
    public static double Drill_tolleranza_Z = 0.03; // 3 cm (esempio)
    public static double Drill_tolleranza_XY = 0.03;
    public static double Drill_tolleranza_Angolo = 0.5;
    public static double Drill_tolleranza_HDT = 0.5;


    //IREDES
    // ---- DRILL PLAN ----
    public static DrillPlan_IR drillPlan;

    public static List<DrillHole_IR> holes = new ArrayList<>();
    public static List<DrillHole_IR> filteredHoles = new ArrayList<>();

    public static List<Pattern_IR> patterns = new ArrayList<>();


    // ---- NAVIGATION DATA ----
    public static NavData_IR navData;
    public static List<NavData_IR> navHistory = new ArrayList<>();


    // ---- RIG STATUS ----
    public static RigStatus_IR rigStatus;


    // ---- RAW POINT CLOUD / ASSIST ----
    public static List<Point3D_IR> rawPoints = new ArrayList<>();
    public static List<Point3D_IR> filteredRawPoints = new ArrayList<>();


    //MC
    public static int Exca_Antenna_Mounting;
    public static int useLowResolution;
    public static String projectTAG;
    public static String progettoSelected;
    public static String progettoSelected_POLY;
    public static String progettoSelected_POINT;
    public static double offset_Z_antenna;
    public static int isWL;
    public static int Extra_Heading;
    public static float myBrightness = 1.0f;
    public static double larghezzaBoom = 0.4;
    public static double larghezzaFrame = 2.2;
    public static double lunghezzaFrame = 3.5;
    public static String S_CRS;
    public static List<Layer> dxfLayers_DTM = new ArrayList<>();
    public static List<Layer> dxfLayers_POLY = new ArrayList<>();
    public static List<Layer> dxfLayers_POINT = new ArrayList<>();
    public static List<Face3D> dxfFaces = new ArrayList<>();
    public static List<Face3D> dxfFacesGL_2D = new ArrayList<>();
    public static List<Face3D> filteredFaces = new ArrayList<>();
    public static List<Face3D> filteredFacesGL_2D = new ArrayList<>();
    public static List<Polyline_2D> polylines_2D = new ArrayList<>();
    public static List<Polyline> polylines = new ArrayList<>();
    public static List<Polyline> polylinesGL_2D = new ArrayList<>();
    public static List<Line> lines_2D = new ArrayList<>();
    public static Polyline selectedPoly;
    public static Polyline selectedPoly_OFFSET;
    public static List<Polyline> filteredPolylines = new ArrayList<>();
    public static List<Polyline> filteredPolylinesGL_2D = new ArrayList<>();
    public static List<Point3D> points = new ArrayList<>();
    public static List<Point3D_Drill> drill_points = new ArrayList<>();
    //public static List<Point3D_Drill> filtered_drill_points = new ArrayList<>();
    public static List<Point3D> filteredPoints = new ArrayList<>();
    public static List<Arc> arcs = new ArrayList<>();
    public static List<Circle> circles = new ArrayList<>();
    public static List<DxfText> dxfTexts = new ArrayList<>();
    public static List<DxfText> filteredDxfTexts = new ArrayList<>();
    public static int Colore_Surf;
    public static int Triangoli_Surf;
    public static int Punti_Surf;
    public static int Poly_Surf;
    public static int ShowText;
    public static int ShowUtils;
    public static int ShowJson;
    public static double RaggioDXF;
    public static List<Point3D> stakedPoints = new ArrayList<>();
    public static int isAutoSnap;
    public static int temaSoftware;
    public static Point3D nearestPoint;
    public static Segment nearestSegment;
    public static boolean pickPP;

    public static int lockUnlock;
    public static int typeView;

    public static double offsetYaw;
    public static boolean isLowerEdge;//detect the lowest bucket edge
    public static int useYawFrame;
    public static int driftStep;
    public static int driftSign;
    public static String lastProjectName;
    public static String lastProjectNamePOLY;
    public static String lastProjectNamePOINT;
    public static double demoNORD;
    public static double demoEAST;
    public static double demoZ;
    public static double HEADING;
    public static double gradientDB;
    public static double distBetween;
    public static double distG1_G2;
    /// /
    public static int leftSensorType;

    public static int rightSensorType;
    public static int useQuickSwitch;
    public static int priorityNet;
    public static String wifiSSID;
    public static int isTiltRotator;
    public static double line_Offset;
    public static int revTiltRot;
    public static int isExtensionBoom;
    public static int showAlign;

    /// ////////canvas data
    public static List<CanvasSegment> canvasSegment = new ArrayList<>();

    public static double larghezza_Carro;
    public static double lunghrzza_Carro;
    public static double larghezza_Frame;
    public static double lunghezza_Frame;
    public static double larghezza_Braccio;

    public static double[] glL_AnchorView;
    public static float[][] GL_Bucket_Coord = new float[12][3];

    public static Point3DF[] GL_BENNA = new Point3DF[100];
    public static float[] GL_ATTACCO = new float[100];
    public static Point3DF[] GL_STICK = new Point3DF[100];
    public static Point3DF[] GL_BOOM1 = new Point3DF[100];
    public static Point3DF[] GL_BOOM1_2 = new Point3DF[100];
    public static Point3DF[] GL_FRAME_BASE = new Point3DF[100];
    public static Point3DF[] GL_LAMA = new Point3DF[100];
    public static Point3DF[] GL_WHEEL = new Point3DF[100];


    /**
     * PNEZD
     */
    public static List<PNEZDPoint> pnezdPoints = new ArrayList<>();
    public static String PNEZDPath = "/storage/emulated/0/StonexMC_V4/Projects/Ex/Ex.csv";


    //to test canvas
    public static double cutWorldX_1;
    public static double cutWorldY_1;

    public static double cutWorldX_2;
    public static double cutWorldY_2;

}
