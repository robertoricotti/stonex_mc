public class MAURITIUSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MAURITIUS__MAINLAND__LE_POUCE_1934__MAURITIUS_GRID__3337.SP":
                MyData.push("SECONDO_S_CRS", "3337");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}