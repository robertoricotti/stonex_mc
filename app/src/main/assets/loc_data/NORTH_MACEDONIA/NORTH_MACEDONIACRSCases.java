public class NORTH_MACEDONIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "NORTH_MACEDONIA__MACEDONIA_STATE_COORDINATE_SYSTEM__6204.SP":
                MyData.push("SECONDO_S_CRS", "6204");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}