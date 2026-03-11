public class GHANACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GHANA__LEIGON__GHANA_METRE_GRID__25000.SP":
                MyData.push("SECONDO_S_CRS", "25000");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GHANA__OFFSHORE__ACCRA__TM_1_NW__2137.SP":
                MyData.push("SECONDO_S_CRSrs", "2137");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}