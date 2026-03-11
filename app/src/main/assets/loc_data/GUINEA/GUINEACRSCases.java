public class GUINEACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GUINEA__EAST_OF_12_W__CONAKRY_1905__UTM_ZONE_29N__31529.SP":
                MyData.push("SECONDO_S_CRS", "31529");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "GUINEA__EAST_OF_12_W__DABOLA_1981__UTM_ZONE_29N__2064.SP":
                MyData.push("SECONDO_S_CRS", "2064");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "GUINEA__EAST_OF_12_W__DABOLA_1981__UTM_ZONE_29N__3462.SP":
                MyData.push("SECONDO_S_CRS", "3462");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "GUINEA__WEST_OF_12_W__CONAKRY_1905__UTM_ZONE_28N__31528.SP":
                MyData.push("SECONDO_S_CRS", "31528");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "GUINEA__WEST_OF_12_W__DABOLA_1981__UTM_ZONE_28N__2063.SP":
                MyData.push("SECONDO_S_CRS", "2063");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "GUINEA__WEST_OF_12_W__DABOLA_1981__UTM_ZONE_28N__3461.SP":
                MyData.push("SECONDO_S_CRS", "3461");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}