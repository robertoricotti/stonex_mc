public class TRINIDAD_AND_TOBAGOCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "TRINIDAD_AND_TOBAGO__TOBAGO__ONSHORE__NAPARIMA_1972__UTM_ZONE_20N__27120.SP":
                MyData.push("crs", "27120");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TRINIDAD_AND_TOBAGO__TRINIDAD__ONSHORE__NAPARIMA_1955__UTM_ZONE_20N__2067.SP":
                MyData.push("crs", "2067");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}