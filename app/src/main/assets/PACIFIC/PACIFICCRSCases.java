public class PACIFICCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PACIFIC__US_INTERESTS_MARIANA_PLATE__NAD83_MA11__6323.SP":
                MyData.push("crs", "6323");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PACIFIC__US_INTERESTS_PACIFIC_PLATE__NAD83_PA11__6320.SP":
                MyData.push("crs", "6320");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}