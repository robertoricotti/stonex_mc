public class GUADELOUPECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GUADELOUPE__GRANDE_TERRE_AND_SURROUNDING_ISLANDS__ONSHORE__GUADELOUPE_1948__UTM_ZONE_20N__2970.SP":
                MyData.push("crs", "2970");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GUADELOUPE__ST_MARTIN_AND_ST_BARTHELEMY__ONSHORE__FORT_MARIGOT__UTM_ZONE_20N__2969.SP":
                MyData.push("crs", "2969");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}