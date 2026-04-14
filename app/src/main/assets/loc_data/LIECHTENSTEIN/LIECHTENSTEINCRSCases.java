public class LIECHTENSTEINCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "LIECHTENSTEIN__CH1903__LV03C_G__21782.SP":
                MyData.push("SECONDO_S_CRS", "21782");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}