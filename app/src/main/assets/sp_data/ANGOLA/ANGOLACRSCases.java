public class ANGOLACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ANGOLA__ANGOLA_PROPER__12_E_TO_18_E__CAMACUPA_1948__UTM_ZONE_33S__22033.SP":
                MyData.push("crs", "22033");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ANGOLA__ANGOLA_PROPER__OFFSHORE__CAMACUPA_1948__TM_12_SE__22092.SP":
                MyData.push("crs", "22092");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ANGOLA__ANGOLA_PROPER__OFFSHORE__WEST_OF_12_E__CAMACUPA_1948__UTM_ZONE_32S__22032.SP":
                MyData.push("crs", "22032");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ANGOLA__CABINDA__MHAST__UTM_ZONE_32S__26432.SP":
                MyData.push("crs", "26432");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ANGOLA__OFFSHORE_BLOCK_15__CAMACUPA_1948__TM_11_30_SE__22091.SP":
                MyData.push("crs", "22091");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ANGOLA__OFFSHORE_NORTH_OF_8_S__WGS_84__TM_12_SE__5842.SP":
                MyData.push("crs", "5842");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}