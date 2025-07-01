public class AMERICAN_SAMOACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "AMERICAN_SAMOA__2_MAIN_ISLAND_GROUPS_AND_ROSE_ISLAND__NAD83_HARN__UTM_ZONE_2S__2195.SP":
                MyData.push("crs", "2195");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "AMERICAN_SAMOA__2_MAIN_ISLAND_GROUPS_AND_ROSE_ISLAND__NAD83_PA11__UTM_ZONE_2S__6636.SP":
                MyData.push("crs", "6636");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "AMERICAN_SAMOA__2_MAIN_ISLAND_GROUPS__AMERICAN_SAMOA_1962__AMERICAN_SAMOA_LAMBERT__3102.SP":
                MyData.push("crs", "3102");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "AMERICAN_SAMOA__AMERICAN_SAMOA_1962__AMERICAN_SAMOA_LAMBERT__2155.SP":
                MyData.push("crs", "2155");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "AMERICAN_SAMOA__AMERICAN_SAMOA_1962__AMERICAN_SAMOA_LAMBERT__2194.SP":
                MyData.push("crs", "2194");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "AMERICAN_SAMOA__NAD83_HARN__UTM_ZONE_59S__2156.SP":
                MyData.push("crs", "2156");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}