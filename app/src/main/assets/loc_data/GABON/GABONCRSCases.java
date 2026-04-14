public class GABONCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GABON__NORTH_OF_EQUATOR_AND_WEST_OF_12_E_ONSHORE__M_PORALOKO__UTM_ZONE_32N__26632.SP":
                MyData.push("SECONDO_S_CRS", "26632");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GABON__ONSHORE__WGS_84__GABON_TM__5223.SP":
                MyData.push("SECONDO_S_CRS", "5223");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GABON__WEST_OF_12_E__M_PORALOKO__UTM_ZONE_32S__26692.SP":
                MyData.push("SECONDO_S_CRS", "26692");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GABON__WGS_84__GABON_TM_2011__5523.SP":
                MyData.push("SECONDO_S_CRS", "5523");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}