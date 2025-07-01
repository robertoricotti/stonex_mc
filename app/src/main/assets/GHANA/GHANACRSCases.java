public class GHANACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GHANA__LEIGON__GHANA_METRE_GRID__25000.SP":
                MyData.push("crs", "25000");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GHANA__OFFSHORE__ACCRA__TM_1_NW__2137.SP":
                MyData.push("crs", "2137");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}