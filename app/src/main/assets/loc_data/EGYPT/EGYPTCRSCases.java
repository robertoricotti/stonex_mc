public class EGYPTCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "EGYPT__29_E_TO_33_E__EGYPT_1907__RED_BELT__22992.SP":
                MyData.push("SECONDO_S_CRS", "22992");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "EGYPT__EAST_OF_33_E_ONSHORE__EGYPT_1907__BLUE_BELT__22991.SP":
                MyData.push("SECONDO_S_CRS", "22991");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "EGYPT__GULF_OF_SUEZ__EGYPT_GULF_OF_SUEZ_S_650_TL__RED_BELT__3355.SP":
                MyData.push("SECONDO_S_CRS", "3355");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "EGYPT__WEST_OF_29_E__NORTH_OF_28_11_N__EGYPT_1907__PURPLE_BELT__22993.SP":
                MyData.push("SECONDO_S_CRS", "22993");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "EGYPT__WEST_OF_29_E__SOUTH_OF_28_11_N__EGYPT_1907__EXTENDED_PURPLE_BELT__22994.SP":
                MyData.push("SECONDO_S_CRS", "22994");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}