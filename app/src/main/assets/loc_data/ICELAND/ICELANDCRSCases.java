public class ICELANDCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ICELAND__ISN2004__5322.SP":
                MyData.push("SECONDO_S_CRS", "5322");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ICELAND__ISN2004__LAEA_EUROPE__5638.SP":
                MyData.push("SECONDO_S_CRS", "5638");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ICELAND__ISN2004__LAMBERT_2004__5325.SP":
                MyData.push("SECONDO_S_CRS", "5325");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ICELAND__ISN2004__LCC_EUROPE__5639.SP":
                MyData.push("SECONDO_S_CRS", "5639");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ICELAND__ISN93__4944.SP":
                MyData.push("SECONDO_S_CRS", "4944");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ICELAND__ISN93__GEOCENTRIC__4387.SP":
                MyData.push("SECONDO_S_CRS", "4387");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ICELAND__ISN93__LAMBERT_1993__3057.SP":
                MyData.push("SECONDO_S_CRS", "3057");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ICELAND__ONSHORE_24_W_TO_18_W__HJORSEY_1955__UTM_ZONE_27N__3055.SP":
                MyData.push("SECONDO_S_CRS", "3055");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ICELAND__ONSHORE_EAST_OF_18_W__HJORSEY_1955__UTM_ZONE_28N__3056.SP":
                MyData.push("SECONDO_S_CRS", "3056");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ICELAND__ONSHORE_WEST_OF_24_W__HJORSEY_1955__UTM_ZONE_26N__3054.SP":
                MyData.push("SECONDO_S_CRS", "3054");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}