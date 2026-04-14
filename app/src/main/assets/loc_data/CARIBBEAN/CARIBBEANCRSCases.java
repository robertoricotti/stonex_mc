public class CARIBBEANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CARIBBEAN__FRENCH_ANTILLES_WEST_OF_60_W__RGAF09__UTM_ZONE_20N__5490.SP":
                MyData.push("SECONDO_S_CRS", "5490");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CARIBBEAN__FRENCH_ANTILLES_WEST_OF_60_W__RRAF_1991__UTM_ZONE_20N__4559.SP":
                MyData.push("SECONDO_S_CRS", "4559");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CARIBBEAN__FRENCH_ANTILLES__RGAF09__5487.SP":
                MyData.push("SECONDO_S_CRS", "5487");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CARIBBEAN__FRENCH_ANTILLES__RRAF_1991__4556.SP":
                MyData.push("SECONDO_S_CRS", "4556");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CARIBBEAN__FRENCH_ANTILLES__RRAF_1991__4972.SP":
                MyData.push("SECONDO_S_CRS", "4972");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CARIBBEAN__FRENCH_ANTILLES__RRAF_1991__GEOCENTRIC__4384.SP":
                MyData.push("SECONDO_S_CRS", "4384");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CARIBBEAN__FRENCH_ANTILLES__RRAF_1991__UTM_ZONE_20N__2989.SP":
                MyData.push("SECONDO_S_CRS", "2989");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CARIBBEAN__PUERTO_RICO_AND_US_VIRGIN_ISLANDS__NAD83__PUERTO_RICO__VIRGIN_IS__32161.SP":
                MyData.push("SECONDO_S_CRS", "32161");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CARIBBEAN__PUERTO_RICO_AND_US_VIRGIN_ISLANDS__ONSHORE__NAD83_2011__PUERTO_RICO_AND_VIRGIN_IS__6566.SP":
                MyData.push("SECONDO_S_CRS", "6566");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CARIBBEAN__PUERTO_RICO_AND_US_VIRGIN_ISLANDS__ONSHORE__NAD83_HARN__PUERTO_RICO_AND_VIRGIN_IS__2866.SP":
                MyData.push("SECONDO_S_CRS", "2866");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CARIBBEAN__PUERTO_RICO_AND_US_VIRGIN_ISLANDS__ONSHORE__NAD83_NSRS2007__PUERTO_RICO_AND_VIRGIN_IS__4437.SP":
                MyData.push("SECONDO_S_CRS", "4437");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}