public class NIGERCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "NIGER__SOUTHEAST__BEDUARAM__TM_13_NE__2931.SP":
                MyData.push("SECONDO_S_CRS", "2931");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}