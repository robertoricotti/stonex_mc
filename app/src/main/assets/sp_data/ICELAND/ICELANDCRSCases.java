public class ICELANDCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ICELAND__ISN2004__5322.SP":
                MyData.push("crs", "5322");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ICELAND__ISN2004__LAEA_EUROPE__5638.SP":
                MyData.push("crs", "5638");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ICELAND__ISN2004__LAMBERT_2004__5325.SP":
                MyData.push("crs", "5325");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ICELAND__ISN2004__LCC_EUROPE__5639.SP":
                MyData.push("crs", "5639");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ICELAND__ISN93__4944.SP":
                MyData.push("crs", "4944");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ICELAND__ISN93__GEOCENTRIC__4387.SP":
                MyData.push("crs", "4387");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ICELAND__ISN93__LAMBERT_1993__3057.SP":
                MyData.push("crs", "3057");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ICELAND__ONSHORE_24_W_TO_18_W__HJORSEY_1955__UTM_ZONE_27N__3055.SP":
                MyData.push("crs", "3055");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ICELAND__ONSHORE_EAST_OF_18_W__HJORSEY_1955__UTM_ZONE_28N__3056.SP":
                MyData.push("crs", "3056");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ICELAND__ONSHORE_WEST_OF_24_W__HJORSEY_1955__UTM_ZONE_26N__3054.SP":
                MyData.push("crs", "3054");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}