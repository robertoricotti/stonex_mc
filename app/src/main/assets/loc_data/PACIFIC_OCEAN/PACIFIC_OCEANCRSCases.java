public class PACIFIC_OCEANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PACIFIC_OCEAN__WGS_84__PDC_MERCATOR__3349.SP":
                MyData.push("SECONDO_S_CRS", "3349");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PACIFIC_OCEAN__WGS_84__PDC_MERCATOR__3832.SP":
                MyData.push("SECONDO_S_CRS", "3832");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}