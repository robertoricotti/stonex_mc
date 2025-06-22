package packexcalib.gnss;

import static services.UpdateValuesService.resultWgs;
import static services.UpdateValuesService.utmToWgs;

import org.locationtech.proj4j.ProjCoordinate;



public class UTM2Deg {

    double latitude, longitude, quota;


    public UTM2Deg(int Zone, char Letter, double Easting, double Northing, double Z, String crs) {

        switch (crs) {
            case "UTM":
                quota = Z;
                double Hem;
                if (Letter > 'M')
                    Hem = 'N';
                else
                    Hem = 'S';
                double north;
                if (Hem == 'S')
                    north = Northing - 10000000;
                else
                    north = Northing;
                latitude = (north / 6366197.724 / 0.9996 + (1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) - 0.006739496742 * Math.sin(north / 6366197.724 / 0.9996) * Math.cos(north / 6366197.724 / 0.9996) * (Math.atan(Math.cos(Math.atan((Math.exp((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3)) - Math.exp(-(Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3))) / 2 / Math.cos((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) + north / 6366197.724 / 0.9996))) * Math.tan((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) + north / 6366197.724 / 0.9996)) - north / 6366197.724 / 0.9996) * 3 / 2) * (Math.atan(Math.cos(Math.atan((Math.exp((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3)) - Math.exp(-(Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3))) / 2 / Math.cos((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) + north / 6366197.724 / 0.9996))) * Math.tan((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) + north / 6366197.724 / 0.9996)) - north / 6366197.724 / 0.9996)) * 180 / Math.PI;

                longitude = Math.atan((Math.exp((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3)) - Math.exp(-(Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3))) / 2 / Math.cos((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) + north / 6366197.724 / 0.9996)) * 180 / Math.PI + Zone * 6 - 183;
                break;
            case "21500":
                //TODO 21500
                utmToWgs.transform(new ProjCoordinate(Easting, Northing, Z), resultWgs);
                longitude=resultWgs.x;
                latitude=resultWgs.y;
                quota=resultWgs.z;
                break;

            case "31300":
                //TODO 31300
                utmToWgs.transform(new ProjCoordinate(Easting, Northing, Z), resultWgs);
                longitude=resultWgs.x;
                latitude=resultWgs.y;
                quota=resultWgs.z;
                break;
            case "31370":
                //TODO 31370
                utmToWgs.transform(new ProjCoordinate(Easting, Northing, Z), resultWgs);
                longitude=resultWgs.x;
                latitude=resultWgs.y;
                quota=resultWgs.z;
                break;
            case "3447":
                //TODO 3447
                utmToWgs.transform(new ProjCoordinate(Easting, Northing, Z), resultWgs);
                longitude=resultWgs.x;
                latitude=resultWgs.y;
                quota=resultWgs.z;
                break;
            case "3812":
                //TODO 3812
                utmToWgs.transform(new ProjCoordinate(Easting, Northing, Z), resultWgs);
                longitude=resultWgs.x;
                latitude=resultWgs.y;
                quota=resultWgs.z;
                break;
            case "23095":
                //TODO 23095
                utmToWgs.transform(new ProjCoordinate(Easting, Northing, Z), resultWgs);
                longitude=resultWgs.x;
                latitude=resultWgs.y;
                quota=resultWgs.z;
                break;
            case "28992":
                //TODO 28992
                utmToWgs.transform(new ProjCoordinate(Easting, Northing, Z), resultWgs);
                longitude=resultWgs.x;
                latitude=resultWgs.y;
                quota=resultWgs.z;
                break;
            case "28991":
                //TODO 28991
                utmToWgs.transform(new ProjCoordinate(Easting, Northing, Z), resultWgs);
                longitude=resultWgs.x;
                latitude=resultWgs.y;
                quota=resultWgs.z;
                break;
            case "2100":
                //TODO 2100
                utmToWgs.transform(new ProjCoordinate(Easting, Northing, Z), resultWgs);
                longitude=resultWgs.x;
                latitude=resultWgs.y;
                quota=resultWgs.z;
                break;
            default:
                quota = Z;
                double Hem1;
                if (Letter > 'M')
                    Hem1 = 'N';
                else
                    Hem1 = 'S';
                double north1;
                if (Hem1 == 'S')
                    north1 = Northing - 10000000;
                else
                    north1 = Northing;
                latitude = (north1 / 6366197.724 / 0.9996 + (1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) - 0.006739496742 * Math.sin(north1 / 6366197.724 / 0.9996) * Math.cos(north1 / 6366197.724 / 0.9996) * (Math.atan(Math.cos(Math.atan((Math.exp((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) / 3)) - Math.exp(-(Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) / 3))) / 2 / Math.cos((north1 - 0.9996 * 6399593.625 * (north1 / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) + north1 / 6366197.724 / 0.9996))) * Math.tan((north1 - 0.9996 * 6399593.625 * (north1 / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) + north1 / 6366197.724 / 0.9996)) - north1 / 6366197.724 / 0.9996) * 3 / 2) * (Math.atan(Math.cos(Math.atan((Math.exp((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) / 3)) - Math.exp(-(Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) / 3))) / 2 / Math.cos((north1 - 0.9996 * 6399593.625 * (north1 / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) + north1 / 6366197.724 / 0.9996))) * Math.tan((north1 - 0.9996 * 6399593.625 * (north1 / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) + north1 / 6366197.724 / 0.9996)) - north1 / 6366197.724 / 0.9996)) * 180 / Math.PI;

                longitude = Math.atan((Math.exp((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) / 3)) - Math.exp(-(Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) / 3))) / 2 / Math.cos((north1 - 0.9996 * 6399593.625 * (north1 / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north1 / 6366197.724 / 0.9996 + Math.sin(2 * north1 / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north1 / 6366197.724 / 0.9996) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north1 / 6366197.724 / 0.9996), 2)) + north1 / 6366197.724 / 0.9996)) * 180 / Math.PI + Zone * 6 - 183;
                break;

        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getQuota() {
        return quota;
    }
}

