public class SOUTH_GEORGIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SOUTH_GEORGIA__ONSHORE__WGS_84__SOUTH_GEORGIA_LAMBERT__3762.SP":
                MyData.push("crs", "3762");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}