public class AUSTRALIA_AND_MACQUARIECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "AUSTRALIA_AND_MACQUARIE__156_E_TO_162_E__GDA94__MGA_ZONE_57__28357.SP":
                MyData.push("crs", "28357");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}