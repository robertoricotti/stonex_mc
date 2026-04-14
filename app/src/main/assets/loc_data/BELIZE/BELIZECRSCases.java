public class BELIZECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BELIZE__ONSHORE__SIBUN_GORGE_1922__COLONY_GRID__5466.SP":
                MyData.push("SECONDO_S_CRS", "5466");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}