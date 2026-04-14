public class UAECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "UAE__ABU_DHABI_AND_DUBAI__ONSHORE_EAST_OF_54_E__TC_1948__UTM_ZONE_40N__30340.SP":
                MyData.push("SECONDO_S_CRS", "30340");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "UAE__ABU_DHABI__ONSHORE_WEST_OF_54_E__TC_1948__UTM_ZONE_39N__30339.SP":
                MyData.push("SECONDO_S_CRS", "30339");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "UAE__DUBAI_MUNICIPALITY__WGS_84__DUBAI_LOCAL_TM__3997.SP":
                MyData.push("SECONDO_S_CRS", "3997");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "UAE__EAST_OF_54_E__NAHRWAN_1967__UTM_ZONE_40N__27040.SP":
                MyData.push("SECONDO_S_CRS", "27040");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}