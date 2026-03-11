public class BAHRAINCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BAHRAIN__ONSHORE__AIN_EL_ABD__BAHRAIN_GRID__20499.SP":
                MyData.push("SECONDO_S_CRS", "20499");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}