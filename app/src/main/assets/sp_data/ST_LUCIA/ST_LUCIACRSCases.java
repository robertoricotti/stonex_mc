public class ST_LUCIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ST_LUCIA__ONSHORE__ST__LUCIA_1955__BRITISH_WEST_INDIES_GRID__2006.SP":
                MyData.push("crs", "2006");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}