public class PUERTO_RICOCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PUERTO_RICO__ONSHORE__PUERTO_RICO_STATE_PLANE_CS_OF_1927__3991.SP":
                MyData.push("crs", "3991");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}