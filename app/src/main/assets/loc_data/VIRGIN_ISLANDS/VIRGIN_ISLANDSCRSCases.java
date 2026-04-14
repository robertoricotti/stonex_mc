public class VIRGIN_ISLANDSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "VIRGIN_ISLANDS__BRITISH__ONSHORE__PUERTO_RICO__UTM_ZONE_20N__3920.SP":
                MyData.push("SECONDO_S_CRS", "3920");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIRGIN_ISLANDS__US__ONSHORE__PUERTO_RICO__ST__CROIX__3992.SP":
                MyData.push("SECONDO_S_CRS", "3992");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}