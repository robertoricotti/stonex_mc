public class TRINIDAD_AND_TOBAGOCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "TRINIDAD_AND_TOBAGO__TOBAGO__ONSHORE__NAPARIMA_1972__UTM_ZONE_20N__27120.SP":
                MyData.push("SECONDO_S_CRS", "27120");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TRINIDAD_AND_TOBAGO__TRINIDAD__ONSHORE__NAPARIMA_1955__UTM_ZONE_20N__2067.SP":
                MyData.push("SECONDO_S_CRS", "2067");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}