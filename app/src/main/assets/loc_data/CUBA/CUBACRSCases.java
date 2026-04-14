public class CUBACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CUBA__ONSHORE_NORTH_OF_21_30_N__NAD27__CUBA_NORTE__2085.SP":
                MyData.push("SECONDO_S_CRS", "2085");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CUBA__ONSHORE_NORTH_OF_21_30_N__NAD27__CUBA_NORTE__3795.SP":
                MyData.push("SECONDO_S_CRS", "3795");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CUBA__ONSHORE_SOUTH_OF_21_30_N__NAD27__CUBA_SUR__2086.SP":
                MyData.push("SECONDO_S_CRS", "2086");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CUBA__ONSHORE_SOUTH_OF_21_30_N__NAD27__CUBA_SUR__3796.SP":
                MyData.push("SECONDO_S_CRS", "3796");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}