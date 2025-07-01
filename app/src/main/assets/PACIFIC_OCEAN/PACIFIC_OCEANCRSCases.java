public class PACIFIC_OCEANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PACIFIC_OCEAN__WGS_84__PDC_MERCATOR__3349.SP":
                MyData.push("crs", "3349");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PACIFIC_OCEAN__WGS_84__PDC_MERCATOR__3832.SP":
                MyData.push("crs", "3832");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}