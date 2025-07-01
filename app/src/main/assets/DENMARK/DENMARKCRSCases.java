public class DENMARKCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "DENMARK__ONSHORE_BORNHOLM__ETRS89__DKTM4__4096.SP":
                MyData.push("crs", "4096");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "DENMARK__ONSHORE_BORNHOLM__ETRS89__KP2000_BORNHOLM__2198.SP":
                MyData.push("crs", "2198");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "DENMARK__ONSHORE_JUTLAND_AND_FUNEN__ETRS89__KP2000_JUTLAND__2196.SP":
                MyData.push("crs", "2196");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "DENMARK__ONSHORE_JUTLAND_EAST_OF_9_E_AND_FUNEN__ETRS89__DKTM2__4094.SP":
                MyData.push("crs", "4094");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "DENMARK__ONSHORE_JUTLAND_WEST_OF_10_E__ETRS89__DKTM1__4093.SP":
                MyData.push("crs", "4093");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "DENMARK__ONSHORE_ZEALAND_AND_LOLLAND__ETRS89__DKTM3__4095.SP":
                MyData.push("crs", "4095");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "DENMARK__ONSHORE_ZEALAND_AND_LOLLAND__ETRS89__KP2000_ZEALAND__2197.SP":
                MyData.push("crs", "2197");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}