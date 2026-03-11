public class WESTERN_SAHARACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "WESTERN_SAHARA__NORTH_OF_24_3_N__MERCHICH__SAHARA_NORD__26194.SP":
                MyData.push("SECONDO_S_CRS", "26194");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "WESTERN_SAHARA__SOUTH_OF_24_3_N__MERCHICH__SAHARA_SUD__26195.SP":
                MyData.push("SECONDO_S_CRS", "26195");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}