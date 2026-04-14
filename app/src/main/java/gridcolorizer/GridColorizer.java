package gridcolorizer;

/*
 GridColorizer.java
 Utility per rasterizzare superfici 3D (Face3D -> triangoli), calcolare zProject al centro di ogni
 cella 1x1 e mantenere lo stato della griglia (minDepth raggiunta, colore) con salvataggio/caricamento
 file binario molto efficiente.

 Uso principale:
  - buildFromFaces(...)
  - rasterizeGrid()
  - updateToolPosition(x,y,z)  // aggiorna la cella corrispondente quando lo strumento passa
  - saveToFile(context, filename)
  - loadFromFile(context, filename)

 Nota: questo file assume l'esistenza della tua classe Point3D e Face3D come descritte nella conversazione.
*/


import android.content.Context;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dxf.Face3D;
import dxf.Point3D;

public class GridColorizer implements Serializable {
    private static final long serialVersionUID = 1L;

    // parametri griglia
    private double minX, minY; // origine griglia
    private double cellSize = 1.0; // 1m
    private int nx, ny; // dimensioni

    // dati della superficie (triangoli preprocessati)
    private transient List<Triangle3D> triangles = new ArrayList<>();

    // z progetto per cella (NaN se non coperta)
    private double[] zProject; // length = nx*ny

    // stato runtime: quota minima raggiunta dal tool (inizialmente +INF) e colore ARGB
    private double[] minDepth; // quota minima raggiunta (zTool min). Use +INF if none
    private int[] color; // ARGB per cella

    // Soglie (metri)
    private double highThreshold = 0.30; // inizio colorazione (es. 30cm sopra la quota progetto)
    private double deadband = 0.05; // +/- intorno alla quota considerato verde
    private double lowThreshold = 0.50; // profondità massima prima che sia tutto rosso

    // palette
    private final int COLOR_BLUE = 0xFF0000FF;
    private final int COLOR_GREEN = 0xFF00FF00;
    private final int COLOR_RED = 0xFFFF0000;

    // costruzione
    public GridColorizer(double minX, double minY, int nx, int ny, double cellSize) {
        this.minX = minX;
        this.minY = minY;
        this.nx = nx;
        this.ny = ny;
        this.cellSize = cellSize;
        allocGridArrays();
    }

    private void allocGridArrays() {
        int N = nx * ny;
        zProject = new double[N];
        minDepth = new double[N];
        color = new int[N];
        for (int i = 0; i < N; i++) {
            zProject[i] = Double.NaN; // non definito
            minDepth[i] = Double.POSITIVE_INFINITY; // nessun valore ancora
            color[i] = COLOR_GREEN; // default
        }
    }

    // thresholds setters
    public void setThresholds(double highThreshold, double deadband, double lowThreshold) {
        this.highThreshold = highThreshold;
        this.deadband = deadband;
        this.lowThreshold = lowThreshold;
    }

    // --- Preprocess Face3D -> triangoli ---
    public void buildTrianglesFromFaces(List<Face3D> faces) {
        triangles = new ArrayList<>();
        for (Face3D f : faces) {
            Point3D p1 = f.getP1();
            Point3D p2 = f.getP2();
            Point3D p3 = f.getP3();
            Point3D p4 = f.getP4();
            boolean isTriangle = (p4 == null) || (almostEqual(p4, p3));
            triangles.add(new Triangle3D(p1, p2, p3));
            if (!isTriangle) {
                // split quad (p1,p3,p4)
                triangles.add(new Triangle3D(p1, p3, p4));
            }
        }
    }

    private boolean almostEqual(Point3D a, Point3D b) {
        if (a == null || b == null) return false;
        double eps = 1e-9;
        return Math.abs(a.getX() - b.getX()) < eps && Math.abs(a.getY() - b.getY()) < eps && Math.abs(a.getZ() - b.getZ()) < eps;
    }

    // --- Rasterizzazione efficiente: per ogni triangolo aggiorni i centri delle celle che interseca ---
    public void rasterizeGrid(boolean chooseTopmost) {
        // reset zProject
        int N = nx * ny;
        for (int i = 0; i < N; i++) zProject[i] = Double.NaN;

        for (Triangle3D t : triangles) {
            int ixMin = (int) Math.floor((t.minX - minX) / cellSize);
            int ixMax = (int) Math.floor((t.maxX - minX) / cellSize);
            int iyMin = (int) Math.floor((t.minY - minY) / cellSize);
            int iyMax = (int) Math.floor((t.maxY - minY) / cellSize);
            if (ixMin > nx - 1 || ixMax < 0 || iyMin > ny - 1 || iyMax < 0) continue; // fuori
            ixMin = clamp(ixMin, 0, nx - 1);
            ixMax = clamp(ixMax, 0, nx - 1);
            iyMin = clamp(iyMin, 0, ny - 1);
            iyMax = clamp(iyMax, 0, ny - 1);

            for (int ix = ixMin; ix <= ixMax; ix++) {
                double cx = minX + (ix + 0.5) * cellSize;
                for (int iy = iyMin; iy <= iyMax; iy++) {
                    double cy = minY + (iy + 0.5) * cellSize;
                    if (!t.containsXY(cx, cy)) continue;
                    double z = t.interpolateZ(cx, cy);
                    int idx = index(ix, iy);
                    if (Double.isNaN(zProject[idx])) zProject[idx] = z;
                    else {
                        if (chooseTopmost) {
                            if (z > zProject[idx]) zProject[idx] = z;
                        } else {
                            if (z < zProject[idx]) zProject[idx] = z;
                        }
                    }
                }
            }
        }

        // inizializza colori in base allo zProject
        for (int ix = 0; ix < nx; ix++) {
            for (int iy = 0; iy < ny; iy++) {
                int idx = index(ix, iy);
                double zp = zProject[idx];
                if (Double.isNaN(zp)) {
                    color[idx] = COLOR_GREEN; // area non coperta -> default (o potresti flaggare come "no data")
                } else {
                    // initial color: consider tool già a quota progetto
                    color[idx] = COLOR_GREEN;
                    // minDepth stays +INF until tool visits
                    minDepth[idx] = Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    // Aggiorna la cella corrispondente alla posizione del tool:
// se zTool è più basso del minimo registrato (o se non c'è ancora), aggiorna minDepth e colore
    public void updateToolPosition(double xTool, double yTool, double zTool) {
        int cellX = (int) Math.floor((xTool - minX) / cellSize);
        int cellY = (int) Math.floor((yTool - minY) / cellSize);
        if (cellX < 0 || cellX >= nx || cellY < 0 || cellY >= ny) return;

        int idx = index(cellX, cellY);

        // se non c'è ancora valore registrato oppure zTool è più basso, aggiorna
        if (Double.isNaN(minDepth[idx]) || zTool < minDepth[idx]) {
            minDepth[idx] = zTool;
            double zp = zProject[idx];
            if (!Double.isNaN(zp)) {
                color[idx] = computeColorFor(zp, minDepth[idx]);
            }
        }
    }


    // compute color dato zProject (quota progetto) e zTool (quota minima raggiunta)
    private int computeColorFor(double zProject, double zTool) {
        double delta = zTool - zProject; // positive -> tool above project; negative -> below project

        // fuori zone
        if (delta >= highThreshold) {
            return COLOR_BLUE; // molto sopra
        }
        if (Math.abs(delta) <= deadband) {
            return COLOR_GREEN; // entro deadband
        }

        if (delta > deadband && delta < highThreshold) {
            // zone: deadband..highThreshold -> interp green -> blue (quando ci si allontana sopra)
            double t = (delta - deadband) / (highThreshold - deadband); // 0..1
            return lerpColor(COLOR_GREEN, COLOR_BLUE, t);
        }

        // delta < -deadband -> tool sotto la quota di progetto
        double depthBelow = -delta; // positive value
        if (depthBelow >= lowThreshold) {
            return COLOR_RED;
        }
        // interp green -> red tra deadband e lowThreshold
        if (depthBelow > deadband && depthBelow < lowThreshold) {
            double t = (depthBelow - deadband) / (lowThreshold - deadband); // 0..1
            return lerpColor(COLOR_GREEN, COLOR_RED, t);
        }

        // fallback
        return COLOR_GREEN;
    }

    // linear interpolation between two ARGB colors (no premultiplied alpha)
    private int lerpColor(int c1, int c2, double t) {
        if (t <= 0) return c1;
        if (t >= 1) return c2;
        int a1 = (c1 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;
        int a = (int) (a1 + t * (a2 - a1));
        int r = (int) (r1 + t * (r2 - r1));
        int g = (int) (g1 + t * (g2 - g1));
        int b = (int) (b1 + t * (b2 - b1));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // --- Salvataggio/Caricamento (binary, efficiente) ---
    // File format (binary): magic "GCLR" (4 bytes), version int, minX,minY,cellSize (double x3), nx,ny (int x2), thresholds (double x3)
    // poi per ogni cella: double zProject, double minDepth, int color

    public void saveToFile(Context ctx, String filename) throws IOException {
        File f = new File(ctx.getFilesDir(), filename);
        try (FileOutputStream fos = new FileOutputStream(f);
             DataOutputStream dos = new DataOutputStream(fos)) {
            dos.writeBytes("GCLR");
            dos.writeInt(1); // version
            dos.writeDouble(minX);
            dos.writeDouble(minY);
            dos.writeDouble(cellSize);
            dos.writeInt(nx);
            dos.writeInt(ny);
            dos.writeDouble(highThreshold);
            dos.writeDouble(deadband);
            dos.writeDouble(lowThreshold);
            int N = nx * ny;
            for (int i = 0; i < N; i++) {
                dos.writeDouble(zProject[i]);
                dos.writeDouble(minDepth[i]);
                dos.writeInt(color[i]);
            }
            dos.flush();
        }
    }

    public boolean loadFromFile(Context ctx, String filename) throws IOException {
        File f = new File(ctx.getFilesDir(), filename);
        if (!f.exists()) return false;
        try (FileInputStream fis = new FileInputStream(f);
             DataInputStream dis = new DataInputStream(fis)) {
            byte[] magic = new byte[3];
            dis.readFully(magic);
            String mag = new String(magic);
            if (!"GCL".equals(mag) && !"GCLR".equals(mag)) {
                // not our file
                return false;
            }
            int version = dis.readInt();
            double fileMinX = dis.readDouble();
            double fileMinY = dis.readDouble();
            double fileCellSize = dis.readDouble();
            int fileNx = dis.readInt();
            int fileNy = dis.readInt();
            double hT = dis.readDouble();
            double dB = dis.readDouble();
            double lT = dis.readDouble();
            // if grid dimensions or origin differ, we could decide to reject or adopt
            if (fileNx != nx || fileNy != ny || Math.abs(fileMinX - minX) > 1e-9 || Math.abs(fileMinY - minY) > 1e-9 || Math.abs(fileCellSize - cellSize) > 1e-9) {
                // In this implementation rifiutiamo (potresti voler adattare la logica per ricampionare)
                return false;
            }
            this.highThreshold = hT;
            this.deadband = dB;
            this.lowThreshold = lT;
            int N = nx * ny;
            for (int i = 0; i < N; i++) {
                zProject[i] = dis.readDouble();
                minDepth[i] = dis.readDouble();
                color[i] = dis.readInt();
            }
            return true;
        }
    }

    // --- Utilities ---
    private int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }

    private int index(int ix, int iy) {
        return ix * ny + iy; // column-major
    }

    // getter per disegno OpenGL (fornisce colore / centro / stato)
    public int getColorAtCell(int ix, int iy) {
        if (ix < 0 || ix >= nx || iy < 0 || iy >= ny) return COLOR_GREEN;
        return color[index(ix, iy)];
    }

    public double getZProjectAtCell(int ix, int iy) {
        return zProject[index(ix, iy)];
    }

    public double getMinDepthAtCell(int ix, int iy) {
        double v = minDepth[index(ix, iy)];
        return (Double.isInfinite(v) ? Double.NaN : v);
    }

    public double getCellCenterX(int ix) {
        return minX + (ix + 0.5) * cellSize;
    }

    public double getCellCenterY(int iy) {
        return minY + (iy + 0.5) * cellSize;
    }

    public int getNx() {
        return nx;
    }

    public int getNy() {
        return ny;
    }

    // --- Inner helper classes: Triangle3D, Point3D, Face3D placeholders ---

    public static class Triangle3D {
        public final Point3D a, b, c;
        public final double minX, maxX, minY, maxY;
        private final double denom;

        public Triangle3D(Point3D a, Point3D b, Point3D c) {
            this.a = a;
            this.b = b;
            this.c = c;
            minX = Math.min(a.getX(), Math.min(b.getX(), c.getX()));
            maxX = Math.max(a.getX(), Math.max(b.getX(), c.getX()));
            minY = Math.min(a.getY(), Math.min(b.getY(), c.getY()));
            maxY = Math.max(a.getY(), Math.max(b.getY(), c.getY()));
            denom = ((b.getY() - c.getY()) * (a.getX() - c.getX()) + (c.getX() - b.getX()) * (a.getY() - c.getY()));
        }

        public boolean containsXY(double x, double y) {
            if (x < minX || x > maxX || y < minY || y > maxY) return false;
            if (Math.abs(denom) < 1e-12) return false;
            double alpha = ((b.getY() - c.getY()) * (x - c.getX()) + (c.getX() - b.getX()) * (y - c.getY())) / denom;
            double beta = ((c.getY() - a.getY()) * (x - c.getX()) + (a.getX() - c.getX()) * (y - c.getY())) / denom;
            double gamma = 1.0 - alpha - beta;
            double eps = -1e-9;
            return alpha >= eps && beta >= eps && gamma >= eps;
        }

        public double interpolateZ(double x, double y) {
            if (Math.abs(denom) < 1e-12) return (a.getZ() + b.getZ() + c.getZ()) / 3.0;
            double alpha = ((b.getY() - c.getY()) * (x - c.getX()) + (c.getX() - b.getX()) * (y - c.getY())) / denom;
            double beta = ((c.getY() - a.getY()) * (x - c.getX()) + (a.getX() - c.getX()) * (y - c.getY())) / denom;
            double gamma = 1.0 - alpha - beta;
            return alpha * a.getZ() + beta * b.getZ() + gamma * c.getZ();
        }
    }

    // Esporta il contenuto della griglia in CSV leggibile
    public void exportToCSV(File csvFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            // intestazione
            pw.println("cellX,cellY,zProject,minDepth,colorARGB");
            for (int y = 0; y < ny; y++) {
                for (int x = 0; x < nx; x++) {
                    int idx = index(x, y);
                    pw.printf(Locale.US, "%d,%d,%.3f,%.3f,%d%n",
                            x, y,
                            zProject[idx],
                            minDepth[idx],
                            color[idx]);
                }
            }
        }
    }

    // Importa un CSV e aggiorna una griglia esistente
    // NB: la griglia deve avere stessa nx,ny altrimenti errore
    public void importFromCSV(File csvFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine(); // salta intestazione
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int idx = index(x, y);
                zProject[idx] = Double.parseDouble(parts[2].trim());
                minDepth[idx] = Double.parseDouble(parts[3].trim());
                color[idx] = Integer.parseInt(parts[4].trim());
            }
        }
    }


}

