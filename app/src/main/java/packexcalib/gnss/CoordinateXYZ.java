package packexcalib.gnss;

public class CoordinateXYZ {
    double Easting, Northing, Quota;
    char Letter;
    int Zone;

    public CoordinateXYZ(double E, double N, double Q, int zone, char Letter) {
        this.Easting = E;
        this.Northing = N;
        this.Quota = Q;
        this.Zone = zone;
        this.Letter = Letter;
    }

    public double getEasting() {
        return Easting;
    }

    public double getNorthing() {
        return Northing;
    }

    public double getQuota() {
        return Quota;
    }

    public int getZone() {
        return Zone;
    }

    public char getLetter() {
        return Letter;
    }
}
