public class BELIZECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BELIZE__ONSHORE__SIBUN_GORGE_1922__COLONY_GRID__5466.SP":
                MyData.push("crs", "5466");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}