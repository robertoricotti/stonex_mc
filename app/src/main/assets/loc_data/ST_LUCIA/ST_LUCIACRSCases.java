public class ST_LUCIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ST_LUCIA__ONSHORE__ST__LUCIA_1955__BRITISH_WEST_INDIES_GRID__2006.SP":
                MyData.push("SECONDO_S_CRS", "2006");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}