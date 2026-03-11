public class MAYOTTECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MAYOTTE__ONSHORE__CADASTRE_1997__4473.SP":
                MyData.push("SECONDO_S_CRS", "4473");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAYOTTE__ONSHORE__CADASTRE_1997__UTM_ZONE_38S__4474.SP":
                MyData.push("SECONDO_S_CRS", "4474");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAYOTTE__ONSHORE__CADASTRE_1997__UTM_ZONE_38S__5879.SP":
                MyData.push("SECONDO_S_CRS", "5879");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAYOTTE__ONSHORE__COMBANI_1950__UTM_ZONE_38S__2980.SP":
                MyData.push("SECONDO_S_CRS", "2980");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAYOTTE__RGM04__4468.SP":
                MyData.push("SECONDO_S_CRS", "4468");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAYOTTE__RGM04__UTM_ZONE_38S__4471.SP":
                MyData.push("SECONDO_S_CRS", "4471");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}