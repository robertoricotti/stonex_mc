public class NORFOLK_ISLANDCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "NORFOLK_ISLAND__EAST_OF_168_E__GDA94__MGA_ZONE_59__6738.SP":
                MyData.push("SECONDO_S_CRS", "6738");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}