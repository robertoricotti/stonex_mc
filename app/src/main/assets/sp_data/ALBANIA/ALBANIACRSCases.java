public class ALBANIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ALBANIA__ALBANIAN_1987__GAUSS_KRUGER_ZONE_4__2199.SP":
                MyData.push("crs", "2199");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALBANIA__ONSHORE__ALBANIAN_1987__GAUSS_KRUGER_ZONE_4__2462.SP":
                MyData.push("crs", "2462");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALBANIA__ONSHORE__ETRS89__ALBANIA_LCC_2010__6962.SP":
                MyData.push("crs", "6962");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALBANIA__ONSHORE__ETRS89__ALBANIA_TM_2010__6870.SP":
                MyData.push("crs", "6870");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}