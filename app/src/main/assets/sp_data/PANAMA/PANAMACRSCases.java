public class PANAMACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PANAMA__MACARIO_SOLIS__5368.SP":
                MyData.push("crs", "5368");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PANAMA__ONSHORE__PANAMA_COLON_1911__PANAMA_LAMBERT__5469.SP":
                MyData.push("crs", "5469");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}