public class ETHIOPIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ETHIOPIA__EAST_OF_42_E__ADINDAN__UTM_ZONE_38N__20138.SP":
                MyData.push("crs", "20138");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}