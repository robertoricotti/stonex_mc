public class COMOROSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "COMOROS__NJAZIDJA__GRANDE_COMORE__GRAND_COMOROS__UTM_ZONE_38S__2999.SP":
                MyData.push("crs", "2999");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}