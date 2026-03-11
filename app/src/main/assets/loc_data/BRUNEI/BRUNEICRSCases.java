public class BRUNEICRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BRUNEI__GDBD2009__5244.SP":
                MyData.push("SECONDO_S_CRS", "5244");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BRUNEI__GDBD2009__BRUNEI_BRSO__5247.SP":
                MyData.push("SECONDO_S_CRS", "5247");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}