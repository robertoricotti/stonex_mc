public class CAMEROONCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CAMEROON__COASTAL_AREA__DOUALA_1948__AEF_WEST__3119.SP":
                MyData.push("crs", "3119");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CAMEROON__COASTAL_AREA__DOUALA_1948__AOF_WEST__2214.SP":
                MyData.push("crs", "2214");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CAMEROON__COASTAL_AREA__MANOCA_1962__UTM_ZONE_32N__2215.SP":
                MyData.push("crs", "2215");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CAMEROON__DOUALA__UTM_ZONE_32N__22832.SP":
                MyData.push("crs", "22832");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CAMEROON__GAROUA_AREA__GAROUA__UTM_ZONE_33N__2312.SP":
                MyData.push("crs", "2312");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CAMEROON__GAROUA__UTM_ZONE_33N__23433.SP":
                MyData.push("crs", "23433");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CAMEROON__N_DJAMENA_AREA__KOUSSERI__UTM_ZONE_33N__2313.SP":
                MyData.push("crs", "2313");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}