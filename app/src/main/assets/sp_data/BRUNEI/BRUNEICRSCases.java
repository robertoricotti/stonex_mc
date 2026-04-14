public class BRUNEICRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BRUNEI__GDBD2009__5244.SP":
                MyData.push("crs", "5244");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BRUNEI__GDBD2009__BRUNEI_BRSO__5247.SP":
                MyData.push("crs", "5247");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}