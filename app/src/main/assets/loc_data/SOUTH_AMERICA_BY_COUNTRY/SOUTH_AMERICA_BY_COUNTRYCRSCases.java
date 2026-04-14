public class SOUTH_AMERICA_BY_COUNTRYCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SOUTH_AMERICA_BY_COUNTRY__SIRGAS__GEOCENTRIC__4376.SP":
                MyData.push("SECONDO_S_CRS", "4376");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}