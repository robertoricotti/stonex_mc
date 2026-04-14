public class GREECECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GREECE__ONSHORE__GGRS87__GREEK_GRID__2100.SP":
                MyData.push("SECONDO_S_CRS", "2100");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "HEPOS__GGRS87_TM87_GRID.SP":
                MyData.push("SECONDO_S_CRS", "150580");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;


        }
    }
}