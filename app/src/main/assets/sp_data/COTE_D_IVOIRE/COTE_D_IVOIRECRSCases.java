public class COTE_D_IVOIRECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "COTE_D_IVOIRE__IVORY_COAST__EAST_OF_6_W__ABIDJAN_1987__UTM_ZONE_30N__2041.SP":
                MyData.push("crs", "2041");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COTE_D_IVOIRE__IVORY_COAST__EAST_OF_6_W__LOCODJO_1965__UTM_ZONE_30N__2040.SP":
                MyData.push("crs", "2040");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COTE_D_IVOIRE__IVORY_COAST__OFFSHORE__ABIDJAN_1987__TM_5_NW__2165.SP":
                MyData.push("crs", "2165");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COTE_D_IVOIRE__IVORY_COAST__OFFSHORE__LOCODJO_1965__TM_5_NW__2164.SP":
                MyData.push("crs", "2164");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COTE_D_IVOIRE__IVORY_COAST__WEST_OF_6_W__ABIDJAN_1987__UTM_ZONE_29N__2043.SP":
                MyData.push("crs", "2043");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COTE_D_IVOIRE__IVORY_COAST__WEST_OF_6_W__LOCODJO_1965__UTM_ZONE_29N__2042.SP":
                MyData.push("crs", "2042");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}