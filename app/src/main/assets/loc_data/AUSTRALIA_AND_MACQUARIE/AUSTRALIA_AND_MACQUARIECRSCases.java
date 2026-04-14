public class AUSTRALIA_AND_MACQUARIECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "AUSTRALIA_AND_MACQUARIE__156_E_TO_162_E__GDA94__MGA_ZONE_57__28357.SP":
                MyData.push("SECONDO_S_CRS", "28357");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}