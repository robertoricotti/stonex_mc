public class ESTONIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ESTONIA__EST97__4934.SP":
                MyData.push("crs", "4934");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ESTONIA__EST97__GEOCENTRIC__4342.SP":
                MyData.push("crs", "4342");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ESTONIA__ESTONIAN_COORDINATE_SYSTEM_OF_1997__3301.SP":
                MyData.push("crs", "3301");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ESTONIA__ONSHORE__ESTONIAN_COORDINATE_SYSTEM_OF_1992__3300.SP":
                MyData.push("crs", "3300");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}