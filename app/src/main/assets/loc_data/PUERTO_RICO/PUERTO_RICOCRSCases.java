public class PUERTO_RICOCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PUERTO_RICO__ONSHORE__PUERTO_RICO_STATE_PLANE_CS_OF_1927__3991.SP":
                MyData.push("SECONDO_S_CRS", "3991");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}