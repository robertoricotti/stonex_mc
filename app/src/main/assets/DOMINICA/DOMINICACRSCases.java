public class DOMINICACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "DOMINICA__ONSHORE__DOMINICA_1945__BRITISH_WEST_INDIES_GRID__2002.SP":
                MyData.push("crs", "2002");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}