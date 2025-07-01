public class CARIBBEANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CARIBBEAN__FRENCH_ANTILLES_WEST_OF_60_W__RGAF09__UTM_ZONE_20N__5490.SP":
                MyData.push("crs", "5490");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CARIBBEAN__FRENCH_ANTILLES_WEST_OF_60_W__RRAF_1991__UTM_ZONE_20N__4559.SP":
                MyData.push("crs", "4559");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CARIBBEAN__FRENCH_ANTILLES__RGAF09__5487.SP":
                MyData.push("crs", "5487");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CARIBBEAN__FRENCH_ANTILLES__RRAF_1991__4556.SP":
                MyData.push("crs", "4556");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CARIBBEAN__FRENCH_ANTILLES__RRAF_1991__4972.SP":
                MyData.push("crs", "4972");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CARIBBEAN__FRENCH_ANTILLES__RRAF_1991__GEOCENTRIC__4384.SP":
                MyData.push("crs", "4384");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CARIBBEAN__FRENCH_ANTILLES__RRAF_1991__UTM_ZONE_20N__2989.SP":
                MyData.push("crs", "2989");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CARIBBEAN__PUERTO_RICO_AND_US_VIRGIN_ISLANDS__NAD83__PUERTO_RICO__VIRGIN_IS__32161.SP":
                MyData.push("crs", "32161");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CARIBBEAN__PUERTO_RICO_AND_US_VIRGIN_ISLANDS__ONSHORE__NAD83_2011__PUERTO_RICO_AND_VIRGIN_IS__6566.SP":
                MyData.push("crs", "6566");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CARIBBEAN__PUERTO_RICO_AND_US_VIRGIN_ISLANDS__ONSHORE__NAD83_HARN__PUERTO_RICO_AND_VIRGIN_IS__2866.SP":
                MyData.push("crs", "2866");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CARIBBEAN__PUERTO_RICO_AND_US_VIRGIN_ISLANDS__ONSHORE__NAD83_NSRS2007__PUERTO_RICO_AND_VIRGIN_IS__4437.SP":
                MyData.push("crs", "4437");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}