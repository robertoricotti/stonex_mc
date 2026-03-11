public class WALLIS_AND_FUTUNACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "WALLIS_AND_FUTUNA__WALLIS__MOP78__UTM_ZONE_1S__2988.SP":
                MyData.push("crs", "2988");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}