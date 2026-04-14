public class FAROE_ISLANDSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "FAROE_ISLANDS__ETRS89__FAROE_TM__5316.SP":
                MyData.push("SECONDO_S_CRS", "5316");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FAROE_ISLANDS__ONSHORE__FD54__UTM_ZONE_29N__3374.SP":
                MyData.push("SECONDO_S_CRS", "3374");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}