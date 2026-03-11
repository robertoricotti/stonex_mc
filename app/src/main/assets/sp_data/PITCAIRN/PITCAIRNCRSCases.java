public class PITCAIRNCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PITCAIRN__PITCAIRN_ISLAND__PITCAIRN_1967__UTM_ZONE_9S__3784.SP":
                MyData.push("crs", "3784");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PITCAIRN__PITCAIRN_ISLAND__PITCAIRN_2006__PITCAIRN_TM_2006__3783.SP":
                MyData.push("crs", "3783");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}