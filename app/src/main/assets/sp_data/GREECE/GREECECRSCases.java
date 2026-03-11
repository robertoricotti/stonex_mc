public class GREECECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GREECE__ONSHORE__GGRS87__GREEK_GRID__2100.SP":
                MyData.push("crs", "2100");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
                case "HEPOS__GGRS87_TM87_GRID.SP":
                MyData.push("crs", "150580");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

                
        }
    }
}