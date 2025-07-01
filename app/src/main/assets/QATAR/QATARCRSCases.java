public class QATARCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "QATAR__ONSHORE__QATAR_1948__QATAR_GRID__2099.SP":
                MyData.push("crs", "2099");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "QATAR__ONSHORE__QATAR_1974__QATAR_NATIONAL_GRID__28600.SP":
                MyData.push("crs", "28600");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "QATAR__ONSHORE__QND95__QATAR_NATIONAL_GRID__2932.SP":
                MyData.push("crs", "2932");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}