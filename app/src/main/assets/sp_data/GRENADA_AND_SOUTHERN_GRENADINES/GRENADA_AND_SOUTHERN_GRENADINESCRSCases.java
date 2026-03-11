public class GRENADA_AND_SOUTHERN_GRENADINESCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GRENADA_AND_SOUTHERN_GRENADINES__ONSHORE__GRENADA_1953__BRITISH_WEST_INDIES_GRID__2003.SP":
                MyData.push("crs", "2003");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}