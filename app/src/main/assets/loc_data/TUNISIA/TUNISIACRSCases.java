public class TUNISIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "TUNISIA__NORTH_OF_34_39_N__CARTHAGE__NORD_TUNISIE__22391.SP":
                MyData.push("SECONDO_S_CRS", "22391");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TUNISIA__OFFSHORE__CARTHAGE__TM_11_NE__2088.SP":
                MyData.push("SECONDO_S_CRS", "2088");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TUNISIA__OFFSHORE__CARTHAGE__UTM_ZONE_32N__22332.SP":
                MyData.push("SECONDO_S_CRS", "22332");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TUNISIA__SOUTH_OF_34_39_N__CARTHAGE__SUD_TUNISIE__22392.SP":
                MyData.push("SECONDO_S_CRS", "22392");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}