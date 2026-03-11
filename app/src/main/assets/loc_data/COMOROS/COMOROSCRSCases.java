public class COMOROSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "COMOROS__NJAZIDJA__GRANDE_COMORE__GRAND_COMOROS__UTM_ZONE_38S__2999.SP":
                MyData.push("SECONDO_S_CRS", "2999");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}