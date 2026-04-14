public class OMANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "OMAN__MAINLAND_EAST_OF_54_E__FAHUD__UTM_ZONE_40N__23240.SP":
                MyData.push("SECONDO_S_CRS", "23240");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "OMAN__ONSHORE_EAST_OF_54_E__PSD93__UTM_ZONE_40N__3440.SP":
                MyData.push("SECONDO_S_CRS", "3440");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "OMAN__ONSHORE_WEST_OF_54_E__FAHUD__UTM_ZONE_39N__23239.SP":
                MyData.push("SECONDO_S_CRS", "23239");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "OMAN__ONSHORE_WEST_OF_54_E__PSD93__UTM_ZONE_39N__3439.SP":
                MyData.push("SECONDO_S_CRS", "3439");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}