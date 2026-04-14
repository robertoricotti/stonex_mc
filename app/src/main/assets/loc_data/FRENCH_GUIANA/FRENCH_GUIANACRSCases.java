public class FRENCH_GUIANACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "FRENCH_GUIANA__COASTAL_AREA_EAST_OF_54_W__CSG67__UTM_ZONE_22N__2971.SP":
                MyData.push("SECONDO_S_CRS", "2971");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "FRENCH_GUIANA__COASTAL_AREA_WEST_OF_54_W__CSG67__UTM_ZONE_21N__3312.SP":
                MyData.push("SECONDO_S_CRS", "3312");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "FRENCH_GUIANA__EAST_OF_54_W__RGFG95__UTM_ZONE_22N__2972.SP":
                MyData.push("SECONDO_S_CRS", "2972");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "FRENCH_GUIANA__RGFG95__4966.SP":
                MyData.push("SECONDO_S_CRS", "4966");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "FRENCH_GUIANA__RGFG95__GEOCENTRIC__4372.SP":
                MyData.push("SECONDO_S_CRS", "4372");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "FRENCH_GUIANA__WEST_OF_54_W__RGFG95__UTM_ZONE_21N__3313.SP":
                MyData.push("SECONDO_S_CRS", "3313");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}