public class SINGAPORECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SINGAPORE__KERTAU_1968__SINGAPORE_GRID__24500.SP":
                MyData.push("SECONDO_S_CRS", "24500");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SINGAPORE__SVY21__SINGAPORE_TM__3414.SP":
                MyData.push("SECONDO_S_CRS", "3414");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}