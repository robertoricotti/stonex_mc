public class BOLIVIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BOLIVIA__66_W_TO_60_W__MARGEN__UTM_ZONE_20S__5355.SP":
                MyData.push("SECONDO_S_CRS", "5355");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOLIVIA__66_W_TO_60_W__PSAD56__UTM_ZONE_20S__24880.SP":
                MyData.push("SECONDO_S_CRS", "24880");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOLIVIA__EAST_OF_60_W__MARGEN__UTM_ZONE_21S__5357.SP":
                MyData.push("SECONDO_S_CRS", "5357");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOLIVIA__EAST_OF_60_W__PSAD56__UTM_ZONE_21S__24881.SP":
                MyData.push("SECONDO_S_CRS", "24881");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOLIVIA__MARGEN__5352.SP":
                MyData.push("SECONDO_S_CRS", "5352");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOLIVIA__WEST_OF_66_W__MARGEN__UTM_ZONE_19S__5356.SP":
                MyData.push("SECONDO_S_CRS", "5356");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}