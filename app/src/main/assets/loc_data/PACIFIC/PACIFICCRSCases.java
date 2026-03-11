public class PACIFICCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PACIFIC__US_INTERESTS_MARIANA_PLATE__NAD83_MA11__6323.SP":
                MyData.push("SECONDO_S_CRS", "6323");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PACIFIC__US_INTERESTS_PACIFIC_PLATE__NAD83_PA11__6320.SP":
                MyData.push("SECONDO_S_CRS", "6320");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}