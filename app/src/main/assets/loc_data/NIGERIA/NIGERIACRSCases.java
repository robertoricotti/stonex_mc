public class NIGERIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "NIGERIA__6_5_E_TO_10_5_E__MINNA__NIGERIA_MID_BELT__26392.SP":
                MyData.push("SECONDO_S_CRS", "26392");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NIGERIA__EAST_OF_10_5_E__MINNA__NIGERIA_EAST_BELT__26393.SP":
                MyData.push("SECONDO_S_CRS", "26393");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NIGERIA__OFFSHORE_DEEP_WATER__EAST_OF_6_E__MINNA__UTM_ZONE_32N__26332.SP":
                MyData.push("SECONDO_S_CRS", "26332");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NIGERIA__OFFSHORE_DEEP_WATER__WEST_OF_6_E__MINNA__UTM_ZONE_31N__26331.SP":
                MyData.push("SECONDO_S_CRS", "26331");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NIGERIA__OFFSHORE__WGS_84__TM_6_NE__2311.SP":
                MyData.push("SECONDO_S_CRS", "2311");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NIGERIA__WEST_OF_6_5_E__MINNA__NIGERIA_WEST_BELT__26391.SP":
                MyData.push("SECONDO_S_CRS", "26391");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}