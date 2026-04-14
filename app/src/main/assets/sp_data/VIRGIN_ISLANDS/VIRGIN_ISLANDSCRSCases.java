public class VIRGIN_ISLANDSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "VIRGIN_ISLANDS__BRITISH__ONSHORE__PUERTO_RICO__UTM_ZONE_20N__3920.SP":
                MyData.push("crs", "3920");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIRGIN_ISLANDS__US__ONSHORE__PUERTO_RICO__ST__CROIX__3992.SP":
                MyData.push("crs", "3992");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}