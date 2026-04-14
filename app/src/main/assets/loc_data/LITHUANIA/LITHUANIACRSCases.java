public class LITHUANIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "LITHUANIA__LIETUVOS_KOORDINOEI_SISTEMA_1994__2600.SP":
                MyData.push("crs", "2600");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LITHUANIA__LKS94__4950.SP":
                MyData.push("crs", "4950");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LITHUANIA__LKS94__ETRS89__GEOCENTRIC__4356.SP":
                MyData.push("crs", "4356");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LITHUANIA__LKS94__LITHUANIA_TM__3346.SP":
                MyData.push("crs", "3346");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}