public class SRI_LANKACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SRI_LANKA__ONSHORE__KANDAWALA__SRI_LANKA_GRID__5234.SP":
                MyData.push("crs", "5234");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SRI_LANKA__ONSHORE__SLD99__SRI_LANKA_GRID_1999__5235.SP":
                MyData.push("crs", "5235");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}