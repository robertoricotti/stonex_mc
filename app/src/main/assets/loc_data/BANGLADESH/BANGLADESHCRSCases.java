public class BANGLADESHCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BANGLADESH__GULSHAN_303__TM_90_NE__3106.SP":
                MyData.push("SECONDO_S_CRS", "3106");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BANGLADESH__ONSHORE_EAST_OF_90_E__KALIANPUR_1937__UTM_ZONE_46N__24306.SP":
                MyData.push("SECONDO_S_CRS", "24306");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BANGLADESH__ONSHORE_WEST_OF_90_E__KALIANPUR_1937__UTM_ZONE_45N__24305.SP":
                MyData.push("SECONDO_S_CRS", "24305");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BANGLADESH__ONSHORE__GULSHAN_303__TM_90_NE__3106.SP":
                MyData.push("SECONDO_S_CRS", "3106");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BANGLADESH__ONSHORE__KALIANPUR_1937__INDIA_ZONE_IIB__24375.SP":
                MyData.push("SECONDO_S_CRS", "24375");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}