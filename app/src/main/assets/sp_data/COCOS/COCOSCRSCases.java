public class COCOSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "COCOS__KEELING__ISLANDS__EAST_OF_96_E__GDA94__MGA_ZONE_47__6737.SP":
                MyData.push("crs", "6737");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COCOS__KEELING__ISLANDS__ONSHORE__GDA94__CKIG94__6723.SP":
                MyData.push("crs", "6723");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COCOS__KEELING__ISLANDS__ONSHORE__WGS_84__CKIG92__6722.SP":
                MyData.push("crs", "6722");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COCOS__KEELING__ISLANDS__WEST_OF_96_E__GDA94__MGA_ZONE_46__6736.SP":
                MyData.push("crs", "6736");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}