public class ST_KITTS_AND_NEVISCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ST_KITTS_AND_NEVIS__ONSHORE__ST__KITTS_1955__BRITISH_WEST_INDIES_GRID__2005.SP":
                MyData.push("crs", "2005");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}