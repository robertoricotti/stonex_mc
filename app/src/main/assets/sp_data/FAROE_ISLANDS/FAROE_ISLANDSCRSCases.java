public class FAROE_ISLANDSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "FAROE_ISLANDS__ETRS89__FAROE_TM__5316.SP":
                MyData.push("crs", "5316");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FAROE_ISLANDS__ONSHORE__FD54__UTM_ZONE_29N__3374.SP":
                MyData.push("crs", "3374");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}