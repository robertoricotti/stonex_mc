public class QATARCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "QATAR__ONSHORE__QATAR_1948__QATAR_GRID__2099.SP":
                MyData.push("SECONDO_S_CRS", "2099");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "QATAR__ONSHORE__QATAR_1974__QATAR_NATIONAL_GRID__28600.SP":
                MyData.push("SECONDO_S_CRS", "28600");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "QATAR__ONSHORE__QND95__QATAR_NATIONAL_GRID__2932.SP":
                MyData.push("SECONDO_S_CRS", "2932");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}