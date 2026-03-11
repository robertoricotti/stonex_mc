public class CHANNEL_ISLANDSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CHANNEL_ISLANDS__GUERNSEY__ALDERNEY__SARK__ETRS89__GUERNSEY_GRID__3108.SP":
                MyData.push("crs", "3108");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CHANNEL_ISLANDS__JERSEY__LES_ECREHOS_AND_LES_MINQUIERS__ETRS89__JERSEY_TRANSVERSE_MERCATOR__3109.SP":
                MyData.push("crs", "3109");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}