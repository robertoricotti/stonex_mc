public class URUGUAYCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "URUGUAY__EAST_OF_54_W__SIRGAS_ROU98__UTM_ZONE_22S__5383.SP":
                MyData.push("SECONDO_S_CRS", "5383");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "URUGUAY__SIRGAS_ROU98__5379.SP":
                MyData.push("SECONDO_S_CRS", "5379");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "URUGUAY__WEST_OF_54_W__SIRGAS_ROU98__UTM_ZONE_21S__5382.SP":
                MyData.push("SECONDO_S_CRS", "5382");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}