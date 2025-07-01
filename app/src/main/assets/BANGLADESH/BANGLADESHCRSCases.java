public class BANGLADESHCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BANGLADESH__GULSHAN_303__TM_90_NE__3106.SP":
                MyData.push("crs", "3106");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BANGLADESH__ONSHORE_EAST_OF_90_E__KALIANPUR_1937__UTM_ZONE_46N__24306.SP":
                MyData.push("crs", "24306");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BANGLADESH__ONSHORE_WEST_OF_90_E__KALIANPUR_1937__UTM_ZONE_45N__24305.SP":
                MyData.push("crs", "24305");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BANGLADESH__ONSHORE__GULSHAN_303__TM_90_NE__3106.SP":
                MyData.push("crs", "3106");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BANGLADESH__ONSHORE__KALIANPUR_1937__INDIA_ZONE_IIB__24375.SP":
                MyData.push("crs", "24375");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}