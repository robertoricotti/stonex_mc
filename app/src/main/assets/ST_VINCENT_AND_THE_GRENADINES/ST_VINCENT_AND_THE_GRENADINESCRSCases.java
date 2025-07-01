public class ST_VINCENT_AND_THE_GRENADINESCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ST_VINCENT_AND_THE_GRENADINES__ONSHORE__ST__VINCENT_45__BRITISH_WEST_INDIES_GRID__2007.SP":
                MyData.push("crs", "2007");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}