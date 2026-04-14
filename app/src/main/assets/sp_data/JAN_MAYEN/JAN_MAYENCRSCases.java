public class JAN_MAYENCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "JAN_MAYEN__ONSHORE__HELLE_1954__JAN_MAYEN_GRID__3058.SP":
                MyData.push("crs", "3058");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}