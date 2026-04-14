public class ANTIGUACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ANTIGUA__ONSHORE__ANTIGUA_1943__BRITISH_WEST_INDIES_GRID__2001.SP":
                MyData.push("crs", "2001");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}