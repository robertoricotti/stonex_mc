public class ANGUILLACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ANGUILLA__ONSHORE__ANGUILLA_1957__BRITISH_WEST_INDIES_GRID__2000.SP":
                MyData.push("crs", "2000");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}