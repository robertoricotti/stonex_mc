public class ST_VINCENT_AND_THE_GRENADINESCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ST_VINCENT_AND_THE_GRENADINES__ONSHORE__ST__VINCENT_45__BRITISH_WEST_INDIES_GRID__2007.SP":
                MyData.push("SECONDO_S_CRS", "2007");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}