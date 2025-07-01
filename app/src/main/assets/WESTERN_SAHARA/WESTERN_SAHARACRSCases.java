public class WESTERN_SAHARACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "WESTERN_SAHARA__NORTH_OF_24_3_N__MERCHICH__SAHARA_NORD__26194.SP":
                MyData.push("crs", "26194");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "WESTERN_SAHARA__SOUTH_OF_24_3_N__MERCHICH__SAHARA_SUD__26195.SP":
                MyData.push("crs", "26195");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}