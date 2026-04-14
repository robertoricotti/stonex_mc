public class SUDANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SUDAN__SOUTH__EAST_OF_30_E__SUDAN__UTM_ZONE_36N__29636.SP":
                MyData.push("crs", "29636");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SUDAN__SOUTH__WEST_OF_30_E__SUDAN__UTM_ZONE_35N__29635.SP":
                MyData.push("crs", "29635");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}