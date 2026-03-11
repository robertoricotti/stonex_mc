public class MAURITIUSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MAURITIUS__MAINLAND__LE_POUCE_1934__MAURITIUS_GRID__3337.SP":
                MyData.push("crs", "3337");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}