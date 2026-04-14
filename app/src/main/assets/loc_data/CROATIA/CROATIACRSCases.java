public class CROATIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CROATIA__EAST_OF_18_E__HTRS96__UTM_ZONE_34N__3768.SP":
                MyData.push("SECONDO_S_CRS", "3768");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CROATIA__HTRS96__4888.SP":
                MyData.push("SECONDO_S_CRS", "4888");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CROATIA__HTRS96__CROATIA_LCC__3766.SP":
                MyData.push("SECONDO_S_CRS", "3766");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CROATIA__ONSHORE__HTRS96__CROATIA_TM__3765.SP":
                MyData.push("SECONDO_S_CRS", "3765");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CROATIA__WEST_OF_18_E__HTRS96__UTM_ZONE_33N__3767.SP":
                MyData.push("SECONDO_S_CRS", "3767");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}