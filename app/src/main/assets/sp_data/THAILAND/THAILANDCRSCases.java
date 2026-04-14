public class THAILANDCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "THAILAND__EAST_OF_102_E__INDIAN_1975__UTM_ZONE_48N__24048.SP":
                MyData.push("crs", "24048");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "THAILAND__ONSHORE_AND_GOT_96_E_TO102_E__INDIAN_1975__UTM_ZONE_47N__24047.SP":
                MyData.push("crs", "24047");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "THAILAND__ONSHORE_EAST_OF_102_E__INDIAN_1954__UTM_ZONE_48N__23948.SP":
                MyData.push("crs", "23948");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}