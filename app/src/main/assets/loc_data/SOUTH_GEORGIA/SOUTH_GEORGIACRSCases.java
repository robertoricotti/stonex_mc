public class SOUTH_GEORGIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SOUTH_GEORGIA__ONSHORE__WGS_84__SOUTH_GEORGIA_LAMBERT__3762.SP":
                MyData.push("SECONDO_S_CRS", "3762");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}