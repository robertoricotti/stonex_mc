public class MAYOTTECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MAYOTTE__ONSHORE__CADASTRE_1997__4473.SP":
                MyData.push("crs", "4473");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAYOTTE__ONSHORE__CADASTRE_1997__UTM_ZONE_38S__4474.SP":
                MyData.push("crs", "4474");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAYOTTE__ONSHORE__CADASTRE_1997__UTM_ZONE_38S__5879.SP":
                MyData.push("crs", "5879");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAYOTTE__ONSHORE__COMBANI_1950__UTM_ZONE_38S__2980.SP":
                MyData.push("crs", "2980");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAYOTTE__RGM04__4468.SP":
                MyData.push("crs", "4468");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAYOTTE__RGM04__UTM_ZONE_38S__4471.SP":
                MyData.push("crs", "4471");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}