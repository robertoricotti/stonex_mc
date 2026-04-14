public class NORFOLK_ISLANDCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "NORFOLK_ISLAND__EAST_OF_168_E__GDA94__MGA_ZONE_59__6738.SP":
                MyData.push("crs", "6738");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}