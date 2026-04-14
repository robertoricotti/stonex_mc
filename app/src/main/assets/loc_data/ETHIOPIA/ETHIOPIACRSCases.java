public class ETHIOPIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ETHIOPIA__EAST_OF_42_E__ADINDAN__UTM_ZONE_38N__20138.SP":
                MyData.push("SECONDO_S_CRS", "20138");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}