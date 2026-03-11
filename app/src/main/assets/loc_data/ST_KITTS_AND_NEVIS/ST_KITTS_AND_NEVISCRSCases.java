public class ST_KITTS_AND_NEVISCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ST_KITTS_AND_NEVIS__ONSHORE__ST__KITTS_1955__BRITISH_WEST_INDIES_GRID__2005.SP":
                MyData.push("SECONDO_S_CRS", "2005");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}