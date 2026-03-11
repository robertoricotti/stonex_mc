public class SUDANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SUDAN__SOUTH__EAST_OF_30_E__SUDAN__UTM_ZONE_36N__29636.SP":
                MyData.push("SECONDO_S_CRS", "29636");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SUDAN__SOUTH__WEST_OF_30_E__SUDAN__UTM_ZONE_35N__29635.SP":
                MyData.push("SECONDO_S_CRS", "29635");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}